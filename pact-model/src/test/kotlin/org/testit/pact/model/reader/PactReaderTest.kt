package org.testit.pact.model.reader

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.testit.pact.model.json.JacksonJsonParser
import org.testit.pact.model.v3.MessagePact
import org.testit.pact.model.v3.Method
import org.testit.pact.model.v3.Pact
import org.testit.pact.model.v3.RequestResponsePact


internal class PactReaderTest {

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
            loadPact("""{
                "provider": { "name": "foo-provider" },
                "consumer": { "name": "bar-consumer" },
                "metadata": { "pact-specification": { "version": "$version" } },
                "interactions": []
            }""")
        }

        @ValueSource(strings = ["1.0.0", "1.1.0", "2.0.0", "4.0.0"])
        @ParameterizedTest fun `not supported`(version: String) {
            assertThrows<UnsupportedPactVersionException> {
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

                @Test fun `pact specification  version must be a string`() =
                        assertThrowsMalformedPactException("pact property 'metadata.pact-specification.version' [true] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": true } }
                            }"""
                        }

            }

        }

    }

    @DisplayName("request response pacts")
    @Nested inner class RequestResponsePactTests {

        @Test fun `minimal pact file content`() {
            val pact = loadPact("""
                {
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                    "interactions": []
                }
                """)

            with(pact as RequestResponsePact) {
                assertThat(provider.name).isEqualTo("foo-provider")
                assertThat(consumer.name).isEqualTo("bar-consumer")
                assertThat(interactions).isEmpty()
                assertThat(metadata.pactSpecification.version).isEqualTo("3.0.0")
            }
        }

        @Test fun `with minimal interaction`() {
            val pact = loadPact("""
                {
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                    "interactions": [{
                        "description": "some unique description",
                        "request": {
                            "method": "GET",
                            "path": "/"
                        },
                        "response": {
                            "status": 200
                        }
                    }]
                }
                """)

            with(pact as RequestResponsePact) {
                assertThat(interactions).hasSize(1)
                with(interactions[0]) {
                    assertThat(description).isEqualTo("some unique description")
                    assertThat(providerStates).isEmpty()
                    with(request) {
                        assertThat(method).isSameAs(Method.GET)
                        assertThat(path).isEqualTo("/")
                        assertThat(headers).isEmpty()
                        assertThat(query).isEmpty()
                        assertThat(body).isNull()
                    }
                    with(response) {
                        assertThat(status).isEqualTo(200)
                        assertThat(headers).isEmpty()
                        assertThat(body).isNull()
                    }
                }
            }
        }

        @Test fun `with maximum interaction`() {
            val pact = loadPact("""
                {
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                    "interactions": [{
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
                        "request": {
                            "method": "POST",
                            "path": "/",
                            "query": {
                                "foo": ["value"],
                                "bar": ["value #1", "value #2"]
                            },
                            "headers": {
                                "Content-Type": "application/json",
                                "X-B3-TraceId": "1a2b3c4d5e6f"
                            },
                            "body": {
                                "foo": "value",
                                "bar": true
                            }
                        },
                        "response": {
                            "status": 200,
                            "headers": {
                                "Content-Type": "application/json",
                                "X-B3-TraceId": "1a2b3c4d5e6f"
                            },
                            "body": {
                                "foo": "value",
                                "bar": true
                            }
                        }
                    }]
                }
                """)

            with(pact as RequestResponsePact) {
                assertThat(interactions).hasSize(1)
                with(interactions[0]) {
                    assertThat(description).isEqualTo("some unique description")
                    assertThat(providerStates).hasSize(2)
                    with(providerStates!!) {
                        with(get(0)) {
                            assertThat(name).isEqualTo("state without parameters")
                            assertThat(parameters).isEmpty()
                        }
                        with(get(1)) {
                            assertThat(name).isEqualTo("state with parameters")
                            assertThat(parameters)
                                    .containsEntry("foo", "string")
                                    .containsEntry("bar", 42)
                                    .containsEntry("xur", true)
                        }
                    }
                    with(request) {
                        assertThat(method).isSameAs(Method.POST)
                        assertThat(path).isEqualTo("/")
                        assertThat(query)
                                .containsEntry("foo", listOf("value"))
                                .containsEntry("bar", listOf("value #1", "value #2"))
                                .hasSize(2)
                        assertThat(headers)
                                .containsEntry("Content-Type", "application/json")
                                .containsEntry("X-B3-TraceId", "1a2b3c4d5e6f")
                                .hasSize(2)
                        assertThat(body).isEqualToIgnoringWhitespace("""{ "foo": "value", "bar": true }""")
                    }
                    with(response) {
                        assertThat(status).isEqualTo(200)
                        assertThat(headers)
                                .containsEntry("Content-Type", "application/json")
                                .containsEntry("X-B3-TraceId", "1a2b3c4d5e6f")
                                .hasSize(2)
                        assertThat(body).isEqualToIgnoringWhitespace("""{ "foo": "value", "bar": true }""")
                    }
                }
            }
        }

        @Test fun `with JSON array content`() {
            val pact = loadPact("""
                {
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                    "interactions": [{
                        "description": "some unique description",
                        "request": {
                            "method": "POST",
                            "path": "/",
                            "body": [{
                                "foo": "bar"
                            },{
                                "foo": null
                            }]
                        },
                        "response": {
                            "status": 200,
                            "body": [{
                                "foo": null
                            },{
                                "foo": "bar"
                            }]
                        }
                    }]
                }
                """)

            with(pact as RequestResponsePact) {
                assertThat(interactions).hasSize(1)
                assertThat(interactions[0].request.body)
                        .isEqualToIgnoringWhitespace("""[{ "foo": "bar" }, { "foo": null } ]""")
                assertThat(interactions[0].response.body)
                        .isEqualToIgnoringWhitespace("""[{ "foo": null }, { "foo": "bar" } ]""")
            }
        }

        @Test fun `with XML content`() {
            val pact = loadPact("""
                {
                    "provider": { "name": "foo-provider" },
                    "consumer": { "name": "bar-consumer" },
                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                    "interactions": [{
                        "description": "some unique description",
                        "request": {
                            "method": "POST",
                            "path": "/",
                            "body": "<message><foo>bar</foo></message>"
                        },
                        "response": {
                            "status": 200,
                            "body": "<message><bar>foo</bar></message>"
                        }
                    }]
                }
                """)

            with(pact as RequestResponsePact) {
                assertThat(interactions).hasSize(1)
                assertThat(interactions[0].request.body)
                        .isEqualToIgnoringWhitespace("<message><foo>bar</foo></message>")
                assertThat(interactions[0].response.body)
                        .isEqualToIgnoringWhitespace("<message><bar>foo</bar></message>")
            }
        }

        @DisplayName("malformed interactions throw exception")
        @Nested inner class MalformedInteractionsThrowException {

            @DisplayName("description property")
            @Nested inner class DescriptionProperty {

                @Test fun `description must not be null`() =
                        assertThrowsMalformedPactException("interaction property 'description' [null] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{ }]
                            }"""
                        }

                @Test fun `description must be a string`() =
                        assertThrowsMalformedPactException("interaction property 'description' [true] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": true
                                }]
                            }"""
                        }

            }

            @DisplayName("providerStates property")
            @Nested inner class ProviderStatesProperty {

                @Test fun `providerStates must be a an array`() =
                        assertThrowsMalformedPactException("interaction property 'providerStates' [true] is not an array of objects") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "providerStates": true
                                }]
                            }"""
                        }

                @Test fun `providerStates must be a an array of objects`() =
                        assertThrowsMalformedPactException("interaction property 'providerStates.[0]' [true] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "providerStates": [true, false]
                                }]
                            }"""
                        }

                @Test fun `providerStates must have names`() =
                        assertThrowsMalformedPactException("interaction property 'providerStates.[0].name' [null] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "providerStates": [{ }]
                                }]
                            }"""
                        }

                @Test fun `providerState name must be a string`() =
                        assertThrowsMalformedPactException("interaction property 'providerStates.[0].name' [true] is not a string") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "providerStates": [{
                                        "name": true
                                    }]
                                }]
                            }"""
                        }

                @Test fun `providerState parameters must be a map`() =
                        assertThrowsMalformedPactException("interaction property 'providerStates.[0].params' [true] is not a map") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
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

            @DisplayName("request property")
            @Nested inner class RequestProperty {

                @Test fun `request must not be null`() =
                        assertThrowsMalformedPactException("interaction property 'request' [null] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description"
                                }]
                            }"""
                        }

                @Test fun `request must be an object`() =
                        assertThrowsMalformedPactException("interaction property 'request' [true] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "request": true
                                }]
                            }"""
                        }

                @DisplayName("method property")
                @Nested inner class MethodProperty {

                    @Test fun `method must not be null`() =
                            assertThrowsMalformedPactException("request property 'method' [null] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {}
                                    }]
                                }"""
                            }

                    @Test fun `method must be a string`() =
                            assertThrowsMalformedPactException("request property 'method' [true] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": true
                                        }
                                    }]
                                }"""
                            }

                }

                @DisplayName("path property")
                @Nested inner class PathProperty {

                    @Test fun `path must not be null`() =
                            assertThrowsMalformedPactException("request property 'path' [null] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET"
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `path must be a string`() =
                            assertThrowsMalformedPactException("request property 'path' [true] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": true
                                        }
                                    }]
                                }"""
                            }

                }

                @DisplayName("headers property")
                @Nested inner class HeadersProperty {

                    @Test fun `headers must be an object`() =
                            assertThrowsMalformedPactException("interaction property 'request.headers' [true] is not an object") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "headers": true
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `header property values must be strings`() =
                            assertThrowsMalformedPactException("interaction property 'request.headers.foo' [true] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "headers": {
                                                "foo": true
                                            }
                                        }
                                    }]
                                }"""
                            }

                }

                @DisplayName("query property")
                @Nested inner class QueryProperty {

                    @Test fun `query must be an object`() =
                            assertThrowsMalformedPactException("interaction property 'request.query' [true] is not an object") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "query": true
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `query property values must be arrays`() =
                            assertThrowsMalformedPactException("interaction property 'request.query.foo' [true] is not an array") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "query": {
                                                "foo": true
                                            }
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `query property values must be arrays of strings`() =
                            assertThrowsMalformedPactException("interaction property 'request.query.foo[0]' [true] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "query": {
                                                "foo": [true, false]
                                            }
                                        }
                                    }]
                                }"""
                            }

                }

                @DisplayName("body property")
                @Nested inner class BodyProperty {

                    @Test fun `body must be a JSON, JSON Array or String`() =
                            assertThrowsMalformedPactException("interaction property 'request.body' [true] is neither a JSON, a JSON array or a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "body": true
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `array body without objects throws exception`() =
                            assertThrowsMalformedPactException("interaction property 'request.body.[0]' [true] is not a JSON object") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/",
                                            "body": [true, false]
                                        }
                                    }]
                                }"""
                            }

                }

            }

            @DisplayName("response property")
            @Nested inner class ResponseProperty {

                @Test fun `response must not be null`() =
                        assertThrowsMalformedPactException("interaction property 'response' [null] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "request": {
                                        "method": "GET",
                                        "path": "/"
                                    }
                                }]
                            }"""
                        }

                @Test fun `response must be an object`() =
                        assertThrowsMalformedPactException("interaction property 'response' [true] is not an object") {
                            """{
                                "provider": { "name": "foo-provider" },
                                "consumer": { "name": "bar-consumer" },
                                "metadata": { "pact-specification": { "version": "3.0.0" } },
                                "interactions": [{
                                    "description": "description",
                                    "request": {
                                        "method": "GET",
                                        "path": "/"
                                    },
                                    "response": true
                                }]
                            }"""
                        }

                @DisplayName("status property")
                @Nested inner class StatusProperty {

                    @Test fun `status must not be null`() =
                            assertThrowsMalformedPactException("interaction property 'response.status' [null] is not a number") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/"
                                        },
                                        "response": {}
                                    }]
                                }"""
                            }

                    @Test fun `status must be a number`() =
                            assertThrowsMalformedPactException("interaction property 'response.status' [true] is not a number") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/"
                                        },
                                        "response": {
                                            "status": true
                                        }
                                    }]
                                }"""
                            }

                }

                @DisplayName("headers property")
                @Nested inner class HeadersProperty {

                    @Test fun `headers must be an object`() =
                            assertThrowsMalformedPactException("interaction property 'response.headers' [true] is not an object") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/"
                                        },
                                        "response": {
                                            "status": 200,
                                            "headers": true
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `header property values must be strings`() =
                            assertThrowsMalformedPactException("interaction property 'response.headers.foo' [true] is not a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/"
                                        },
                                        "response": {
                                            "status": 200,
                                            "headers": {
                                                "foo": true
                                            }
                                        }
                                    }]
                                }"""
                            }

                }

                @DisplayName("body property")
                @Nested inner class BodyProperty {

                    @Test fun `body must be a JSON, JSON Array or String`() =
                            assertThrowsMalformedPactException("interaction property 'response.body' [true] is neither a JSON, a JSON array or a string") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/"
                                        },
                                        "response": {
                                            "status": 200,
                                            "body": true
                                        }
                                    }]
                                }"""
                            }

                    @Test fun `array body without objects throws exception`() =
                            assertThrowsMalformedPactException("interaction property 'response.body.[0]' [true] is not a JSON object") {
                                """{
                                    "provider": { "name": "foo-provider" },
                                    "consumer": { "name": "bar-consumer" },
                                    "metadata": { "pact-specification": { "version": "3.0.0" } },
                                    "interactions": [{
                                        "description": "description",
                                        "request": {
                                            "method": "GET",
                                            "path": "/"
                                        },
                                        "response": {
                                            "status": 200,
                                            "body": [true, false]
                                        }
                                    }]
                                }"""
                            }

                }

            }

        }

    }

    @DisplayName("message pacts")
    @Nested inner class MessagePactTests {

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
                assertThat(metadata.pactSpecification.version).isEqualTo("3.0.0")
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
                    with(providerStates!![0]) {
                        assertThat(name).isEqualTo("state without parameters")
                        assertThat(parameters).isEmpty()
                    }
                    with(providerStates!![1]) {
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

    }

    private fun loadPact(content: String): Pact = cut.loadPact(content.trimIndent().byteInputStream())

    private fun assertThrowsMalformedPactException(expectedReason: String, contentSupplier: () -> String) {
        assertThatThrownBy { loadPact(contentSupplier()) }
                .isInstanceOf(MalformedPactException::class.java)
                .hasMessage("The pact is malformed: $expectedReason")
    }

}