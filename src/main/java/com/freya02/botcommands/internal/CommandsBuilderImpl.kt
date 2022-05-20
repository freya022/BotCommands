package com.freya02.botcommands.internal

import com.freya02.botcommands.annotations.api.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.annotations.api.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.DefaultMessages
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommandManager
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.waiter.EventWaiter
import com.freya02.botcommands.internal.application.ApplicationCommandListener
import com.freya02.botcommands.internal.application.ApplicationCommandsBuilder
import com.freya02.botcommands.internal.application.ApplicationUpdaterListener
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlersBuilder
import com.freya02.botcommands.internal.components.ComponentsBuilder
import com.freya02.botcommands.internal.modals.ModalHandlersBuilder
import com.freya02.botcommands.internal.prefixed.CommandListener
import com.freya02.botcommands.internal.prefixed.HelpCommand
import com.freya02.botcommands.internal.prefixed.PrefixedCommandsBuilder
import com.freya02.botcommands.internal.utils.ClassInstancer
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.function.Function
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

class CommandsBuilderImpl(context: BContextImpl, classes: Set<Class<*>>, slashGuildIds: List<Long>) {
    private val prefixedCommandsBuilder: PrefixedCommandsBuilder

    //	private final EventListenersBuilder eventListenersBuilder; //TODO event listener
    private val applicationCommandsBuilder: ApplicationCommandsBuilder
    private val autocompletionHandlersBuilder: AutocompletionHandlersBuilder
    private val modalHandlersBuilder: ModalHandlersBuilder
    private val componentsBuilder: ComponentsBuilder
    private val context: BContextImpl
    private val classes: MutableList<KClass<*>>
    private val usePing: Boolean
    private val ignoredClasses: MutableList<KClass<*>> = ArrayList()

    init {
        if (classes.isEmpty()) LOGGER.warn("No classes have been found, make sure you have at least one search path")
        this.context = context
        this.prefixedCommandsBuilder = PrefixedCommandsBuilder(context)
        this.componentsBuilder = ComponentsBuilder(context)
        this.usePing = context.prefixes.isEmpty()
        if (usePing) LOGGER.info("No prefix has been set, using bot ping as prefix")

        this.classes = classes.filter {
            !(it.isAnonymousClass or it.isHidden or it.isSynthetic)
        } .map { it.kotlin }.toMutableList()

        this.applicationCommandsBuilder = ApplicationCommandsBuilder(context, slashGuildIds)

//		this.eventListenersBuilder = new EventListenersBuilder(context); //TODO event listener
        this.autocompletionHandlersBuilder = AutocompletionHandlersBuilder(context)
        this.modalHandlersBuilder = ModalHandlersBuilder(context)
    }

    @Throws(Exception::class)
    private fun buildClasses() {
        classes.removeIf { c ->
            try {
                return@removeIf !ReflectionUtilsKt.isInstantiable(c)
            } catch (e: Exception) {
                LOGGER.error("An error occurred while trying to find if a class is instantiable", e)
                throw RuntimeException("An error occurred while trying to find if a class is instantiable", e)
            }
        }

        for (aClass in classes) {
            processClass(aClass)
        }

        if (!context.isHelpDisabled) {
            check(context.findFirstCommand(CommandPath.of("help")) == null) { "Help command was detected before build, and default help is not disabled, consider disabling it with TextCommandsBuilder#disableHelpCommand" }

            processClass(HelpCommand::class)

            val helpInfo = context.findFirstCommand(CommandPath.of("help"))
                ?: throw IllegalStateException("HelpCommand did not build properly")

            val help = helpInfo.instance as HelpCommand
            help.generate()
        }

        prefixedCommandsBuilder.postProcess()

//        if (context.componentManager != null) { //TODO components
//            //Load button listeners
//            for (aClass in classes) {
//                componentsBuilder.processClass(aClass)
//            }
//        } else {
//            LOGGER.info("ComponentManager is not set, the Components API, paginators and menus won't be usable")
//        }

        applicationCommandsBuilder.postProcess()

        if (context.componentManager != null) {
            componentsBuilder.postProcess()
        }

//		eventListenersBuilder.postProcess(); //TODO event listener
//        autocompletionHandlersBuilder.postProcess() //TODO autocomplete
//        modalHandlersBuilder.postProcess() //TODO modals

        context.registrationListeners.forEach { it.onBuildComplete() }

        LOGGER.info("Finished registering all commands")
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class, InstantiationException::class)
    private fun processClass(aClass: KClass<*>) {
        if (!aClass.isAbstract && !aClass.java.isInterface) {
            var foundSomething = false

            //Search for methods annotated with a compatible annotation
            for (method in aClass.memberFunctions) {
                foundSomething = foundSomething or processMethod(aClass, method)
            }

            if (!foundSomething) {
                ignoredClasses.add(aClass)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(InvocationTargetException::class, InstantiationException::class, IllegalAccessException::class)
    private inline fun <reified T : Any> tryInstantiateMethod(
        requiredAnnotation: KClass<out Annotation>,
        requiredClassDesc: String,
        method: KFunction<*>
    ): T? {
        val declaringClass = method.javaMethod!!.declaringClass.kotlin

        if (method.findAnnotations(requiredAnnotation).isNotEmpty()) {
            requireUser(T::class.isSuperclassOf(declaringClass), method) {
                "Method is annotated with @${requiredAnnotation.java.simpleName} but its class does not extend ${T::class.simpleName}"
            }

            val annotatedInstance = ClassInstancer.instantiate(context, declaringClass) as T
            requireUser(method.isPublic, method) {
                "$requiredClassDesc is not public"
            }

            requireUser(!method.isStatic, method) {
                "$requiredClassDesc is static"
            }

            return annotatedInstance
        }

        return null
    }

    @Throws(InvocationTargetException::class, InstantiationException::class, IllegalAccessException::class)
    private fun processMethod(clazz: KClass<*>, method: KFunction<*>): Boolean { //TODO pass to method above
//        for (annotation in applicationMethodAnnotations) {
//            val applicationCommand =
//                tryInstantiateMethod<ApplicationCommand>(annotation, "Application command", method)
//            if (applicationCommand != null) {
//                applicationCommandsBuilder.processApplicationCommand(applicationCommand, method)
//                return true
//            }
//        }
//
//        val textCommand = tryInstantiateMethod<TextCommand>(JDATextCommand::class, "Text command", method)
//        if (textCommand != null) {
//            prefixedCommandsBuilder.processPrefixedCommand(textCommand, method)
//            return true
//        }
//
//        val eventListener = tryInstantiateMethod<Any>(
//            JDAEventListener::class,
//            "JDA event listener",
//            method
//        ) //TODO event listener
//        if (eventListener != null) {
//            eventListenersBuilder.processEventListener(eventListener, method)
//            return true
//        }
//
//        val autocompletionHandler = tryInstantiateMethod<Any>(
//            AutocompletionHandler::class,
//            "Slash command auto completion",
//            method
//        )
//        if (autocompletionHandler != null) {
//            autocompletionHandlersBuilder.processHandler(autocompletionHandler, method)
//            return true
//        }
//
//        val modalHandler = tryInstantiateMethod<Any>(ModalHandler::class, "Modal handler", method)
//        if (modalHandler != null) {
//            modalHandlersBuilder.processHandler(modalHandler, method)
//            return true
//        }

        if (method.hasAnnotation<Declaration>()) {
            val args = ApplicationCommandManager(context)
            method.call(ClassInstancer.instantiate(context, clazz), context, args) //TODO injection

            return true
        }

        return false
    }

    /**
     * Builds the command listener and automatically registers all listener to the JDA instance
     *
     * @param jda The JDA instance of your bot
     */
    @Throws(Exception::class)
    fun build(jda: JDA) {
        var jda = jda
        if (jda.shardInfo.shardId != 0) {
            LOGGER.warn("A shard other than 0 was passed to CommandsBuilder#build, shard 0 is needed to handle DMing exceptions, manually retrieving shard 0...")
            val manager = jda.shardManager
                ?: throw IllegalArgumentException("Unable to retrieve Shard 0 as shard manager is null")

            jda = manager.getShardById(0) ?: throwUser("Unable to retrieve Shard 0")
        }

        if (jda.status != JDA.Status.CONNECTED) {
            try {
                LOGGER.warn("JDA should already be ready when you call #build on CommandsBuilder !")
                jda.awaitReady()
            } catch (e: InterruptedException) {
                throw RuntimeException("CommandsBuilder got interrupted while waiting for JDA to be ready", e)
            }
        }

        val intents = listOf(
            GatewayIntent.GUILD_MESSAGES
        )

        check(jda.gatewayIntents.containsAll(intents)) {
            "JDA must have these intents enabled: ${intents.joinToString { it.name }}"
        }

        setupContext(jda)

        ReflectionUtilsKt.scanAnnotations(classes)

        buildClasses()

        context.addEventListeners(
            EventWaiter(jda),
            CommandListener(context),
            ApplicationUpdaterListener(context),
            ApplicationCommandListener(context)
        )

        if (ignoredClasses.isNotEmpty()) {
            LOGGER.trace("Ignored classes in search paths:")
            for (ignoredClass in ignoredClasses) {
                LOGGER.trace("\t{}", ignoredClass.simpleName)
            }
        }

        ConflictDetector.detectConflicts()
    }

    private fun setupContext(jda: JDA?) {
        context.setJDA(jda)

        if (usePing) {
            context.addPrefix("<@" + jda!!.selfUser.id + "> ")
            context.addPrefix("<@!" + jda.selfUser.id + "> ")
        }

        context.registerConstructorParameter(BContext::class.java) { context } //TODO maybe unify this stuff ?
        context.registerCommandDependency(BContext::class.java) { context }
        context.registerCustomResolver(BContext::class.java) { _, _, _ -> context }

        context.registerConstructorParameter(JDA::class.java) { jda }
        context.registerCommandDependency(JDA::class.java) { jda }
        context.registerCustomResolver(JDA::class.java) { _, _, _ -> jda }

        context.setDefaultMessageProvider(DefaultMessagesFunction())
    }

    private class DefaultMessagesFunction : Function<Locale, DefaultMessages> {
        private val localeDefaultMessagesMap: MutableMap<Locale, DefaultMessages> = HashMap()

        override fun apply(locale: Locale): DefaultMessages {
            return localeDefaultMessagesMap.computeIfAbsent(locale, ::DefaultMessages)
        }
    }

    companion object {
        private val LOGGER = Logging.getLogger()
        private val applicationMethodAnnotations = listOf(JDASlashCommand::class, JDAMessageCommand::class, JDAUserCommand::class)
    }
}