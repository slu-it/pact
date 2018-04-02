package org.testit.pact.model

data class Request(
        val method: HttpMethod,
        val path: String,
        val query: Map<String, List<String>>,
        val headers: Map<String, String>,
        val body: String?
)