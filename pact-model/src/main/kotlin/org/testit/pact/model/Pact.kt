package org.testit.pact.model

interface Pact {
    val provider: Provider
    val consumer: Consumer
    val metadata: PactMetadata
}