package io.github.freya022.pokedex.bot.config

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

data class Config(val token: String) {

    companion object {
        private val logger = KotlinLogging.logger { }

        @get:BService
        val instance: Config by lazy {
            val configFilePath: Path = Environment.configFolder.resolve("config.json")
            logger.info { "Loading configuration at ${configFilePath.absolutePathString()}" }

            return@lazy DefaultObjectMapper.mapper.readValue(configFilePath.readText())
        }
    }
}
