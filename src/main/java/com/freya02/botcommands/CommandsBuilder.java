package com.freya02.botcommands;

import com.freya02.botcommands.annotation.RequireOwner;
import com.freya02.botcommands.buttons.ButtonsBuilder;
import com.freya02.botcommands.buttons.DefaultIdManager;
import com.freya02.botcommands.buttons.IdManager;
import com.freya02.botcommands.prefixed.*;
import com.freya02.botcommands.prefixed.annotation.AddExecutableHelp;
import com.freya02.botcommands.prefixed.annotation.AddSubcommandHelp;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import com.freya02.botcommands.slash.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();

	private final List<Long> slashGuildIds = new ArrayList<>();

	private final BContextImpl context = new BContextImpl();
	private final PrefixedCommandsBuilder prefixedCommandsBuilder = new PrefixedCommandsBuilder(context);
	private final SlashCommandsBuilder slashCommandsBuilder = new SlashCommandsBuilder(context, slashGuildIds);
	private final ButtonsBuilder buttonsBuilder = new ButtonsBuilder(context);

	private boolean disableHelpCommand;
	private boolean disableSlashHelpCommand;

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
	 * Disables the /help command for slash commands
	 *
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder disableSlashHelpCommand() {
		this.disableSlashHelpCommand = true;

		return this;
	}

	/**
	 * Debug feature - Makes it so slash commands are only updated on these guilds
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
	 * Sets the ID manager, used to generate Discord buttons IDs and store component data
	 *
	 * @param idManager The {@link IdManager}
	 * @return This builder for chaining convenience
	 * @see DefaultIdManager
	 */
	public CommandsBuilder setIdManager(IdManager idManager) {
		context.setIdManager(Objects.requireNonNull(idManager, "ID manager cannot be null"));

		return this;
	}

	/**
	 * Sets the {@linkplain PermissionProvider}
	 *
	 * @param provider The {@linkplain PermissionProvider}, for command privileges and slash command whitelist
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder setPermissionProvider(PermissionProvider provider) {
		context.setPermissionProvider(Objects.requireNonNull(provider, "Permission provider cannot be null"));

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
		context.registerInstanceSupplier(classType, instanceSupplier);

		return this;
	}

	/**
	 * Adds a filter for received messages (could prevent regular commands from runnings), <b>See {@link BContext#addFilter(Predicate)} for more info</b>
	 *
	 * @param filter The filter to add, should return <code>false</code> if the message has to be ignored
	 * @return This builder for chaining convenience
	 * @see BContext#addFilter(Predicate)
	 */
	public CommandsBuilder addFilter(Predicate<MessageInfo> filter) {
		context.addFilter(filter);

		return this;
	}

	private void buildClasses(List<Class<?>> classes) {
		try {
			for (Class<?> aClass : classes) {
				processClass(aClass);
			}

			if (!disableHelpCommand) {
				processClass(HelpCommand.class);

				final HelpCommand help = (HelpCommand) context.findCommand("help");
				if (help == null) throw new IllegalStateException("HelpCommand did not build properly");
				help.generate();
			}

			if (!disableSlashHelpCommand) {
				processClass(SlashHelpCommand.class);

				final SlashCommandInfo info = context.findSlashCommand("help");
				if (info == null) throw new IllegalStateException("SlashHelpCommand did not build properly");

				((SlashHelpCommand) info.getInstance()).generate();
			}

			//Load button listeners
			for (Class<?> aClass : classes) {
				buttonsBuilder.processButtonListener(aClass);
			}

			LOGGER.info("Loaded {} commands", context.getCommands().size());
			printCommands(context.getCommands(), 0);

			LOGGER.info("Loaded {} slash commands", context.getSlashCommands().size());
			printSlashCommands(context.getSlashCommands());

			slashCommandsBuilder.postProcess();

			buttonsBuilder.postProcess();

			context.getRegistrationListeners().forEach(RegistrationListener::onBuildComplete);

			LOGGER.info("Finished registering all commands");
		} catch (Throwable e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("An error occurred while loading the commands, the commands will not work");
			} else { //Dont want this error hidden by the lack of logging framework
				System.err.println("An error occurred while loading the commands, the commands will not work");
			}

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

	private void printSlashCommands(Collection<SlashCommandInfo> commands) {
		for (SlashCommandInfo command : commands) {
			LOGGER.debug("{} - '{}' Bot permission=[{}] User permissions=[{}]",
					command.isGuildOnly() ? "Guild    " : "Guild+DMs",
					command.getPath(),
					command.getBotPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")),
					command.getUserPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")));
		}
	}

	private void processClass(Class<?> aClass) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		if (isCommand(aClass) && aClass.getDeclaringClass() == null) { //Declaring class returns null for anonymous classes, we only need to check if the class is not an inner class
			boolean isInstantiable = Utils.isInstantiable(aClass);

			if (isInstantiable) {
				Object someCommand;

				if (!Command.class.isAssignableFrom(aClass) && !SlashCommand.class.isAssignableFrom(aClass))
					throw new IllegalArgumentException("Class " + aClass + " should extend Command or SlashCommand");

				//The command object has to be created either by the instance supplier
				// or by the **only** constructor a class has
				// It must resolve all parameters types with the registered parameter suppliers
				final InstanceSupplier<?> instanceSupplier = context.getInstanceSupplier(aClass);
				if (instanceSupplier != null) {
					someCommand = instanceSupplier.get(context);
				} else {
					final Constructor<?>[] constructors = aClass.getConstructors();
					if (constructors.length == 0)
						throw new IllegalArgumentException("Class " + aClass.getName() + " must have an accessible constructor");

					if (constructors.length > 1)
						throw new IllegalArgumentException("Class " + aClass.getName() + " must have exactly one constructor");

					final Constructor<?> constructor = constructors[0];

					if (Command.class.isAssignableFrom(aClass)) { //Obligatory arg
						if (Arrays.stream(constructor.getParameterTypes()).noneMatch(BContext.class::isAssignableFrom))
							throw new IllegalArgumentException("Class " + aClass.getName() + " should have a BContext in its constructor arguments");
					}

					List<Object> parameterObjs = new ArrayList<>();

					Class<?>[] parameterTypes = constructor.getParameterTypes();
					for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
						Class<?> parameterType = parameterTypes[i];

						if (BContext.class.isAssignableFrom(parameterType)) {
							parameterObjs.add(context);
						} else {
							final ConstructorParameterSupplier<?> supplier = context.getParameterSupplier(parameterType);
							if (supplier == null)
								throw new IllegalArgumentException(String.format("Found no constructor parameter supplier for parameter #%d of type %s in class %s", i, parameterType.getSimpleName(), aClass.getSimpleName()));

							parameterObjs.add(supplier.get(aClass));
						}
					}

					someCommand = constructor.newInstance(parameterObjs.toArray());
				}

				context.getClassToObjMap().put(aClass, someCommand);

				if (someCommand instanceof Command) {
					prefixedCommandsBuilder.processPrefixedCommand((Command) someCommand);
				} else if (someCommand instanceof SlashCommand) {
					slashCommandsBuilder.processSlashCommand((SlashCommand) someCommand);
				} else {
					throw new IllegalArgumentException("How did you even give a command that doesn't extend Command or SlashCommand ??? at " + someCommand.getClass().getName());
				}
			}
		}
	}

	private boolean isCommand(Class<?> aClass) {
		if (Modifier.isAbstract(aClass.getModifiers()))
			return false;

		if (SlashCommand.class.isAssignableFrom(aClass))
			return true;

		return Command.class.isAssignableFrom(aClass) && aClass.isAnnotationPresent(JdaCommand.class);
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance<br>
	 * <b>You can have up to 2 nested sub-folders in the specified package</b>, this means you can have your package structure like this:
	 * <ul>
	 *     <li>com.freya02.bot.commands
	 *     <ul>
	 *         <li>slash
	 *         <ul>
	 *             <li>fun
	 *             <ul>
	 *                 <li>Meme</li>
	 *                 <li>Fish</li>
	 *                 <li>...</li>
	 *             </ul>
	 *             </li>
	 *         </ul>
	 *         </li>
	 *     </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param jda                The JDA instance of your bot
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.commands
	 * @throws IOException If an exception occurs when reading the jar path or getting classes
	 */
	public void build(JDA jda, @NotNull String commandPackageName) throws IOException {
		Utils.requireNonBlank(commandPackageName, "Command package");

		final List<GatewayIntent> intents = List.of(
				GatewayIntent.GUILD_MESSAGES
		);
		if (!jda.getGatewayIntents().containsAll(intents)) {
			throw new IllegalStateException("JDA must have these intents enabled: " + intents.stream().map(Enum::name).collect(Collectors.joining(", ")));
		}

		setupContext(jda);

		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		buildClasses(Utils.getClasses(IOUtils.getJarPath(callerClass), commandPackageName, 3));

		context.addEventListeners(new com.freya02.botcommands.waiter.EventWaiter());

		context.addEventListeners(new CommandListener(context), new SlashCommandListener(context));
	}

	private void setupContext(JDA jda) {
		context.setJDA(jda);
		if (usePing) {
			context.addPrefix("<@" + jda.getSelfUser().getId() + "> ");
			context.addPrefix("<@!" + jda.getSelfUser().getId() + "> ");
		}
	}

	public BContext getContext() {
		return context;
	}
}