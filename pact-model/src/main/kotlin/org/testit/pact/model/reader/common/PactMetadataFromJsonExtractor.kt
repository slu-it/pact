package org.testit.pact.model.reader.common

import org.testit.pact.model.PactSpecification
import org.testit.pact.model.reader.MalformedPactException

internal class PactMetadataFromJsonExtractor {

    fun extractSpecification(json: Map<String, Any>) = try {
        val metadata = json["metadata"]
        require(metadata is Map<*, *>) { "pact property 'metadata' [$metadata] is not an object" }
        metadata as Map<*, *>

        val pactSpecification = metadata["pact-specification"]
        require(pactSpecification is Map<*, *>) { "pact property 'metadata.pact-specification' [$pactSpecification] is not an object" }
        pactSpecification as Map<*, *>

        val version = pactSpecification["version"]
        require(version is String) { "pact property 'metadata.pact-specification.version' [$version] is not a string" }
        version as String

        val specification = PactSpecification.parse(version)
        require(specification != null) { "pact property 'metadata.pact-specification.version' [$version] is not a known version" }

        specification!!
    } catch (e: IllegalArgumentException) {
        throw MalformedPactException(e.message)
    }

}