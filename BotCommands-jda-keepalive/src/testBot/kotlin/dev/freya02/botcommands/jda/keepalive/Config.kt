package dev.freya02.botcommands.jda.keepalive

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import kotlin.io.path.Path

data class Config(
    val token: String,
) {

    companion object {
        val configDirectory = Path("test-files", "test", "dev-config")
        private val configFile = configDirectory.resolve("config.json")

        @get:BService
        val instance: Config by lazy {
            DefaultObjectMapper.mapper
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(configFile.toFile())
        }
    }
}
