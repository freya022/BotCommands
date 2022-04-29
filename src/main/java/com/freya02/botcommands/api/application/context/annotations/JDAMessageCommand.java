package com.freya02.botcommands.api.application.context.annotations;

import com.freya02.botcommands.api.annotations.BotPermissions;
import com.freya02.botcommands.api.annotations.Cooldown;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;
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
 * @see GlobalMessageEvent#getTarget()
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#user-commands">Discord docs</a>
 * @see UserPermissions @UserPermissions
 * @see BotPermissions @BotPermissions
 * @see Cooldown @Cooldown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JDAMessageCommand {
	/**
	 * Specified the application command scope for this command
	 *
	 * @return Scope of the command
	 *
	 * @see CommandScope
	 */
	CommandScope scope() default CommandScope.GUILD;

	/**
	 * Primary name of the command, <b>must not contain any spaces and no upper cases</b>
	 *
	 * @return Name of the command
	 */
	String name();
}
