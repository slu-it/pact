package org.testit.pact.provider.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL


internal class TargetTest {

    val cut = Target()

    @Test fun `default target definition`() {
        with(cut) {
            assertThat(protocol).isEqualTo("http")
            assertThat(host).isEqualTo("localhost")
            assertThat(port).isEqualTo(8080)
            assertThat(path).isEqualTo("")
        }
    }

    @Nested inner class `all properties can be changed` {

        @Test fun protocol() {
            cut.protocol = "https"
            assertThat(cut.protocol).isEqualTo("https")
        }

        @Test fun host() {
            cut.host = "example.com"
            assertThat(cut.host).isEqualTo("example.com")
        }

        @Test fun port() {
            cut.port = 9090
            assertThat(cut.port).isEqualTo(9090)
        }

        @Test fun path() {
            cut.path = "/base"
            assertThat(cut.path).isEqualTo("/base")
        }

    }

    @Nested inner class `all properties can be bound to a lambda` {

        var protocol = "http"
        var host = "localhost"
        var port = 8080
        var path = ""

        @Test fun protocol() {
            cut.bindProtocol { protocol }
            protocol = "https"
            assertThat(cut.protocol).isEqualTo("https")
        }

        @Test fun host() {
            cut.bindHost { host }
            host = "example.com"
            assertThat(cut.host).isEqualTo("example.com")
        }

        @Test fun port() {
            cut.bindPort { port }
            port = 9090
            assertThat(cut.port).isEqualTo(9090)
        }

        @Test fun path() {
            cut.bindPath { path }
            path = "/base"
            assertThat(cut.path).isEqualTo("/base")
        }

    }

    @Nested inner class `url value is generated correctly` {

        @Test fun `default values`() {
            assertThat(cut.url).isEqualTo(URL("http://localhost:8080"))
        }

        @Test fun `custom values`() {
            with(cut) {
                protocol = "https"
                host = "example.com"
                port = 9090
                path = "/base"
                assertThat(url).isEqualTo(URL("https://example.com:9090/base"))
            }
        }

        @Test fun `missing slash between port and path are added`() {
            with(cut) {
                path = "base"
                assertThat(url).isEqualTo(URL("http://localhost:8080/base"))
            }
        }

    }

    @Nested inner class `url with additional path is generated correctly` {

        @Test fun `default values`() {
            val urlWith = cut.urlWith("/foo")
            assertThat(urlWith).isEqualTo(URL("http://localhost:8080/foo"))
        }

        @Test fun `missing slash between port and additional path are added`() {
            val urlWith = cut.urlWith("foo")
            assertThat(urlWith).isEqualTo(URL("http://localhost:8080/foo"))
        }

        @Test fun `missing slash between path and additional path are added`() {
            cut.path = "/base"
            val urlWith = cut.urlWith("foo")
            assertThat(urlWith).isEqualTo(URL("http://localhost:8080/base/foo"))
        }

    }

}