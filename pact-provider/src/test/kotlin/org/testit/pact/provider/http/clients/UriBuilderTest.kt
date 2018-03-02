package org.testit.pact.provider.http.clients

import au.com.dius.pact.model.Request
import org.apache.http.client.utils.URIBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.testit.pact.provider.http.Target


internal class UriBuilderTest {

    val target = Target()
    val request = Request()

    @Test fun `default Target and Request baseline`() {
        val uri = UriBuilder.build(target, request)
        assertThat(uri)
                .hasScheme("http")
                .hasHost("localhost")
                .hasPort(8080)
                .hasPath("/") // from Request defaults
                .hasNoParameters()
    }

    @Test fun `custom Target and default Request`() {
        with(target) {
            protocol = "https"
            host = "example.com"
            port = 9090
            path = "/base"
        }
        val uri = UriBuilder.build(target, request)
        assertThat(uri)
                .hasScheme("https")
                .hasHost("example.com")
                .hasPort(9090)
                .hasPath("/base/")
                .hasNoParameters()
    }

    @Test fun `default Target and custom Request`() {
        with(request) {
            path = "/base"
            query = mapOf("foo" to listOf("1", "7"), "bar" to listOf("true"))
        }
        val uri = UriBuilder.build(target, request)
        assertThat(uri).hasPath("/base")
        assertThat(uri.query).isEqualTo("foo=1&foo=7&bar=true")
    }

    @Nested inner class `null handling` {

        @Test fun `Request with null path is handled correctly`() {
            request.path = null
            val uri = UriBuilder.build(target, request)
            assertThat(uri).hasPath("")
        }

        @Test fun `Request with null query is handled correctly`() {
            request.query = null
            val uri = UriBuilder.build(target, request)
            assertThat(uri).hasNoParameters()
        }

    }

    @Nested inner class `query parameter border cases` {

        /**
         * This might turn out to be wrong behaviour. There is no easy
         * way of adding a parameter without value(s) using the [URIBuilder]
         * class. Which means the only way of expressing parameters without
         * values is to create custom query strings. Until it is clear if we
         * need to consider this case, parameters without values are ignored.
         */
        @Test fun `parameter without values is ignored`() {
            request.query = mapOf("value" to listOf("foo"), "noValues" to listOf())
            val uri = UriBuilder.build(target, request)
            assertThat(uri.query).isEqualTo("value=foo")
        }

    }

}