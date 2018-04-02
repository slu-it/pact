package org.testit.pact.matchers

import org.testit.pact.model.Response

class ResponseMatcher {

    // TODO: support matching rules for status, headers and body

    private val statusMatcher = StatusMatcher()
    private val headersMatcher = HeadersMatcher()
    private val bodyMatcher = BodyMatcher()

    fun matchStatus(response: Response, actualStatus: Int): MatcherResult =
            statusMatcher.match(response, actualStatus)

    fun matchHeaders(response: Response, actualHeaders: Map<String, String>): MatcherResult =
            headersMatcher.match(response, actualHeaders)

    fun matchBody(response: Response, actualBody: String?): MatcherResult =
            bodyMatcher.match(response, actualBody)

}