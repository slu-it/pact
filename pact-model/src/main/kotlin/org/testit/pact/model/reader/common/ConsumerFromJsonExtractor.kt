package org.testit.pact.model.reader.common

import org.testit.pact.model.Consumer
import org.testit.pact.model.reader.MalformedPactException

internal class ConsumerFromJsonExtractor {

    fun extract(json: Map<String, Any>) = try {
        val consumer = json["consumer"]
        require(consumer is Map<*, *>) { "pact property 'consumer' [$consumer] is not an object" }
        consumer as Map<*, *>

        val name = consumer["name"]
        require(name is String) { "pact property 'consumer.name' [$name] is not a string" }
        name as String

        Consumer(name)
    } catch (e: IllegalArgumentException) {
        throw MalformedPactException(e.message)
    }

}