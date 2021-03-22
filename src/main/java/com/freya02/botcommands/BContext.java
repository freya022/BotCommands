package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

public interface BContext {
	@NotNull
	JDA getJda();

	@NotNull
	List<String> getPrefixes();

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

	@NotNull
	Supplier<EmbedBuilder> getDefaultEmbedSupplier();

	@NotNull
	Supplier<InputStream> getDefaultFooterIconSupplier();

	void setPrefixes(List<String> prefix);

	void addOwner(long ownerId);
}
