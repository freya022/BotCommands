package com.freya02.botcommands.api;

import com.freya02.botcommands.api.annotations.RequireOwner;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.builder.ApplicationCommandsBuilder;
import com.freya02.botcommands.api.builder.DebugBuilder;
import com.freya02.botcommands.api.builder.ExtensionsBuilder;
import com.freya02.botcommands.api.builder.TextCommandsBuilder;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.DefaultComponentManager;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.CommandsBuilderImpl;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class CommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context = new BContextImpl();

	private final Set<String> packageNames = new HashSet<>();
	private final Set<Class<?>> manualClasses = new HashSet<>();

	private final TextCommandsBuilder textCommandBuilder = new TextCommandsBuilder(context);
	private final ApplicationCommandsBuilder applicationCommandBuilder = new ApplicationCommandsBuilder(context);
	private final ExtensionsBuilder extensionsBuilder = new ExtensionsBuilder(context);
	private final DebugBuilder debugBuilder = new DebugBuilder();

	private CommandsBuilder() {}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled by default
	 *
	 * @param topOwnerId The most owner of the bot
	 *                   
	 * @see #addOwners(long...)
	 */
	public static CommandsBuilder newBuilder(long topOwnerId) {
		return new CommandsBuilder().addOwners(topOwnerId);
	}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled by default
	 */
	public static CommandsBuilder newBuilder() {
		return new CommandsBuilder();
	}

	/**
	 * Adds owners, they can access the commands annotated with {@linkplain RequireOwner} and receives exceptions in DMs
	 *
	 * @param ownerIds IDs of the owners
	 * @return This builder
	 */
	public CommandsBuilder addOwners(long... ownerIds) {
		for (long ownerId : ownerIds) {
			context.addOwner(ownerId);
		}

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
	 * Sets the uncaught exception handler used by the thread pools such of command handlers / components
	 *
	 * <br><br>Notes: You will need to handle things such as already acknowledged interactions (in the case of interaction events, where the exception happened after the interaction has been acknowledged), see {@link Interaction#isAcknowledged()}
	 *
	 * @param exceptionHandler The handler to call on uncaught exceptions
	 * @see Utils#getException(Throwable)
	 * @see ExceptionHandlerAdapter
	 */
	public CommandsBuilder setUncaughtExceptionHandler(@Nullable ExceptionHandler exceptionHandler) {
		context.setUncaughtExceptionHandler(exceptionHandler);

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
	 * Registers a command / application command's class so it can be instantiated later in {@link #build(JDA, String)}<br>
	 *
	 * @param clazz The command's class to register
	 * @return This builder for chaining convenience
	 * @throws IllegalArgumentException If the class is not a {@link TextCommand} nor a {@link ApplicationCommand}
	 */
	public CommandsBuilder registerCommand(Class<?> clazz) {
		if (!TextCommand.class.isAssignableFrom(clazz) && !ApplicationCommand.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("You can't register a class that's not a TextCommand or an ApplicationCommand, provided: " + clazz.getName());
		}

		manualClasses.add(clazz);

		return this;
	}

	/**
	 * Adds the commands of this packages in this builder, all the classes which extends {@link TextCommand}, {@link ApplicationCommand} and other classes which contains annotated methods, will be registered.
	 * <br><b>Tip:</b> you can have your package structure such as:
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
	 * |__text
	 *   |
	 *   |__moderation
	 *      |
	 *      |__Ban.java
	 *         Mute.java
	 *         ...
	 * </code></pre>
	 *
	 * @param commandPackageName The package name where all the commands are, ex: {@code com.freya02.bot.commands}
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addSearchPath(String commandPackageName) {
		Utils.requireNonBlank(commandPackageName, "Command package");
		packageNames.add(commandPackageName);

		return this;
	}

	/**
	 * Configures some settings related to framework extensions
	 *
	 * @param consumer The consumer to run in order to configure extension settings
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder extensionsBuilder(Consumer<ExtensionsBuilder> consumer) {
		consumer.accept(extensionsBuilder);

		return this;
	}

	/**
	 * Configures some settings related to text commands
	 *
	 * @param consumer The consumer to run in order to configure text commands
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder textCommandBuilder(Consumer<TextCommandsBuilder> consumer) {
		consumer.accept(textCommandBuilder);

		return this;
	}

	/**
	 * Configures some settings related to application commands
	 *
	 * @param consumer The consumer to run in order to configure application commands
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder applicationCommandBuilder(@NotNull Consumer<@NotNull ApplicationCommandsBuilder> consumer) {
		consumer.accept(applicationCommandBuilder);

		return this;
	}

	/**
	 * Configures some settings related to debugging
	 *
	 * @param consumer The consumer to run in order to configure debug features
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder debugBuilder(@NotNull Consumer<@NotNull DebugBuilder> consumer) {
		consumer.accept(debugBuilder);

		return this;
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda                The JDA instance of your bot
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.commands
	 * @see #addSearchPath(String)
	 */
	@Blocking
	public void build(JDA jda, @NotNull String commandPackageName) {
		addSearchPath(commandPackageName);

		build(jda);
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda The JDA instance of your bot
	 */
	@Blocking
	public void build(JDA jda) {
		try {
			new CommandsBuilderImpl(context, packageNames, manualClasses, applicationCommandBuilder.getSlashGuildIds()).build(jda);
		} catch (RuntimeException e) {
			LOGGER.error("An error occurred while creating the framework, aborted");

			throw e;
		} catch (Throwable e) {
			LOGGER.error("An error occurred while creating the framework, aborted");

			throw new RuntimeException(e);
		}
	}

	public BContext getContext() {
		return context;
	}
}