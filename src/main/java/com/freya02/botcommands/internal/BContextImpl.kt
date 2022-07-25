package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.*;
import com.freya02.botcommands.api.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.application.ApplicationCommandManager;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandUpdateResult;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.api.components.ComponentInteractionFilter;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.parameters.CustomResolver;
import com.freya02.botcommands.api.parameters.CustomResolverFunction;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.api.prefixed.HelpConsumer;
import com.freya02.botcommands.api.prefixed.TextCommandFilter;
import com.freya02.botcommands.core.api.config.BConfig;
import com.freya02.botcommands.core.internal.ClassPathContainer;
import com.freya02.botcommands.core.internal.EventDispatcher;
import com.freya02.botcommands.core.internal.ServiceContainer;
import com.freya02.botcommands.internal.application.*;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.modals.ModalMaps;
import com.freya02.botcommands.internal.prefixed.TextCommandCandidates;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextSubcommandCandidates;
import com.freya02.botcommands.internal.utils.Utils;
import dev.minn.jda.ktx.events.CoroutineEventManager;
import gnu.trove.TCollections;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import kotlin.reflect.KClass;
import kotlin.reflect.KType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BContextImpl implements BContext {
	private static final Logger LOGGER = Logging.getLogger();

	private final BConfig config;
	private final CoroutineEventManager eventManager;
	private final ClassPathContainer classPathContainer;
	private final ServiceContainer serviceContainer;
	private final EventDispatcher eventDispatcher;

	private final ApplicationCommandsContextImpl applicationCommandsContext = new ApplicationCommandsContextImpl();
	private final ApplicationCommandManager applicationCommandManager = new ApplicationCommandManager(this); //TODO merge

	private final List<Long> ownerIds = new ArrayList<>();
	private final List<String> prefixes = new ArrayList<>();

	private final Map<Class<?>, ConstructorParameterSupplier<?>> parameterSupplierMap = new HashMap<>();
	private final Map<Class<?>, InstanceSupplier<?>> instanceSupplierMap = new HashMap<>();
	private final List<DynamicInstanceSupplier> dynamicInstanceSuppliers = new ArrayList<>();
	private final Map<Class<?>, Supplier<?>> commandDependencyMap = new HashMap<>();
	private final Map<Class<?>, MethodParameterSupplier<?>> methodParameterSupplierMap = new HashMap<>();

	private final Map<KClass<?>, Object> classToObjMap = new HashMap<>();
	private final Map<CommandPath, TextCommandCandidates> textCommandMap = new HashMap<>();
	private final Map<CommandPath, TextSubcommandCandidates> textSubcommandsMap = new HashMap<>();
	private final ModalMaps modalMaps = new ModalMaps();

	private boolean onlineAppCommandCheckEnabled;

	private final Map<String, AutocompletionHandlerInfo> autocompleteHandlersMap = new HashMap<>();

	private final TLongSet testGuildIds = TCollections.synchronizedSet(new TLongHashSet());

	private final List<TextCommandFilter> textFilters = new ArrayList<>();
	private final List<ApplicationCommandFilter> applicationFilters = new ArrayList<>();
	private final List<ComponentInteractionFilter> componentFilters = new ArrayList<>();

	private JDA jda;
	private Supplier<EmbedBuilder> defaultEmbedSupplier = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = () -> null;

	private HelpConsumer helpConsumer;
	private ComponentManager componentManager;
	private SettingsProvider settingProvider;

	private final List<RegistrationListener> registrationListeners = new ArrayList<>();
	private Consumer<EmbedBuilder> helpBuilderConsumer;

	private ApplicationCommandsBuilder slashCommandsBuilder;
	private ApplicationCommandsCache applicationCommandsCache;
	private Function<@NotNull DiscordLocale, @NotNull DefaultMessages> defaultMessageProvider;
	private ExceptionHandler uncaughtExceptionHandler;

	private final Map<KType, AutocompletionTransformer<?>> autocompletionTransformers = new HashMap<>();

	private long nextExceptionDispatch = 0;

	private final LocalizationManager localizationManager = new LocalizationManager();

	public BContextImpl(@NotNull BConfig config, @NotNull CoroutineEventManager eventManager) {
		this.config = config;
		this.eventManager = eventManager;

		this.defaultMessageProvider = new DefaultMessagesFunction();

		this.classPathContainer = new ClassPathContainer(this);
		this.serviceContainer = new ServiceContainer(this); //Puts itself, ctx, cem and cpc
		this.eventDispatcher = new EventDispatcher(this); //Service put in ctor

		serviceContainer.preloadServices$BotCommands();
	}

	public BConfig getConfig() {
		return config;
	}

	public CoroutineEventManager getEventManager() {
		return eventManager;
	}

	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public ServiceContainer getServiceContainer() {
		return serviceContainer;
	}

	public ClassPathContainer getClassPathContainer() {
		return classPathContainer;
	}

	@Override
	@NotNull
	public JDA getJDA() {
		return serviceContainer.getService(JDA.class);
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
	public DefaultMessages getDefaultMessages(@NotNull DiscordLocale locale) {
		return defaultMessageProvider.apply(locale);
	}

	public void setDefaultMessageProvider(@NotNull Function<@NotNull DiscordLocale, @NotNull DefaultMessages> defaultMessageProvider) {
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

	@Override
	@NotNull
	public ApplicationCommandsContextImpl getApplicationCommandsContext() {
		return applicationCommandsContext;
	}

	@Override
	@NotNull
	public Supplier<EmbedBuilder> getDefaultEmbedSupplier() {
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

	@NotNull
	private CommandPath getEffectivePath(@NotNull AbstractCommandInfo commandInfo) {
		return commandInfo.getCommandId() == null
				? commandInfo.getPath()
				: CommandPath.of(commandInfo.getCommandId());
	}

	public CommandPath addSlashCommand(SlashCommandInfo commandInfo) {
		final CommandPath path = getEffectivePath(commandInfo);

		final CommandInfoMap<SlashCommandInfo> slashCommandMap = getApplicationCommandsContext().getSlashCommandsMap();

		slashCommandMap.put(path, commandInfo);

		return path;
	}

	public CommandPath addUserCommand(UserCommandInfo commandInfo) {
		final CommandPath path = getEffectivePath(commandInfo);

		UserCommandInfo oldCmd = getApplicationCommandsContext().getUserCommandsMap().put(path, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two user commands have the same names: '%s' from %s and %s",
					path,
					Utils.formatMethodShort(oldCmd.getMethod()),
					Utils.formatMethodShort(commandInfo.getMethod())));
		}

		return path;
	}

	public CommandPath addMessageCommand(MessageCommandInfo commandInfo) {
		final CommandPath path = getEffectivePath(commandInfo);

		MessageCommandInfo oldCmd = getApplicationCommandsContext().getMessageCommandsMap().put(path, commandInfo);

		if (oldCmd != null) {
			throw new IllegalStateException(String.format("Two message commands have the same names: '%s' from %s and %s",
					path,
					Utils.formatMethodShort(oldCmd.getMethod()),
					Utils.formatMethodShort(commandInfo.getMethod())));
		}

		return path;
	}

	public void addAutocompletionHandler(AutocompletionHandlerInfo handlerInfo) {
		final AutocompletionHandlerInfo oldHandler = autocompleteHandlersMap.put(handlerInfo.getHandlerName(), handlerInfo);

		if (oldHandler != null) {
			throw new IllegalArgumentException("Tried to register autocompletion handler '" + handlerInfo.getHandlerName() + "' at " + Utils.formatMethodShort(handlerInfo.getMethod()) + " was already registered at " + Utils.formatMethodShort(oldHandler.getMethod()));
		}
	}

	@Nullable
	public AutocompletionHandlerInfo getAutocompletionHandler(String autocompletionHandlerName) {
		return autocompleteHandlersMap.get(autocompletionHandlerName);
	}

	@Override
	public void invalidateAutocompletionCache(String autocompletionHandlerName) {
		final AutocompletionHandlerInfo handler = getAutocompletionHandler(autocompletionHandlerName);
		if (handler == null) throw new IllegalArgumentException("Autocompletion handler name not found for '" + autocompletionHandlerName + "'");

		handler.invalidate();
	}

	public Collection<TextCommandCandidates> getCommands() {
		return Collections.unmodifiableCollection(textCommandMap.values());
	}

	@UnmodifiableView
	public Collection<? extends ApplicationCommandInfo> getApplicationCommandsView() {
		return getApplicationCommandsContext()
				.getApplicationCommandInfoMap()
				.getAllApplicationCommandsView();
	}

	@Override
	public void dispatchException(@NotNull String message, @Nullable Throwable t) {
		if (nextExceptionDispatch < System.currentTimeMillis()) {
			nextExceptionDispatch = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);

			String exceptionStr = t == null ? "" : "\nException : \n%s".formatted(Utils.getException(t));

			jda.retrieveApplicationInfo()
					.map(ApplicationInfo::getOwner)
					.flatMap(User::openPrivateChannel)
					.flatMap(channel -> channel.sendMessage("%s%s\n\nPlease check the logs for more detail and possible exceptions".formatted(message, exceptionStr)))
					.queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
							x -> LOGGER.warn("Could not send exception DM to owner")));
		}
	}

	@Override
	public void addTextFilter(TextCommandFilter filter) {
		Checks.notNull(filter, "Text command filter");

		textFilters.add(filter);
	}

	@Override
	public void addApplicationFilter(ApplicationCommandFilter filter) {
		Checks.notNull(filter, "Application command filter");

		applicationFilters.add(filter);
	}

	@Override
	public void addComponentFilter(ComponentInteractionFilter filter) {
		Checks.notNull(filter, "Component interaction filter");

		componentFilters.add(filter);
	}

	@Override
	public void removeTextFilter(TextCommandFilter filter) {
		Checks.notNull(filter, "Text command filter");

		textFilters.remove(filter);
	}

	@Override
	public void removeApplicationFilter(ApplicationCommandFilter filter) {
		Checks.notNull(filter, "Application command filter");

		applicationFilters.remove(filter);
	}

	@Override
	public void removeComponentFilter(ComponentInteractionFilter filter) {
		Checks.notNull(filter, "Component interaction filter");

		componentFilters.remove(filter);
	}

	public List<TextCommandFilter> getTextFilters() {
		return textFilters;
	}

	public List<ApplicationCommandFilter> getApplicationFilters() {
		return applicationFilters;
	}

	public List<ComponentInteractionFilter> getComponentFilters() {
		return componentFilters;
	}

	@Override
	public void overrideHelp(HelpConsumer helpConsumer) {
		Checks.notNull(helpConsumer, "Help replacement consumer");

		this.helpConsumer = helpConsumer;
	}

	@Override
	public HelpConsumer getHelpConsumer() {
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
		return config.getComponentManager();
	}

	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	public Object getClassInstance(KClass<?> clazz) {
		return classToObjMap.get(clazz);
	}

	public void putClassInstance(KClass<?> clazz, Object obj) {
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
	public Map<Guild, CompletableFuture<CommandUpdateResult>> scheduleApplicationCommandsUpdate(Iterable<Guild> guilds, boolean force, boolean onlineCheck) {
		return slashCommandsBuilder.scheduleApplicationCommandsUpdate(guilds, false, onlineCheck);
	}

	@Override
	@NotNull
	public CompletableFuture<CommandUpdateResult> scheduleApplicationCommandsUpdate(Guild guild, boolean force, boolean onlineCheck) {
		return slashCommandsBuilder.scheduleApplicationCommandsUpdate(guild, force, onlineCheck);
	}

	public ApplicationCommandsBuilder getSlashCommandsBuilder() {
		return slashCommandsBuilder;
	}

	@Override
	public <T> void registerCustomResolver(Class<T> parameterType, CustomResolverFunction<T> function) {
		ParameterResolvers.register(new CustomResolver(parameterType, function));
	}

	public <T> void registerConstructorParameter(Class<T> parameterType, ConstructorParameterSupplier<T> parameterSupplier) {
		parameterSupplierMap.put(parameterType, parameterSupplier);
	}

	public <T> void registerInstanceSupplier(Class<T> classType, InstanceSupplier<T> instanceSupplier) {
		instanceSupplierMap.put(classType, instanceSupplier);
	}

	public void registerDynamicInstanceSupplier(DynamicInstanceSupplier dynamicInstanceSupplier) {
		dynamicInstanceSuppliers.add(dynamicInstanceSupplier);
	}

	public ConstructorParameterSupplier<?> getParameterSupplier(Class<?> parameterType) {
		return parameterSupplierMap.get(parameterType);
	}

	public InstanceSupplier<?> getInstanceSupplier(Class<?> classType) {
		return instanceSupplierMap.get(classType);
	}

	public List<DynamicInstanceSupplier> getDynamicInstanceSuppliers() {
		return dynamicInstanceSuppliers;
	}

	public <T> void registerCommandDependency(Class<T> fieldType, Supplier<T> supplier) {
		commandDependencyMap.put(fieldType, supplier);
	}

	public Supplier<?> getCommandDependency(Class<?> fieldType) {
		return commandDependencyMap.get(fieldType);
	}

	public <T> void registerMethodParameterSupplier(Class<T> parameterType, MethodParameterSupplier<T> supplier) {
		methodParameterSupplierMap.put(parameterType, supplier);
	}

	public MethodParameterSupplier<?> getMethodParameterSupplier(Class<?> parameterType) {
		return methodParameterSupplierMap.get(parameterType);
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

	@Override
	public TLongSet getTestGuildIds() {
		return testGuildIds;
	}

	public void addTestGuildIds(long... ids) {
		testGuildIds.addAll(ids);
	}

	public AutocompletionTransformer<?> getAutocompletionTransformer(KType type) {
		return autocompletionTransformers.get(type);
	}

	//TODO use KType
	public <T> void registerAutocompletionTransformer(Class<T> type, AutocompletionTransformer<T> autocompletionTransformer) {
		autocompletionTransformers.put(ParameterType.ofClass(type).getType(), autocompletionTransformer);
	}

	public boolean isOnlineAppCommandCheckEnabled() {
		return onlineAppCommandCheckEnabled;
	}

	public void enableOnlineAppCommandCheck() {
		this.onlineAppCommandCheckEnabled = true;
	}

	public ModalMaps getModalMaps() {
		return modalMaps;
	}

	public LocalizationManager getLocalizationManager() {
		return localizationManager;
	}

	public ApplicationCommandManager getApplicationCommandManager() {
		return applicationCommandManager;
	}
}
