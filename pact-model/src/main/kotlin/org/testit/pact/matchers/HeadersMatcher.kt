package org.testit.pact.matchers

import org.testit.pact.model.Response

internal class HeadersMatcher {

    fun match(response: Response, actualHeaders: Map<String, String>): MatcherResult {
        if (response.headers.isEmpty()) {
            return Match
        }
        return doMatch(response, actualHeaders)
    }

    private fun doMatch(response: Response, actualHeaders: Map<String, String>): MatcherResult {
        val reasons = mutableListOf<String>()

        with(response.headers) {
            filter { !actualHeaders.containsKey(it.key) }.forEach {
                reasons.add("expected response 'headers' to contain [${it.key}] but they didn't")
            }
            filter { actualHeaders.containsKey(it.key) }.forEach {
                val expected = it.value
                val actual = actualHeaders[it.key]
                if (actual != expected) {
                    reasons.add("expected response 'header' [${it.key}] to be equal to [$expected] but was [$actual]")
                }
            }
        }

        if (reasons.isNotEmpty()) {
            return mismatch(reasons)
        }
        return Match
    }

}