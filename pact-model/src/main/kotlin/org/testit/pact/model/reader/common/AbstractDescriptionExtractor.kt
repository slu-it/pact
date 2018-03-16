package org.testit.pact.model.reader.common

internal abstract class AbstractDescriptionExtractor {

    protected abstract val containerType: String

    fun extract(data: Map<String, Any>): String {
        val node = data["description"]
        require(node is String) { "$containerType property 'description' [$node] is not a string" }
        return node as String
    }

}