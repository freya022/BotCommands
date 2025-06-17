package io.github.freya022.botcommands.autoconfigure

import io.github.freya022.botcommands.internal.core.SpringBotCommandsConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(SpringBotCommandsConfiguration::class)
internal open class BotCommandsAutoConfiguration