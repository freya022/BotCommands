package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.internal.core.BotCommandsInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Import(BotCommandsInitializer::class)
@Retention(AnnotationRetention.RUNTIME)
@ComponentScan(
    basePackages = [
        "io.github.freya022.botcommands.api",
        "io.github.freya022.botcommands.internal",
    ]
)
annotation class EnableBotCommands
