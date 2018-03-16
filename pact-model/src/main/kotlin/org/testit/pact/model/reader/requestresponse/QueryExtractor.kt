package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.v3.Query

internal class QueryExtractor {

    fun extract(data: Map<*, *>): Query {
        val node = data["query"]
        return when (node) {
            null -> emptyMap()
            is Map<*, *> -> node.map(::toQueryEntry).toMap()
            else -> error("interaction property 'request.query' [$node] is not an object")
        }
    }

    private fun toQueryEntry(data: Map.Entry<*, *>): Pair<String, List<String>> {
        require(data.value is List<*>) { "interaction property 'request.query.${data.key}' [${data.value}] is not an array" }
        (data.value as List<*>).forEachIndexed { index, it ->
            require(it is String) { "interaction property 'request.query.${data.key}[$index]' [$it] is not a string" }
        }

        @Suppress("UNCHECKED_CAST")
        return (data.key as String) to (data.value as List<String>)
    }

}