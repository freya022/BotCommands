package com.freya02.botcommands.api.commands.application.annotations;

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder;
import com.freya02.botcommands.api.core.SettingsProvider;
import kotlin.jvm.functions.Function1;
import net.dv8tion.jda.api.entities.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a text command as being usable in NSFW channels only.
 * <br>NSFW commands will be shown in help content only if called in an NSFW channel,
 * DM consent is <b>not</b> checked as text commands are guild-only.
 * <br>Overrides the NSFW status of the class, if applied on a method.
 *
 * <p><b>Note:</b> For application commands, see the {@code #nsfw} parameter of your annotation
 *
 * @see TextCommandBuilder#nsfw(Function1) DSL equivalent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NSFW {
	/**
	 * Specifies whether this NSFW command should work in guild channels
	 *
	 * @return <code>true</code> if the command should run on guild channels
	 */
	boolean guild() default true;

	/**
	 * Specifies whether this NSFW command should work in a users DMs
	 * <br><b>The user also needs to consent to NSFW DMs</b>
	 *
	 * @return <code>true</code> if the command should run in user DMs
	 * @see SettingsProvider#doesUserConsentNSFW(User)
	 */
	boolean dm() default false;
}