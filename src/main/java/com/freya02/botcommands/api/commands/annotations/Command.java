package com.freya02.botcommands.api.commands.annotations

import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration;
import com.freya02.botcommands.api.core.service.annotations.BService;

/**
 * Enables this class to be scanned for one or more commands.<br>
 * This is a specialization of [BService] for commands.
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