package com.freya02.botcommands;

import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.MessageInfo;
import com.freya02.botcommands.slash.SlashCommandInfo;
import com.freya02.botcommands.slash.SlashCommandsBuilder;
import com.freya02.botcommands.slash.SlashCommandsCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BContextImpl implements BContext {
	private static final Logger LOGGER = Logging.getLogger();
	private final List<Long> ownerIds = new ArrayList<>();
	private final List<String> prefixes = new ArrayList<>();
	private final DefaultMessages defaultMessages = new DefaultMessages();

	private final Map<Class<?>, ConstructorParameterSupplier<?>> parameterSupplierMap = new HashMap<>();
	private final Map<Class<?>, InstanceSupplier<?>> instanceSupplierMap = new HashMap<>();
	private final Map<Class<?>, Supplier<?>> commandDependencyMap = new HashMap<>();

	private final Map<Class<?>, Object> classToObjMap = new HashMap<>();
	private final Map<String, Command> commandMap = new HashMap<>();
	private final Map<Long, Map<String, SlashCommandInfo>> guildSlashCommandMap = new HashMap<>();
	private final Map<String, SlashCommandInfo> globalSlashCommandMap = new HashMap<>();

	private final List<Predicate<MessageInfo>> filters = new ArrayList<>();

	private JDA jda;
	private Supplier<EmbedBuilder> defaultEmbedSupplier = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = () -> null;

	private boolean addSubcommandHelpByDefault, addExecutableHelpByDefault;
	private Consumer<BaseCommandEvent> helpConsumer;
	private ComponentManager componentManager;
	private PermissionProvider permissionProvider = new DefaultPermissionProvider();
	private SettingsProvider settingProvider;

	private final List<RegistrationListener> registrationListeners = new ArrayList<>();
	private Consumer<EmbedBuilder> helpBuilderConsumer;

	private SlashCommandsBuilder slashCommandsBuilder;
	private SlashCommandsCache slashCommandsCache;

	@Override
	@NotNull
	public JDA getJDA() {
		return jda;
	}

	public void setJDA(JDA jda) {
		this.jda = jda;
	}

	@Override
	@NotNull
	public List<String> getPrefixes() {
		return prefixes;
	}

	@Override
	public void addPrefix(String prefix) {
		prefixes.add(prefix);
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
	public SlashCommandInfo findSlashCommand(@Nullable Guild guild, @NotNull String name) {
		return getSlashCommandMap(guild).get(name);
	}

	@Override
	public List<String> getSlashCommandsPaths(Guild guild) {
		return getSlashCommandMap(guild).values()
				.stream()
				.map(SlashCommandInfo::getPath)
				.collect(Collectors.toList());
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

	public void addSlashCommand(@Nullable Guild guild, String path, SlashCommandInfo commandInfo) {
		final Map<String, SlashCommandInfo> slashCommandMap = getSlashCommandMap(guild);

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

	public Collection<SlashCommandInfo> getSlashCommands(@Nullable Guild guild) {
		return Collections.unmodifiableCollection(getSlashCommandMap(guild).values());
	}

	public void dispatchException(String message, Throwable e) {
		for (Long ownerId : getOwnerIds()) {
			getJDA().openPrivateChannelById(ownerId)
					.queue(channel -> channel.sendMessage(message + ", exception : \r\n" + Utils.getException(e).toString())
							.queue(null, new ErrorHandler()
									.handle(ErrorResponse.CANNOT_SEND_TO_USER,
											t -> LOGGER.warn("Could not send exception DM to owner of ID {}", ownerId))));
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
		Checks.notNull(helpConsumer, "Help replacement consumer");

		this.helpConsumer = helpConsumer;
	}

	@Override
	public Consumer<BaseCommandEvent> getHelpConsumer() {
		return helpConsumer;
	}

	@Override
	public List<RegistrationListener> getRegistrationListeners() {
		return Collections.unmodifiableList(registrationListeners);
	}

	@Override
	public void addRegistrationListeners(RegistrationListener... listeners) {
		Collections.addAll(registrationListeners, listeners);
	}

	@Override
	@Nullable
	public ComponentManager getComponentManager() {
		return componentManager;
	}

	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	public Object getClassInstance(Class<?> clazz) {
		return classToObjMap.get(clazz);
	}

	public void putClassInstance(Class<?> clazz, Object obj) {
		classToObjMap.put(clazz, obj);
	}

	public void addEventListeners(Object... listeners) {
		if (jda.getShardManager() != null) {
			jda.getShardManager().addEventListener(listeners);
		} else {
			jda.addEventListener(listeners);
		}
	}

	public void setPermissionProvider(PermissionProvider permissionProvider) {
		this.permissionProvider = permissionProvider;
	}

	@Override
	public PermissionProvider getPermissionProvider() {
		return permissionProvider;
	}

	public void setSettingsProvider(SettingsProvider settingsProvider) {
		this.settingProvider = settingsProvider;
	}

	@Override
	public @Nullable SettingsProvider getSettingsProvider() {
		return settingProvider;
	}

	public void setHelpBuilderConsumer(Consumer<EmbedBuilder> builderConsumer) {
		this.helpBuilderConsumer = builderConsumer;
	}

	public Consumer<EmbedBuilder> getHelpBuilderConsumer() {
		return helpBuilderConsumer;
	}

	public void setSlashCommandsBuilder(SlashCommandsBuilder slashCommandsBuilder) {
		this.slashCommandsBuilder = slashCommandsBuilder;
	}

	@Override
	public boolean tryUpdateGuildCommands(Iterable<Guild> guilds) throws IOException {
		return slashCommandsBuilder.tryUpdateGuildCommands(guilds);
	}

	public <T> void registerConstructorParameter(Class<T> parameterType, ConstructorParameterSupplier<T> parameterSupplier) {
		parameterSupplierMap.put(parameterType, parameterSupplier);
	}

	public <T> void registerInstanceSupplier(Class<T> classType, InstanceSupplier<T> instanceSupplier) {
		instanceSupplierMap.put(classType, instanceSupplier);
	}

	public ConstructorParameterSupplier<?> getParameterSupplier(Class<?> parameterType) {
		return parameterSupplierMap.get(parameterType);
	}

	public InstanceSupplier<?> getInstanceSupplier(Class<?> classType) {
		return instanceSupplierMap.get(classType);
	}

	public <T> void registerCommandDependency(Class<T> fieldType, Supplier<T> supplier) {
		commandDependencyMap.put(fieldType, supplier);
	}

	public Supplier<?> getCommandDependency(Class<?> fieldType) {
		return commandDependencyMap.get(fieldType);
	}

	public SlashCommandsCache getSlashCommandsCache() {
		return slashCommandsCache;
	}

	public void setSlashCommandsCache(SlashCommandsCache cachedSlashCommands) {
		this.slashCommandsCache = cachedSlashCommands;
	}
	
	Map<String, SlashCommandInfo> getSlashCommandMap(Guild guild) {
		return guild == null ? globalSlashCommandMap : guildSlashCommandMap.computeIfAbsent(guild.getIdLong(), x -> new HashMap<>());
	}
}
