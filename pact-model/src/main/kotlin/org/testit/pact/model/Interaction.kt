package org.testit.pact.model

data class Interaction(
        val description: String,
        val providerStates: List<ProviderState>,
        val request: Request,
        val response: Response
)