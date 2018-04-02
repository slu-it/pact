package org.testit.pact.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.testit.pact.model.PactSpecification.Companion.parse


internal class PactSpecificationTest {

    @CsvSource(
            "1.0, V1_0", "1.0.0, V1_0",
            "1.1, V1_1", "1.1.0, V1_1",
            "2.0, V2_0", "2.0.0, V2_0",
            "3.0, V3_0", "3.0.0, V3_0",
            "4.0, V4_0", "4.0.0, V4_0"
    )
    @ParameterizedTest(name = "[{0}] is parsed as [{1}]")
    fun `version numbers can be parsed`(version: String, expected: PactSpecification) {
        assertThat(parse(version)).isSameAs(expected)
    }

    @ValueSource(strings = ["5.0", "foo"])
    @ParameterizedTest fun `parsing unknown versions returns null`(version: String) {
        assertThat(parse(version)).isNull()
    }

    @EnumSource(PactSpecification::class)
    @ParameterizedTest fun `toString method returns version property`(specification: PactSpecification) {
        assertThat(specification.toString()).isEqualTo(specification.version)
    }

}