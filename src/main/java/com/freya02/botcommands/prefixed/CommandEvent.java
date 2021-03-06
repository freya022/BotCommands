package com.freya02.botcommands.prefixed;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.prefixed.exceptions.BadIdException;
import com.freya02.botcommands.prefixed.exceptions.NoIdException;
import com.freya02.botcommands.prefixed.impl.BaseCommandEventImpl;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>Contains all the information about the triggered command</p>
 * <p>Arguments are tokenized and resolved into entities if possible</p>
 *
 * @see BaseCommandEvent
 */
public abstract class CommandEvent extends BaseCommandEventImpl {
	public CommandEvent(BContext context, GuildMessageReceivedEvent event, String args) {
		super(context, event, args);
	}

	/**
	 * Returns the <b>resolved</b> arguments of the command event, these can be a {@linkplain User}, {@linkplain Role}, {@linkplain TextChannel} or a {@linkplain String}
	 *
	 * @return List of arguments
	 */
	public abstract List<Object> getArguments();

	/**
	 * Checks if the next argument exists and is of type T, returns <code>true</code> if so
	 *
	 * @param clazz Class of the requested type
	 * @param <T>   Type of the requested argument
	 * @return <code>true</code> if the argument exists, <code>false</code> if not
	 */
	public abstract <T> boolean hasNext(Class<T> clazz);

	public abstract <T> T peekArgument(Class<T> clazz);

	/**
	 * Returns the next argument if it is of type T
	 *
	 * @param clazz Class of the requested type
	 * @param <T>   Type of the requested argument
	 * @return The argument of type T if it exists
	 * @throws NoSuchElementException In case there is no more arguments to be read
	 */
	@Nonnull
	public abstract <T> T nextArgument(Class<T> clazz);

	/**
	 * Returns the next IMentionable
	 *
	 * @param classes Class(es) of the requested type
	 * @param <T>     Type of the requested argument
	 * @return The argument of type T, extending IMentionable, if it exists
	 * @throws BadIdException         In case the ID is not a valid snowflake, or does not refer to a known IMentionable
	 * @throws NoIdException          In case there is no ID / IMentionable in the message
	 * @throws NoSuchElementException In case there is no more arguments to be read, or the type isn't the same
	 */
	@Nonnull
	public abstract <T extends IMentionable> T resolveNext(Class<?>... classes) throws NoIdException, BadIdException;
}
