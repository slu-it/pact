package org.testit.pact.model.reader.requestresponse

import com.fasterxml.jackson.databind.ObjectMapper

internal class RequestBodyExtractor : AbstractBodyExtractor() {
    override val container = "request"
}

internal class ResponseBodyExtractor : AbstractBodyExtractor() {
    override val container = "response"
}

internal abstract class AbstractBodyExtractor {

    protected abstract val container: String

    private val objectMapper = ObjectMapper() // TODO: abstract to JsonWriter?

    fun extract(data: Map<*, *>): String? {
        val node = data["body"]
        return when (node) {
            null -> null
            is List<*> -> serializeAsJson(node)
            is Map<*, *> -> serializeAsJson(node)
            is String -> node
            else -> error("interaction property '$container.body' [$node] is neither a JSON, a JSON array or a string")
        }
    }

    private fun serializeAsJson(data: List<*>): String? {
        data.forEachIndexed { index, thing ->
            require(thing is Map<*, *>) { "interaction property '$container.body.[$index]' [$thing] is not a JSON object" }
        }
        return objectMapper.writeValueAsString(data)
    }

    private fun serializeAsJson(data: Map<*, *>) = objectMapper.writeValueAsString(data)

}