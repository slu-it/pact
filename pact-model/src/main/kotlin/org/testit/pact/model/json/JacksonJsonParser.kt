package org.testit.pact.model.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import org.testit.pact.model.reader.MalformedPactFileException
import java.io.InputStream

internal class JacksonJsonParser : JsonParser {

    private val objectMapper = ObjectMapper()

    override fun parse(inputStream: InputStream): Map<String, Any> = try {
        objectMapper.readValue(inputStream, JsonAsMap::class.java)
    } catch (e: JsonParseException) {
        throw MalformedPactFileException(e)
    }

    private class JsonAsMap : HashMap<String, Any>()

}