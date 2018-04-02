package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.*
import org.testit.pact.model.reader.MalformedPactException

internal class RequestResponsePactFromJsonExtractor {

    private val descriptionExtractor = DescriptionExtractor()
    private val providerStateExtractor = ProviderStateExtractor()
    private val requestExtractor = RequestExtractor()
    private val responseExtractor = ResponseExtractor()

    fun extract(provider: Provider, consumer: Consumer, metadata: PactMetadata, interactionData: List<Map<String, Any>>): RequestResponsePact {
        try {
            val interactions = interactionData.map(::toInteraction)
            return RequestResponsePact(provider, consumer, metadata, interactions)
        } catch (e: IllegalArgumentException) {
            throw MalformedPactException(e.message, e)
        } catch (e: IllegalStateException) {
            throw MalformedPactException(e.message, e)
        }
    }

    private fun toInteraction(data: Map<String, Any>): Interaction {
        val description = descriptionExtractor.extract(data)
        val providerStates = providerStateExtractor.extract(data)
        val request = requestExtractor.extract(data)
        val response = responseExtractor.extract(data)
        return Interaction(description, providerStates, request, response)
    }

}