package org.testit.pact.model.reader.message

import com.fasterxml.jackson.databind.ObjectMapper

internal class ContentsExtractor {

    private val objectMapper = ObjectMapper() // TODO: abstract to JsonWriter?

    fun extract(data: Map<String, Any>): String? {
        val node = data["contents"]
        return when (node) {
            null -> null
            is List<*> -> serializeAsJson(node)
            is Map<*, *> -> serializeAsJson(node)
            is String -> node
            else -> error("message property 'contents' [$node] is neither a JSON, a JSON array or a string")
        }
    }

    private fun serializeAsJson(data: List<*>): String? {
        data.forEachIndexed { index, thing ->
            require(thing is Map<*, *>) { "message property 'contents.[$index]' [$thing] is not a JSON object" }
        }
        return objectMapper.writeValueAsString(data)
    }

    private fun serializeAsJson(data: Map<*, *>) = objectMapper.writeValueAsString(data)

}