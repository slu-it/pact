package org.testit.pact.provider

interface ExecutablePactFactory {
    fun createExecutablePacts(consumerFilter: String? = null, callbackHandler: Any): List<ExecutablePact>
}