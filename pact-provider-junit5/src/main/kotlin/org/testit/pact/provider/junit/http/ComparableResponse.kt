package org.testit.pact.provider.junit.http

data class ComparableResponse(
        val status: Int,
        val headers: Map<String, String>,
        val body: String?
)