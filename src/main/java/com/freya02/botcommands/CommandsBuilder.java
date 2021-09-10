package com.freya02.botcommands;

import com.freya02.botcommands.annotation.RequireOwner;
import com.freya02.botcommands.application.ApplicationCommand;
import com.freya02.botcommands.builder.ExtensionsBuilder;
import com.freya02.botcommands.builder.TextCommandsBuilder;
import com.freya02.botcommands.components.ComponentManager;
import com.freya02.botcommands.components.DefaultComponentManager;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.utils.IOUtils;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.prefixed.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CommandsBuilder {
	private final List<Long> slashGuildIds = new ArrayList<>();

	private final BContextImpl context = new BContextImpl();

	private final Set<Class<?>> classes = new HashSet<>();

	private final TextCommandsBuilder textCommandBuilder = new TextCommandsBuilder(context);
	private final ExtensionsBuilder extensionsBuilder = new ExtensionsBuilder(context);

	private CommandsBuilder(long topOwnerId) {
		context.addOwner(topOwnerId);
	}

	/**
	 * Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled by default
	 *
	 * @param topOwnerId The most owner of the bot
	 */
	public static CommandsBuilder newBuilder(long topOwnerId) {
		return new CommandsBuilder(topOwnerId);
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
	 * Allows to change the framework's default messages
	 *
	 * @param provider Function which gives a {@link DefaultMessages} instance for the supplied {@link Guild}
	 *                 <br>The Guild supplied <b>can be null</b> (for example in global application commands used in DMs)
	 * @return This builder for chaining convenience
	 */
	public CommandsBuilder setDefaultMessagesProvider(@NotNull Function<Guild, DefaultMessages> provider) {
		context.setDefaultMessageProvider(provider);

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
	 * @throws IllegalArgumentException If the class is not a {@link Command} nor a {@link ApplicationCommand}
	 */
	public CommandsBuilder registerCommand(Class<?> clazz) {
		if (!Command.class.isAssignableFrom(clazz) && !ApplicationCommand.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("You can't register a class that's not a Command or a SlashCommand, provided: " + clazz.getName());
		}

		classes.add(clazz);

		return this;
	}

	/**
	 * Adds the commands of this packages in this builder, all the classes which extends {@link Command} or {@link ApplicationCommand} will be registered<br>
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

	public CommandsBuilder extensionsBuilder(Consumer<ExtensionsBuilder> consumer) {
		consumer.accept(extensionsBuilder);

		return this;
	}
	
	public CommandsBuilder textCommandBuilder(Consumer<TextCommandsBuilder> consumer) {
		consumer.accept(textCommandBuilder);
		
		return this;
	}

	//skip can be inlined by 2 but inlining would conflict with the above overload and also remove 1 stack frame, reintroducing the parameter need
	@SuppressWarnings("SameParameterValue")
	private void addSearchPath(String commandPackageName, int skip) throws IOException {
		Utils.requireNonBlank(commandPackageName, "Command package");

		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(skip).findFirst().orElseThrow().getDeclaringClass());
		classes.addAll(Utils.getClasses(IOUtils.getJarPath(callerClass), commandPackageName, 3));
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda                The JDA instance of your bot
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.commands
	 * @throws IOException If an exception occurs when reading the jar path or getting classes
	 * @see #addSearchPath(String)
	 */
	@Blocking
	public void build(JDA jda, @NotNull String commandPackageName) throws IOException {
		addSearchPath(commandPackageName, 2);
		
		new CommandsBuilderImpl(context, slashGuildIds, classes).build(jda);
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda The JDA instance of your bot
	 */
	@Blocking
	public void build(JDA jda) {
		new CommandsBuilderImpl(context, slashGuildIds, classes).build(jda);
	}

	public BContext getContext() {
		return context;
	}
}