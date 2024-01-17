package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand

/**
 * Base class for **annotated** application commands such as slash / context commands.
 *
 * You are not required to use this if you use the [DSL declaration mode][AppDeclaration].
 *
 * @see JDASlashCommand @JDASlashCommand
 * @see JDAMessageCommand @JDAMessageCommand
 * @see JDAUserCommand @JDAUserCommand
 */
abstract class ApplicationCommand : GuildApplicationSettings