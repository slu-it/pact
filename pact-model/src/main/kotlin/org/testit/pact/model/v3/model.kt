package org.testit.pact.model.v3

// TODO:
//  - Generators
//  - Matchers

interface Pact {
    val provider: Provider
    val consumer: Consumer
    val metadata: PactMetadata
}

data class PactMetadata(
        val pactSpecification: PactSpecification
)

data class PactSpecification(val version: String)

data class Provider(val name: String)
data class Consumer(val name: String)

data class RequestResponsePact(
        override val provider: Provider,
        override val consumer: Consumer,
        override val metadata: PactMetadata,
        val interactions: List<Interaction>
) : Pact

data class Interaction(
        val description: String,
        val providerStates: List<ProviderState>?,
        val request: Request,
        val response: Response
)

data class Request(
        val method: Method,
        val path: String,
        val query: Query,
        val headers: Headers,
        val body: String?
)

enum class Method {
    GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE, PATCH;

    companion object {
        fun parse(value: String): Method? {
            val name = value.trim().toUpperCase()
            return values().find { it.name == name }
        }
    }
}
typealias Query = Map<String, List<String>>

data class Response(
        val status: Int?,
        val headers: Headers?,
        val body: String?
)

typealias Headers = Map<String, String>

data class MessagePact(
        override val provider: Provider,
        override val consumer: Consumer,
        override val metadata: PactMetadata,
        val messages: List<Message>
) : Pact

data class Message(
        val description: String,
        val providerStates: List<ProviderState>?,
        val contents: String?,
        val metaData: MessageMetaData?
)

typealias MessageMetaData = Map<String, String>

data class ProviderState(
        val name: String,
        val parameters: ProviderStateParameters
)

typealias ProviderStateParameters = Map<String, Any>