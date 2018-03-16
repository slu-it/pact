package org.testit.pact.model.reader.requestresponse

import org.testit.pact.model.reader.common.AbstractDescriptionExtractor

internal class DescriptionExtractor : AbstractDescriptionExtractor() {
    override val containerType = "interaction"
}