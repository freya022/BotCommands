package com.freya02.botcommands;

import com.freya02.botcommands.annotation.Hidden;
import com.freya02.botcommands.annotation.JdaCommand;
import com.freya02.botcommands.annotation.JdaSubcommand;
import com.freya02.botcommands.annotation.RequireOwner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CommandsBuilder {
	private final String prefix;
	private final List<Long> ownerIds = new ArrayList<>();

	private String userPermErrorMsg = "You are not allowed to do this";
	private String botPermErrorMsg = "I don't have the required permissions to do this";
	private String ownerOnlyErrorMsg = "Only the owner can use this";

	private String userCooldownMsg = "You must wait **%.2f seconds**";
	private String channelCooldownMsg = "You must wait **%.2f seconds in this channel**";
	private String guildCooldownMsg = "You must wait **%.2f seconds in this guild**";

	private String commandNotFoundMsg = "Unknown command";
	private String roleOnlyErrorMsg = "You must have the role `%s` for this";

	private boolean usePingAsPrefix;

	private Supplier<EmbedBuilder> defaultEmbedFunction = EmbedBuilder::new;
	private Supplier<InputStream> defaultFooterIconSupplier = InputStream::nullInputStream;

	public CommandsBuilder setUsePingAsPrefix(boolean usePingAsPrefix) {
		this.usePingAsPrefix = usePingAsPrefix;
		return this;
	}

	public CommandsBuilder(String prefix, long topOwnerId) {
		this.prefix = prefix;
		ownerIds.add(topOwnerId);
	}

	public CommandsBuilder setRoleOnlyErrorMsg(String roleOnlyErrorMsg) {
		this.roleOnlyErrorMsg = roleOnlyErrorMsg;
		return this;
	}

	public CommandsBuilder setUserCooldownMsg(String userCooldownMsg) {
		this.userCooldownMsg = userCooldownMsg;
		return this;
	}

	public CommandsBuilder setChannelCooldownMsg(String channelCooldownMsg) {
		this.channelCooldownMsg = channelCooldownMsg;
		return this;
	}

	public CommandsBuilder setGuildCooldownMsg(String guildCooldownMsg) {
		this.guildCooldownMsg = guildCooldownMsg;
		return this;
	}

	public CommandsBuilder setOwnerOnlyErrorMsg(String ownerOnlyErrorMsg) {
		this.ownerOnlyErrorMsg = ownerOnlyErrorMsg;
		return this;
	}

	public CommandsBuilder setUserPermErrorMsg(String userPermErrorMsg) {
		this.userPermErrorMsg = userPermErrorMsg;
		return this;
	}

	public CommandsBuilder setBotPermErrorMsg(String botPermErrorMsg) {
		this.botPermErrorMsg = botPermErrorMsg;
		return this;
	}

	public CommandsBuilder setCommandNotFoundMsg(String commandNotFoundMsg) {
		this.commandNotFoundMsg = commandNotFoundMsg;
		return this;
	}

	/** <p>Sets the embed builder and the footer icon that this library will use as base embed builder</p>
	 * <p><b>Note : The icon name when used will be "icon.jpg", your icon must be a JPG file and be the same name</b></p>
	 *
	 * @param defaultEmbedFunction The default embed builder
	 * @param defaultFooterIconSupplier The default icon for the footer
	 * @return This instance
	 */
	public CommandsBuilder setDefaultEmbedFunction(Supplier<EmbedBuilder> defaultEmbedFunction, Supplier<InputStream> defaultFooterIconSupplier) {
		this.defaultEmbedFunction = Objects.requireNonNull(defaultEmbedFunction);
		this.defaultFooterIconSupplier = Objects.requireNonNull(defaultFooterIconSupplier);
		return this;
	}

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
				return clazz.getDeclaredConstructor(parent.getClass()).newInstance(parent);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
				failedClasses.add(" - " + clazz.getSimpleName());
			}
		}

		return null;
	}

	private ListenerAdapter buildClasses(List<Class<?>> classes) {
		final TreeMap<String, CommandInfo> commandMap = new TreeMap<>();
		for (Class<?> aClass : classes) {
			if (!Modifier.isAbstract(aClass.getModifiers()) && aClass.isAnnotationPresent(JdaCommand.class) && !aClass.isAnnotationPresent(JdaSubcommand.class) && Command.class.isAssignableFrom(aClass)) {
				try {
					final Command command = (Command) aClass.getDeclaredConstructors()[0].newInstance();

					CommandInfo info = processCommand(command);

					commandMap.put(info.getName(), info);

					for (String alias : info.getAliases()) {
						commandMap.put(alias, info);
					}
				} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
					failedClasses.add(" - " + aClass.getSimpleName());
				}
			}
		}

		CommandInfo helpCommandInfo = processCommand(new HelpCommand(defaultEmbedFunction, commandMap));

		commandMap.put(helpCommandInfo.getName(), helpCommandInfo);

		System.out.println("Loaded " + commandMap.size() + " command");
		if (failedClasses.isEmpty()) {
			System.err.println("Finished registering all commands");
		} else {
			System.err.println("Finished registering command, but some failed");
			System.err.println(failedClasses.size() + " command(s) failed loading:\r\n" + String.join("\r\n", failedClasses));
		}

		return new CommandListener(prefix, ownerIds, userPermErrorMsg, botPermErrorMsg, commandNotFoundMsg, ownerOnlyErrorMsg, roleOnlyErrorMsg, userCooldownMsg, channelCooldownMsg, guildCooldownMsg, usePingAsPrefix, defaultEmbedFunction, defaultFooterIconSupplier, commandMap);
	}

	public ListenerAdapter build(String commandPackageName) throws IOException {
		if (!usePingAsPrefix && (prefix == null || prefix.isBlank())) {
			throw new IllegalArgumentException("You must either use ping as prefix or a prefix");
		}

		final Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return buildClasses(Utils.getClasses(IOUtils.getJarPath(callerClass), commandPackageName, 3));
	}

	public ListenerAdapter build(InputStream classStream) {
		if (!usePingAsPrefix && (prefix == null || prefix.isBlank())) {
			throw new IllegalArgumentException("You must either use ping as prefix or a prefix");
		}

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

	@SuppressWarnings("DuplicatedCode")
	private CommandInfo processCommand(Command cmd) {
		final Class<? extends Command> commandClass = cmd.getClass();

		if (commandClass.isAnnotationPresent(JdaCommand.class) || commandClass.isAnnotationPresent(JdaSubcommand.class)) {
			boolean isHidden = false;
			boolean isOwnerOnly = false;

			for (Annotation annotation : commandClass.getAnnotations()) {
				if (annotation instanceof Hidden) {
					isHidden = true;
				} else if (annotation instanceof RequireOwner) {
					isOwnerOnly = true;
				}
			}

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

			return new CommandInfo(cmd, name, aliases, description, category, isHidden, isOwnerOnly, userPermissions, botPermissions, requiredRole, cooldown, cooldownScope, subcommandInfo);
		}

		throw new IllegalArgumentException("Command does not have JdaCommand annotation");
	}
}