package org.testit.pact.model.reader

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.testit.pact.model.MessagePact
import org.testit.pact.model.Pact
import org.testit.pact.model.PactSpecification.V3_0
import org.testit.pact.model.json.JacksonJsonParser

@DisplayName("PactReader: MessagePact tests")
internal class PactReaderMessagePactTest {

    private val jsonParser = JacksonJsonParser()
    private val cut = PactReader(jsonParser)

    @Test fun `minimal pact file content`() {
        val pact = loadPact("""
            {
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "3.0.0" } },
                "messages": []
            }
            """)

        with(pact as MessagePact) {
            assertThat(provider.name).isEqualTo("foo-provider")
            assertThat(consumer.name).isEqualTo("bar-consumer")
            assertThat(messages).isEmpty()
            assertThat(specification).isEqualTo(V3_0)
        }
    }

    @Test fun `with minimal message`() {
        val pact = loadPact("""
            {
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "3.0.0" } },
                "messages": [{
                    "description": "some unique description"
                }]
            }
            """)

        with(pact as MessagePact) {
            assertThat(messages).hasSize(1)
            with(messages[0]) {
                assertThat(description).isEqualTo("some unique description")
                assertThat(providerStates).isEmpty()
                assertThat(contents).isNull()
                assertThat(metaData).isEmpty()
            }
        }
    }

    @Test fun `with maximum message`() {
        val pact = loadPact("""
            {
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "3.0.0" } },
                "messages": [{
                    "description": "some unique description",
                    "providerStates": [{
                            "name": "state without parameters"
                        },{
                            "name": "state with parameters",
                            "params": {
                                "foo": "string",
                                "bar": 42,
                                "xur": true
                            }
                        }],
                    "contents": {
                        "foo": "string",
                        "bar": 42,
                        "xur": true
                    },
                    "metaData": {
                        "contentType": "application/json",
                        "id": "42",
                        "dynamic": "true"
                    }
                }]
            }
            """)

        with(pact as MessagePact) {
            assertThat(messages).hasSize(1)
            with(messages[0]) {
                assertThat(description).isEqualTo("some unique description")

                assertThat(providerStates).hasSize(2)
                with(providerStates[0]) {
                    assertThat(name).isEqualTo("state without parameters")
                    assertThat(parameters).isEmpty()
                }
                with(providerStates[1]) {
                    assertThat(name).isEqualTo("state with parameters")
                    assertThat(parameters)
                            .containsEntry("foo", "string")
                            .containsEntry("bar", 42)
                            .containsEntry("xur", true)
                }

                assertThat(contents).isEqualToIgnoringWhitespace("""
                    {
                        "foo": "string",
                        "bar": 42,
                        "xur": true
                    }
                    """)

                assertThat(metaData)
                        .containsEntry("contentType", "application/json")
                        .containsEntry("id", "42")
                        .containsEntry("dynamic", "true")
                        .hasSize(3)
            }
        }
    }

    @Test fun `with JSON array content`() {
        val pact = loadPact("""
            {
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "3.0.0" } },
                "messages": [{
                    "description": "description",
                    "contents": [{
                        "foo": "bar"
                    },{
                        "foo": null
                    }]
                }]
            }
            """)

        with(pact as MessagePact) {
            assertThat(messages).hasSize(1)
            assertThat(messages[0].contents).isEqualToIgnoringWhitespace("""
                [{ "foo": "bar" }, { "foo": null } ]
                """)
        }
    }

    @Test fun `with XML content`() {
        val pact = loadPact("""
            {
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "3.0.0" } },
                "messages": [{
                    "description": "description",
                    "contents": "<message><foo>bar</foo></message>"
                }]
            }
            """)

        with(pact as MessagePact) {
            assertThat(messages).hasSize(1)
            assertThat(messages[0].contents).isEqualTo("<message><foo>bar</foo></message>")
        }
    }

    @DisplayName("malformed messages throw exception")
    @Nested inner class MalformedMessagesThrowException {

        @DisplayName("description property")
        @Nested inner class DescriptionProperty {

            @Test fun `description must not be null`() =
                    assertThrowsMalformedPactException("message property 'description' [null] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{ }]
                        }"""
                    }

            @Test fun `description must be a string`() =
                    assertThrowsMalformedPactException("message property 'description' [true] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": true
                            }]
                        }"""
                    }

        }

        @DisplayName("providerStates property")
        @Nested inner class ProviderStatesProperty {

            @Test fun `providerStates must be a an array`() =
                    assertThrowsMalformedPactException("message property 'providerStates' [true] is not an array of objects") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "providerStates": true
                            }]
                        }"""
                    }

            @Test fun `providerStates must be a an array of objects`() =
                    assertThrowsMalformedPactException("message property 'providerStates.[0]' [true] is not an object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "providerStates": [true, false]
                            }]
                        }"""
                    }

            @Test fun `providerStates must have names`() =
                    assertThrowsMalformedPactException("message property 'providerStates.[0].name' [null] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "providerStates": [{ }]
                            }]
                        }"""
                    }

            @Test fun `providerState name must be a string`() =
                    assertThrowsMalformedPactException("message property 'providerStates.[0].name' [true] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "providerStates": [{
                                    "name": true
                                }]
                            }]
                        }"""
                    }

            @Test fun `providerState parameters must be a map`() =
                    assertThrowsMalformedPactException("message property 'providerStates.[0].params' [true] is not a map") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "providerStates": [{
                                    "name": "name",
                                    "params": true
                                }]
                            }]
                        }"""
                    }

            @Test fun `providerState parameters must not contain null entries`() =
                    assertThrowsMalformedPactException("message property 'providerStates.[0].params.foo' is null") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "providerStates": [{
                                    "name": "name",
                                    "params": {
                                        "foo": null
                                    }
                                }]
                            }]
                        }"""
                    }

        }

        @DisplayName("contents property")
        @Nested inner class ContentsProperty {

            @Test fun `contents must be a JSON, JSON Array or String`() =
                    assertThrowsMalformedPactException("message property 'contents' [true] is neither a JSON, a JSON array or a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "contents": true
                            }]
                        }"""
                    }

            @Test fun `array content without objects throws exception`() =
                    assertThrowsMalformedPactException("message property 'contents.[0]' [true] is not a JSON object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "contents": [true, false]
                            }]
                        }"""
                    }

        }

        @DisplayName("metaData property")
        @Nested inner class MetaDataProperty {

            @Test fun `metaData must be a JSON`() =
                    assertThrowsMalformedPactException("message property 'metaData' [true] is not an object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "metaData": true
                            }]
                        }"""
                    }

            @Test fun `metaData entries are not allowed to be null`() =
                    assertThrowsMalformedPactException("message property 'metaData.foo' [null] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "metaData": {
                                    "foo": null
                                }
                            }]
                        }"""
                    }

            @Test fun `metaData entries must be strings`() =
                    assertThrowsMalformedPactException("message property 'metaData.foo' [true] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "messages": [{
                                "description": "description",
                                "metaData": {
                                    "foo": true
                                }
                            }]
                        }"""
                    }

        }

    }

    private fun loadPact(content: String): Pact = cut.loadPact(content.trimIndent().byteInputStream())

    private fun assertThrowsMalformedPactException(expectedReason: String, contentSupplier: () -> String) {
        assertThatThrownBy { loadPact(contentSupplier()) }
                .isInstanceOf(MalformedPactException::class.java)
                .hasMessage("The pact is malformed: $expectedReason")
    }

}