package org.testit.pact.provider.message

import au.com.dius.pact.model.v3.messaging.Message
import au.com.dius.pact.model.v3.messaging.MessagePact
import org.testit.pact.commons.logger
import org.testit.pact.provider.ExecutablePact
import org.testit.pact.provider.ExecutablePactFactory
import org.testit.pact.provider.sources.PactSource

class MessagePacts(
        private val pactSource: PactSource,
        private val provider: String
) : ExecutablePactFactory {

    private val log = MessagePacts::class.logger
    private val matcher = MessageMatcher()

    override fun createExecutablePacts(consumerFilter: String?, callbackHandler: Any?): List<ExecutablePact> {
        if(callbackHandler == null) error("message pacts require a callback handler")
        return loadMessagePacts(provider, consumerFilter)
                .flatMap { pact ->
                    pact.messages.map { message ->
                        ExecutablePact(
                                name = "${pact.consumer.name}: ${message.description}",
                                executable = { executeMessageTest(message, callbackHandler) }
                        )
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

    private fun executeMessageTest(expectedMessage: Message, callbackHandler: Any) {
        val actualMessage = produceMessage(expectedMessage, callbackHandler)

        val result = matcher.match(expectedMessage, actualMessage)
        if (result.hasErrors) {
            throw AssertionError("Message expectation(s) were not met:\n\n$result")
        }

        log.info("Message interaction [{}] matched expectations.", expectedMessage.description)
    }

    private fun produceMessage(message: Message, callbackHandler: Any): ActualMessage {
        val messageProviderMethod = callbackHandler.javaClass.declaredMethods
                .single { it.getAnnotation(MessageProducer::class.java)?.value == message.description }
        return messageProviderMethod.invoke(callbackHandler) as ActualMessage
    }

}