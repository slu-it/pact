package org.testit.pact.matchers

import org.testit.pact.model.Response
import org.testit.pact.model.json.JacksonJsonParser


internal class BodyMatcher {

    // TODO: support HTML?

    private companion object {
        val JSON_CONTENT_TYPE = Regex("""application/((.)*?\+)?json(;.*)?""")
        val XML_CONTENT_TYPE = Regex("""application/xml(;.*)?""")

        val JSON_GUESS = Regex("""^\s*(true|false|null|[0-9]+|"\w*|\{\s*(}|"\w+)|\[\s*).*""")
        val XML_HEADER_GUESS = Regex("""^\s*<\?xml\s*version.*""")
        val XML_GUESS = Regex("""^\s*<\w+\s*(:\w+=["”][^"”]+["”])?.*""")
        val HTML_GUESS = Regex("""^\s*(<!DOCTYPE)|(<HTML>)|(<html>).*""")
    }

    private val jsonBodyMatcher = JsonBodyMatcher()
    private val xmlBodyMatcher = XmlBodyMatcher()
    private val plainBodyMatcher = PlainBodyMatcher()

    fun match(response: Response, actualBody: String?): MatcherResult = when {
        response.body == null -> Match
        actualBody == null -> mismatch("expected response 'body' not to be null but it was")
        else -> matchByContentType(response, actualBody)
    }

    private fun matchByContentType(response: Response, actualBody: String): MatcherResult {
        val contentType = response.headers["Content-Type"] ?: determineContentType(response.body!!)
        return when {
            contentType.matches(JSON_CONTENT_TYPE) -> jsonBodyMatcher.match(response, actualBody)
            contentType.matches(XML_CONTENT_TYPE) -> xmlBodyMatcher.match(response, actualBody)
            else -> plainBodyMatcher.match(response, actualBody)
        }
    }

    private fun determineContentType(content: String): String {
        // TODO: log!
        val sample = content.take(64)
                .replace('\n', ' ')
                .replace('\r', ' ')
        return when {
            sample.matches(XML_HEADER_GUESS) -> "application/xml"
            sample.matches(HTML_GUESS) -> "text/html"
            sample.matches(JSON_GUESS) -> "application/json"
            sample.matches(XML_GUESS) -> "application/xml"
            else -> "text/plain"
        }
    }

    private class JsonBodyMatcher {

        private val jsonParser = JacksonJsonParser() // TODO: as constructor param

        fun match(response: Response, actualBody: String): MatcherResult {
            val responseBody = response.body ?: error("------") // TODO: message?

            val expectedJson = jsonParser.parse(responseBody.byteInputStream())
            val actualJson = jsonParser.parse(actualBody.byteInputStream())

            if (expectedJson == actualJson) {
                return Match
            }

            val reasons = getReasons("\$", expectedJson, actualJson)
            return Mismatch(reasons)
        }

        @Suppress("UNCHECKED_CAST")
        private fun getReasons(parentPath: String, expectedJson: Map<String, Any>, actualJson: Map<String, Any>): List<String> {
            val reasons = mutableListOf<String>()
            expectedJson.forEach { key, expectedValue ->
                val path = "$parentPath.$key"
                val actualValue = actualJson[key]
                if (actualValue != expectedValue) {

                    if (expectedValue is Map<*, *> && actualValue is Map<*, *>) {

                        expectedValue as Map<String, Any>
                        actualValue as Map<String, Any>
                        reasons.addAll(getReasons(path, expectedValue, actualValue))

                    } else if (expectedValue is List<*> && actualValue is List<*>) {



                    } else {
                        reasons.add("expected response 'body' property [$path] to be equal to [$expectedValue] but was [$actualValue]")
                    }

                }
            }
            return reasons
        }

    }

    private class XmlBodyMatcher {

        fun match(response: Response, actualBody: String): MatcherResult {
            if (response.body == actualBody) {
                return Match
            }
            TODO()
        }

    }

    private class PlainBodyMatcher {

        fun match(response: Response, actualBody: String?): MatcherResult {
            if (response.body == actualBody) {
                return Match
            }
            return mismatch("expected response 'body' to be equal to [${response.body}] but was [$actualBody]")
        }

    }

}