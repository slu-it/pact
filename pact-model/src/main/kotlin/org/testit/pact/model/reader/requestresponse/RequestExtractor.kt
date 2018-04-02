package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.HttpMethod
import org.testit.pact.model.Request

internal class RequestExtractor {

    private val headersExtractor = RequestHeadersExtractor()
    private val queryExtractor = QueryExtractor()
    private val bodyExtractor = RequestBodyExtractor()

    fun extract(data: Map<String, Any>): Request {
        val request = data["request"]
        require(request is Map<*, *>) { "interaction property 'request' [$request] is not an object" }
        request as Map<*, *>

        val method = getMethod(request)
        val path = getPath(request)
        val headers = headersExtractor.extract(request)
        val query = queryExtractor.extract(request)
        val body = bodyExtractor.extract(request)
        return Request(method, path, query, headers, body)
    }

    private fun getMethod(data: Map<*, *>): HttpMethod {
        val method = data["method"]
        require(method is String) { "interaction property 'request.method' [$method] is not a string" }
        method as String

        val httpMethod = HttpMethod.parse(method)
        require(httpMethod != null) { "interaction property 'request.method' [$method] is not a known HTTP method" }
        return httpMethod!!
    }

    private fun getPath(data: Map<*, *>): String {
        val path = data["path"]
        require(path is String) { "interaction property 'request.path' [$path] is not a string" }
        return path as String
    }

}