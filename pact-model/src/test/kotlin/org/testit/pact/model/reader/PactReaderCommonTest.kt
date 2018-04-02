package org.testit.pact.model.reader

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.testit.pact.model.Pact
import org.testit.pact.model.PactSpecification
import org.testit.pact.model.json.JacksonJsonParser


@DisplayName("PactReader: common tests")
internal class PactReaderCommonTest {

    private val jsonParser = JacksonJsonParser()
    private val cut = PactReader(jsonParser)

    @Test fun `pact file content must be JSON`() {
        assertThrows<MalformedPactFileException> {
            loadPact("<pact>fo bar</pact>")
        }
    }

    @Test fun `pact file must contain either interactions or messages`() {
        assertThrows<UnidentifiablePactException> {
            loadPact("""{
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "3.0.0" } }
                }""")
        }
    }

    @DisplayName("only supported pact specification versions are allowed")
    @Nested inner class SupportedVersions {

        @ValueSource(strings = ["3.0.0"])
        @ParameterizedTest fun `supported`(version: String) {
            val pact = loadPact("""{
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "$version" } },
                "interactions": []
            }""")
            assertThat(pact.specification).isEqualTo(PactSpecification.parse(version))
        }

        @ValueSource(strings = ["1.0.0", "1.1.0", "2.0.0", "4.0.0"])
        @ParameterizedTest fun `not supported`(version: String) {
            assertThrows<UnsupportedPactSpecificationVersionException> {
                loadPact("""{
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "$version" } }
                }""")
            }
        }

    }

    @DisplayName("all pacts must contain basic data")
    @Nested inner class BasicDataTests {

        @DisplayName("provider")
        @Nested inner class ProviderTests {

            @Test fun `provider must exist`() =
                    assertThrowsMalformedPactException("pact property 'provider' [null] is not an object") {
                        """{
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } }
                        }"""
                    }

            @Test fun `provider must be an object`() =
                    assertThrowsMalformedPactException("pact property 'provider' [true] is not an object") {
                        """{
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "provider": true
                        }"""
                    }

            @Test fun `provider must have a name`() =
                    assertThrowsMalformedPactException("pact property 'provider.name' [null] is not a string") {
                        """{
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "provider": { }
                        }"""
                    }

            @Test fun `provider name must be a string`() =
                    assertThrowsMalformedPactException("pact property 'provider.name' [true] is not a string") {
                        """{
                            "consumer": { "name": "bar-consumer" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "provider": { "name": true }
                        }"""
                    }

        }

        @DisplayName("consumer")
        @Nested inner class ConsumerTests {

            @Test fun `consumer must exist`() =
                    assertThrowsMalformedPactException("pact property 'consumer' [null] is not an object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } }
                        }"""
                    }

            @Test fun `consumer must be an object`() =
                    assertThrowsMalformedPactException("pact property 'consumer' [true] is not an object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "consumer": true
                        }"""
                    }

            @Test fun `consumer must have a name`() =
                    assertThrowsMalformedPactException("pact property 'consumer.name' [null] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "consumer": { }
                        }"""
                    }

            @Test fun `consumer name must be a string`() =
                    assertThrowsMalformedPactException("pact property 'consumer.name' [true] is not a string") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "metadata": { "pact-specification": { "version": "3.0.0" } },
                            "consumer": { "name": true }
                        }"""
                    }

        }

        @DisplayName("metadata")
        @Nested inner class MetadataTests {

            @Test fun `metadata must exist`() =
                    assertThrowsMalformedPactException("pact property 'metadata' [null] is not an object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" }
                        }"""
                    }

            @Test fun `metadata must be an object`() =
                    assertThrowsMalformedPactException("pact property 'metadata' [true] is not an object") {
                        """{
                            "provider": { "name": "foo-provider" },
                            "consumer": { "name": "bar-consumer" },
                            "metadata": true
                        }"""
                    }

            @DisplayName("metadata must contain pact specification")
            @Nested inner class PactSpecificationTests {

                @Test fun `pact specification must exist`() =
                        assertThrowsMalformedPactException("pact property 'metadata.pact-specification' [null] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { }
                            }"""
                        }

                @Test fun `pact specification must be an object`() =
                        assertThrowsMalformedPactException("pact property 'metadata.pact-specification' [true] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": true }
                            }"""
                        }

                @Test fun `pact specification must have a version`() =
                        assertThrowsMalformedPactException("pact property 'metadata.pact-specification.version' [null] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { } }
                            }"""
                        }

                @Test fun `pact specification version must be a string`() =
                        assertThrowsMalformedPactException("pact property 'metadata.pact-specification.version' [true] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": true } }
                            }"""
                        }

                @Test fun `pact specification version must be a known version`() =
                        assertThrowsMalformedPactException("pact property 'metadata.pact-specification.version' [abc] is not a known version") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "abc" } }
                            }"""
                        }

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