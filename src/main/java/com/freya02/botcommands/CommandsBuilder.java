package com.freya02.botcommands;

import com.freya02.botcommands.annotation.ConditionalUse;
import com.freya02.botcommands.annotation.JdaCommand;
import com.freya02.botcommands.annotation.RequireOwner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CommandsBuilder {
	private final BContextImpl context = new BContextImpl();

	private CommandsBuilder(@NotNull String prefix, long topOwnerId) {
		Utils.requireNonBlankString(prefix, "Prefix is null");
		context.setPrefixes(List.of(prefix));
		context.addOwner(topOwnerId);
	}

	private CommandsBuilder(long topOwnerId) {
		context.addOwner(topOwnerId);
	}

	/**Constructs a new instance of {@linkplain CommandsBuilder} with ping-as-prefix enabled
	 * @param topOwnerId The most owner of the bot
	 */
	public static CommandsBuilder withPing(long topOwnerId) {
		return new CommandsBuilder(topOwnerId);
	}

	/**Constructs a new instance of {@linkplain CommandsBuilder}
	 * @param prefix Prefix of the bot
	 * @param topOwnerId The most owner of the bot
	 */
	public static CommandsBuilder withPrefix(@NotNull String prefix, long topOwnerId) {
		return new CommandsBuilder(prefix, topOwnerId);
	}

	/** <p>Sets the displayed message when the command is on per-user cooldown</p>
	 * <p><b>Requires one string format for the per-user cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds**</i></p>
	 * @param userCooldownMsg Message to display when the command is on per-user cooldown
	 * @return This builder
	 */
	public CommandsBuilder setUserCooldownMsg(@NotNull String userCooldownMsg) {
		context.getDefaultMessages().setUserCooldownMsg(userCooldownMsg);
		return this;
	}

	/** <p>Sets the displayed message when the command is on per-channel cooldown</p>
	 * <p><b>Requires one string format for the per-channel cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds in this channel**</i></p>
	 * @param channelCooldownMsg Message to display when the command is on per-channel cooldown
	 * @return This builder
	 */
	public CommandsBuilder setChannelCooldownMsg(@NotNull String channelCooldownMsg) {
		context.getDefaultMessages().setChannelCooldownMsg(channelCooldownMsg);
		return this;
	}

	/** <p>Sets the displayed message when the command is on per-guild cooldown</p>
	 * <p><b>Requires one string format for the per-guild cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds in this guild**</i></p>
	 * @param guildCooldownMsg Message to display when the command is on per-guild cooldown
	 * @return This builder
	 */
	public CommandsBuilder setGuildCooldownMsg(@NotNull String guildCooldownMsg) {
		context.getDefaultMessages().setGuildCooldownMsg(guildCooldownMsg);
		return this;
	}

	/** <p>Sets the displayed message when the command is only usable by the owner</p>
	 * <p><i>Default message : Only the owner can use this</i></p>
	 * @param ownerOnlyErrorMsg Message to display when the command is only usable by the owner
	 * @return This builder
	 */
	public CommandsBuilder setOwnerOnlyErrorMsg(@NotNull String ownerOnlyErrorMsg) {
		context.getDefaultMessages().setOwnerOnlyErrorMsg(ownerOnlyErrorMsg);
		return this;
	}

	/** <p>Sets the displayed message when the user does not have enough permissions</p>
	 * <p><i>Default message : You are not allowed to do this</i></p>
	 * @param userPermErrorMsg Message to display when the user does not have enough permissions
	 * @return This builder
	 */
	public CommandsBuilder setUserPermErrorMsg(@NotNull String userPermErrorMsg) {
		context.getDefaultMessages().setUserPermErrorMsg(userPermErrorMsg);
		return this;
	}

	/** <p>Sets the displayed message when the bot does not have enough permissions</p>
	 * <p><i>Default message : I don't have the required permissions to do this</i></p>
	 * @param botPermErrorMsg Message to display when the bot does not have enough permissions
	 * @return This builder
	 */
	public CommandsBuilder setBotPermErrorMsg(@NotNull String botPermErrorMsg) {
		context.getDefaultMessages().setBotPermErrorMsg(botPermErrorMsg);
		return this;
	}

	/** <p>Sets the displayed message when the command is not found</p>
	 * <p><i>Default message : Unknown command, maybe you meant: %s</i></p>
	 * @param commandNotFoundMsg Message to display when the command is not found
	 * @return This builder
	 */
	public CommandsBuilder setCommandNotFoundMsg(@NotNull String commandNotFoundMsg) {
		context.getDefaultMessages().setCommandNotFoundMsg(commandNotFoundMsg);
		return this;
	}

	/** <p>Sets the embed builder and the footer icon that this library will use as base embed builder</p>
	 * <p><b>Note : The icon name when used will be "icon.jpg", your icon must be a JPG file and be the same name</b></p>
	 *
	 * @param defaultEmbedFunction The default embed builder
	 * @param defaultFooterIconSupplier The default icon for the footer
	 * @return This builder
	 */
	public CommandsBuilder setDefaultEmbedFunction(@NotNull Supplier<EmbedBuilder> defaultEmbedFunction, @NotNull Supplier<InputStream> defaultFooterIconSupplier) {
		this.context.setDefaultEmbedSupplier(defaultEmbedFunction);
		this.context.setDefaultFooterIconSupplier(defaultFooterIconSupplier);
		return this;
	}

	/**Adds owners, they can access the commands annotated with {@linkplain RequireOwner}
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

	private final List<String> failedClasses = new ArrayList<>();
	private Command getSubcommand(Class<? extends Command> clazz, Command parent) {
		if (!Modifier.isAbstract(clazz.getModifiers())) {
			try {
				boolean isInstantiable = isInstantiable(clazz);

				if (isInstantiable) {
					if (Modifier.isStatic(clazz.getModifiers())) { //Static inner class doesn't need declaring class's instance
						final Constructor<? extends Command> constructor = clazz.getDeclaredConstructor(BContext.class);
						constructor.setAccessible(true);
						return constructor.newInstance(context);
					} else {
						final Constructor<? extends Command> constructor = clazz.getDeclaredConstructor(parent.getClass(), BContext.class);
						constructor.setAccessible(true);
						return constructor.newInstance(parent, context);
					}
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
				failedClasses.add(" - " + clazz.getSimpleName());
			}
		}

		return null;
	}

	private ListenerAdapter buildClasses(List<Class<?>> classes) {
		for (Class<?> aClass : classes) {
			processClass(aClass);
		}
		processClass(HelpCommand.class);

		final HelpCommand help = (HelpCommand) context.findCommand("help");
		if (help == null) throw new IllegalStateException("HelpCommand did not build properly");
		help.generate();

		System.out.println("Loaded " + context.getCommands().size() + " command");
		if (failedClasses.isEmpty()) {
			System.err.println("Finished registering all commands");
		} else {
			System.err.println("Finished registering command, but some failed");
			System.err.println(failedClasses.size() + " command(s) failed loading:\r\n" + String.join("\r\n", failedClasses));
		}

		return new CommandListener(context);
	}

	private void processClass(Class<?> aClass) {
		if (!Modifier.isAbstract(aClass.getModifiers()) && aClass.isAnnotationPresent(JdaCommand.class) && Command.class.isAssignableFrom(aClass)
				&& aClass.getDeclaringClass() == null) { //Declaring class returns null for anonymous classes, we only need to check if the class is not an inner class
			try {
				boolean isInstantiable = isInstantiable(aClass);

				if (isInstantiable) {
					final Constructor<?> constructor = aClass.getDeclaredConstructor(BContext.class);
					constructor.setAccessible(true);
					final Command command = (Command) constructor.newInstance(context);

					context.addCommand(command.getInfo().getName(), command.getInfo().getAliases(), command);

					for (Class<? extends Command> subcommandClazz : aClass.getAnnotation(JdaCommand.class).subcommands()) {
						final Command subcommand = getSubcommand(subcommandClazz, command);

						if (subcommand != null) {
							command.getInfo().addSubcommand(subcommand);
						}
					}
				}
			} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
				failedClasses.add(" - " + aClass.getSimpleName());
			}
		}
	}

	private boolean isInstantiable(Class<?> aClass) throws IllegalAccessException, InvocationTargetException {
		boolean canInstantiate = true;
		for (Method declaredMethod : aClass.getDeclaredMethods()) {
			if (declaredMethod.isAnnotationPresent(ConditionalUse.class)) {
				if (Modifier.isStatic(declaredMethod.getModifiers())) {
					if (declaredMethod.getParameterCount() == 0 && declaredMethod.getReturnType() == boolean.class) {
						declaredMethod.setAccessible(true);
						canInstantiate = (boolean) declaredMethod.invoke(null);
					} else {
						System.err.println("WARN: method " + aClass.getName() + '#' + declaredMethod.getName() + " is annotated @ConditionalUse but does not have the correct signature (return boolean, no parameters)");
					}
				} else {
					System.err.println("WARN: method " + aClass.getName() + '#' + declaredMethod.getName() + " is annotated @ConditionalUse but is not static");
				}
				break;
			}
		}
		return canInstantiate;
	}

	/** Builds the command listener
	 * @param commandPackageName The package name where all the commands are, ex: com.freya02.commands
	 * @return The ListenerAdapter
	 * @throws IOException If an exception occurs when reading the jar path or getting classes
	 */
	public ListenerAdapter build(JDA jda, @NotNull String commandPackageName) throws IOException {
		Utils.requireNonBlankString(commandPackageName, "Command package name is null");

		setupContext(jda);

		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return buildClasses(Utils.getClasses(IOUtils.getJarPath(callerClass), commandPackageName, 3));
	}

	private void setupContext(JDA jda) {
		context.setJda(jda);
		if (context.getPrefixes().isEmpty()) {
			context.setPrefixes(List.of("<@" + jda.getSelfUser().getId() + "> ", "<@!" + jda.getSelfUser().getId() + "> "));
		}
	}

	public BContext getContext() {
		return context;
	}

	/** Builds the command listener
	 *
	 * @param classStream Input stream of String(s), each line is a class name (package.classname)
	 * @return The ListenerAdapter
	 */
	public ListenerAdapter build(JDA jda, InputStream classStream) {
		setupContext(jda);

		final BufferedReader stream = new BufferedReader(new InputStreamReader(classStream));
		final List<Class<?>> classes = stream.lines().map(s -> {
			try {
				return Class.forName(s);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			return null;
		}).collect(Collectors.toList());

		return buildClasses(classes);
	}
}