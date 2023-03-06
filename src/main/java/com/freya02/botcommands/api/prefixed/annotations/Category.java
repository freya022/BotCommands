package com.freya02.botcommands.api.prefixed.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify the global category of the current command class.
 * <br>This is only used for the help command and only usable on top level commands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Category {
	String value();
}
