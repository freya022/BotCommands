package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.TextDeclaration
import io.github.freya022.botcommands.api.core.service.annotations.BService

/**
 * Enables this class to be scanned for one or more commands.<br>
 * This is a specialization of [@BService][BService] for commands.
 *
 * A warning will be logged if this class does not have any commands,
 * i.e., methods that declare commands with annotations, or methods that declare using the DSL.
 *
 * @see BService @BService
 *
 * @see AppDeclaration @AppDeclaration
 * @see JDASlashCommand @JDASlashCommand
 * @see JDAMessageCommand @JDAMessageCommand
 * @see JDAUserCommand @JDAUserCommand
 *
 * @see TextDeclaration @TextDeclaration
 * @see JDATextCommand @JDATextCommand
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY) //Read by ClassGraph
annotation class Command  