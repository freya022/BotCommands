package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

public interface BContext {
	/**
	 * Returns the JDA instance associated with this context
	 *
	 * @return the JDA instance of this context
	 */
	@NotNull
	JDA getJDA();

	/**
	 * Returns the full list of prefixes used to trigger the bot
	 *
	 * @return Full list of prefixes
	 */
	@NotNull
	List<String> getPrefixes();

	/**
	 * Returns the preferred prefix for triggering this bot
	 *
	 * @return The preferred prefix
	 */
	@NotNull
	default String getPrefix() {
		return getPrefixes().get(0);
	}

	/**
	 * Returns a list of IDs of the bot owners
	 *
	 * @return a list of IDs of the bot owners
	 */
	@NotNull
	List<Long> getOwnerIds();

	/**
	 * Tells whether this user is an owner or not
	 *
	 * @param userId ID of the user
	 * @return <code>true</code> if the user is an owner
	 */
	default boolean isOwner(long userId) {
		return getOwnerIds().contains(userId);
	}

	@NotNull
	DefaultMessages getDefaultMessages();

	/**
	 * Returns the {@linkplain Command} object of the specified command name, the name can be an alias too
	 *
	 * @param name Name / alias of the command
	 * @return The {@linkplain Command} object of the command name
	 */
	@Nullable
	Command findCommand(@NotNull String name);

	/**
	 * Returns the default {@linkplain EmbedBuilder} supplier
	 *
	 * @return The default {@linkplain EmbedBuilder} supplier
	 * @see CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)
	 */
	@NotNull
	Supplier<EmbedBuilder> getDefaultEmbedSupplier();

	/**
	 * Returns the default icon {@linkplain InputStream} supplier
	 *
	 * @return The default icon {@linkplain InputStream} supplier
	 * @see CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)
	 */
	@NotNull
	Supplier<InputStream> getDefaultFooterIconSupplier();

	/**
	 * Adds an user to the blacklist, they won't be able to use commands
	 *
	 * @param userId ID of the user to blacklist
	 */
	void addToBlacklist(long userId);

	/**
	 * Adds an user to the blacklist, they won't be able to use commands
	 *
	 * @param user The user to blacklist
	 */
	default void addToBlacklist(User user) {
		addToBlacklist(user.getIdLong());
	}

	/**
	 * Removes an user from the blacklist, they won't be able to use commands
	 *
	 * @param userId ID of the user to remove from the blacklist
	 */
	void removeFromBlacklist(long userId);

	/**
	 * Removes an user from the blacklist, they won't be able to use commands
	 *
	 * @param user The user to remove from the blacklist
	 */
	default void removeFromBlacklist(User user) {
		removeFromBlacklist(user.getIdLong());
	}

	/**
	 * Tells if the user is blacklisted
	 *
	 * @param userId ID of the user to check on
	 * @return <code>true</code> if the user is blacklisted, false otherwise
	 */
	boolean isBlacklisted(long userId);

	/**
	 * Tells if the user is blacklisted
	 *
	 * @param user The user to check on
	 * @return <code>true</code> if the user is blacklisted, false otherwise
	 */
	default boolean isBlacklisted(User user) {
		return isBlacklisted(user.getIdLong());
	}

	/**
	 * Returns the blacklist, this blacklist could be backed by another list.
	 *
	 * @return The blacklist
	 */
	List<Long> getBlacklist();

	/**
	 * Sets the blacklist reference to use in the framework<br>
	 * <b>This means modifications to the blacklist from the framework or from your side will happen on both lists</b>
	 *
	 * @param blacklist A blacklist to use
	 */
	void setBlacklist(List<Long> blacklist);
}
