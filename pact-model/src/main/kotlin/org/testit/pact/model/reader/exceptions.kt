package org.testit.pact.model.reader

import com.fasterxml.jackson.core.JsonParseException


class MalformedPactFileException(cause: JsonParseException)
    : PactReaderException("The pact file is malformed: ${cause.message}", cause)

class UnsupportedPactVersionException(actualVersion: String)
    : PactReaderException("The pact's version $actualVersion is not supported! Currently only v3.0.0 is supported.")

class UnidentifiablePactException
    : MalformedPactException("The pact kind could not be determined! It contained neither 'interactions' nor 'messages'.")

open class MalformedPactException(reason: String?, cause: Throwable? = null)
    : PactReaderException("The pact is malformed: $reason", cause)

open class PactReaderException(msg: String?, cause: Throwable? = null)
    : RuntimeException(msg, cause)