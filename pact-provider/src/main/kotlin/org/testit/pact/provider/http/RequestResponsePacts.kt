package org.testit.pact.provider.http

import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import org.testit.pact.commons.logger
import org.testit.pact.provider.ExecutablePact
import org.testit.pact.provider.http.clients.ApacheHttpClient
import org.testit.pact.provider.http.clients.HttpClient
import org.testit.pact.provider.message.MessagePacts
import org.testit.pact.provider.sources.PactSource

class RequestResponsePacts(
        private val pactSource: PactSource,
        private val provider: String,
        private val httpClient: HttpClient = ApacheHttpClient(),
        val target: Target = Target()
) {

    private val log = MessagePacts::class.logger
    private val matcher = ResponseMatcher()

    fun createExecutablePacts(consumerFilter: String? = null, callbackHandler: Any? = null): List<ExecutablePact> {
        val providerStateHandler = ProviderStateHandler(callbackHandler)
        return loadRequestResponsePacts(provider, consumerFilter)
                .flatMap { pact ->
                    pact.interactions.map { interaction ->
                        ExecutablePact(
                                name = "${pact.consumer.name}: ${interaction.description}",
                                executable = { executeRequestResponseTest(interaction, providerStateHandler) }
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
            throw NoRequestResponsePactsFoundException(pactSource, provider, consumerFilter)
        }
        return pacts
    }

    private fun executeRequestResponseTest(interaction: RequestResponseInteraction, providerStateHandler: ProviderStateHandler) {
        providerStateHandler.prepare(interaction)

        val expectedResponse = interaction.response
        val actualResponse = httpClient.send(interaction.request, target)

        val result = matcher.match(expectedResponse, actualResponse)
        if (result.hasErrors) {
            throw ResponseMismatchException(result)
        }

        log.info("Response for interaction [{}] matched expectations.", interaction.description)
    }

}

class NoRequestResponsePactsFoundException(pactSource: PactSource, provider: String, consumerFilter: String?)
    : RuntimeException("Did not find any request / response pacts in source [$pactSource] for the provider [$provider] and a consumer filter of [$consumerFilter]")

class ResponseMismatchException(val result: ResponseMatcher.Result)
    : AssertionError("Response expectation(s) were not met:\n\n$result")