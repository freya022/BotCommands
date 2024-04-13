package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.internal.core.BotCommandsInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component

/**
 * Entry point annotation for Spring projects.
 *
 * This annotation can only be used once in your main class.
 *
 * The only requirement for a basic bot is a service extending [JDAService],
 * learn more on [the wiki](https://freya022.github.io/BotCommands/3.X/setup/getting-started/from-scratch/#creating-a-jdaservice).
 *
 * ### Usage with BotCommands annotations
 * While this enables support for Spring-managed instances to be used in the application,
 * some classes may still require usage of specific annotations,
 * such as [@Command][Command] or [@Resolver][Resolver],
 * but are [@Component][Component] specializations, and thus Spring beans.
 *
 * ### Component scanning
 * Your main class must **not** use [@ComponentScan][ComponentScan] directly,
 * prefer using your own annotation meta-annotated with the scanner, for example:
 * ```kt
 * @ComponentScan("my.bot.package")
 * annotation class EnableMyBot
 *
 * @EnableMyBot
 * @EnableBotCommands
 * @EnableAutoConfiguration
 * @ConfigurationPropertiesScan
 * class MyBotApplication
 * ```
 *
 * This is due to [this issue](https://github.com/spring-projects/spring-framework/issues/31957#issuecomment-1880081941):
 * ```
 * The recommendation is that you either declare one or more local `@ComponentScan` annotations OR one or more `@ComponentScan` meta-annotations.
 * If you combine local and meta-annotations for `@ComponentScan`, only the local `@ComponentScan` annotations will be honored.
 * ```
 *
 * If you have a better solution, please suggest it.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Import(BotCommandsInitializer::class)
@Retention(AnnotationRetention.RUNTIME)
@ComponentScan(
    basePackages = [
        "io.github.freya022.botcommands.api",
        "io.github.freya022.botcommands.internal",
    ]
)
annotation class EnableBotCommands
