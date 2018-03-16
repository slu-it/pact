package org.testit.pact.model.json

import java.io.InputStream

interface JsonParser {
    fun parse(inputStream: InputStream): Map<String, Any>
}