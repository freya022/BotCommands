package com.freya02.botcommands.api.commands.prefixed.annotations;

import com.freya02.botcommands.api.commands.annotations.Command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a function as one which declares text commands
 * <p>
 * <b>Note:</b> The function may be called more than once.
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Command}.
 *
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TextDeclaration {}