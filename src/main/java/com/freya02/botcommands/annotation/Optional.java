package com.freya02.botcommands.annotation;

import com.freya02.botcommands.prefixed.annotation.Executable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes an optional parameter in an {@linkplain Executable} command
 *
 * <br><b>The behavior of {@linkplain Executable} commands is pretty unsafe if you combine strings and numbers in the same command</b>
 *
 * <h2>Consider this annotation as experimental</h2>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Optional { }