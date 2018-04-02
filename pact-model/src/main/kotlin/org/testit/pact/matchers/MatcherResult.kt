package org.testit.pact.matchers


sealed class MatcherResult

object Match : MatcherResult()

data class Mismatch(val reasons: List<String>) : MatcherResult()

internal fun mismatch(vararg reasons: String): Mismatch {
    return Mismatch(reasons.toList())
}

internal fun mismatch(reasons: Iterable<String>): Mismatch {
    return Mismatch(reasons.toList())
}