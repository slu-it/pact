[![License](https://img.shields.io/badge/License-Apache%20License%202.0-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/nt-ca-aqe/pact.svg?branch=master)](https://travis-ci.org/nt-ca-aqe/pact)

# testIT | Pact Libraries

This repository hosts libraries related to working with Pact (https://docs.pact.io).

## Pact Provider JUnit 5

The `pact-provider-junit5` module contains a basic provider side test factory
implementations for running `RequestResponsePact` and `MessagePact` pacts
using JUnit 5's `@TestFactory` mechanism.

**Request / Response Pact provider side tests:**

```kotlin
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [Application::class], webEnvironment = RANDOM_PORT)
class MyRequestResponseContractTest {

    val testFactory = RequestResponsePactTestFactory(
            pactSource = PactFileLoader("src/test/pacts"),
            provider = "some-provider"
    )

    @LocalServerPort
    fun init(port: Int) {
        testFactory.httpTarget.port = { port }
    }

    @TestFactory fun `all my request response contract tests`() =
            testFactory.createTests(callbackHandler = this)

    @ProviderState("Some provider state with {parameters}")
    fun `some provider state`(params: Map<String, String>) {
        // ...
    }

}
```

**Message Pact provider side tests:**

```kotlin
class MyMessageContractTest {

    val messageProducer = ...

    val testFactory = MessagePactTestFactory(
            pactSource = PactFileLoader("src/test/pacts/message"),
            provider = "library-service"
    )

    @TestFactory fun `all my message contract tests`() =
            testFactory.createTests(callbackHandler = this)

    @MessageProducer("Some Message Producer")
    fun verifySomeMessage(): ComparableMessage {
        val message = messageProducer.produceMessage()
        return ComparableMessage(message.body)
    }

}
```

## Licensing
TestUtils is licensed under [The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

## Sponsoring
The contents of this repository is mainly developed by
[NovaTec Consulting GmbH](http://www.novatec-gmbh.de/),
a German consultancy firm that drives quality in software development projects.
