package com.freya02.botcommands.api.commands.annotations;

import com.freya02.botcommands.api.annotations.AppendMode;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the needed permissions of the user to use this text / application commands
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UserPermissions {
	/**
	 * Sets the append mode for this attribute
	 *
	 * @return The append mode for this attribute
	 * @see AppendMode
	 */
	AppendMode mode() default AppendMode.SET;

	/**
	 * Required {@link Permission permissions} of the user
	 *
	 * @return Required {@link Permission permissions} of the user
	 */
	Permission[] value() default {};
}
