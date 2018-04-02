package org.testit.pact.model

data class RequestResponsePact(
        override val provider: Provider,
        override val consumer: Consumer,
        override val specification: PactSpecification,
        val interactions: List<Interaction>
) : Pact