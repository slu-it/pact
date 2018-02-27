package org.testit.pact.provider.junit

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.testit.pact.provider.http.RequestResponsePacts
import org.testit.pact.provider.message.MessagePacts

object PactTestFactory {

    fun createTests(pacts: RequestResponsePacts, consumerFilter: String? = null, callbackHandler: Any? = null): List<DynamicTest> {
        return pacts.createExecutablePacts(consumerFilter, callbackHandler)
                .map { dynamicTest(it.name, it.executable) }
    }

    fun createTests(pacts: MessagePacts, consumerFilter: String? = null, callbackHandler: Any): List<DynamicTest> {
        return pacts.createExecutablePacts(consumerFilter, callbackHandler)
                .map { dynamicTest(it.name, it.executable) }
    }

}