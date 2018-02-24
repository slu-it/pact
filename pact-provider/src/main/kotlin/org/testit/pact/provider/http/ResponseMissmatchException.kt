package org.testit.pact.provider.http

class ResponseMissmatchException(val result: ResponseMatcher.Result)
    : AssertionError("Response expectation(s) were not met:\n\n$result")