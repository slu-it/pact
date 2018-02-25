package org.testit.pact.provider

interface ExecutablePactFactory {
    fun createExecutablePacts(consumerFilter: String? = null, callbackHandler: Any? = null): List<ExecutablePact>
}