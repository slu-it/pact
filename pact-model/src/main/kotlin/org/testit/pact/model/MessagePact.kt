package org.testit.pact.model

data class MessagePact(
        override val provider: Provider,
        override val consumer: Consumer,
        override val specification: PactSpecification,
        val messages: List<Message>
) : Pact