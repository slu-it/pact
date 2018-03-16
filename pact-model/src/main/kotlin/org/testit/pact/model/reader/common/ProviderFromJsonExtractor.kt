package org.testit.pact.model.reader.common

import org.testit.pact.model.reader.MalformedPactException
import org.testit.pact.model.v3.Provider

internal class ProviderFromJsonExtractor {

    fun extract(json: Map<String, Any>) = try {
        val provider = json["provider"]
        require(provider is Map<*, *>) { "pact property 'provider' [$provider] is not an object" }
        provider as Map<*, *>

        val name = provider["name"]
        require(name is String) { "pact property 'provider.name' [$name] is not a string" }
        name as String

        Provider(name)
    } catch (e: IllegalArgumentException) {
        throw MalformedPactException(e.message)
    }

}