package org.testit.pact.provider.message

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testit.pact.provider.sources.LocalFiles
import java.time.OffsetDateTime


internal class MessagePactsTest {

    val cut = MessagePacts(LocalFiles("src/test/pacts/MessagePactsTest"), "library-service")

    @Test fun `without a consumer filter all found pacts are returned`() {
        val executablePacts = cut.createExecutablePacts(callbackHandler = CallbackHandler())
        assertThat(executablePacts).hasSize(2)
    }

    @Test fun `with a consumer filter only matching pacts are returned`() {
        val executablePacts = cut.createExecutablePacts("library-notifier", CallbackHandler())
        assertThat(executablePacts).hasSize(1)
    }

    @Test fun `exception is thrown if no pacts could be found`() {
        assertThrows<NoMessagePactsFoundException> {
            cut.createExecutablePacts("unknown", CallbackHandler())
        }
    }

    @Nested inner class `response matching` {

        val callbackHandler = CallbackHandler()

        @Test fun `if there are no mismatches all pacts are executed successfully`() {
            cut.createExecutablePacts(callbackHandler = callbackHandler).forEach { it.executable() }
        }

        @Test fun `body mismatches are detected`() {
            callbackHandler.messageBody = """
                {
                    "id": "aa1dc09f-7b64-4e7e-a6f6-7eb50dcd6e9d",
                    "bookId": "wrong-id",
                    "isbn": "0091956141",
                    "timestamp": "${OffsetDateTime.now()}"
                }
                """
            with(executePactsExpectingError()) {
                assertThat(result.bodyErrors[0])
                        .isEqualTo("[\$.bookId] was expected to be [9bf258be-19d4-4338-b172-60a1b7ef076b] but was actually [wrong-id]")
                assertThat(result.bodyErrors[1])
                        .isEqualTo("[\$.isbn] was expected to be [9780091956141] but was actually [0091956141]")
            }
        }

        fun executePactsExpectingError() = assertThrows<MessageMismatchException> {
            cut.createExecutablePacts("library-enrichment", callbackHandler).forEach { it.executable() }
        }

    }

    @Nested inner class `message producers` {

        @Test fun `exception in case a callback method could not be found`() {
            assertThrows<MessageProducerMethodNotFoundException> {
                executePacts(NoOpCallbackHandler())
            }
        }

        @Test fun `exception in case a callback method is malformed`() {
            assertThrows<MalformedMessageProducerMethodException> {
                executePacts(MalformedCallbackHandler())
            }
        }

        @Test fun `exception in case there was an exception while invoking a provider state method`() {
            assertThrows<MessageProducerInvocationException> {
                executePacts(ExceptionalCallbackHandler())
            }
        }

        fun executePacts(callbackHandler: Any) =
                cut.createExecutablePacts("library-enrichment", callbackHandler).forEach { it.executable() }

    }

    class NoOpCallbackHandler

    class CallbackHandler {

        var messageBody: String = """
                {
                    "id": "aa1dc09f-7b64-4e7e-a6f6-7eb50dcd6e9d",
                    "bookId": "9bf258be-19d4-4338-b172-60a1b7ef076b",
                    "isbn": "9780091956141",
                    "timestamp": "${OffsetDateTime.now()}"
                }
                """

        @MessageProducer("'The Martian' was added event", "any book was added event")
        fun messageProducer(): ActualMessage {
            return ActualMessage(messageBody.toByteArray())
        }

    }

    class MalformedCallbackHandler {

        @MessageProducer("'The Martian' was added event")
        fun messageProducer(paramA: String): ActualMessage {
            return ActualMessage("".toByteArray())
        }

    }

    class ExceptionalCallbackHandler {

        @MessageProducer("'The Martian' was added event")
        fun messageProducer(): ActualMessage {
            throw RuntimeException()
        }

    }

}