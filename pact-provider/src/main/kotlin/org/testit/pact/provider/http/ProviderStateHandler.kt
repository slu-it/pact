package org.testit.pact.provider.http

import au.com.dius.pact.model.Interaction
import java.lang.reflect.Method

class ProviderStateHandler(
        private val callbackHandler: Any?
) {

    private val providerStateMethods = collectProviderStateMethods(callbackHandler)

    fun prepare(interaction: Interaction) {
        if (interaction.providerStates.isNotEmpty() && callbackHandler == null) {
            throw ProviderStateHandlerNotSetException(interaction)
        }
        interaction.providerStates.forEach { state ->
            val stateName = state.name
            val parameters = state.params

            val method = providerStateMethods[stateName.toLowerCase()]
                    ?: throw ProviderStateMethodNotFoundException(stateName)
            when {
                method.parameterCount == 0 -> method.tryToInvokeMethod(stateName)
                method.parameterCount == 1 -> method.tryToInvokeMethod(stateName, parameters)
                else -> throw MalformedProviderStateMethodException(stateName)
            }
        }
    }

    private fun Method.tryToInvokeMethod(stateName: String, vararg parameters: Any) {
        if (parameterTypes.any { !Map::class.java.isAssignableFrom(it) }) {
            throw MalformedProviderStateMethodException(stateName)
        }
        try {
            invoke(callbackHandler, *parameters)
        } catch (e: Exception) {
            throw ProviderStateInvocationException(stateName, e)
        }
    }

    private fun collectProviderStateMethods(callbackHandler: Any?): Map<String, Method> {
        if (callbackHandler == null)
            return emptyMap()
        return callbackHandler.javaClass.declaredMethods
                .filter { it.isAnnotationPresent(ProviderState::class.java) }
                .groupBy { it.getAnnotation(ProviderState::class.java).value }
                .map<String, List<Method>, Pair<String, Method>> { it.key.toLowerCase() to it.value.single() }
                .toMap()
    }

}

class ProviderStateHandlerNotSetException(interaction: Interaction)
    : RuntimeException("There are provider state(s) defined for interaction [${interaction.description}] but you didn't set a callback handler!")

class ProviderStateMethodNotFoundException(stateName: String)
    : RuntimeException("Could not find a method for provider state [$stateName]!")

class MalformedProviderStateMethodException(stateName: String)
    : RuntimeException("The method for provider state [$stateName] is malformed! Only none or a single parameter of type Map<String, Any> are allowed.")

class ProviderStateInvocationException(stateName: String, cause: Throwable)
    : RuntimeException("There was an exception while preparing the provider state [$stateName]:", cause)