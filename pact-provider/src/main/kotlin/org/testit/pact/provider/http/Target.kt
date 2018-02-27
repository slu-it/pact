package org.testit.pact.provider.http

class Target(
        protocol: String = "http",
        host: String = "localhost",
        port: Int = 8080,
        contextPath: String = "/"
) {

    var protocol: () -> String = { protocol }
    var host: () -> String = { host }
    var port: () -> Int = { port }
    var path: () -> String = { contextPath }

}