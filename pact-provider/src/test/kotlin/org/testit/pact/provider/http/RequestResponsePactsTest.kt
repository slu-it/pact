package org.testit.pact.provider.http

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.RegisterExtension
import org.testit.pact.provider.sources.LocalFiles
import utils.WireMockExtension

@TestInstance(PER_CLASS)
internal class RequestResponsePactsTest {

    @RegisterExtension
    @JvmField val wireMock = WireMockExtension()

    val cut = RequestResponsePacts(LocalFiles("src/test/pacts/RequestResponsePactsTest"), "library-service")

    @BeforeEach fun setPort() {
        cut.target.bindPort { wireMock.port() }
    }

    @Test fun `without a consumer filter all found pacts are returned`() {
        val executablePacts = cut.createExecutablePacts()
        assertThat(executablePacts).hasSize(2)
    }

    @Test fun `with a consumer filter only matching pacts are returned`() {
        val executablePacts = cut.createExecutablePacts("library-ui")
        assertThat(executablePacts).hasSize(1)
    }

    @Test fun `exception is thrown if no pacts could be found`() {
        assertThrows<NoRequestResponsePactsFoundException> {
            cut.createExecutablePacts("unknown")
        }
    }

    @Nested inner class `response matching` {

        @Test fun `if there are no mismatches all pacts are executed successfully`() {
            stubServerResponse()
            executePacts()
        }

        @Nested inner class `missmatches will throw assertion error` {

            @Test fun `status code mismatch is detected`() {
                stubServerResponse(status = 200)
                with(executePactsExpectingError()) {
                    assertThat(result.statusError)
                            .isEqualTo("[status] was expected to be [201] but was actually [200]")
                }
            }

            @Test fun `header mismatch is detected`() {
                stubServerResponse(correlationId = "wrong-id")
                with(executePactsExpectingError()) {
                    assertThat(result.headerErrors)
                            .containsOnly("[X-CorrelationId] was expected to be [46d287e5-5d6b-42bf-83be-f7085ea132ce] but was actually [wrong-id]")
                }
            }

            @Test fun `content type mismatch is detected`() {
                stubServerResponse(contentType = "application/xml")
                with(executePactsExpectingError()) {
                    assertThat(result.bodyErrors)
                            .containsOnly("[Content-Type] expected it to be [application/json] but was [application/xml] - no further comparison executed")
                }
            }

            @Test fun `body mismatches are detected`() {
                stubServerResponse(body = """
                {
                  "title": "The Martian",
                  "isbn": "9780091956141",
                  "authors": []
                }
                """)
                with(executePactsExpectingError()) {
                    assertThat(result.bodyErrors[0])
                            .containsSubsequence("[\$] was expected to be [", "] but was actually [", "]")
                    assertThat(result.bodyErrors[1])
                            .containsSubsequence("[\$.authors] was expected to be [", "] but was actually [", "]")
                }
            }

        }

        fun stubServerResponse(
                status: Int = 201,
                correlationId: String = "46d287e5-5d6b-42bf-83be-f7085ea132ce",
                contentType: String = "application/json",
                body: String = """
                {
                  "title": "The Martian",
                  "isbn": "9780091956141",
                  "authors": ["Andy Weir"],
                  "numberOfPages": 384
                }
                """
        ) = wireMock.givenThat(post(urlEqualTo("/api/books"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", contentType)
                        .withHeader("X-CorrelationId", correlationId)
                        .withBody(body)))

        fun executePacts() = cut.createExecutablePacts("library-ui").forEach { it.executable() }
        fun executePactsExpectingError() = assertThrows<ResponseMismatchException> { executePacts() }

    }

    @Nested inner class `provider state` {

        @BeforeEach fun stubResponse() {
            wireMock.givenThat(get(urlEqualTo("/api"))
                    .willReturn(aResponse()
                            .withStatus(200)))
        }

        @Test fun `provider state methods are invoked on the callback handler`() {
            val callbackHandler = CallbackHandler()
            executePacts(callbackHandler)
            with(callbackHandler) {
                assertThat(someProviderStateInvoked).isTrue()
                assertThat(someProviderStateInvokedWithParameters).isTrue()
                assertThat(parameters).isEqualTo(mapOf("foo" to "bar", "bar" to "foo"))
            }
        }

        @Test fun `exception in case a callback method could not be found`() {
            assertThrows<ProviderStateMethodNotFoundException> {
                executePacts(NoOpCallbackHandler())
            }
        }

        @Test fun `exception in case a callback method is malformed`() {
            assertThrows<MalformedProviderStateMethodException> {
                executePacts(MalformedCallbackHandler())
            }
        }

        @Test fun `exception in case there is no callback handler but interaction has provider states`() {
            assertThrows<ProviderStateHandlerNotSetException> {
                executePacts(null)
            }
        }

        @Test fun `exception in case there was an exception while invoking a provider state method`() {
            assertThrows<ProviderStateInvocationException> {
                executePacts(ExceptionalCallbackHandler())
            }
        }

        fun executePacts(callbackHandler: Any?) =
                cut.createExecutablePacts("library-enrichment", callbackHandler).forEach { it.executable() }

    }

    class NoOpCallbackHandler
    class CallbackHandler {

        var someProviderStateInvoked = false
        var someProviderStateInvokedWithParameters = false
        var parameters = mapOf<String, String>()

        @ProviderState("some provider state")
        fun someProviderState() {
            someProviderStateInvoked = true
        }

        @ProviderState("some provider state with parameters")
        fun someProviderStateWithParameters(params: Map<String, String>) {
            someProviderStateInvokedWithParameters = true
            parameters = params
        }

    }

    class MalformedCallbackHandler {

        @ProviderState("some provider state")
        fun someProviderState() {
        }

        @ProviderState("some provider state with parameters")
        fun someProviderStateWithParameters(params: Map<String, String>, paramA: String) {
        }

    }

    class ExceptionalCallbackHandler {

        @ProviderState("some provider state")
        fun someProviderState() {
            throw RuntimeException()
        }

        @ProviderState("some provider state with parameters")
        fun someProviderStateWithParameters(params: Map<String, String>, paramA: String) {
            throw RuntimeException()
        }

    }

}