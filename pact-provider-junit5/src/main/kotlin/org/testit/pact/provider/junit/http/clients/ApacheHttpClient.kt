package org.testit.pact.provider.junit.http.clients

import au.com.dius.pact.model.Request
import au.com.dius.pact.model.orElse
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpResponse
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.*
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.testit.pact.provider.junit.http.ComparableResponse
import org.testit.pact.provider.junit.http.HttpTarget
import java.net.URI

class ApacheHttpClient(
        private val client: CloseableHttpClient = HttpClients.createDefault()
) : HttpClient {

    override fun execute(pactRequest: Request, httpTarget: HttpTarget): ComparableResponse {
        val httpRequest = buildRequest(pactRequest, httpTarget)
        return client.execute(httpRequest).use { extractResponse(it) }
    }

    private fun buildRequest(request: Request, httpTarget: HttpTarget): HttpUriRequest {
        val uri = buildUri(httpTarget, request)
        return buildRequestBase(uri, request).apply {
            addHeaders(request)
            addBody(request)
        }
    }

    private fun buildUri(httpTarget: HttpTarget, request: Request): URI {
        val uriBuilder = URIBuilder().apply {
            scheme = httpTarget.protocol()
            host = httpTarget.host()
            port = httpTarget.port()
            path = httpTarget.contextPath() + request.path

            if (!isUrlEncodedFormPost(request)) {
                request.query?.forEach { (key, values) ->
                    values.forEach { value ->
                        addParameter(key, value)
                    }
                }
            }
        }
        return uriBuilder.build()
    }

    private fun buildRequestBase(uri: URI, request: Request) = when (request.lowerCaseMethod) {
        "get" -> HttpGet(uri)
        "post" -> HttpPost(uri)
        "put" -> HttpPut(uri)
        "delete" -> HttpDelete(uri)
        "options" -> HttpOptions(uri)
        "head" -> HttpHead(uri)
        "trace" -> HttpTrace(uri)
        "patch" -> HttpPatch(uri)
        else -> error("unmapped http method: ${request.lowerCaseMethod}")
    }

    private fun HttpRequestBase.addHeaders(request: Request) {
        val requestHeaders = request.headers
        if (requestHeaders != null) {
            if (!requestHeaders.containsKey("Content-Type")) {
                this.addHeader("Content-Type", ContentType.APPLICATION_JSON.mimeType)
            }
            requestHeaders.forEach { (name, value) ->
                this.addHeader(name, value)
            }
        }
    }

    private fun HttpRequestBase.addBody(request: Request) {
        if (this is HttpEntityEnclosingRequest) {
            if (isUrlEncodedFormPost(request)) {
                val charset = "UTF-8"
                val parameters = request.query.flatMap { entry -> entry.value.map { BasicNameValuePair(entry.key, it) } }
                this.entity = UrlEncodedFormEntity(parameters, charset)
            } else if (request.body?.isPresent() == true) {
                this.entity = StringEntity(request.body.orElse(""))
            }
        }
    }

    private fun extractResponse(httpResponse: HttpResponse): ComparableResponse {
        val status: Int = httpResponse.statusLine.statusCode
        val headers: Map<String, String> = httpResponse.allHeaders.associate { header -> header.name to header.value }
        val body: String? = httpResponse.entity?.let {
            val contentType: ContentType = it.contentType
                    ?.let { ContentType.parse(it.value) }
                    ?: ContentType.TEXT_PLAIN
            EntityUtils.toString(it, contentType.charset?.name() ?: "UTF-8")
        }
        return ComparableResponse(status = status, headers = headers, body = body)
    }

    private fun isUrlEncodedFormPost(request: Request) =
            request.lowerCaseMethod == "post" && request.mimeType() == "application/x-www-form-urlencoded"

    private val Request.lowerCaseMethod
        get() = method.toLowerCase()

}
