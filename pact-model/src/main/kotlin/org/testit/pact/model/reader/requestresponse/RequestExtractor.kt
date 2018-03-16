package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.v3.Method
import org.testit.pact.model.v3.Request

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

    private fun getMethod(data: Map<*, *>): Method {
        val method = data["method"]
        require(method is String) { "request property 'method' [$method] is not a string" }
        return Method.parse(method as String) ?: error("could not identify method [$method]") // TODO: better text
    }

    private fun getPath(data: Map<*, *>): String {
        val path = data["path"]
        require(path is String) { "request property 'path' [$path] is not a string" }
        return path as String
    }

}