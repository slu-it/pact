package utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class WireMockExtension(
        options: Options = WireMockConfiguration().dynamicPort()
) : WireMockServer(options), BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) = start()
    override fun beforeEach(context: ExtensionContext) = resetAll()
    override fun afterAll(context: ExtensionContext) = stop()

}