package com.freya02.botcommands;

import com.freya02.botcommands.buttons.KeyProvider;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.MessageInfo;
import com.freya02.botcommands.slash.SlashCommandInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BContextImpl implements BContext {
	private static final Logger LOGGER = Logging.getLogger();
	private final List<Long> ownerIds = new ArrayList<>();
	private final List<String> prefixes = new ArrayList<>();
	private final DefaultMessages defaultMessages = new DefaultMessages();

	private final Map<Class<?>, Object> classToObjMap = new HashMap<>();
	private final Map<String, Command> commandMap = new HashMap<>();
	private final Map<String, SlashCommandInfo> slashCommandMap = new HashMap<>();

	private final List<Predicate<MessageInfo>> filters = new ArrayList<>();

	private JDA jda;
	private Supplier<EmbedBuilder> defaultEmbedSupplier = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = () -> null;

	private boolean addSubcommandHelpByDefault, addExecutableHelpByDefault;
	private Consumer<BaseCommandEvent> helpConsumer;
	private KeyProvider keyProvider;

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
	public SlashCommandInfo findSlashCommand(@NotNull String name) {
		return slashCommandMap.get(name);
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

	public void addSlashCommand(String path, SlashCommandInfo commandInfo) {
		String path2 = path;
		int index;
		while ((index = path2.lastIndexOf('/')) != -1) {
			for (String p : slashCommandMap.keySet()) {
				if (p.startsWith(path2)) {
					throw new IllegalStateException(String.format("Tried to add a command with path %s but a equal/shorter path already exists: %s", path, path2));
				}
			}

			path2 = path2.substring(0, index);
		}

		SlashCommandInfo oldCmd = slashCommandMap.put(path, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two slash commands have the same paths: '%s' from %s and %s",
					path,
					oldCmd.getCommandMethod(),
					commandInfo.getCommandMethod()));
		}
	}

	public Collection<Command> getCommands() {
		return Collections.unmodifiableCollection(commandMap.values());
	}

	public Collection<SlashCommandInfo> getSlashCommands() {
		return Collections.unmodifiableCollection(slashCommandMap.values());
	}

	public void dispatchException(String message, Throwable e) {
		for (Long ownerId : getOwnerIds()) {
			final User owner = getJDA().retrieveUserById(ownerId, false).complete();

			if (owner == null) {
				LOGGER.error("Top owner ID {} does not exist !", ownerId);
				return;
			}

			owner.openPrivateChannel().queue(
					channel -> channel.sendMessage(message + ", exception : \r\n" + e.toString()).queue(null, t -> LOGGER.error("Could not send message to owner : {}", message)),
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

	public boolean shouldAddSubcommandHelpByDefault() {
		return addSubcommandHelpByDefault;
	}

	public void setAddSubcommandHelpByDefault(boolean addSubcommandHelpByDefault) {
		this.addSubcommandHelpByDefault = addSubcommandHelpByDefault;
	}

	public boolean shouldAddExecutableHelpByDefault() {
		return addExecutableHelpByDefault;
	}

	public void setAddExecutableHelpByDefault(boolean addExecutableHelpByDefault) {
		this.addExecutableHelpByDefault = addExecutableHelpByDefault;
	}

	@Override
	public void overrideHelp(Consumer<BaseCommandEvent> helpConsumer) {
		this.helpConsumer = helpConsumer;
	}

	@Override
	public Consumer<BaseCommandEvent> getHelpConsumer() {
		return helpConsumer;
	}

	public KeyProvider getKeyProvider() {
		return keyProvider;
	}

	public void setKeyProvider(KeyProvider keyProvider) {
		this.keyProvider = keyProvider;
	}

	public Map<Class<?>, Object> getClassToObjMap() {
		return classToObjMap;
	}
}
