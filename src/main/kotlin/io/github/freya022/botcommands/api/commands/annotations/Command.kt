package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Enables this class to be scanned for one or more commands.<br>
 * This is a specialization of [@BService][BService] for commands.
 *
 * A warning will be logged if this class does not have any commands,
 * i.e., methods that declare commands with annotations, or methods that declare using the DSL.
 *
 * @see BService @BService
 *
 * @see GlobalApplicationCommandProvider Declaring global application commands using the DSL
 * @see GuildApplicationCommandProvider Declaring guild application commands using the DSL
 * @see JDASlashCommand @JDASlashCommand
 * @see JDAMessageCommand @JDAMessageCommand
 * @see JDAUserCommand @JDAUserCommand
 *
 * @see TextCommandProvider Declaring text commands using the DSL
 * @see JDATextCommandVariation @JDATextCommandVariation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Bean
@Component
annotation class Command