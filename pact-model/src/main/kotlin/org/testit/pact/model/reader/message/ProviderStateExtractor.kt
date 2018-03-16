package org.testit.pact.model.reader.message

import org.testit.pact.model.reader.common.AbstractProviderStateExtractor

internal class ProviderStateExtractor : AbstractProviderStateExtractor() {
    override val containerType = "message"
}