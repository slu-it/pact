package org.testit.pact.provider.junit

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.testit.pact.provider.ExecutablePactFactory
import org.testit.pact.provider.http.ExecutableRequestResponsePactFactory
import org.testit.pact.provider.message.ExecutableMessagePactFactory
import org.testit.pact.provider.sources.PactSource

class PactTestFactory(
        private val factory: ExecutablePactFactory
) {

    fun createTests(consumerFilter: String? = null, callbackHandler: Any): List<DynamicTest> {
        return factory.createExecutablePacts(consumerFilter, callbackHandler)
                .map { dynamicTest(it.name, it.executable) }
    }

    companion object {

        fun requestResponsePacts(pactSource: PactSource, provider: String): PactTestFactory {
            return PactTestFactory(ExecutableRequestResponsePactFactory(pactSource, provider))
        }

        fun messagePacts(pactSource: PactSource, provider: String): PactTestFactory {
            return PactTestFactory(ExecutableMessagePactFactory(pactSource, provider))
        }

    }

}