package org.testit.pact.provider.junit.http.clients

import au.com.dius.pact.model.Request
import org.testit.pact.provider.junit.http.ComparableResponse
import org.testit.pact.provider.junit.http.HttpTarget

interface HttpClient {
    fun execute(pactRequest: Request, httpTarget: HttpTarget): ComparableResponse
}