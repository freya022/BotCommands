package com.freya02.botcommands.api.application.context.annotations;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Cooldown;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for user commands
 * 
 * <p>
 * <b>The targeted method must have a {@link GlobalUserEvent} or a {@link GuildUserEvent} and the only other arguments possible are a {@link Member} or a {@link User}, which will be the <i>targeted</i> entity</b>
 *
 * @see GlobalUserEvent#getTarget()
 * @see GlobalUserEvent#getTargetMember()
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#user-commands">Discord docs</a>
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JDAUserCommand {
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
}
