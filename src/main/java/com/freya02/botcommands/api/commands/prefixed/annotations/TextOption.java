package com.freya02.botcommands.api.commands.prefixed.annotations;

import com.freya02.botcommands.api.commands.annotations.Optional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify a text command parameter is supplied from a Discord message.
 * <br>This also can set name and example of {@linkplain JDATextCommand text commands} parameters
 * <p>
 * {@link #name()} is optional if the parameter name is available (add -parameters to your java compiler)
 *
 * @see Optional Optional (can also see @Nullable)
 * @see Nullable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface TextOption {
	String name() default "";

	String example() default "";
}
