package org.testit.pact.provider.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.testit.pact.provider.sources.LocalFiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RequestResponsePactsTest {

    val wireMock = WireMockServer(8080)
    val cut = RequestResponsePacts(LocalFiles("src/test/pacts/RequestResponsePactsTest"), "library-service")

    @BeforeAll fun startServer() = wireMock.start()
    @BeforeEach fun resetServer() = wireMock.resetAll()
    @AfterAll fun stopServer() = wireMock.stop()

    @Test fun `without a consumer filter all found pacts are returned`() {
        val executablePacts = cut.createExecutablePacts(callbackHandler = CallbackHandler())
        assertEquals(2, executablePacts.size)
    }

    @Test fun `with a consumer filter only matching pacts are returned`() {
        val executablePacts = cut.createExecutablePacts("library-ui", CallbackHandler())
        assertEquals(1, executablePacts.size)
    }

    @Nested inner class `missmatches will throw assertion error` {

        val validResponseBody = """
                {
                  "title": "The Martian",
                  "isbn": "9780091956141",
                  "authors": ["Andy Weir"],
                  "numberOfPages": 384
                }
                """

        @Test fun `matching response expectations`() {
            stubResponse()
            executePacts()
        }

        @Test fun `status code mismatch is detected`() {
            stubResponse(status = 200)
            with(executePactsExpectingError()) {
                assertThat(result.statusError)
                        .isEqualTo("[status] was expected to be [201] but was actually [200]")
            }
        }

        @Test fun `header mismatch is detected`() {
            stubResponse(correlationId = "wrong-id")
            with(executePactsExpectingError()) {
                assertThat(result.headerErrors)
                        .containsOnly("[X-CorrelationId] was expected to be [46d287e5-5d6b-42bf-83be-f7085ea132ce] but was actually [wrong-id]")
            }
        }

        @Test fun `content type mismatch is detected`() {
            stubResponse(contentType = "application/xml")
            with(executePactsExpectingError()) {
                assertThat(result.bodyErrors)
                        .containsOnly("[Content-Type] expected it to be [application/json] but was [application/xml] - no further comparison executed")
            }
        }

        fun stubResponse(
                status: Int = 201,
                correlationId: String = "46d287e5-5d6b-42bf-83be-f7085ea132ce",
                contentType: String = "application/json"
        ) = wireMock.givenThat(post(urlEqualTo("/api/books"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", contentType)
                        .withHeader("X-CorrelationId", correlationId)
                        .withBody(validResponseBody)))

        fun executePacts() = cut.createExecutablePacts("library-ui", CallbackHandler()).forEach { it.executable() }
        fun executePactsExpectingError() = assertThrows<ResponseMissmatchException> { executePacts() }

    }

    inner class CallbackHandler

}