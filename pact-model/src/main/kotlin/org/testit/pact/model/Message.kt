package org.testit.pact.model

data class Message(
        val description: String,
        val providerStates: List<ProviderState>,
        val contents: String?,
        val metaData: Map<String, String>
)