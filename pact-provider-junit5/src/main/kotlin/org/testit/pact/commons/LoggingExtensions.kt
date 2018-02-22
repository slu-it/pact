package org.testit.pact.commons

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/** Returns the [Logger] instance of this [KClass]. */
internal val KClass<*>.logger: Logger get() = LoggerFactory.getLogger(java)