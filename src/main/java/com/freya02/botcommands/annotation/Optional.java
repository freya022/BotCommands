package com.freya02.botcommands.annotation;

import com.freya02.botcommands.application.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.prefixed.annotation.Executable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes an optional parameter in an {@linkplain Executable} or a {@linkplain JdaSlashCommand} command
 *
 * <h2>For regex commands: Consider this annotation as experimental</h2>
 * <p>
 * <b>The behavior of {@linkplain Executable} commands is pretty unsafe if you combine strings and numbers in the same command</b>
 * </p>
 *
 * <h2>For application commands:</h2>
 * <p>
 * <b>This works perfectly as it's just a hint for discord</b>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Optional { }