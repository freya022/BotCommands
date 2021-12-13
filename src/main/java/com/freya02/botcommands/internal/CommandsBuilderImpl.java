package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.DefaultMessages;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.RegistrationListener;
import com.freya02.botcommands.api.annotations.JDAEventListener;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.slash.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.waiter.EventWaiter;
import com.freya02.botcommands.internal.application.ApplicationCommandListener;
import com.freya02.botcommands.internal.application.ApplicationCommandsBuilder;
import com.freya02.botcommands.internal.application.ApplicationUpdaterListener;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlersBuilder;
import com.freya02.botcommands.internal.components.ComponentsBuilder;
import com.freya02.botcommands.internal.events.EventListenersBuilder;
import com.freya02.botcommands.internal.prefixed.CommandListener;
import com.freya02.botcommands.internal.prefixed.HelpCommand;
import com.freya02.botcommands.internal.prefixed.PrefixedCommandsBuilder;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.utils.ClassInstancer;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CommandsBuilderImpl {
	private static final Logger LOGGER = Logging.getLogger();
	private static final List<Class<? extends Annotation>> applicationMethodAnnotations = List.of(JDASlashCommand.class, JDAMessageCommand.class, JDAUserCommand.class);

	private final PrefixedCommandsBuilder prefixedCommandsBuilder;
	private final ApplicationCommandsBuilder applicationCommandsBuilder;
	private final EventListenersBuilder eventListenersBuilder;
	private final AutocompletionHandlersBuilder autocompletionHandlersBuilder;

	private final ComponentsBuilder componentsBuilder;

	private final BContextImpl context;
	private final Set<Class<?>> classes;

	private final boolean usePing;
	
	private final List<Class<?>> ignoredClasses = new ArrayList<>();

	public CommandsBuilderImpl(BContextImpl context, List<Long> slashGuildIds, Set<Class<?>> classes) {
		if (classes.isEmpty())
			LOGGER.warn("No classes have been found, make sure you have at least one search path");

		this.context = context;
		this.prefixedCommandsBuilder = new PrefixedCommandsBuilder(context);
		this.componentsBuilder = new ComponentsBuilder(context);
		
		this.usePing = context.getPrefixes().isEmpty();
		if (usePing) LOGGER.info("No prefix has been set, using bot ping as prefix");

		this.classes = classes;
		this.applicationCommandsBuilder = new ApplicationCommandsBuilder(context, slashGuildIds);

		this.eventListenersBuilder = new EventListenersBuilder(context);
		this.autocompletionHandlersBuilder = new AutocompletionHandlersBuilder(context);
	}

	private void buildClasses() throws Exception {
		classes.removeIf(c -> {
			try {
				return !ReflectionUtils.isInstantiable(c);
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

			final TextCommandInfo helpInfo = context.findFirstCommand(CommandPath.of("help"));
			if (helpInfo == null) throw new IllegalStateException("HelpCommand did not build properly");

			final HelpCommand help = (HelpCommand) helpInfo.getInstance();
			help.generate();
		}

		prefixedCommandsBuilder.postProcess();

		if (context.getComponentManager() != null) {
			//Load button listeners
			for (Class<?> aClass : classes) {
				componentsBuilder.processClass(aClass);
			}
		} else {
			LOGGER.info("ComponentManager is not set, the Components API, paginators and menus won't be usable");
		}

		applicationCommandsBuilder.postProcess();

		if (context.getComponentManager() != null) {
			componentsBuilder.postProcess();
		}

		eventListenersBuilder.postProcess();

		autocompletionHandlersBuilder.postProcess();

		context.getRegistrationListeners().forEach(RegistrationListener::onBuildComplete);

		LOGGER.info("Finished registering all commands");
	}

	private void processClass(Class<?> aClass) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		if (!Modifier.isAbstract(aClass.getModifiers()) && !Modifier.isInterface(aClass.getModifiers())) {
			boolean foundSomething = false;

			//Search for methods annotated with a compatible annotation
			for (Method method : aClass.getDeclaredMethods()) {
				foundSomething |= processMethod(method);
			}

			if (!foundSomething) {
				ignoredClasses.add(aClass);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private <T> T tryInstantiateMethod(@NotNull Class<? extends Annotation> requiredAnnotation,
	                                   @NotNull Class<T> requiredClass,
	                                   @NotNull String requiredClassDesc,
	                                   @NotNull Method method) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		final Class<?> declaringClass = method.getDeclaringClass();

		if (method.isAnnotationPresent(requiredAnnotation)) {
			if (!requiredClass.isAssignableFrom(declaringClass))
				throw new IllegalArgumentException("Method " + Utils.formatMethodShort(method) + " is annotated with @" + requiredAnnotation.getSimpleName() + " but its class does not extend " + requiredClass.getSimpleName());

			final T annotatedInstance = (T) ClassInstancer.instantiate(context, declaringClass);

			if (!method.canAccess(annotatedInstance))
				throw new IllegalStateException(requiredClassDesc + " " + Utils.formatMethodShort(method) + " is not public");

			if (Modifier.isStatic(method.getModifiers()))
				throw new IllegalStateException(requiredClassDesc + " " + Utils.formatMethodShort(method) + " is static");

			return annotatedInstance;
		}

		return null;
	}

	private boolean processMethod(Method method) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		for (Class<? extends Annotation> annotation : applicationMethodAnnotations) {
			final ApplicationCommand applicationCommand = tryInstantiateMethod(annotation, ApplicationCommand.class, "Application command", method);

			if (applicationCommand != null) {
				applicationCommandsBuilder.processApplicationCommand(applicationCommand, method);

				return true;
			}
		}

		final TextCommand textCommand = tryInstantiateMethod(JDATextCommand.class, TextCommand.class, "Text command", method);
		if (textCommand != null) {
			prefixedCommandsBuilder.processPrefixedCommand(textCommand, method);

			return true;
		}

		final Object eventListener = tryInstantiateMethod(JDAEventListener.class, Object.class, "JDA event listener", method);
		if (eventListener != null) {
			eventListenersBuilder.processEventListener(eventListener, method);

			return true;
		}

		final Object autocompletionHandler = tryInstantiateMethod(AutocompletionHandler.class, Object.class, "Slash command auto completion", method);
		if (autocompletionHandler != null) {
			autocompletionHandlersBuilder.processHandler(autocompletionHandler, method);

			return true;
		}

		return false;
	}

	/**
	 * Builds the command listener and automatically registers all listener to the JDA instance
	 *
	 * @param jda The JDA instance of your bot
	 */
	public void build(JDA jda) throws Exception {
		if (jda.getShardInfo().getShardId() != 0) {
			LOGGER.warn("A shard other than 0 was passed to CommandsBuilder#build, shard 0 is needed to handle DMing exceptions, manually retrieving shard 0...");

			final ShardManager manager = jda.getShardManager();
			if (manager == null) throw new IllegalArgumentException("Unable to retrieve Shard 0 as shard manager is null");

			jda = manager.getShardById(0);

			if (jda == null) throw new IllegalArgumentException("Unable to retrieve Shard 0");
		}

		if (jda.getStatus() != JDA.Status.CONNECTED) {
			try {
				LOGGER.warn("JDA should already be ready when you call #build on CommandsBuilder !");

				jda.awaitReady();
			} catch (InterruptedException e) {
				throw new RuntimeException("CommandsBuilder got interrupted while waiting for JDA to be ready", e);
			}
		}

		final List<GatewayIntent> intents = List.of(
				GatewayIntent.GUILD_MESSAGES
		);
		if (!jda.getGatewayIntents().containsAll(intents)) {
			throw new IllegalStateException("JDA must have these intents enabled: " + intents.stream().map(Enum::name).collect(Collectors.joining(", ")));
		}

		setupContext(jda);

		ReflectionUtils.scanAnnotations(classes);

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

		ConflictDetector.detectConflicts();
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

		context.setDefaultMessageProvider(new Function<>() {
			private final Map<Locale, DefaultMessages> localeDefaultMessagesMap = new HashMap<>();

			@Override
			public DefaultMessages apply(Guild guild) {
				final Locale effectiveLocale = context.getEffectiveLocale(guild);

				return localeDefaultMessagesMap.computeIfAbsent(effectiveLocale, DefaultMessages::new);
			}
		});
	}
}