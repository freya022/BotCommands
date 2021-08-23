package com.freya02.botcommands;

import com.freya02.botcommands.annotation.Dependency;
import com.freya02.botcommands.annotation.RequireOwner;
import com.freya02.botcommands.application.ApplicationCommandListener;
import com.freya02.botcommands.application.ApplicationCommandsBuilder;
import com.freya02.botcommands.application.context.ContextCommandListener;
import com.freya02.botcommands.application.context.annotations.JdaMessageCommand;
import com.freya02.botcommands.application.context.annotations.JdaUserCommand;
import com.freya02.botcommands.application.slash.SlashCommand;
import com.freya02.botcommands.application.slash.SlashCommandListener;
import com.freya02.botcommands.application.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.components.DefaultComponentManager;
import com.freya02.botcommands.components.internal.ComponentsBuilder;
import com.freya02.botcommands.parameters.*;
import com.freya02.botcommands.prefixed.*;
import com.freya02.botcommands.prefixed.annotation.AddExecutableHelp;
import com.freya02.botcommands.prefixed.annotation.AddSubcommandHelp;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import com.freya02.botcommands.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private static final List<Class<? extends Annotation>> methodAnnotations = List.of(JdaSlashCommand.class, JdaMessageCommand.class, JdaUserCommand.class);

	private final List<Long> slashGuildIds = new ArrayList<>();

	private final BContextImpl context = new BContextImpl();
	private final PrefixedCommandsBuilder prefixedCommandsBuilder = new PrefixedCommandsBuilder(context);
	private final ApplicationCommandsBuilder applicationCommandsBuilder = new ApplicationCommandsBuilder(context, slashGuildIds);
	private final ComponentsBuilder componentsBuilder = new ComponentsBuilder(context);

	private final Set<Class<?>> classes = new HashSet<>();

	private boolean disableHelpCommand;

	private boolean usePing;

	private CommandsBuilder(@NotNull String prefix, long topOwnerId) {
		Utils.requireNonBlank(prefix, "Prefix");
		context.setPrefixes(List.of(prefix));
		context.addOwner(topOwnerId);
	}

	private CommandsBuilder(long topOwnerId) {
		context.addOwner(topOwnerId);

		usePing = true;
	}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled
	 *
	 * @param topOwnerId The most owner of the bot
	 */
	public static CommandsBuilder withPing(long topOwnerId) {
		return new CommandsBuilder(topOwnerId);
	}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder}
	 *
	 * @param prefix     Prefix of the bot
	 * @param topOwnerId The most owner of the bot
	 */
	public static CommandsBuilder withPrefix(@NotNull String prefix, long topOwnerId) {
		return new CommandsBuilder(prefix, topOwnerId);
	}

	/**
	 * Allows to change the framework's default messages while keeping the builder pattern
	 *
	 * @param modifier Consumer to change the default messages
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder overrideMessages(Consumer<DefaultMessages> modifier) {
		modifier.accept(context.getDefaultMessages());

		return this;
	}

	/**
	 * Enables {@linkplain AddSubcommandHelp} on all registered commands
	 *
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addSubcommandHelpByDefault() {
		context.setAddSubcommandHelpByDefault(true);

		return this;
	}

	/**
	 * Enables {@linkplain AddExecutableHelp} on all registered commands
	 *
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addExecutableHelpByDefault() {
		context.setAddExecutableHelpByDefault(true);

		return this;
	}

	/**
	 * Disables the help command for prefixed commands and replaces the implementation when incorrect syntax is detected<br>
	 * <b>You can provide an empty implementation if you wish to just disable all the help stuff completely</b>
	 *
	 * @param helpConsumer Consumer used to show help when a command is detected but their syntax is invalid, can do nothing
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder disableHelpCommand(@NotNull Consumer<BaseCommandEvent> helpConsumer) {
		this.disableHelpCommand = true;
		this.context.overrideHelp(helpConsumer);

		return this;
	}

	/**
	 * Debug feature - Makes it so application commands are only updated on these guilds
	 *
	 * @param slashGuildIds IDs of the guilds
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder updateCommandsOnGuildIds(List<Long> slashGuildIds) {
		this.slashGuildIds.clear();
		this.slashGuildIds.addAll(slashGuildIds);

		return this;
	}

	/**
	 * Sets the component manager, used to handle storing/retrieving persistent/lambda components handlers
	 *
	 * @param componentManager The {@link ComponentManager}
	 * @return This builder for chaining convenience
	 * @see DefaultComponentManager
	 */
	public CommandsBuilder setComponentManager(ComponentManager componentManager) {
		context.setComponentManager(Objects.requireNonNull(componentManager, "Component manager cannot be null"));

		return this;
	}

	/**
	 * Sets the {@linkplain PermissionProvider}
	 *
	 * @param providerFunction The function which gives {@linkplain PermissionProvider} from a BContext <b>(I highlight suggest you pass the BContext to the provider so you can easily update the commands when something changes)</b>, for command privileges and application command whitelist
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder setPermissionProvider(Function<BContext, PermissionProvider> providerFunction) {
		context.setPermissionProvider(Objects.requireNonNull(providerFunction.apply(context), "Permission provider cannot be null"));

		return this;
	}

	/**
	 * Sets the {@linkplain SettingsProvider}, used to take guild-specific settings such as prefixes
	 *
	 * @param provider The {@linkplain SettingsProvider}
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder setSettingsProvider(SettingsProvider provider) {
		context.setSettingsProvider(Objects.requireNonNull(provider, "Settings provider cannot be null"));

		return this;
	}

	/**
	 * Sets the help builder consumer, it allows you to add stuff in the help embeds when they are created.
	 *
	 * @param builderConsumer The help builder consumer, modifies the EmbedBuilder
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder setHelpBuilderConsumer(Consumer<EmbedBuilder> builderConsumer) {
		context.setHelpBuilderConsumer(builderConsumer);

		return this;
	}

	/**
	 * <p>Sets the embed builder and the footer icon that this library will use as base embed builder</p>
	 * <p><b>Note : The icon name when used will be "icon.jpg", your icon must be a JPG file and be the same name</b></p>
	 *
	 * @param defaultEmbedFunction      The default embed builder
	 * @param defaultFooterIconSupplier The default icon for the footer
	 * @return This builder
	 */
	public CommandsBuilder setDefaultEmbedFunction(@NotNull Supplier<EmbedBuilder> defaultEmbedFunction, @NotNull Supplier<InputStream> defaultFooterIconSupplier) {
		this.context.setDefaultEmbedSupplier(defaultEmbedFunction);
		this.context.setDefaultFooterIconSupplier(defaultFooterIconSupplier);
		return this;
	}

	/**
	 * Adds owners, they can access the commands annotated with {@linkplain RequireOwner}
	 *
	 * @param ownerIds Owners Long IDs to add
	 * @return This builder
	 */
	public CommandsBuilder addOwners(long... ownerIds) {
		for (long ownerId : ownerIds) {
			context.addOwner(ownerId);
		}

		return this;
	}

	/**
	 * Adds a prefix to choose from the list of prefixes
	 *
	 * @param prefix The prefix to add
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addPrefix(String prefix) {
		context.addPrefix(prefix);

		return this;
	}

	/**
	 * Registers a constructor parameter supplier, this means that your commands can have the given parameter type in it's constructor, and it will be injected during instantiation
	 *
	 * @param parameterType     The type of the parameter inside your constructor
	 * @param parameterSupplier The supplier for this parameter
	 * @param <T>               Type of the parameter
	 * @return This builder for chaining convenience
	 */
	public <T> CommandsBuilder registerConstructorParameter(Class<T> parameterType, ConstructorParameterSupplier<T> parameterSupplier) {
		if (context.getParameterSupplier(parameterType) != null)
			throw new IllegalStateException("Parameter supplier already exists for parameter of type " + parameterType.getName());

		context.registerConstructorParameter(parameterType, parameterSupplier);

		return this;
	}

	/**
	 * Registers a instance supplier, this means that your commands can be instantiated using the given {@link InstanceSupplier}<br><br>
	 * Instead of resolving the parameters manually with {@link #registerConstructorParameter(Class, ConstructorParameterSupplier)} you can use this to give directly the command's instance
	 *
	 * @param classType        Type of the command's class
	 * @param instanceSupplier Instance supplier for this command
	 * @param <T>              Type of the command's class
	 * @return This builder for chaining convenience
	 */
	public <T> CommandsBuilder registerInstanceSupplier(Class<T> classType, InstanceSupplier<T> instanceSupplier) {
		if (context.getInstanceSupplier(classType) != null)
			throw new IllegalStateException("Instance supplier already exists for class " + classType.getName());

		context.registerInstanceSupplier(classType, instanceSupplier);

		return this;
	}

	/**
	 * Registers a command dependency supplier, the supplier will be used on every field of the same type in a command if annotated with {@link Dependency @Dependency}
	 *
	 * @param fieldType Type of the field's object
	 * @param supplier  Field supplier for this type
	 * @param <T>       Type of the field's object
	 * @return This builder for chaining convenience
	 */
	public <T> CommandsBuilder registerCommandDependency(Class<T> fieldType, Supplier<T> supplier) {
		if (context.getCommandDependency(fieldType) != null)
			throw new IllegalStateException("Command dependency already exists for fields of type " + fieldType.getName());

		context.registerCommandDependency(fieldType, supplier);

		return this;
	}

	/**
	 * Adds a {@link RegistrationListener} to this command builder, giving you various event of what is getting loaded
	 *
	 * @param listeners The {@link RegistrationListener RegistrationListeners} to register
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addRegistrationListeners(RegistrationListener... listeners) {
		context.addRegistrationListeners(listeners);

		return this;
	}

	/**
	 * Adds a filter for received messages (could prevent regular commands from running), <b>See {@link BContext#addFilter(Predicate)} for more info</b>
	 *
	 * @param filter The filter to add, should return <code>false</code> if the message has to be ignored
	 * @return This builder for chaining convenience
	 * @see BContext#addFilter(Predicate)
	 */
	public CommandsBuilder addFilter(Predicate<MessageInfo> filter) {
		context.addFilter(filter);

		return this;
	}

	/**
	 * Registers a command / application command's class so it can be instantiated later in {@link #build(JDA, String)}<br>
	 *
	 * @param clazz The command's class to register
	 * @return This builder for chaining convenience
	 * @throws IllegalArgumentException If the class is not a {@link Command} nor a {@link SlashCommand}
	 */
	public CommandsBuilder registerCommand(Class<?> clazz) {
		if (!Command.class.isAssignableFrom(clazz) && !SlashCommand.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("You can't register a class that's not a Command or a SlashCommand, provided: " + clazz.getName());
		}

		classes.add(clazz);

		return this;
	}

	/**
	 * Adds the commands of this packages in this builder, all the classes which extends {@link Command} or {@link SlashCommand} will be registered<br>
	 * <b>You can have up to 2 nested sub-folders in the specified package</b>, this means you can have your package structure like this:
	 *
	 * <pre><code>
	 * |
	 * |__slash
	 * |  |
	 * |  |__fun
	 * |     |
	 * |     |__Meme.java
	 * |        Fish.java
	 * |        ...
	 * |
	 * |__regular
	 *   |
	 *   |__moderation
	 *      |
	 *      |__Ban.java
	 *         Mute.java
	 *         ...
	 * </code></pre>
	 *
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.commands
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addSearchPath(String commandPackageName) throws IOException {
		addSearchPath(commandPackageName, 2);

		return this;
	}

	/**
	 * Registers a parameter resolver, must have one or more of the 3 interfaces, {@link RegexParameterResolver}, {@link SlashParameterResolver} and {@link ComponentParameterResolver}
	 *
	 * @param resolver Your own ParameterResolver to register
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder registerParameterResolver(ParameterResolver resolver) {
		ParameterResolvers.register(resolver);

		return this;
	}

	//skip can be inlined by 2 but inlining would conflict with the above overload and also remove 1 stack frame, reintroducing the parameter need
	@SuppressWarnings("SameParameterValue")
	private void addSearchPath(String commandPackageName, int skip) throws IOException {
		Utils.requireNonBlank(commandPackageName, "Command package");

		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(skip).findFirst().orElseThrow().getDeclaringClass());
		classes.addAll(Utils.getClasses(IOUtils.getJarPath(callerClass), commandPackageName, 3));
	}

	private void buildClasses() {
		try {
			classes.removeIf(c -> {
				try {
					return !Utils.isInstantiable(c);
				} catch (IllegalAccessException | InvocationTargetException e) {
					LOGGER.error("An error occurred while trying to find if a class is instantiable", e);

					throw new RuntimeException("An error occurred while trying to find if a class is instantiable", e);
				}
			});

			for (Class<?> aClass : classes) {
				processClass(aClass);
			}

			if (!disableHelpCommand) {
				processClass(HelpCommand.class);

				final HelpCommand help = (HelpCommand) context.findCommand("help");
				if (help == null) throw new IllegalStateException("HelpCommand did not build properly");
				help.generate();
			}

			if (context.getComponentManager() != null) {
				//Load button listeners
				for (Class<?> aClass : classes) {
					componentsBuilder.processClass(aClass);
				}
			} else {
				LOGGER.info("ComponentManager is not set, the Components API, paginators and menus won't be usable");
			}

			LOGGER.info("Loaded {} commands", context.getCommands().size());
			printCommands(context.getCommands(), 0);

			applicationCommandsBuilder.postProcess();

			if (context.getComponentManager() != null) {
				componentsBuilder.postProcess();
			}

			context.getRegistrationListeners().forEach(RegistrationListener::onBuildComplete);

			LOGGER.info("Finished registering all commands");
		} catch (Throwable e) {
			LOGGER.error("An error occurred while loading the commands, the commands will not work");

			throw new RuntimeException(e);
		}
	}

	private void printCommands(Collection<Command> commands, int indent) {
		for (Command command : commands) {
			LOGGER.debug("{}- '{}' Bot permission=[{}] User permissions=[{}]",
					"\t".repeat(indent),
					command.getInfo().getName(),
					command.getInfo().getBotPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")),
					command.getInfo().getUserPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")));

			printCommands(command.getInfo().getSubcommands(), indent + 1);
		}
	}

	private void processClass(Class<?> aClass) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		if (isCommand(aClass) && aClass.getDeclaringClass() == null) { //Declaring class returns null for anonymous classes, we only need to check if the class is not an inner class
			boolean isInstantiable = Utils.isInstantiable(aClass);

			if (isInstantiable) {
				if (!Command.class.isAssignableFrom(aClass))
					throw new IllegalArgumentException("Class " + aClass + " should extend Command");

				final Command someCommand = (Command) ClassInstancer.instantiate(context, aClass);

				prefixedCommandsBuilder.processPrefixedCommand(someCommand);
			} else {
				LOGGER.error("A non-instantiable class tried to get processed, this should have been filtered by #buildClasses, please report to the devs");
			}
		} else {
			//If not a text command, search for methods annotated with a compatible annotation
			for (Method method : aClass.getDeclaredMethods()) {
				for (Class<? extends Annotation> annotation : methodAnnotations) {
					if (method.isAnnotationPresent(annotation)) {
						final Object annotatedInstance = ClassInstancer.instantiate(context, aClass);
						
						applicationCommandsBuilder.processApplicationCommand(annotatedInstance, method);
						
						break;
					}
				}
			}
		}
	}

	private boolean isCommand(Class<?> aClass) {
		if (Modifier.isAbstract(aClass.getModifiers()))
			return false;

		return Command.class.isAssignableFrom(aClass) && aClass.isAnnotationPresent(JdaCommand.class);
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda                The JDA instance of your bot
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.commands
	 * @throws IOException If an exception occurs when reading the jar path or getting classes
	 * @see #addSearchPath(String)
	 */
	public void build(JDA jda, @NotNull String commandPackageName) throws IOException {
		addSearchPath(commandPackageName, 2);

		build(jda);
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda The JDA instance of your bot
	 */
	public void build(JDA jda) {
		final List<GatewayIntent> intents = List.of(
				GatewayIntent.GUILD_MESSAGES
		);
		if (!jda.getGatewayIntents().containsAll(intents)) {
			throw new IllegalStateException("JDA must have these intents enabled: " + intents.stream().map(Enum::name).collect(Collectors.joining(", ")));
		}

		setupContext(jda);

		buildClasses();

		context.addEventListeners(
				new EventWaiter(jda),
				new CommandListener(context),
				new ApplicationCommandListener(context),
				new SlashCommandListener(context),
				new ContextCommandListener(context)
		);
	}

	private void setupContext(JDA jda) {
		context.setJDA(jda);
		if (usePing) {
			context.addPrefix("<@" + jda.getSelfUser().getId() + "> ");
			context.addPrefix("<@!" + jda.getSelfUser().getId() + "> ");
		}

		registerConstructorParameter(BContext.class, ignored -> context);
		registerCommandDependency(BContext.class, () -> context);
		
		registerConstructorParameter(JDA.class, ignored -> jda);
		registerCommandDependency(JDA.class, () -> jda);
	}

	public BContext getContext() {
		return context;
	}
}