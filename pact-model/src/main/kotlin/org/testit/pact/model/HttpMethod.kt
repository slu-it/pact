package org.testit.pact.model

enum class HttpMethod {

    GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE, PATCH;

    companion object {

        fun parse(value: String): HttpMethod? {
            val name = value.trim().toUpperCase()
            return values().find { it.name == name }
        }

    }

}