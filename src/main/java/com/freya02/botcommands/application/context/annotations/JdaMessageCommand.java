package com.freya02.botcommands.application.context.annotations;

import com.freya02.botcommands.CooldownScope;
import com.freya02.botcommands.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.application.context.message.GuildMessageEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for user commands
 *
 * <p>
 * <b>The targeted method must have a {@link GlobalMessageEvent} or a {@link GuildMessageEvent} and the only other argument possible is a {@link Message}, which will be the <i>targeted</i> message</b>
 *
 * @see GlobalMessageEvent#getTargetMessage()
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#user-commands">Discord docs</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JdaMessageCommand {
	/**
	 * Whether this application command should only work in a {@linkplain Guild}
	 *
	 * @return <code>true</code> if the application command only works in a {@linkplain Guild}
	 */
	boolean guildOnly() default true;

	/**
	 * Primary name of the command, <b>must not contain any spaces and no upper cases</b>
	 *
	 * @return Name of the command
	 */
	String name();

	/**
	 * Required {@linkplain Permission permissions} of the bot
	 *
	 * @return Required {@linkplain Permission permissions} of the bot
	 */
	Permission[] botPermissions() default {};

	/**
	 * Required {@linkplain Permission permissions} of the user
	 *
	 * @return Required {@linkplain Permission permissions} of the user
	 */
	Permission[] userPermissions() default {};

	/**
	 * Cooldown time <b>in milliseconds</b> before the command can be used again in the scope specified by {@linkplain #cooldownScope()}
	 *
	 * @return Cooldown time
	 */
	int cooldown() default 0;

	/**
	 * Scope of the cooldown, can be either {@linkplain CooldownScope#USER}, {@linkplain CooldownScope#CHANNEL} or {@linkplain CooldownScope#GUILD}
	 *
	 * @return Scope of the cooldown
	 */
	CooldownScope cooldownScope() default CooldownScope.USER;
}
