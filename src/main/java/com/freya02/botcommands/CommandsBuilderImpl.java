package com.freya02.botcommands;

import com.freya02.botcommands.application.ApplicationCommand;
import com.freya02.botcommands.application.ApplicationCommandListener;
import com.freya02.botcommands.application.ApplicationCommandsBuilder;
import com.freya02.botcommands.application.ApplicationUpdaterListener;
import com.freya02.botcommands.application.context.annotations.JdaMessageCommand;
import com.freya02.botcommands.application.context.annotations.JdaUserCommand;
import com.freya02.botcommands.application.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.components.internal.ComponentsBuilder;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.ClassInstancer;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.CommandListener;
import com.freya02.botcommands.prefixed.HelpCommand;
import com.freya02.botcommands.prefixed.PrefixedCommandsBuilder;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import com.freya02.botcommands.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

final class CommandsBuilderImpl {
	private static final Logger LOGGER = Logging.getLogger();
	private static final List<Class<? extends Annotation>> methodAnnotations = List.of(JdaSlashCommand.class, JdaMessageCommand.class, JdaUserCommand.class);

	private final PrefixedCommandsBuilder prefixedCommandsBuilder;
	private final ApplicationCommandsBuilder applicationCommandsBuilder;

	private final ComponentsBuilder componentsBuilder;

	private final BContextImpl context;
	private final Set<Class<?>> classes;

	private final boolean usePing;
	
	private final List<Class<?>> ignoredClasses = new ArrayList<>();

	CommandsBuilderImpl(BContextImpl context, List<Long> slashGuildIds, Set<Class<?>> classes) {
		if (classes.isEmpty())
			LOGGER.warn("No classes have been found, make sure you have at least one search path");

		this.context = context;
		this.prefixedCommandsBuilder = new PrefixedCommandsBuilder(context);
		this.componentsBuilder = new ComponentsBuilder(context);
		
		this.usePing = context.getPrefixes().isEmpty();
		this.classes = classes;
		this.applicationCommandsBuilder = new ApplicationCommandsBuilder(context, slashGuildIds);
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

			if (!context.isHelpDisabled()) {
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
		} catch (RuntimeException e) {
			LOGGER.error("An error occurred while loading the commands, the commands will not work");

			throw e;
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
			boolean foundSomething = false;
			
			//If not a text command, search for methods annotated with a compatible annotation
			for (Method method : aClass.getDeclaredMethods()) {
				for (Class<? extends Annotation> annotation : methodAnnotations) {
					if (method.isAnnotationPresent(annotation)) {
						if (!ApplicationCommand.class.isAssignableFrom(aClass))
							throw new IllegalArgumentException("Method " + Utils.formatMethodShort(method) + " is annotated with @" + annotation.getSimpleName() + " but it's class does not extend ApplicationCommand");
						
						final ApplicationCommand annotatedInstance = (ApplicationCommand) ClassInstancer.instantiate(context, aClass);
						
						applicationCommandsBuilder.processApplicationCommand(annotatedInstance, method);
						
						foundSomething = true;
						
						break;
					}
				}
			}

			if (!foundSomething) {
				ignoredClasses.add(aClass);
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

		Utils.scanOptionals(classes);

		buildClasses();

		context.addEventListeners(
				new EventWaiter(jda),
				new CommandListener(context),
				new ApplicationUpdaterListener(context),
				new ApplicationCommandListener(context)
		);

		if (!ignoredClasses.isEmpty()) {
			LOGGER.trace("Ignored classes in search paths:");
			for (Class<?> ignoredClass : ignoredClasses) {
				LOGGER.trace("\t{}", ignoredClass.getName());
			}
		}
	}

	private void setupContext(JDA jda) {
		context.setJDA(jda);
		if (usePing) {
			context.addPrefix("<@" + jda.getSelfUser().getId() + "> ");
			context.addPrefix("<@!" + jda.getSelfUser().getId() + "> ");
		}

		context.registerConstructorParameter(BContext.class, ignored -> context);
		context.registerCommandDependency(BContext.class, () -> context);
		context.registerCustomResolver(BContext.class, ignored -> context);

		context.registerConstructorParameter(JDA.class, ignored -> jda);
		context.registerCommandDependency(JDA.class, () -> jda);
		context.registerCustomResolver(JDA.class, ignored -> jda);

		context.setDefaultMessageProvider(guild -> new DefaultMessages(context.getSettingsProvider() != null ? context.getSettingsProvider().getLocale(guild) : Locale.getDefault()));
	}
}