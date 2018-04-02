package org.testit.pact.model

data class ProviderState(
        val name: String,
        val parameters: Map<String, Any>
)