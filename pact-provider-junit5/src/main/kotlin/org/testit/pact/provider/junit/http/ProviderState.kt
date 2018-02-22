package org.testit.pact.provider.junit.http

@Retention
@Target(AnnotationTarget.FUNCTION)
annotation class ProviderState(val value: String)