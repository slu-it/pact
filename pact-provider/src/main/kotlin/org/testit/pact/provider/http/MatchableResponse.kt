package org.testit.pact.provider.http

data class MatchableResponse(
        val status: Int,
        val headers: Map<String, String>,
        val body: String?
)