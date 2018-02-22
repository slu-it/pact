package org.testit.pact.provider.junit.http

class HttpTarget(
        protocol: String = "http",
        host: String = "localhost",
        port: Int = 8080,
        contextPath: String = "/"
) {

    var protocol: () -> String = { protocol }
    var host: () -> String = { host }
    var port: () -> Int = { port }
    var contextPath: () -> String = { contextPath }

}