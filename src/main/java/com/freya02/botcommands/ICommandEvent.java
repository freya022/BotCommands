package com.freya02.botcommands;

import com.freya02.botcommands.exceptions.BadIdException;
import com.freya02.botcommands.exceptions.NoIdException;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;

public interface ICommandEvent {
	/**
	 * Returns the <b>resolved</b> arguments of the command event, these can be a {@linkplain User}, {@linkplain Role}, {@linkplain TextChannel} or a {@linkplain String}
	 *
	 * @return List of arguments
	 */
	List<Object> getArguments();

	/**
	 * Checks if the next argument exists and is of type T, returns <code>true</code> if so
	 *
	 * @param clazz Class of the requested type
	 * @param <T>   Type of the requested argument
	 * @return <code>true</code> if the argument exists, <code>false</code> if not
	 */
	<T> boolean hasNext(Class<T> clazz);

	@SuppressWarnings("unchecked")
	<T> T peekArgument(Class<T> clazz);

	/**
	 * Returns the next argument if it is of type T
	 *
	 * @param clazz Class of the requested type
	 * @param <T>   Type of the requested argument
	 * @return The argument of type T if it exists
	 * @throws NoSuchElementException In case there is no more arguments to be read
	 */
	@Nonnull
	<T> T nextArgument(Class<T> clazz);

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
	<T extends IMentionable> T resolveNext(Class<?>... classes) throws NoIdException, BadIdException;
}
