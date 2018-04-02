package org.testit.pact.model.reader.common

import org.testit.pact.model.ProviderState

internal abstract class AbstractProviderStateExtractor {

    protected abstract val containerType: String

    fun extract(data: Map<String, Any>): List<ProviderState> {
        val node = data["providerStates"]
        return when (node) {
            null -> emptyList()
            is List<*> -> node.mapIndexed(::toProviderState)
            else -> error("$containerType property 'providerStates' [$node] is not an array of objects")
        }
    }

    private fun toProviderState(index: Int, data: Any?): ProviderState {
        require(data is Map<*, *>) { "$containerType property 'providerStates.[$index]' [$data] is not an object" }
        data as Map<*, *>

        val name = getName(index, data)
        val parameters = getParameters(index, data)
        return ProviderState(name, parameters)
    }

    private fun getName(index: Int, data: Map<*, *>): String {
        val name = data["name"]
        require(name is String) { "$containerType property 'providerStates.[$index].name' [$name] is not a string" }
        return name as String
    }

    private fun getParameters(index: Int, data: Map<*, *>): Map<String, Any> {
        val params = data["params"]
        return when (params) {
            null -> emptyMap()
            is Map<*, *> -> params.map { toProviderStateParameter(index, it) }.toMap()
            else -> error("$containerType property 'providerStates.[$index].params' [$params] is not a map")
        }
    }

    private fun toProviderStateParameter(index: Int, data: Map.Entry<*, *>): Pair<String, Any> {
        require(data.value != null) { "$containerType property 'providerStates.[$index].params.${data.key}' is null" }
        return (data.key as String) to data.value!!
    }

}