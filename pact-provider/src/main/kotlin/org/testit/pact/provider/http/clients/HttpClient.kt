package org.testit.pact.provider.http.clients

import au.com.dius.pact.model.Request
import org.testit.pact.provider.http.MatchableResponse
import org.testit.pact.provider.http.Target

interface HttpClient {
    fun send(pactRequest: Request, target: Target): MatchableResponse
}