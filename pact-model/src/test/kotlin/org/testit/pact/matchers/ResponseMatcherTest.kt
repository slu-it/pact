package org.testit.pact.matchers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.testit.pact.model.Response


internal class ResponseMatcherTest {

    private val cut = ResponseMatcher()

    @DisplayName("status matching")
    @Nested inner class StatusMatching {

        @Test fun `null response status matches any actual status`() {
            (0..999).forEach {
                val result = matchStatus(null, it)
                assertThat(result).isSameAs(Match)
            }
        }

        @Test fun `response status matches equal actual status`() {
            (0..999).forEach {
                val result = matchStatus(it, it)
                assertThat(result).isSameAs(Match)
            }
        }

        @Test fun `response status does not match unequal actual status`() {
            val result = matchStatus(200, 400) as Mismatch
            assertThat(result.reasons)
                    .containsExactly("expected response 'status' to be [200] but was [400]")
        }

        private fun matchStatus(status: Int?, actualStatus: Int): MatcherResult {
            return cut.matchStatus(response(status = status), actualStatus)
        }

    }

    @DisplayName("headers matching")
    @Nested inner class HeadersMatching {

        private val responseHeaders = mutableMapOf<String, String>()
        private val actualHeaders = mutableMapOf<String, String>()

        @Test fun `empty response headers matches empty actual headers`() {
            assertThat(matchHeaders()).isSameAs(Match)
        }

        @Test fun `empty response headers matches any actual headers`() {
            actualHeaders["Content-Type"] = "application/json"
            assertThat(matchHeaders()).isSameAs(Match)
        }

        @Test fun `equal response and actual headers matches`() {
            responseHeaders["Content-Type"] = "application/json"
            actualHeaders["Content-Type"] = "application/json"
            assertThat(matchHeaders()).isSameAs(Match)
        }

        @Test fun `response header entries must exist in actual headers`() {
            responseHeaders["Content-Type"] = "application/json"
            responseHeaders["X-Correlation-ID"] = "foo-bar"
            assertThat(mismatchHeaders().reasons)
                    .contains("expected response 'headers' to contain [Content-Type] but they didn't")
                    .contains("expected response 'headers' to contain [X-Correlation-ID] but they didn't")
                    .hasSize(2)
        }

        @Test fun `response header entries must have equal value in actual headers`() {
            responseHeaders["Content-Type"] = "application/json"
            responseHeaders["X-Correlation-ID"] = "foo-bar"
            actualHeaders["Content-Type"] = "application/hal+json"
            actualHeaders["X-Correlation-ID"] = "123456"
            assertThat(mismatchHeaders().reasons)
                    .contains("expected response 'header' [Content-Type] to be equal to [application/json] but was [application/hal+json]")
                    .contains("expected response 'header' [X-Correlation-ID] to be equal to [foo-bar] but was [123456]")
                    .hasSize(2)
        }

        private fun mismatchHeaders(): Mismatch {
            return matchHeaders() as Mismatch
        }

        private fun matchHeaders(): MatcherResult {
            return cut.matchHeaders(response(headers = responseHeaders), actualHeaders)
        }

    }

    @DisplayName("body matching")
    @Nested inner class BodyMatching {

        private var responseBody: String? = null
        private var actualBody: String? = null

        @Test fun `null response body and null actual body matches`() {
            responseBody = null
            actualBody = null
            assertThat(matchBody()).isSameAs(Match)
        }

        @Test fun `null response body and empty actual body matches`() {
            responseBody = null
            actualBody = ""
            assertThat(matchBody()).isSameAs(Match)
        }

        @Test fun `empty response body and null actual body does not match`() {
            responseBody = ""
            actualBody = null
            assertThat((matchBody() as Mismatch).reasons)
                    .containsExactly("expected response 'body' not to be null but it was")
        }

        @Test fun `any response body and null actual body does not match`() {
            responseBody = "foo"
            actualBody = null
            assertThat((matchBody() as Mismatch).reasons)
                    .containsExactly("expected response 'body' not to be null but it was")
        }

        @DisplayName("plain contents")
        @Nested inner class PlainContent {

            @Test fun `empty response body and empty actual body matches`() {
                responseBody = ""
                actualBody = ""
                assertThat(matchBody()).isSameAs(Match)
            }

            @Test fun `equal response and actual body matches`() {
                responseBody = "foo, bar - abc"
                actualBody = "foo, bar - abc"
                assertThat(matchBody()).isSameAs(Match)
            }

            @Test fun `unequal response and actual body does not match`() {
                responseBody = "foo"
                actualBody = "bar"
                assertThat((matchBody() as Mismatch).reasons)
                        .containsExactly("expected response 'body' to be equal to [foo] but was [bar]")
            }

        }

        @DisplayName("JSON contents with Content-Type header")
        @Nested inner class JsonContentWithContentTypeHeader {

            @Test fun `empty objects match`() {
                responseBody = "{}"
                actualBody = "{}"
                assertThat(matchBody()).isSameAs(Match)
            }

            @Test fun `two objects with equal properties match`() {
                responseBody = """{"foo": "bar"}"""
                actualBody = """{"foo": "bar"}"""
                assertThat(matchBody()).isSameAs(Match)
            }

            @Test fun `two objects with unequal top level properties does not match`() {
                responseBody = """{"foo": "bar"}"""
                actualBody = """{"foo": "xur"}"""
                assertThat((matchBody() as Mismatch).reasons)
                        .containsExactly("expected response 'body' property [$.foo] to be equal to [bar] but was [xur]")
            }

            @Test fun `two objects with unequal nested properties does not match`() {
                responseBody = """{"foo": { "bar": true } }"""
                actualBody = """{"foo": { "bar": false } }"""
                assertThat((matchBody() as Mismatch).reasons)
                        .containsExactly("expected response 'body' property [$.foo.bar] to be equal to [true] but was [false]")
            }

            @Disabled
            @Test fun `two objects with unequal arrays does not match`() {
                responseBody = """{"foo": [ "bar" ] }"""
                actualBody = """{"foo": [ "xur" ] }"""
                assertThat((matchBody() as Mismatch).reasons)
                        .containsExactly("expected response 'body' property [$.foo[0]] to be equal to [bar] but was [xur]")
            }

            private fun matchBody(): MatcherResult = matchBody("application/json")

        }

        @DisplayName("JSON contents without Content-Type header")
        @Nested inner class JsonContentWithoutContentTypeHeader {

            @Test fun `empty objects match`() {
                responseBody = "{}"
                actualBody = "{}"
                assertThat(matchBody()).isSameAs(Match)
            }

            @Test fun `two objects with equal properties match`() {
                responseBody = """{"foo": "bar"}"""
                actualBody = """{"foo": "bar"}"""
                assertThat(matchBody()).isSameAs(Match)
            }

            private fun matchBody(): MatcherResult = matchBody(null)

        }

        @DisplayName("XML contents with Content-Type header")
        @Nested inner class XmlContentWithContentTypeHeader {

            @Test fun `two DOMs with equal nodes match`() {
                responseBody = "<foo>bar</foo>"
                actualBody = "<foo>bar</foo>"
                assertThat(matchBody()).isSameAs(Match)
            }

            private fun matchBody(): MatcherResult = matchBody("application/xml")

        }

        @DisplayName("XML contents without Content-Type header")
        @Nested inner class XmlContentWithoutContentTypeHeader {

            @Test fun `two DOMs with equal nodes match`() {
                responseBody = "<foo>bar</foo>"
                actualBody = "<foo>bar</foo>"
                assertThat(matchBody()).isSameAs(Match)
            }

            private fun matchBody(): MatcherResult = matchBody(null)

        }

        private fun matchBody(contentType: String? = null): MatcherResult {
            val headers = mutableMapOf<String, String>()
            if (contentType != null) {
                headers["Content-Type"] = contentType
            }
            return cut.matchBody(response(body = responseBody, headers = headers), actualBody)
        }

    }

    private fun response(status: Int? = null, headers: Map<String, String> = emptyMap(), body: String? = null): Response {
        return Response(status, headers, body)
    }

}