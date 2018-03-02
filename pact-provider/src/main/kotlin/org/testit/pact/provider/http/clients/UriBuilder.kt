package org.testit.pact.provider.http.clients

import au.com.dius.pact.model.Request
import mu.KotlinLogging.logger
import org.apache.http.client.utils.URIBuilder
import org.testit.pact.provider.http.Target
import java.net.URI

internal object UriBuilder {

    private val log = logger {}

    fun build(target: Target, request: Request): URI {
        log.debug { "building URI for target '${target.url}' and request [path='${request.path}'; queryParameters=${request.query}]" }

        val url = request.path?.let { target.urlWith(it) } ?: target.url
        val uriBuilder = URIBuilder(url.toURI())

        request.query?.forEach { (key, values) ->
            if (values.isEmpty()) {
                log.warn { "query parameter '$key' has no values and will be ignored" }
            }
            values.forEach { value ->
                uriBuilder.addParameter(key, value)
            }
        }

        return uriBuilder.build().also { log.debug { "built URI: $it" } }
    }

}