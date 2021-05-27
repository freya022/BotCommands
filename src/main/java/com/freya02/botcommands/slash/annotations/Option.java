package com.freya02.botcommands.slash.annotations;

import com.freya02.botcommands.annotation.Optional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to set name and description of {@linkplain JdaSlashCommand slash commands}
 * <p>
 * {@linkplain #name()} is optional if parameter name is available (add -parameters to your java compiler)
 *
 * @see Optional
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Option {
	String name() default "";

	String description() default "";
}
