package org.testit.pact.provider.junit

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.testit.pact.provider.http.RequestResponsePacts
import org.testit.pact.provider.http.ProviderState
import org.testit.pact.provider.message.MessagePacts
import org.testit.pact.provider.message.MessageProducer

/**
 * This class can be used to create [dynamic tests][DynamicTest] for JUnit 5
 * [@TestFactory][TestFactory] methods. You can provide either a [RequestResponsePacts]
 * or a [MessagePacts] instance in order to create the tests.
 *
 * Example:
 * ```
 * val pacts = RequestResponsePacts(LocalFiles("src/test/pacts"), "provider")
 *
 * @TestFactory fun `'consumer 1' contract tests`() =
 *         PactTestFactory.createTests(pacts, "consumer 1", this)
 * ```
 *
 * @see DynamicTest
 * @see TestFactory
 * @see RequestResponsePacts
 * @see MessagePacts
 */
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

        /**
         * Creates a list of [dynamic tests][DynamicTest] based on the given [RequestResponsePacts]. In order to
         * handle provider state of the pacts a callback handler might be provided.
         *
         * A callback handler can be an instance of _any_ class containing [@ProviderState][ProviderState] methods.
         * Usually this is the test class instance itself.
         *
         * @param pacts the pacts in which the tests will be based on
         * @param consumerFilter an optional consumer filter for limiting which pacts are actually used
         * @param callbackHandler an optional callback handler for [@ProviderState][ProviderState] methods
         *
         * @see DynamicTest
         * @see RequestResponsePacts
         */
        @JvmStatic
        fun createTests(pacts: RequestResponsePacts, consumerFilter: String? = null, callbackHandler: Any? = null): List<DynamicTest> {
            return pacts.createExecutablePacts(consumerFilter, callbackHandler)
                    .map { dynamicTest(it.name, it.executable) }
        }

        /**
         * Creates a list of [dynamic tests][DynamicTest] based on the given [MessagePacts]. In order to handle
         * message producers of the pacts, a callback handler must be provided.
         *
         * A callback handler can be an instance of _any_ class containing [@MessageProducer][MessageProducer] methods.
         * Usually this is the test class instance itself.
         *
         * @param pacts the pacts in which the tests will be based on
         * @param consumerFilter an optional consumer filter for limiting which pacts are actually used
         * @param callbackHandler a callback handler for [@MessageProducer][MessageProducer] methods
         *
         * @see DynamicTest
         * @see MessagePacts
         */
        @JvmStatic
        fun createTests(pacts: MessagePacts, consumerFilter: String? = null, callbackHandler: Any): List<DynamicTest> {
            return pacts.createExecutablePacts(consumerFilter, callbackHandler)
                    .map { dynamicTest(it.name, it.executable) }
        }

    }

}