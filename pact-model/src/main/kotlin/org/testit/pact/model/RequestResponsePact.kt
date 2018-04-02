package org.testit.pact.model

data class RequestResponsePact(
        override val provider: Provider,
        override val consumer: Consumer,
        override val metadata: PactMetadata,
        val interactions: List<Interaction>
) : Pact