package com.freya02.botcommands.api.annotations;

import com.freya02.botcommands.api.SettingsProvider;
import net.dv8tion.jda.api.entities.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated element as being for use in NSFW channels only
 * <br><b>Using it on a method will override the values inherited from the class</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NSFW {
	/**
	 * Specifies whether this NSFW command should work in guild channels
	 *
	 * @return <code>true</code> if the command should run on guild channel
	 */
	boolean guild() default true;

	/**
	 * Specifies whether this NSFW command should work in a users DMs
	 * <br><b>The user also needs to consent to NSFW DMs</b>
	 *
	 * @return <code>true</code> if the command should run in users DMs
	 * @see SettingsProvider#doesUserConsentNSFW(User)
	 */
	boolean dm() default false;
}