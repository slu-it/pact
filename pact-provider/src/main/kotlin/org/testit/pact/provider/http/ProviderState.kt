package org.testit.pact.provider.http

import kotlin.annotation.Target

@Retention
@Target(AnnotationTarget.FUNCTION)
annotation class ProviderState(val value: String)