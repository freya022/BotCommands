package com.freya02.botcommands.internal;

import com.freya02.botcommands.*;
import com.freya02.botcommands.application.ApplicationCommandInfo;
import com.freya02.botcommands.application.ApplicationCommandsBuilder;
import com.freya02.botcommands.application.ApplicationCommandsCache;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.MessageInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
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
	private final EnumMap<CommandType, Map<String, ? extends ApplicationCommandInfo>> applicationCommandMap = new EnumMap<>(CommandType.class);

	private final List<Predicate<MessageInfo>> filters = new ArrayList<>();

	private JDA jda;
	private Supplier<EmbedBuilder> defaultEmbedSupplier = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = () -> null;

	private boolean addSubcommandHelpByDefault, addExecutableHelpByDefault;
	private Consumer<BaseCommandEvent> helpConsumer;
	private ComponentManager componentManager;
	private SettingsProvider settingProvider;

	private final List<RegistrationListener> registrationListeners = new ArrayList<>();
	private Consumer<EmbedBuilder> helpBuilderConsumer;

	private ApplicationCommandsBuilder slashCommandsBuilder;
	private ApplicationCommandsCache applicationCommandsCache;

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
	public SlashCommandInfo findSlashCommand(@NotNull String path) {
		return getSlashCommandsMap().get(path);
	}
	
	public UserCommandInfo findUserCommand(String name) {
		return getUserCommandsMap().get(name);
	}

	public MessageCommandInfo findMessageCommand(String name) {
		return getMessageCommandsMap().get(name);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private Map<String, SlashCommandInfo> getSlashCommandsMap() {
		return (Map<String, SlashCommandInfo>) applicationCommandMap.computeIfAbsent(CommandType.SLASH, x -> Collections.synchronizedMap(new HashMap<String, SlashCommandInfo>()));
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	private Map<String, UserCommandInfo> getUserCommandsMap() {
		return (Map<String, UserCommandInfo>) applicationCommandMap.computeIfAbsent(CommandType.USER_CONTEXT, x -> Collections.synchronizedMap(new HashMap<String, UserCommandInfo>()));
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private Map<String, MessageCommandInfo> getMessageCommandsMap() {
		return (Map<String, MessageCommandInfo>) applicationCommandMap.computeIfAbsent(CommandType.MESSAGE_CONTEXT, x -> Collections.synchronizedMap(new HashMap<String, MessageCommandInfo>()));
	}

	@Override
	public List<String> getSlashCommandsPaths() {
		return getSlashCommandsMap().values()
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
		final Map<String, SlashCommandInfo> slashCommandMap = getSlashCommandsMap();

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
	
	public void addUserCommand(String name, UserCommandInfo commandInfo) {
		final Map<String, UserCommandInfo> userCommandMap = getUserCommandsMap();

		UserCommandInfo oldCmd = userCommandMap.put(name, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two user commands have the same names: '%s' from %s and %s",
					name,
					oldCmd.getCommandMethod(),
					commandInfo.getCommandMethod()));
		}
	}

	public void addMessageCommand(String name, MessageCommandInfo commandInfo) {
		final Map<String, MessageCommandInfo> messageCommandMap = getMessageCommandsMap();

		MessageCommandInfo oldCmd = messageCommandMap.put(name, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two message commands have the same names: '%s' from %s and %s",
					name,
					oldCmd.getCommandMethod(),
					commandInfo.getCommandMethod()));
		}
	}
	
	public void addSlashCommandAlternative(String path, SlashCommandInfo commandInfo) {
		//it's pretty much possible that the path already exist if two guilds use the same language for example
		//Still, check that the alternative path points to the same path if it exists
		final SlashCommandInfo oldVal = getSlashCommandsMap().put(path, commandInfo);
		if (oldVal != commandInfo && oldVal != null) {
			throw new IllegalStateException(String.format("Tried to add a localized slash command path but one already exists and isn't from the same command: %s and %s, path: %s",
					oldVal.getCommandMethod(),
					commandInfo.getCommandMethod(),
					path));
		}
	}

	public Collection<Command> getCommands() {
		return Collections.unmodifiableCollection(commandMap.values());
	}

	public Collection<ApplicationCommandInfo> getApplicationCommands() {
		return applicationCommandMap.values()
				.stream()
				.flatMap(commandMap -> commandMap.values().stream())
				.collect(Collectors.toUnmodifiableList());
	}
	
	public Collection<SlashCommandInfo> getSlashCommands() {
		return Collections.unmodifiableCollection(getSlashCommandsMap().values());
	}

	public void dispatchException(String message, Throwable e) {
		for (Long ownerId : getOwnerIds()) {
			getJDA().openPrivateChannelById(ownerId)
					.queue(channel -> channel.sendMessage(message + ", exception : \r\n" + Utils.getException(e))
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

	public void setSlashCommandsBuilder(ApplicationCommandsBuilder slashCommandsBuilder) {
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

	public ApplicationCommandsCache getApplicationCommandsCache() {
		return applicationCommandsCache;
	}

	public void setApplicationCommandsCache(ApplicationCommandsCache cachedSlashCommands) {
		this.applicationCommandsCache = cachedSlashCommands;
	}

	public boolean isHelpDisabled() {
		return helpConsumer != null;
	}
}
