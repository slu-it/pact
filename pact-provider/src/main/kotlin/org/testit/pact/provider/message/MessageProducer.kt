package org.testit.pact.provider.message

@Retention
@Target(AnnotationTarget.FUNCTION)
annotation class MessageProducer(vararg val value: String)