package org.testit.pact.model.reader

import org.testit.pact.model.json.JsonParser
import org.testit.pact.model.reader.common.ConsumerFromJsonExtractor
import org.testit.pact.model.reader.common.PactMetadataFromJsonExtractor
import org.testit.pact.model.reader.common.ProviderFromJsonExtractor
import org.testit.pact.model.reader.message.MessagePactFromJsonExtractor
import org.testit.pact.model.reader.requestresponse.RequestResponsePactFromJsonExtractor
import org.testit.pact.model.v3.MessagePact
import org.testit.pact.model.v3.Pact
import org.testit.pact.model.v3.PactMetadata
import org.testit.pact.model.v3.RequestResponsePact
import java.io.InputStream

class PactReader(
        private val jsonParser: JsonParser
) {

    private val providerExtractor = ProviderFromJsonExtractor()
    private val consumerExtractor = ConsumerFromJsonExtractor()
    private val metadataExtractor = PactMetadataFromJsonExtractor()

    private val requestResponsePactExtractor = RequestResponsePactFromJsonExtractor()
    private val messagePactExtractor = MessagePactFromJsonExtractor()

    /**
     * Parses the given [InputStream] as either a [RequestResponsePact] or a
     * [MessagePact], depending ont the actual content.
     *
     * Currently only Pact specification v3.0.0 content is supported!
     *
     * @throws MalformedPactFileException
     *          if the given stream contains non-JSON content
     * @throws MalformedPactException
     *          if the pact is missing required data
     * @throws UnsupportedPactVersionException
     *          if the stream contains a pact of an unsupported version
     */
    @Suppress("UNCHECKED_CAST")
    fun loadPact(inputStream: InputStream): Pact {
        val json = jsonParser.parse(inputStream)

        val provider = providerExtractor.extract(json)
        val consumer = consumerExtractor.extract(json)
        val metadata = metadataExtractor.extract(json)

        assertSupportedVersions(metadata)

        return when {
            json["interactions"] is List<*> -> {
                val interactions = json["interactions"] as List<Map<String, Any>>
                requestResponsePactExtractor.extract(provider, consumer, metadata, interactions)
            }
            json["messages"] is List<*> -> {
                val messages = json["messages"] as List<Map<String, Any>>
                messagePactExtractor.extract(provider, consumer, metadata, messages)
            }
            else -> throw UnidentifiablePactException()
        }
    }

    private fun assertSupportedVersions(metadata: PactMetadata) {
        val version = metadata.pactSpecification.version
        if (version != "3.0.0") {
            throw UnsupportedPactVersionException(version)
        }
    }

}