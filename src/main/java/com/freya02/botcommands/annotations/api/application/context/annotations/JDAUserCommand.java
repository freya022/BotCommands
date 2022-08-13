package com.freya02.botcommands.annotations.api.application.context.annotations;

import com.freya02.botcommands.annotations.api.annotations.BotPermissions;
import com.freya02.botcommands.annotations.api.annotations.Cooldown;
import com.freya02.botcommands.annotations.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
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
	 * Specified the application command scope for this command
	 *
	 * @return Scope of the command
	 *
	 * @see CommandScope
	 */
	CommandScope scope() default CommandScope.GLOBAL_NO_DM;

	/**
	 * Specifies whether the application command is disabled by default, so that administrators can further configure the command
	 * <br><b>If this is used in coordination with {@link UserPermissions} then they will be cleared if this is default locked</b>,
	 * as to allow discord to lock the command for everyone, until an admin configures it.
	 * <br>This does NOT affect administrators.
	 *
	 * @return <code>true</code> if the command should be disabled by default
	 */
	boolean defaultLocked() default false;

	/**
	 * Primary name of the command, <b>must not contain any spaces and no upper cases</b>
	 *
	 * @return Name of the command
	 */
	String name();
}
