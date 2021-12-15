package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.*;
import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandUpdateResult;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.parameters.CustomResolver;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.MessageInfo;
import com.freya02.botcommands.internal.application.*;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextCommandCandidates;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextSubcommandCandidates;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BContextImpl implements BContext {
	private static final Logger LOGGER = Logging.getLogger();
	private final List<Long> ownerIds = new ArrayList<>();
	private final List<String> prefixes = new ArrayList<>();

	private final Map<Class<?>, ConstructorParameterSupplier<?>> parameterSupplierMap = new HashMap<>();
	private final Map<Class<?>, InstanceSupplier<?>> instanceSupplierMap = new HashMap<>();
	private final Map<Class<?>, Supplier<?>> commandDependencyMap = new HashMap<>();

	private final Map<Class<?>, Object> classToObjMap = new HashMap<>();
	private final Map<CommandPath, TextCommandCandidates> textCommandMap = new HashMap<>();
	private final Map<CommandPath, TextSubcommandCandidates> textSubcommandsMap = new HashMap<>();
	private final ApplicationCommandInfoMap applicationCommandInfoMap = new ApplicationCommandInfoMap();

	private final List<Predicate<MessageInfo>> filters = new ArrayList<>();

	private JDA jda;
	private Supplier<EmbedBuilder> defaultEmbedSupplier = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = () -> null;

	private Consumer<BaseCommandEvent> helpConsumer;
	private ComponentManager componentManager;
	private SettingsProvider settingProvider;

	private final List<RegistrationListener> registrationListeners = new ArrayList<>();
	private Consumer<EmbedBuilder> helpBuilderConsumer;

	private ApplicationCommandsBuilder slashCommandsBuilder;
	private ApplicationCommandsCache applicationCommandsCache;
	private Function<Guild, DefaultMessages> defaultMessageProvider;
	private ExceptionHandler uncaughtExceptionHandler;

	private final Map<Class<?>, AutocompletionTransformer<?>> autocompletionTransformers = new HashMap<>();

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
	public DefaultMessages getDefaultMessages(@Nullable Guild guild) {
		return defaultMessageProvider.apply(guild);
	}

	public void setDefaultMessageProvider(@NotNull Function<Guild, DefaultMessages> defaultMessageProvider) {
		this.defaultMessageProvider = defaultMessageProvider;
	}

	@Override
	@Nullable
	public TextCommandInfo findFirstCommand(@NotNull CommandPath path) {
		final TextCommandCandidates candidates = textCommandMap.get(path);
		if (candidates == null) return null;

		return candidates.findFirst();
	}

	@Nullable
	@Override
	public TextCommandCandidates findCommands(@NotNull CommandPath path) {
		return textCommandMap.get(path);
	}

	@Override
	@Nullable
	public TextCommandCandidates findFirstTextSubcommands(CommandPath path) {
		final List<TextCommandCandidates> list = Objects.requireNonNullElseGet(textSubcommandsMap.get(path), Collections::emptyList);

		if (list.isEmpty()) return null;
		else return list.get(0);
	}

	@Override
	@Nullable
	public List<TextCommandCandidates> findTextSubcommands(CommandPath path) {
		return textSubcommandsMap.get(path);
	}

	@Nullable
	@Override
	public SlashCommandInfo findSlashCommand(@NotNull CommandPath path) {
		return getSlashCommandsMap().get(path);
	}

	@Nullable
	@Override
	public UserCommandInfo findUserCommand(@NotNull String name) {
		return getUserCommandsMap().get(CommandPath.ofName(name));
	}

	@Nullable
	@Override
	public MessageCommandInfo findMessageCommand(@NotNull String name) {
		return getMessageCommandsMap().get(CommandPath.ofName(name));
	}

	@NotNull
	public ApplicationCommandInfoMap getApplicationCommandInfoMap() {
		return applicationCommandInfoMap;
	}

	@Override
	@NotNull
	@UnmodifiableView
	public ApplicationCommandInfoMapView getApplicationCommandInfoMapView() {
		return applicationCommandInfoMap;
	}

	@NotNull
	private CommandInfoMap<SlashCommandInfo> getSlashCommandsMap() {
		return getApplicationCommandInfoMap().getSlashCommands();
	}

	@Override
	@NotNull
	@UnmodifiableView
	public CommandInfoMap<SlashCommandInfo> getSlashCommandsMapView() {
		return getApplicationCommandInfoMapView().getSlashCommandsView();
	}

	@NotNull
	private CommandInfoMap<UserCommandInfo> getUserCommandsMap() {
		return getApplicationCommandInfoMap().getUserCommands();
	}

	@Override
	@NotNull
	@UnmodifiableView
	public CommandInfoMap<UserCommandInfo> getUserCommandsMapView() {
		return getApplicationCommandInfoMapView().getUserCommandsView();
	}

	@NotNull
	private CommandInfoMap<MessageCommandInfo> getMessageCommandsMap() {
		return getApplicationCommandInfoMap().getMessageCommands();
	}

	@Override
	@NotNull
	@UnmodifiableView
	public CommandInfoMap<MessageCommandInfo> getMessageCommandsMapView() {
		return getApplicationCommandInfoMapView().getMessageCommandsView();
	}

	@Override
	public List<CommandPath> getSlashCommandsPaths() {
		return getSlashCommandsMap().values()
				.stream()
				.map(SlashCommandInfo::getPath)
				.collect(Collectors.toList());
	}

	@Override
	@NotNull
	public  Supplier<EmbedBuilder> getDefaultEmbedSupplier() {
		return defaultEmbedSupplier;
	}

	public void setDefaultEmbedSupplier(Supplier<EmbedBuilder> defaultEmbedSupplier) {
		this.defaultEmbedSupplier = Objects.requireNonNull(defaultEmbedSupplier, "Default embed supplier cannot be null");
	}

	@Override
	@NotNull
	public  Supplier<InputStream> getDefaultFooterIconSupplier() {
		return defaultFooterIconSupplier;
	}

	public void addOwner(long ownerId) {
		ownerIds.add(ownerId);
	}

	public void setDefaultFooterIconSupplier(Supplier<InputStream> defaultFooterIconSupplier) {
		this.defaultFooterIconSupplier = Objects.requireNonNull(defaultFooterIconSupplier, "Default footer icon supplier cannot be null");
	}

	public void addTextCommand(TextCommandInfo commandInfo) {
		final CommandPath path = commandInfo.getPath();
		final List<CommandPath> aliases = commandInfo.getAliases();

		textCommandMap.compute(path, (k, v) -> {
			if (v == null) return new TextCommandCandidates(commandInfo);
			else v.add(commandInfo);

			return v;
		});

		final CommandPath parentPath = path.getParent();
		if (parentPath != null) { //Add subcommands to cache
			// If subcommands candidates exist, append, if not then create
			textSubcommandsMap.compute(parentPath, (x, candidates) -> (candidates == null) ? new TextSubcommandCandidates(commandInfo) : candidates.addSubcommand(commandInfo));
		}

		for (CommandPath alias : aliases) {
			textCommandMap.compute(alias, (k, v) -> {
				if (v == null) return new TextCommandCandidates(commandInfo);
				else v.add(commandInfo);

				return v;
			});
		}
	}

	public void addSlashCommand(SlashCommandInfo commandInfo) {
		final CommandPath path = commandInfo.getPath();

		final Map<CommandPath, SlashCommandInfo> slashCommandMap = getSlashCommandsMap();

		//Checks below this block only check if shorter or equal commands exists
		// We need to check if longer commands exists
		//Would be more performant if we used a Trie
		for (Map.Entry<CommandPath, SlashCommandInfo> entry : slashCommandMap.entrySet()) {
			final CommandPath commandPath = entry.getKey();
			final SlashCommandInfo mapInfo = entry.getValue();

			if (commandPath.getNameCount() > path.getNameCount() && commandPath.startsWith(path)) {
				throw new IllegalStateException(String.format("Tried to add a command with path '%s' (at %s) but a equal/longer path already exists: '%s' (at %s)",
						path,
						Utils.formatMethodShort(commandInfo.getCommandMethod()),
						commandPath,
						Utils.formatMethodShort(mapInfo.getCommandMethod())));
			}
		}

		CommandPath p = path;
		do {
			final SlashCommandInfo mapInfo = slashCommandMap.get(p);
			
			if (mapInfo != null) {
				throw new IllegalStateException(String.format("Tried to add a command with path '%s' (at %s) but a equal/shorter path already exists: '%s' (at %s)",
						path,
						Utils.formatMethodShort(commandInfo.getCommandMethod()),
						p,
						Utils.formatMethodShort(mapInfo.getCommandMethod())));
			}
		} while ((p = p.getParent()) != null);

		slashCommandMap.put(path, commandInfo);
	}
	
	public void addUserCommand(UserCommandInfo commandInfo) {
		final CommandPath path = commandInfo.getPath();

		UserCommandInfo oldCmd = getUserCommandsMap().put(path, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two user commands have the same names: '%s' from %s and %s",
					path,
					Utils.formatMethodShort(oldCmd.getCommandMethod()),
					Utils.formatMethodShort(commandInfo.getCommandMethod())));
		}
	}

	public void addMessageCommand(MessageCommandInfo commandInfo) {
		final CommandPath path = commandInfo.getPath();

		MessageCommandInfo oldCmd = getMessageCommandsMap().put(path, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two message commands have the same names: '%s' from %s and %s",
					path,
					Utils.formatMethodShort(oldCmd.getCommandMethod()),
					Utils.formatMethodShort(commandInfo.getCommandMethod())));
		}
	}

	public <T extends ApplicationCommandInfo> void addApplicationCommandAlternative(CommandPath path, CommandType type, T commandInfo) {
		//it's pretty much possible that the path already exist if two guilds use the same language for example
		//Still, check that the alternative path points to the same path if it exists

		final ApplicationCommandInfo oldVal = getApplicationCommandInfoMap().put(type, path, commandInfo);
		if (oldVal != commandInfo && oldVal != null) {
			throw new IllegalStateException(String.format("Tried to add a localized application command path but one already exists and isn't from the same command: %s and %s, path: %s",
					Utils.formatMethodShort(oldVal.getCommandMethod()),
					Utils.formatMethodShort(commandInfo.getCommandMethod()),
					path));
		}
	}

	public Collection<TextCommandCandidates> getCommands() {
		return Collections.unmodifiableCollection(textCommandMap.values());
	}

	@UnmodifiableView
	public Collection<? extends ApplicationCommandInfo> getApplicationCommandsView() {
		return applicationCommandInfoMap.getAllApplicationCommandsView();
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
	@Nullable
	public SettingsProvider getSettingsProvider() {
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
	@NotNull
	public Map<Guild, CompletableFuture<CommandUpdateResult>> scheduleApplicationCommandsUpdate(Iterable<Guild> guilds) throws IOException {
		return slashCommandsBuilder.scheduleApplicationCommandsUpdate(guilds);
	}

	@Override
	public <T> void registerCustomResolver(Class<T> parameterType, Function<Event, T> function) {
		ParameterResolvers.register(new CustomResolver(parameterType, function));
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

	public void setUncaughtExceptionHandler(@Nullable ExceptionHandler exceptionHandler) {
		this.uncaughtExceptionHandler = exceptionHandler;
	}

	@Nullable
	@Override
	public ExceptionHandler getUncaughtExceptionHandler() {
		return uncaughtExceptionHandler;
	}

	public AutocompletionTransformer<?> getAutocompletionTransformer(Class<?> type) {
		return autocompletionTransformers.get(type);
	}

	public <T> void registerAutocompletionTransformer(Class<T> type, AutocompletionTransformer<T> autocompletionTransformer) {
		autocompletionTransformers.put(type, autocompletionTransformer);
	}
}
