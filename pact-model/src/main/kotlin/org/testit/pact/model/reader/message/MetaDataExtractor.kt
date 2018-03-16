package org.testit.pact.model.reader.message

internal class MetaDataExtractor {

    fun extract(data: Map<String, Any>): Map<String, String> {
        val node = data["metaData"]
        return when (node) {
            null -> emptyMap()
            is Map<*, *> -> node.map(::toMetaDataEntry).toMap()
            else -> error("message property 'metaData' [$node] is not an object")
        }
    }

    private fun toMetaDataEntry(data: Map.Entry<*, *>): Pair<String, String> {
        require(data.value is String) { "message property 'metaData.${data.key}' [${data.value}] is not a string" }
        return (data.key as String) to (data.value as String)
    }

}