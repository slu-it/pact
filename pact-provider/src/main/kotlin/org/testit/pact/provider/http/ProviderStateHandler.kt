package org.testit.pact.provider.http

import au.com.dius.pact.model.Interaction
import java.lang.reflect.Method

class ProviderStateHandler(
        private val callbackHandler: Any?
) {

    private val providerStateMethods = collectProviderStateMethods(callbackHandler)

    fun prepare(interaction: Interaction) {
        if (interaction.providerStates.isNotEmpty() && callbackHandler == null) {
            error("No callback handler defined, but interaction needs provider state!")
        }
        interaction.providerStates.forEach { state ->
            val stateName = state.name
            val parameters = state.params

            val method = providerStateMethods[stateName]
                    ?: throw ProviderStateMethodNotFoundException(stateName)
            when {
                method.parameterCount == 0 -> method.invoke(callbackHandler)
                method.parameterCount == 1 -> method.invoke(callbackHandler, parameters)
                else -> throw MalformedProviderStateMethodException(stateName)
            }
        }
    }

    private fun collectProviderStateMethods(callbackHandler: Any?): Map<String, Method> {
        if (callbackHandler == null)
            return emptyMap()
        return callbackHandler.javaClass.declaredMethods
                .filter { it.isAnnotationPresent(ProviderState::class.java) }
                .groupBy { it.getAnnotation(ProviderState::class.java).value }
                .map<String, List<Method>, Pair<String, Method>> { it.key to it.value.single() }
                .toMap()
    }

}

class ProviderStateMethodNotFoundException(state: String)
    : RuntimeException("Could not find a method for provider state [$state]!")

class MalformedProviderStateMethodException(state: String)
    : RuntimeException("The method for provider state [$state] is malformed! Only none or a single parameter of type Map<String, Any> are allowed.")