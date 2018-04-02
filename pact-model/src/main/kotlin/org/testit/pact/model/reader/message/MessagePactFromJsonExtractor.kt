package org.testit.pact.model.reader.message

import org.testit.pact.model.*
import org.testit.pact.model.reader.MalformedPactException

internal class MessagePactFromJsonExtractor {

    private val descriptionExtractor = DescriptionExtractor()
    private val providerStateExtractor = ProviderStateExtractor()
    private val contentsExtractor = ContentsExtractor()
    private val metaDataExtractor = MetaDataExtractor()

    fun extract(provider: Provider, consumer: Consumer, specification: PactSpecification, messagesData: List<Map<String, Any>>): MessagePact {
        try {
            val messages = messagesData.map(::toMessage)
            return MessagePact(provider, consumer, specification, messages)
        } catch (e: IllegalArgumentException) {
            throw MalformedPactException(e.message, e)
        } catch (e: IllegalStateException) {
            throw MalformedPactException(e.message, e)
        }
    }

    private fun toMessage(data: Map<String, Any>): Message {
        val description = descriptionExtractor.extract(data)
        val providerStates = providerStateExtractor.extract(data)
        val contents = contentsExtractor.extract(data)
        val metaData = metaDataExtractor.extract(data)
        return Message(description, providerStates, contents, metaData)
    }

}