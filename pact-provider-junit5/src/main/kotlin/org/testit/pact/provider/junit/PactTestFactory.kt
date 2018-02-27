package org.testit.pact.provider.junit

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.testit.pact.provider.http.RequestResponsePacts
import org.testit.pact.provider.message.MessagePacts

class PactTestFactory {

    /*
     * Exposes the static factory for Java clients and still makes it usable for
     * Kotlin clients as well.
     *
     * If this file were an `object` instead of a `class`, it would expose an ugly
     * static `INSTANCE` field for Java clients. Since this is an API class, doing
     * it this way instead of a more elegant Kotlin purist way is acceptable.
     */
    companion object {

        @JvmStatic
        fun createTests(pacts: RequestResponsePacts, consumerFilter: String? = null, callbackHandler: Any? = null): List<DynamicTest> {
            return pacts.createExecutablePacts(consumerFilter, callbackHandler)
                    .map { dynamicTest(it.name, it.executable) }
        }

        @JvmStatic
        fun createTests(pacts: MessagePacts, consumerFilter: String? = null, callbackHandler: Any): List<DynamicTest> {
            return pacts.createExecutablePacts(consumerFilter, callbackHandler)
                    .map { dynamicTest(it.name, it.executable) }
        }

    }

}