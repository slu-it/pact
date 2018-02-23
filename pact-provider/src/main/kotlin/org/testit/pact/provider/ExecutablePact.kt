package org.testit.pact.provider

data class ExecutablePact(
        val name: String,
        val executable: () -> Unit
)