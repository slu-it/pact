package org.testit.pact.provider.junit

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.testit.pact.provider.ExecutablePactFactory

class PactTestFactory(
        val factory: ExecutablePactFactory
) {

    fun createTests(consumerFilter: String? = null, callbackHandler: Any): List<DynamicTest> {
        return factory.createExecutablePacts(consumerFilter, callbackHandler)
                .map { dynamicTest(it.name, it.executable) }
    }

}