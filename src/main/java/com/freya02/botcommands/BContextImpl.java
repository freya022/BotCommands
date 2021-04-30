package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BContextImpl implements BContext {
	private final List<Long> ownerIds = new ArrayList<>();
	private final List<String> prefixes = new ArrayList<>();
	private final DefaultMessages defaultMessages = new DefaultMessages();

	private final Map<String, Command> commandMap = new HashMap<>();

	private final List<Predicate<MessageInfo>> filters = new ArrayList<>();

	private JDA jda;
	private Supplier<EmbedBuilder> defaultEmbedSupplier = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = () -> null;

	@Override
	@NotNull
	public JDA getJDA() {
		return jda;
	}

	public void setJda(JDA jda) {
		this.jda = jda;
	}

	@Override
	@NotNull
	public List<String> getPrefixes() {
		return prefixes;
	}

	@Override
	@NotNull
	public List<Long> getOwnerIds() {
		return ownerIds;
	}

	@Override
	@NotNull
	public DefaultMessages getDefaultMessages() {
		return defaultMessages;
	}

	@Override
	@Nullable
	public Command findCommand(@NotNull String name) {
		return commandMap.get(name);
	}

	@Override
	public @NotNull Supplier<EmbedBuilder> getDefaultEmbedSupplier() {
		return defaultEmbedSupplier;
	}

	public void setDefaultEmbedSupplier(Supplier<EmbedBuilder> defaultEmbedSupplier) {
		this.defaultEmbedSupplier = Objects.requireNonNull(defaultEmbedSupplier, "Default embed supplier cannot be null");
	}

	@Override
	public @NotNull Supplier<InputStream> getDefaultFooterIconSupplier() {
		return defaultFooterIconSupplier;
	}

	public void setPrefixes(List<String> prefix) {
		prefixes.addAll(prefix);
	}

	public void addOwner(long ownerId) {
		ownerIds.add(ownerId);
	}

	public void setDefaultFooterIconSupplier(Supplier<InputStream> defaultFooterIconSupplier) {
		this.defaultFooterIconSupplier = Objects.requireNonNull(defaultFooterIconSupplier, "Default footer icon supplier cannot be null");
	}

	public void addCommand(String name, String[] aliases, Command command) {
		Command oldCmd = commandMap.put(name, command);
		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two commands have the same name: '%s' from %s and %s",
					name,
					command.getClass().getName(),
					oldCmd.getClass().getName()));
		}

		for (String alias : aliases) {
			oldCmd = commandMap.put(alias, command);

			if (oldCmd != null) {
				throw new IllegalStateException(String.format("Two commands have the same name: '%s' from %s and %s",
						alias,
						command.getClass().getName(),
						oldCmd.getClass().getName()));
			}
		}
	}

	public Collection<Command> getCommands() {
		return Collections.unmodifiableCollection(commandMap.values());
	}

	public void dispatchException(String message, Throwable e) {
		for (Long ownerId : getOwnerIds()) {
			final User owner = getJDA().retrieveUserById(ownerId, false).complete();

			if (owner == null) {
				System.err.println("Top owner ID is wrong !");
				return;
			}

			owner.openPrivateChannel().queue(
					channel -> channel.sendMessage(message + ", exception : \r\n" + e.toString()).queue(null, t -> System.err.println("Could not send message to owner : " + message)),
					ignored -> {});
		}
	}

	@Override
	public void addFilter(Predicate<MessageInfo> filter) {
		filters.add(filter);
	}

	@Override
	public void removeFilter(Predicate<MessageInfo> filter) {
		filters.remove(filter);
	}

	public List<Predicate<MessageInfo>> getFilters() {
		return filters;
	}
}
