package org.testit.pact.matchers

import org.testit.pact.model.Response

internal class StatusMatcher {

    fun match(response: Response, actualStatus: Int) = when (response.status) {
        null, actualStatus -> Match
        else -> mismatch("expected response 'status' to be [${response.status}] but was [$actualStatus]")
    }

}