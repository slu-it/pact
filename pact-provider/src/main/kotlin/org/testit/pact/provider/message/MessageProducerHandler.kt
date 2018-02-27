package org.testit.pact.provider.message

import au.com.dius.pact.model.v3.messaging.Message
import java.lang.reflect.Method

class MessageProducerHandler(
        private val callbackHandler: Any
) {

    private val messageProducerMethods = collectMessageProducerMethods(callbackHandler)

    fun produce(message: Message): ActualMessage {
        val producerName = message.description

        val method = messageProducerMethods[producerName]
                ?: throw MessageProducerMethodNotFoundException(producerName)
        return when {
            method.parameterCount == 0 -> method.tryToInvokeMethod(producerName)
            else -> throw MalformedMessageProducerMethodException(producerName)
        }
    }

    private fun Method.tryToInvokeMethod(stateName: String, vararg parameters: Any): ActualMessage {
        try {
            return invoke(callbackHandler, *parameters) as ActualMessage
        } catch (e: Exception) {
            throw MessageProducerInvocationException(stateName, e)
        }
    }

    private fun collectMessageProducerMethods(callbackHandler: Any): Map<String, Method> {
        return callbackHandler.javaClass.declaredMethods
                .filter { it.isAnnotationPresent(MessageProducer::class.java) }
                .flatMap { method ->
                    method.getAnnotation(MessageProducer::class.java).value.map { alias ->
                        alias to method
                    }
                }
                .toMap()
    }

}

class MessageProducerMethodNotFoundException(producerName: String)
    : RuntimeException("Could not find a method for message producer [$producerName]!")

class MalformedMessageProducerMethodException(producerName: String)
    : RuntimeException("The method for message producer [$producerName] is malformed! Only methods without parameters are allowed.")

class MessageProducerInvocationException(producerName: String, cause: Throwable)
    : RuntimeException("There was an exception while producing the message [$producerName]:", cause)