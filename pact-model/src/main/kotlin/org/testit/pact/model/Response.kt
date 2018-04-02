package org.testit.pact.model

data class Response(
        val status: Int?,
        val headers: Map<String, String>,
        val body: String?
)