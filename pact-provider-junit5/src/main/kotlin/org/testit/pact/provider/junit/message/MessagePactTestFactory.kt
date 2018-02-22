package org.testit.pact.provider.junit.message

import au.com.dius.pact.model.v3.messaging.Message
import au.com.dius.pact.model.v3.messaging.MessagePact
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.testit.pact.commons.logger
import org.testit.pact.provider.junit.PactSource

class MessagePactTestFactory(
        private val pactSource: PactSource,
        private val provider: String
) {

    private val log = MessagePactTestFactory::class.logger
    private val comparator = MessageComparator()

    fun createTests(consumerFilter: String? = null, callbackHandler: Any) = loadMessagePacts(provider, consumerFilter)
            .flatMap { pact ->
                pact.messages.map { message ->
                    dynamicTest("${pact.consumer.name}: ${message.description}") {
                        executeMessageTest(message, callbackHandler)
                    }
                }
            }

    private fun loadMessagePacts(providerFilter: String, consumerFilter: String?): List<MessagePact> {
        val pacts = pactSource.loadPacts(providerFilter, consumerFilter)
                .filter { it is MessagePact }
                .map { it as MessagePact }
        log.debug("loaded {} message pacts from [{}] for providerFilter={} and consumerFilter={}", pacts.size, pactSource, providerFilter, consumerFilter)
        if (pacts.isEmpty()) {
            error("no matching pacts found")
        }
        return pacts
    }

    private fun executeMessageTest(message: Message, callbackHandler: Any) {
        val actualMessage = produceMessage(message, callbackHandler)

        val result = comparator.compare(message, actualMessage)
        if (result.hasErrors) {
            throw AssertionError("Message expectation(s) were not met:\n\n$result")
        }

        log.info("Message interaction [{}] matched expectations.", message.description)
    }

    private fun produceMessage(message: Message, callbackHandler: Any): ComparableMessage {
        val messageProviderMethod = callbackHandler.javaClass.declaredMethods
                .single { it.getAnnotation(MessageProducer::class.java)?.value == message.description }
        return messageProviderMethod.invoke(callbackHandler) as ComparableMessage
    }

}