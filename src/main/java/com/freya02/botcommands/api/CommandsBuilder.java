package com.freya02.botcommands.api;

import com.freya02.botcommands.annotations.api.annotations.RequireOwner;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.builder.ApplicationCommandsBuilder;
import com.freya02.botcommands.api.builder.DebugBuilder;
import com.freya02.botcommands.api.builder.ExtensionsBuilder;
import com.freya02.botcommands.api.builder.TextCommandsBuilder;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.DefaultComponentManager;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.utils.Utils;
import dev.minn.jda.ktx.events.CoroutineEventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public final class CommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context = new BContextImpl(null, null);

	private final Set<String> packages = new HashSet<>();
	private final Set<Class<?>> classes = new HashSet<>();

	private final TextCommandsBuilder textCommandBuilder = new TextCommandsBuilder(context);
	private final ApplicationCommandsBuilder applicationCommandBuilder = new ApplicationCommandsBuilder(context);
	private final ExtensionsBuilder extensionsBuilder = new ExtensionsBuilder(context);
	private final DebugBuilder debugBuilder = new DebugBuilder();

	private CommandsBuilder(long topOwnerId) {
		context.addOwner(topOwnerId);
	}

	private CommandsBuilder() {
		LOGGER.info("No owner ID specified, exceptions won't be sent to owners");
	}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled by default
	 *
	 * @param topOwnerId The most owner of the bot
	 */
	public static CommandsBuilder newBuilder(long topOwnerId) {
		Checks.positive(topOwnerId, "Owner ID");

		return new CommandsBuilder(topOwnerId);
	}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled by default
	 */
	public static CommandsBuilder newBuilder() {
		return new CommandsBuilder();
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
	 * <p>Sets the embed builder and the footer icon that this library will use as base embed builder</p>
	 * <p><b>Note : The icon name when used will be "icon.jpg", your icon must be a JPG file and be the same name</b></p>
	 *
	 * @param defaultEmbedFunction      The default embed builder
	 * @param defaultFooterIconSupplier The default icon for the footer
	 * @return This builder
	 */
	public CommandsBuilder setDefaultEmbedFunction(@NotNull Supplier<EmbedBuilder> defaultEmbedFunction, @NotNull Supplier<InputStream> defaultFooterIconSupplier) { //TODO move to text command config
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds a {@link RegistrationListener} to this command builder, giving you various event of what is getting loaded
	 *
	 * @param listeners The {@link RegistrationListener RegistrationListeners} to register
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addRegistrationListeners(RegistrationListener... listeners) { //TODO change to custom BC events
		context.addRegistrationListeners(listeners);

		return this;
	}

	/**
	 * Registers a command / application command's class<br>
	 *
	 * @param clazz The command's class to register
	 * @return This builder for chaining convenience
	 * @throws IllegalArgumentException If the class is not a {@link TextCommand} nor a {@link ApplicationCommand}
	 */
	public CommandsBuilder registerCommand(Class<?> clazz) {
		if (!TextCommand.class.isAssignableFrom(clazz) && !ApplicationCommand.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("You can't register a class that's not a Command or a SlashCommand, provided: " + clazz.getName());
		}

		classes.add(clazz);

		return this;
	}

	/**
	 * Adds the commands of this packages in this builder, all the classes which extends {@link TextCommand}, {@link ApplicationCommand} and other classes which contains annotated methods, will be registered<br>
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
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.bot.commands
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder addSearchPath(String commandPackageName) throws IOException {
		Utils.requireNonBlank(commandPackageName, "Command package");

		packages.add(commandPackageName);

		return this;
	}

	/**
	 * Configures some settings related to framework extensions
	 *
	 * @param consumer The consumer to run in order to configure extension settings
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder extensionsBuilder(Consumer<ExtensionsBuilder> consumer) {  //TODO move to auto service discovery
		consumer.accept(extensionsBuilder);

		ReceiverConsumer<ExtensionsBuilder> lol = extensionsBuilder1 -> {

		};

		return this;
	}

	/**
	 * Configures some settings related to text commands
	 *
	 * @param consumer The consumer to run in order to configure text commands
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder textCommandBuilder(Consumer<TextCommandsBuilder> consumer) { //TODO move to config
		consumer.accept(textCommandBuilder);

		return this;
	}

	/**
	 * Configures some settings related to application commands
	 *
	 * @param consumer The consumer to run in order to configure application commands
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder applicationCommandBuilder(@NotNull Consumer<@NotNull ApplicationCommandsBuilder> consumer) { //TODO move to config
		consumer.accept(applicationCommandBuilder);

		return this;
	}

	/**
	 * Configures some settings related to debugging
	 *
	 * @param consumer The consumer to run in order to configure debug features
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder debugBuilder(@NotNull Consumer<@NotNull DebugBuilder> consumer) { //TODO move to config
		consumer.accept(debugBuilder);

		return this;
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param manager An optional {@link CoroutineEventManager} if you wish to set one
	 */
	public void build(@Nullable CoroutineEventManager manager) throws IOException {
		try {
			throw new UnsupportedOperationException();
		} catch (RuntimeException e) {
			LOGGER.error("An error occurred while creating the framework, aborted");

			throw e;
		} catch (Throwable e) {
			LOGGER.error("An error occurred while creating the framework, aborted");

			throw new RuntimeException(e);
		}
	}

	public void build() throws IOException {
		build(null);
	}

	public BContext getContext() {
		return context;
	}
}