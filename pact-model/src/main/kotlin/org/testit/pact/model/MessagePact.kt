package org.testit.pact.model

data class MessagePact(
        override val provider: Provider,
        override val consumer: Consumer,
        override val metadata: PactMetadata,
        val messages: List<Message>
) : Pact