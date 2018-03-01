package org.testit.pact.provider.sources

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactReader
import mu.KotlinLogging.logger
import java.io.File

class LocalFiles(
        private val pactFolder: String
) : PactSource {

    private val log = logger {}

    override fun loadPacts(providerFilter: String, consumerFilter: String?): List<Pact> {
        val consumerOrAstrix = consumerFilter ?: "*"
        log.info { "trying to load pacts for provider '$providerFilter' and consumer '$consumerOrAstrix' from '$pactFolder'" }

        val pacts = getPotentialPactFiles()
                .also { log.debug { "files identified as potential pacts: $it" } }
                .mapNotNull(::tryToLoadPact).toMap()
                .also { log.debug { "${it.size} files successfully loaded as pacts: ${it.fileNames}" } }
                .filter { it.value.provider.name == providerFilter }
                .also { log.debug { "${it.size} pacts match provider '$providerFilter': ${it.fileNames}" } }
                .filter { if (consumerFilter != null) it.value.consumer.name == consumerFilter else true }
                .also { log.debug { "${it.size} pacts match provider '$providerFilter' and consumer '$consumerOrAstrix': ${it.fileNames}" } }

        log.info { "actually loaded pacts: ${pacts.fileNames}" }
        return pacts.map { it.value }
    }

    private fun getPotentialPactFiles(): List<File> {
        val folder = File(pactFolder).also {
            require(it.isDirectory) { "Folder '$pactFolder' is not a directory or it does not exist!" }
        }
        return folder.listFiles { _, filename -> filename.endsWith(".json") }.toList()
    }

    private fun tryToLoadPact(file: File): Pair<String, Pact>? = try {
        file.name to PactReader.loadPact(file)
    } catch (e: Exception) {
        log.warn(e) { "[$file] could not be loaded as a pact" }
        null
    }

    private val Map<String, Pact>.fileNames: List<String>
        get() = map { it.key }

    override fun toString() = "LocalFiles: $pactFolder"

}