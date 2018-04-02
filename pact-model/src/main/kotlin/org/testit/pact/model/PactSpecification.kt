package org.testit.pact.model

enum class PactSpecification(
        private val shortVersion: String,
        val version: String
) {

    V1_0("1.0", "1.0.0"),
    V1_1("1.1", "1.1.0"),
    V2_0("2.0", "2.0.0"),
    V3_0("3.0", "3.0.0"),
    V4_0("4.0", "4.0.0");

    companion object {

        fun parse(value: String): PactSpecification? {
            return PactSpecification.values().find { value.startsWith(it.shortVersion) }
        }

    }

    override fun toString(): String = version

}