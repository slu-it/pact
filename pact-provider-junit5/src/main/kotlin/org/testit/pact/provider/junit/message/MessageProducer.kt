package org.testit.pact.provider.junit.message

@Retention
@Target(AnnotationTarget.FUNCTION)
annotation class MessageProducer(val value: String)