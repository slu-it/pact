package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.v3.Headers

internal class RequestHeadersExtractor : AbstractHeadersExtractor() {
    override val container = "request"
}

internal class ResponseHeadersExtractor : AbstractHeadersExtractor() {
    override val container = "response"
}

internal abstract class AbstractHeadersExtractor {

    protected abstract val container: String

    fun extract(data: Map<*, *>): Headers {
        val node = data["headers"]
        return when (node) {
            null -> emptyMap()
            is Map<*, *> -> node.map(::toHeaderEntry).toMap()
            else -> error("interaction property '$container.headers' [$node] is not an object")
        }
    }

    private fun toHeaderEntry(data: Map.Entry<*, *>): Pair<String, String> {
        require(data.value is String) { "interaction property '$container.headers.${data.key}' [${data.value}] is not a string" }
        return (data.key as String) to (data.value as String)
    }

}