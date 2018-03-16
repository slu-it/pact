package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.v3.Response

internal class ResponseExtractor {

    private val headersExtractor = ResponseHeadersExtractor()
    private val bodyExtractor = ResponseBodyExtractor()

    fun extract(data: Map<String, Any>): Response {
        val response = data["response"]
        require(response is Map<*, *>) { "interaction property 'response' [$response] is not an object" }
        response as Map<*, *>

        val status = getStatus(response)
        val headers = headersExtractor.extract(response)
        val body = bodyExtractor.extract(response)
        return Response(status, headers, body)
    }

    private fun getStatus(data: Map<*, *>): Int {
        val status = data["status"]
        require(status is Int) { "interaction property 'response.status' [$status] is not a number" }
        return status as Int
    }

}