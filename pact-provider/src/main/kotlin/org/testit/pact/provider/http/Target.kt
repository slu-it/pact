package org.testit.pact.provider.http

import java.net.URL

class Target(
        protocol: String = "http",
        host: String = "localhost",
        port: Int = 8080,
        path: String = ""
) {

    private var protocolSupplier: () -> String = { protocol }
    private var hostSupplier: () -> String = { host }
    private var portSupplier: () -> Int = { port }
    private var pathSupplier: () -> String = { path }

    var protocol: String
        get() = protocolSupplier()
        set(value) = bindProtocol { value }

    fun bindProtocol(protocolSupplier: () -> String) {
        this.protocolSupplier = protocolSupplier
    }

    var host: String
        get() = hostSupplier()
        set(value) = bindHost { value }

    fun bindHost(hostSupplier: () -> String) {
        this.hostSupplier = hostSupplier
    }

    var port: Int
        get() = portSupplier()
        set(value) = bindPort { value }

    fun bindPort(portSupplier: () -> Int) {
        this.portSupplier = portSupplier
    }

    var path: String
        get() = pathSupplier().addMissingSlash()
        set(value) = bindPath { value }

    fun bindPath(pathSupplier: () -> String) {
        this.pathSupplier = pathSupplier
    }

    val url: URL
        get() = URL("$protocol://$host:$port$path")

    fun urlWith(additionalPath: String): URL {
        return URL("$url${additionalPath.addMissingSlash()}")
    }

    private fun String.addMissingSlash() = if (isEmpty() || startsWith('/')) this else "/$this"

}