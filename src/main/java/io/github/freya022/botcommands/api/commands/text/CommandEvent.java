package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit;
import io.github.freya022.botcommands.api.commands.text.exceptions.BadIdException;
import io.github.freya022.botcommands.api.commands.text.exceptions.NoIdException;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.internal.commands.text.BaseCommandEventImpl;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Text command event containing all the information about the triggered command.
 *
 * <p>Arguments are tokenized and resolved into entities if possible
 *
 * <p><b>This is only used as a command fallback</b>
 *
 * @see BaseCommandEvent
 */
public abstract class CommandEvent extends BaseCommandEventImpl {
	public CommandEvent(BContext context, MessageReceivedEvent event, String args, CancellableRateLimit cancellableRateLimit) {
		super(context, event, args, cancellableRateLimit);
	}

	/**
	 * Returns the <b>resolved</b> arguments of the command event, these can be a {@link User}, {@link Role}, {@link TextChannel} or a {@link String}
	 *
	 * @return List of arguments
	 */
	public abstract List<Object> getArguments();

	/**
	 * Checks if the next argument exists and is of type T, returns {@code true} if so
	 *
	 * @param clazz Class of the requested type
	 * @param <T>   Type of the requested argument
	 * @return {@code true} if the argument exists, {@code false} if not
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
	@NotNull
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
	@NotNull
	public abstract <T extends IMentionable> T resolveNext(Class<?>... classes) throws NoIdException, BadIdException;
}
