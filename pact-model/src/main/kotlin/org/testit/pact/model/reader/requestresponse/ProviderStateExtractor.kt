package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.reader.common.AbstractProviderStateExtractor

internal class ProviderStateExtractor : AbstractProviderStateExtractor() {
    override val containerType = "interaction"
}