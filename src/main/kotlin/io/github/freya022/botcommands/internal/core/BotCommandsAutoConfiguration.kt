package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.config.JDAConfiguration
import io.github.freya022.botcommands.internal.core.config.BotCommandsCoreConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(
    basePackages = [
        "io.github.freya022.botcommands.api",
        "io.github.freya022.botcommands.internal",
    ]
)
@ConfigurationPropertiesScan(basePackageClasses = [JDAConfiguration::class, BotCommandsCoreConfiguration::class])
open class BotCommandsAutoConfiguration