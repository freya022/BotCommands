package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.internal.core.config.BotCommandsCoreConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

/**
 * Entry point annotation for Spring projects.
 *
 * This annotation can only be used once in your main class.
 *
 * The only requirement for a basic bot is a service extending [JDAService],
 * learn more on [the wiki](https://freya022.github.io/BotCommands/3.X/setup/getting-started/#creating-a-jdaservice).
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
 *
 * ### Configuration
 * Most of it can be done using Spring properties, you can see the property names on each configuration.
 *
 * If you want to do it using code, or need to configure something not available using properties,
 * you can use the configurer interfaces:
 * - [BConfigConfigurer] ([BConfig])
 * - [BDebugConfigConfigurer] ([BDebugConfig])
 * - [BServiceConfigConfigurer] ([BServiceConfig])
 * - [BDatabaseConfigConfigurer] ([BDatabaseConfig])
 * - [BLocalizationConfigConfigurer] ([BLocalizationConfig])
 * - [BTextConfigConfigurer] ([BTextConfig])
 * - [BApplicationConfigConfigurer] ([BApplicationConfig])
 * - [BComponentsConfigConfigurer] ([BComponentsConfig])
 * - [BCoroutineScopesConfigConfigurer] ([BCoroutineScopesConfigConfigurer])
 */
@Deprecated("Replaced with autoconfiguration", level = DeprecationLevel.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ComponentScan(
    basePackages = [
        "io.github.freya022.botcommands.api",
        "io.github.freya022.botcommands.internal",
    ]
)
@ConfigurationPropertiesScan(basePackageClasses = [JDAConfiguration::class, BotCommandsCoreConfiguration::class])
@EnableAutoConfiguration
annotation class EnableBotCommands
