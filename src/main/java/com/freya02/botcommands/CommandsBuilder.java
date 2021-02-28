package com.freya02.botcommands;

import com.freya02.botcommands.annotation.*;
import com.freya02.botcommands.regex.CommandTransformer;
import com.freya02.botcommands.regex.MethodPattern;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
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
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CommandsBuilder {
	private List<String> prefixes;
	private final List<Long> ownerIds = new ArrayList<>();

	private String userPermErrorMsg = "You are not allowed to do this";
	private String botPermErrorMsg = "I don't have the required permissions to do this";
	private String ownerOnlyErrorMsg = "Only the owner can use this";

	private String userCooldownMsg = "You must wait **%.2f seconds**";
	private String channelCooldownMsg = "You must wait **%.2f seconds in this channel**";
	private String guildCooldownMsg = "You must wait **%.2f seconds in this guild**";

	private String commandNotFoundMsg = "Unknown command, maybe you meant: %s";
	private String commandDisabledMsg = "This command is disabled for the moment";
	private String roleOnlyErrorMsg = "You must have the role `%s` for this";

	private Supplier<EmbedBuilder> defaultEmbedFunction = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = InputStream::nullInputStream;
	private final List<String> disabledCommands = new ArrayList<>();

	private CommandsBuilder(@NotNull String prefix, long topOwnerId) {
		Utils.requireNonBlankString(prefix, "Prefix is null");
		this.prefixes = List.of(prefix);
		ownerIds.add(topOwnerId);
	}

	private CommandsBuilder(long topOwnerId) {
		this.prefixes = null;
		ownerIds.add(topOwnerId);
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

	/** <p>Sets the displayed message when the user does not have the command's specified role</p>
	 * <p><b>Requires one string format for the role name</b></p>
	 * <p><i>Default message : You must have the role `%s` for this</i></p>
	 * @param roleOnlyErrorMsg Message to display when the user does not have the command's specified role
	 * @return This builder
	 */
	public CommandsBuilder setRoleOnlyErrorMsg(@NotNull String roleOnlyErrorMsg) {
		this.roleOnlyErrorMsg = Utils.requireNonBlankString(roleOnlyErrorMsg, "Role only error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the command is on per-user cooldown</p>
	 * <p><b>Requires one string format for the per-user cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds**</i></p>
	 * @param userCooldownMsg Message to display when the command is on per-user cooldown
	 * @return This builder
	 */
	public CommandsBuilder setUserCooldownMsg(@NotNull String userCooldownMsg) {
		this.userCooldownMsg = Utils.requireNonBlankString(userCooldownMsg, "User cooldown error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the command is on per-channel cooldown</p>
	 * <p><b>Requires one string format for the per-channel cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds in this channel**</i></p>
	 * @param channelCooldownMsg Message to display when the command is on per-channel cooldown
	 * @return This builder
	 */
	public CommandsBuilder setChannelCooldownMsg(@NotNull String channelCooldownMsg) {
		this.channelCooldownMsg = Utils.requireNonBlankString(channelCooldownMsg, "Channel cooldown error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the command is on per-guild cooldown</p>
	 * <p><b>Requires one string format for the per-guild cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds in this guild**</i></p>
	 * @param guildCooldownMsg Message to display when the command is on per-guild cooldown
	 * @return This builder
	 */
	public CommandsBuilder setGuildCooldownMsg(@NotNull String guildCooldownMsg) {
		this.guildCooldownMsg = Utils.requireNonBlankString(guildCooldownMsg, "Guild cooldown error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the command is only usable by the owner</p>
	 * <p><i>Default message : Only the owner can use this</i></p>
	 * @param ownerOnlyErrorMsg Message to display when the command is only usable by the owner
	 * @return This builder
	 */
	public CommandsBuilder setOwnerOnlyErrorMsg(@NotNull String ownerOnlyErrorMsg) {
		this.ownerOnlyErrorMsg = Utils.requireNonBlankString(ownerOnlyErrorMsg, "Owner only error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the user does not have enough permissions</p>
	 * <p><i>Default message : You are not allowed to do this</i></p>
	 * @param userPermErrorMsg Message to display when the user does not have enough permissions
	 * @return This builder
	 */
	public CommandsBuilder setUserPermErrorMsg(@NotNull String userPermErrorMsg) {
		this.userPermErrorMsg = Utils.requireNonBlankString(userPermErrorMsg, "User permission error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the bot does not have enough permissions</p>
	 * <p><i>Default message : I don't have the required permissions to do this</i></p>
	 * @param botPermErrorMsg Message to display when the bot does not have enough permissions
	 * @return This builder
	 */
	public CommandsBuilder setBotPermErrorMsg(@NotNull String botPermErrorMsg) {
		this.botPermErrorMsg = Utils.requireNonBlankString(botPermErrorMsg, "Bot permission error message is null");
		return this;
	}

	/** <p>Sets the displayed message when the command is not found</p>
	 * <p><i>Default message : Unknown command, maybe you meant: %s</i></p>
	 * @param commandNotFoundMsg Message to display when the command is not found
	 * @return This builder
	 */
	public CommandsBuilder setCommandNotFoundMsg(@NotNull String commandNotFoundMsg) {
		Utils.requireNonBlankString(commandNotFoundMsg, "'Command not found' error message is null");
		if (!commandNotFoundMsg.contains("%s")) {
			throw new IllegalArgumentException("The 'Command not found' string must contain one %s formatter");
		}
		this.commandNotFoundMsg = commandNotFoundMsg;
		return this;
	}

	/** <p>Sets the displayed message when the command is disabled (via {@linkplain ConditionalUse})</p>
	 * <p><i>Default message : The command is disabled for the moment</i></p>
	 * @param commandDisabledMsg Message to display when the command is not found
	 * @return This builder
	 */
	public CommandsBuilder setCommandDisabledMsg(String commandDisabledMsg) {
		this.commandDisabledMsg = commandDisabledMsg;
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
		this.defaultEmbedFunction = Objects.requireNonNull(defaultEmbedFunction);
		this.defaultFooterIconSupplier = Objects.requireNonNull(defaultFooterIconSupplier);
		return this;
	}

	/**Adds owners, they can access the commands annotated with {@linkplain RequireOwner}
	 *
	 * @param ownerIds Owners Long IDs to add
	 * @return This builder
	 */
	public CommandsBuilder addOwners(long... ownerIds) {
		for (long ownerId : ownerIds) {
			this.ownerIds.add(ownerId);
		}

		return this;
	}

	private final List<String> failedClasses = new ArrayList<>();
	private Command getSubcommand(Class<? extends Command> clazz, Command parent) {
		if (!Modifier.isAbstract(clazz.getModifiers())) {
			try {
				boolean isInstantiable = isInstantiable(clazz);

				if (isInstantiable) {
					try {
						final Constructor<? extends Command> constructor = clazz.getDeclaredConstructor(parent.getClass());
						constructor.setAccessible(true);
						return constructor.newInstance(parent);
					} catch (NoSuchMethodException ignored) {
						final Constructor<? extends Command> constructor = clazz.getDeclaredConstructor();
						constructor.setAccessible(true);
						return constructor.newInstance();
					}
				} else {
					final String completeCmdName = getCommandName(parent.getClass()) + '.' + getCommandName(clazz);
					disabledCommands.add(completeCmdName);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
				failedClasses.add(" - " + clazz.getSimpleName());
			}
		}

		return null;
	}

	private ListenerAdapter buildClasses(List<Class<?>> classes) {
		final Map<String, CommandInfo> commandMap = new HashMap<>();
		for (Class<?> aClass : classes) {
			if (!Modifier.isAbstract(aClass.getModifiers()) && aClass.isAnnotationPresent(JdaCommand.class) && !aClass.isAnnotationPresent(JdaSubcommand.class) && Command.class.isAssignableFrom(aClass)) {
				try {
					boolean isInstantiable = isInstantiable(aClass);

					if (isInstantiable) {
						final Constructor<?> constructor = aClass.getDeclaredConstructor();
						constructor.setAccessible(true);
						final Command command = (Command) constructor.newInstance();

						CommandInfo info = processCommand(command);

						commandMap.put(info.getName(), info);

						for (String alias : info.getAliases()) {
							commandMap.put(alias, info);
						}
					} else {
						disabledCommands.add(getCommandName(aClass));
					}
				} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
					failedClasses.add(" - " + aClass.getSimpleName());
				}
			}
		}

		CommandInfo helpCommandInfo = processCommand(new HelpCommand(defaultEmbedFunction, commandMap, prefixes.get(0)));

		commandMap.put(helpCommandInfo.getName(), helpCommandInfo);

		System.out.println("Loaded " + commandMap.size() + " command");
		if (failedClasses.isEmpty()) {
			System.err.println("Finished registering all commands");
		} else {
			System.err.println("Finished registering command, but some failed");
			System.err.println(failedClasses.size() + " command(s) failed loading:\r\n" + String.join("\r\n", failedClasses));
		}

		return new CommandListener(prefixes, ownerIds, userPermErrorMsg, botPermErrorMsg, commandNotFoundMsg, commandDisabledMsg, ownerOnlyErrorMsg, roleOnlyErrorMsg, userCooldownMsg, channelCooldownMsg, guildCooldownMsg, defaultEmbedFunction, defaultFooterIconSupplier, commandMap, disabledCommands);
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

		if (prefixes == null) prefixes = List.of(jda.getSelfUser().getAsMention() + ' ', "<@!" + jda.getSelfUser().getId() + "> ");

		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return buildClasses(Utils.getClasses(IOUtils.getJarPath(callerClass), commandPackageName, 3));
	}

	/** Builds the command listener
	 *
	 * @param classStream Input stream of String(s), each line is a class name (package.classname)
	 * @return The ListenerAdapter
	 */
	public ListenerAdapter build(JDA jda, InputStream classStream) {
		if (prefixes == null) prefixes = List.of(jda.getSelfUser().getAsMention() + ' ', "<@!" + jda.getSelfUser().getId() + "> ");

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

	private static String getCommandName(Class<?> clazz) {
		if (clazz.isAnnotationPresent(JdaCommand.class)) {
			return clazz.getAnnotation(JdaCommand.class).name();
		} else if (clazz.isAnnotationPresent(JdaSubcommand.class)) {
			return clazz.getAnnotation(JdaSubcommand.class).name();
		}

		return null;
	}

	private CommandInfo processCommand(Command cmd) {
		final Class<? extends Command> commandClass = cmd.getClass();

		if (commandClass.isAnnotationPresent(JdaCommand.class) || commandClass.isAnnotationPresent(JdaSubcommand.class)) {
			boolean isHidden = commandClass.isAnnotationPresent(Hidden.class);
			boolean isOwnerOnly = commandClass.isAnnotationPresent(RequireOwner.class);
			boolean addSubcommandHelp = commandClass.isAnnotationPresent(AddSubcommandHelp.class);
			boolean addExecutableHelp = commandClass.isAnnotationPresent(AddExecutableHelp.class);

			String name;
			String[] aliases = null;
			String description;
			String category = null;

			Permission[] userPermissions;
			Permission[] botPermissions;

			String requiredRole;

			int cooldown;
			CooldownScope cooldownScope;

			List<CommandInfo> subcommandInfo = new ArrayList<>();
			if (commandClass.isAnnotationPresent(JdaCommand.class)) {
				final JdaCommand commandAnnot = commandClass.getAnnotation(JdaCommand.class);
				category = commandAnnot.category();

				if (commandAnnot.name().contains(" "))
					throw new IllegalArgumentException("Command name cannot have spaces in '" + commandAnnot.name() + "'");

				name = commandAnnot.name();
				aliases = commandAnnot.aliases();
				description = commandAnnot.description();

				userPermissions = commandAnnot.userPermissions();
				botPermissions = commandAnnot.botPermissions();

				requiredRole = commandAnnot.requiredRole();

				cooldown = commandAnnot.cooldown();
				cooldownScope = commandAnnot.cooldownScope();

				for (Class<? extends Command> subcommandClazz : commandAnnot.subcommands()) {
					final Command subcommand = getSubcommand(subcommandClazz, cmd);

					if (subcommand != null) {
						subcommandInfo.add(processCommand(subcommand));
					}
				}
			} else {
				final JdaSubcommand commandAnnot = commandClass.getAnnotation(JdaSubcommand.class);

				if (commandAnnot.name().contains(" "))
					throw new IllegalArgumentException("Command name cannot have spaces in '" + commandAnnot.name() + "'");

				name = commandAnnot.name();
				description = commandAnnot.description();

				userPermissions = commandAnnot.userPermissions();
				botPermissions = commandAnnot.botPermissions();

				requiredRole = commandAnnot.requiredRole();

				cooldown = commandAnnot.cooldown();
				cooldownScope = commandAnnot.cooldownScope();
			}

			final List<MethodPattern> methodPatterns = CommandTransformer.getMethodPatterns(cmd);

			return new CommandInfo(cmd, name, aliases, description, category, isHidden, isOwnerOnly, userPermissions, botPermissions, requiredRole, cooldown, cooldownScope, subcommandInfo, addSubcommandHelp, addExecutableHelp, methodPatterns);
		}

		throw new IllegalArgumentException("Command does not have JdaCommand annotation");
	}
}