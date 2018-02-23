package org.testit.pact.provider.http

import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import org.testit.pact.commons.logger
import org.testit.pact.provider.ExecutablePact
import org.testit.pact.provider.ExecutablePactFactory
import org.testit.pact.provider.http.clients.ApacheHttpClient
import org.testit.pact.provider.http.clients.HttpClient
import org.testit.pact.provider.message.ExecutableMessagePactFactory
import org.testit.pact.provider.sources.PactSource

class ExecutableRequestResponsePactFactory(
        private val pactSource: PactSource,
        private val provider: String,
        private val httpClient: HttpClient = ApacheHttpClient(),
        val target: Target = Target()
) : ExecutablePactFactory {

    private val log = ExecutableMessagePactFactory::class.logger
    private val matcher = ResponseMatcher()

    override fun createExecutablePacts(consumerFilter: String?, callbackHandler: Any): List<ExecutablePact> {
        return loadRequestResponsePacts(provider, consumerFilter)
                .flatMap { pact ->
                    pact.interactions.map { interaction ->
                        ExecutablePact(
                                name = "${pact.consumer.name}: ${interaction.description}",
                                executable = { executeRequestResponseTest(interaction, callbackHandler) }
                        )
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
        val actualResponse = httpClient.send(interaction.request, target)

        val result = matcher.match(expectedResponse, actualResponse)
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