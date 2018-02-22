package org.testit.pact.provider.junit.http

import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.testit.pact.commons.logger
import org.testit.pact.provider.junit.PactSource
import org.testit.pact.provider.junit.http.clients.ApacheHttpClient
import org.testit.pact.provider.junit.http.clients.HttpClient
import org.testit.pact.provider.junit.message.MessagePactTestFactory

class RequestResponsePactTestFactory(
        private val pactSource: PactSource,
        private val provider: String,
        private val httpClient: HttpClient = ApacheHttpClient(),
        val httpTarget: HttpTarget = HttpTarget()
) {

    private val log = MessagePactTestFactory::class.logger
    private val comparator = ResponseComparator()

    fun createTests(consumerFilter: String? = null, callbackHandler: Any): List<DynamicTest> {
        return loadRequestResponsePacts(provider, consumerFilter)
                .flatMap { pact ->
                    pact.interactions.map { interaction ->
                        dynamicTest("${pact.consumer.name}: ${interaction.description}") {
                            executeRequestResponseTest(interaction, callbackHandler)
                        }
                    }
                }
    }

    private fun loadRequestResponsePacts(providerFilter: String, consumerFilter: String?): List<RequestResponsePact> {
        val pacts = pactSource.loadPacts(providerFilter, consumerFilter)
                .filter { it is RequestResponsePact }
                .map { it as RequestResponsePact }
        log.debug("loaded {} request/response pacts from [{}] for providerFilter={} and consumerFilter={}", pacts.size, pactSource, providerFilter, consumerFilter)
        if (pacts.isEmpty()) {
            error("no matching pacts found")
        }
        return pacts
    }

    private fun executeRequestResponseTest(interaction: RequestResponseInteraction, callbackHandler: Any) {
        prepareInteractionState(interaction, callbackHandler)

        val expectedResponse = interaction.response
        val actualResponse = httpClient.execute(interaction.request, httpTarget)

        val result = comparator.compare(expectedResponse, actualResponse)
        if (result.hasErrors) {
            throw AssertionError("Response expectation(s) were not met:\n\n$result")
        }

        log.info("Response for interaction [{}] matched expectations.", interaction.description)
    }

    private fun prepareInteractionState(interaction: Interaction, callbackHandler: Any) {
        interaction.providerStates.forEach { state ->
            val stateChangeMethod = callbackHandler.javaClass.declaredMethods
                    .single { it.getAnnotation(ProviderState::class.java)?.value == state.name }
            if (stateChangeMethod.parameterCount == 0) {
                stateChangeMethod.invoke(callbackHandler)
            } else {
                stateChangeMethod.invoke(callbackHandler, state.params)
            }
        }
    }

}