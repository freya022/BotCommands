package com.freya02.botcommands.api.commands.annotations;

import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration;
import com.freya02.botcommands.api.core.service.annotations.BService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables this class to be scanned for one or more commands.
 * <br>This is a specialization of {@link BService} for commands.
 *
 * <p>A warning will be logged if this class does not have any commands,
 * i.e. methods that declare commands with annotations, or methods that declare using the DSL.
 *
 * @see BService
 *
 * @see AppDeclaration
 * @see JDASlashCommand
 * @see JDAMessageCommand
 * @see JDAUserCommand
 * @see TextDeclaration
 * @see JDATextCommand
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) //Read by ClassGraph
public @interface Command { }