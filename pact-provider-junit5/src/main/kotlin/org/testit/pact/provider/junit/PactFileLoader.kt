package org.testit.pact.provider.junit

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactReader
import java.io.File

class PactFileLoader(
        private val pactFolder: String
) : PactSource {

    override fun loadPacts(providerFilter: String, consumerFilter: String?): List<Pact> {
        val folder = File(pactFolder)
        if (!folder.isDirectory) {
            error("Folder [$pactFolder] is not a directory or it does not exist!")
        }
        return folder.listFiles { _, filename -> filename.endsWith(".json") }
                .map { PactReader.loadPact(it) }
                .filter { it.provider.name == providerFilter }
                .filter { if (consumerFilter != null) it.consumer.name == consumerFilter else true }
    }

    override fun toString() = "Pact Source: [$pactFolder]"

}