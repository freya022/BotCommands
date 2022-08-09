package com.freya02.botcommands.internal

import com.freya02.botcommands.api.*
import com.freya02.botcommands.api.application.ApplicationCommandFilter
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer
import com.freya02.botcommands.api.components.ComponentInteractionFilter
import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.api.parameters.CustomResolverFunction
import com.freya02.botcommands.api.parameters.ParameterResolvers
import com.freya02.botcommands.api.parameters.ParameterType.Companion.ofClass
import com.freya02.botcommands.api.prefixed.HelpConsumer
import com.freya02.botcommands.api.prefixed.TextCommandFilter
import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.EventDispatcher
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.ApplicationCommandsCache
import com.freya02.botcommands.internal.application.ApplicationCommandsContextImpl
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo
import com.freya02.botcommands.internal.prefixed.TextCommandCandidates
import com.freya02.botcommands.internal.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.prefixed.TextSubcommandCandidates
import dev.minn.jda.ktx.events.CoroutineEventManager
import gnu.trove.TCollections
import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.ErrorResponse
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KType

class BContextImpl(val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    internal val classPathContainer: ClassPathContainer
    val serviceContainer: ServiceContainer
    val eventDispatcher: EventDispatcher

    private val parameterSupplierMap: MutableMap<Class<*>, ConstructorParameterSupplier<*>> = hashMapOf()
    private val instanceSupplierMap: MutableMap<Class<*>, InstanceSupplier<*>> = hashMapOf()
    val dynamicInstanceSuppliers: MutableList<DynamicInstanceSupplier> = arrayListOf()
    private val classToObjMap: MutableMap<KClass<*>, Any> = hashMapOf()
    private val commandDependencyMap: MutableMap<Class<*>, Supplier<*>> = hashMapOf()
    private val methodParameterSupplierMap: MutableMap<Class<*>, MethodParameterSupplier<*>> = hashMapOf()
    private val registrationListeners: MutableList<RegistrationListener> = arrayListOf()

    private var nextExceptionDispatch: Long = 0

    private val ownerIds: MutableList<Long> = arrayListOf()
    private var uncaughtExceptionHandler: ExceptionHandler? = null
    private var defaultMessageProvider: Function<DiscordLocale, DefaultMessages>

    val localizationManager = LocalizationManager()

    private val prefixes: MutableList<String> = arrayListOf()
    private var defaultEmbedSupplier: Supplier<EmbedBuilder> = Supplier { EmbedBuilder() }
    private var defaultFooterIconSupplier = Supplier<InputStream> { null }
    private var helpConsumer: HelpConsumer? = null
    private var helpBuilderConsumer: Consumer<EmbedBuilder>? = null
    private val textCommandMap: MutableMap<CommandPath, TextCommandCandidates> = hashMapOf()
    private val textSubcommandsMap: MutableMap<CommandPath, TextSubcommandCandidates> = hashMapOf()
    internal val textFilters: MutableList<TextCommandFilter> = arrayListOf()

    private val applicationCommandsContext = ApplicationCommandsContextImpl(this)
    var isOnlineAppCommandCheckEnabled = false
        private set
    internal val applicationFilters: MutableList<ApplicationCommandFilter> = arrayListOf()
    private val autocompletionTransformers: MutableMap<KType, AutocompletionTransformer<*>> = hashMapOf()
    @Deprecated("To be removed") var applicationCommandsCache: ApplicationCommandsCache? = null

    internal val componentFilters: MutableList<ComponentInteractionFilter> = arrayListOf()

    private val autocompleteHandlersMap: MutableMap<String, AutocompletionHandlerInfo> = hashMapOf()
    private val testGuildIds = TCollections.synchronizedSet(TLongHashSet())

    init {
        defaultMessageProvider = DefaultMessagesFunction()
        classPathContainer = ClassPathContainer(this)
        serviceContainer = ServiceContainer(this) //Puts itself, ctx, cem and cpc
        eventDispatcher = EventDispatcher(this) //Service put in ctor
        serviceContainer.preloadServices()
    }

    override fun <T : Any> getService(clazz: KClass<T>): T {
        return serviceContainer.getService(clazz)
    }

    override fun <T : Any> getService(clazz: Class<T>): T {
        return serviceContainer.getService(clazz)
    }

    override fun getJDA(): JDA {
        return serviceContainer.getService(JDA::class)
    }

    override fun getPrefixes(): List<String> {
        return prefixes
    }

    override fun addPrefix(prefix: String) {
        prefixes.add(prefix)
    }

    override fun getOwnerIds(): List<Long> {
        return ownerIds
    }

    override fun getDefaultMessages(locale: DiscordLocale): DefaultMessages {
        return defaultMessageProvider.apply(locale)
    }

    fun setDefaultMessageProvider(defaultMessageProvider: Function<DiscordLocale, DefaultMessages>) {
        this.defaultMessageProvider = defaultMessageProvider
    }

    override fun findFirstCommand(path: CommandPath): TextCommandInfo? {
        val candidates = textCommandMap[path] ?: return null
        return candidates.findFirst()
    }

    override fun findCommands(path: CommandPath): TextCommandCandidates? {
        return textCommandMap[path]
    }

    override fun findFirstTextSubcommands(path: CommandPath): TextCommandCandidates? {
        return textSubcommandsMap[path]?.firstOrNull()
    }

    override fun findTextSubcommands(path: CommandPath): List<TextCommandCandidates>? {
        return textSubcommandsMap[path]
    }

    override fun getApplicationCommandsContext(): ApplicationCommandsContextImpl {
        return applicationCommandsContext
    }

    override fun getDefaultEmbedSupplier(): Supplier<EmbedBuilder> {
        return defaultEmbedSupplier
    }

    fun setDefaultEmbedSupplier(defaultEmbedSupplier: Supplier<EmbedBuilder>) {
        this.defaultEmbedSupplier = Objects.requireNonNull(defaultEmbedSupplier, "Default embed supplier cannot be null")
    }

    override fun getDefaultFooterIconSupplier(): Supplier<InputStream> {
        return defaultFooterIconSupplier
    }

    fun addOwner(ownerId: Long) {
        ownerIds.add(ownerId)
    }

    fun setDefaultFooterIconSupplier(defaultFooterIconSupplier: Supplier<InputStream>) {
        this.defaultFooterIconSupplier = Objects.requireNonNull(defaultFooterIconSupplier, "Default footer icon supplier cannot be null")
    }

    fun addTextCommand(commandInfo: TextCommandInfo) {
        val path = commandInfo.path
        val aliases = commandInfo.aliases
        textCommandMap.compute(path) { _: CommandPath, v: TextCommandCandidates? ->
            when {
                v != null -> v.also { it.add(commandInfo) }
                else -> TextCommandCandidates(commandInfo)
            }
        }
        val parentPath = path.parent
        if (parentPath != null) { //Add subcommands to cache
            // If subcommands candidates exist, append, if not then create
            textSubcommandsMap.compute(parentPath) { _: CommandPath, candidates: TextSubcommandCandidates? ->
                when (candidates) {
                    null -> TextSubcommandCandidates(commandInfo)
                    else -> candidates.addSubcommand(commandInfo)
                }
            }
        }
        for (alias in aliases) { //TODO replace by a loop in the annotation processor
            textCommandMap.compute(alias) { _: CommandPath, v: TextCommandCandidates? ->
                when {
                    v != null -> v.also { it.add(commandInfo) }
                    else -> TextCommandCandidates(commandInfo)
                }
            }
        }
    }

    fun getAutocompletionHandler(autocompletionHandlerName: String): AutocompletionHandlerInfo? {
        return autocompleteHandlersMap[autocompletionHandlerName]
    }

    override fun invalidateAutocompletionCache(autocompletionHandlerName: String) {
        val handler = getAutocompletionHandler(autocompletionHandlerName)
            ?: throwUser("Autocompletion handler name not found for '$autocompletionHandlerName'")
        handler.invalidate()
    }

    val commands: Collection<TextCommandCandidates>
        get() = Collections.unmodifiableCollection(textCommandMap.values)

    val applicationCommandsView: Collection<ApplicationCommandInfo>
        get() = getApplicationCommandsContext()
            .mutableApplicationCommandMap
            .allApplicationCommands

    override fun dispatchException(message: String, t: Throwable?) {
        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)

            val exceptionStr = if (t == null) "" else "\nException : \n%s".format(t.getDeepestCause())

            jda.retrieveApplicationInfo()
                .map { obj: ApplicationInfo -> obj.owner }
                .flatMap { obj: User -> obj.openPrivateChannel() }
                .flatMap { channel: PrivateChannel ->
                    channel.sendMessage("$message$exceptionStr\n\nPlease check the logs for more detail and possible exceptions")
                }
                .queue(
                    null,
                    ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) { LOGGER.warn("Could not send exception DM to owner") }
                )
        }
    }

    override fun addTextFilter(filter: TextCommandFilter) {
        textFilters.add(filter)
    }

    override fun addApplicationFilter(filter: ApplicationCommandFilter) {
        applicationFilters.add(filter)
    }

    override fun addComponentFilter(filter: ComponentInteractionFilter) {
        componentFilters.add(filter)
    }

    override fun removeTextFilter(filter: TextCommandFilter) {
        textFilters.remove(filter)
    }

    override fun removeApplicationFilter(filter: ApplicationCommandFilter) {
        applicationFilters.remove(filter)
    }

    override fun removeComponentFilter(filter: ComponentInteractionFilter) {
        componentFilters.remove(filter)
    }

    override fun overrideHelp(helpConsumer: HelpConsumer) {
        this.helpConsumer = helpConsumer
    }

    override fun getHelpConsumer(): HelpConsumer {
        return helpConsumer!!
    }

    override fun getRegistrationListeners(): List<RegistrationListener> {
        return Collections.unmodifiableList(registrationListeners)
    }

    override fun addRegistrationListeners(vararg listeners: RegistrationListener) {
        registrationListeners += listeners
    }

    override fun getComponentManager(): ComponentManager {
        return serviceContainer.getService(config.componentManagerStrategy)
    }

    fun getClassInstance(clazz: KClass<*>): Any? {
        return classToObjMap[clazz]
    }

    fun putClassInstance(clazz: KClass<*>, obj: Any) {
        classToObjMap[clazz] = obj
    }

    fun addEventListeners(vararg listeners: Any) {
        if (jda.shardManager != null) {
            jda.shardManager!!.addEventListener(*listeners)
        } else {
            jda.addEventListener(*listeners)
        }
    }

    override fun getSettingsProvider(): SettingsProvider? {
        TODO()
//        return config.settingsProvider
    }

    fun setHelpBuilderConsumer(builderConsumer: Consumer<EmbedBuilder>?) {
        helpBuilderConsumer = builderConsumer
    }

    override fun getHelpBuilderConsumer(): Consumer<EmbedBuilder> {
        return helpBuilderConsumer!!
    }

    override fun <T> registerCustomResolver(parameterType: Class<T>, function: CustomResolverFunction<T>) {
        ParameterResolvers.register(CustomResolver(parameterType, function))
    }

    fun <T> registerConstructorParameter(parameterType: Class<T>, parameterSupplier: ConstructorParameterSupplier<T>) {
        parameterSupplierMap[parameterType] = parameterSupplier
    }

    fun <T> registerInstanceSupplier(classType: Class<T>, instanceSupplier: InstanceSupplier<T>) {
        instanceSupplierMap[classType] = instanceSupplier
    }

    fun registerDynamicInstanceSupplier(dynamicInstanceSupplier: DynamicInstanceSupplier) {
        dynamicInstanceSuppliers.add(dynamicInstanceSupplier)
    }

    fun getParameterSupplier(parameterType: Class<*>): ConstructorParameterSupplier<*> {
        return parameterSupplierMap[parameterType]!!
    }

    fun getInstanceSupplier(classType: Class<*>): InstanceSupplier<*> {
        return instanceSupplierMap[classType]!!
    }

    fun <T> registerCommandDependency(fieldType: Class<T>, supplier: Supplier<T>) {
        commandDependencyMap[fieldType] = supplier
    }

    fun getCommandDependency(fieldType: Class<*>): Supplier<*> {
        return commandDependencyMap[fieldType]!!
    }

    fun <T> registerMethodParameterSupplier(parameterType: Class<T>, supplier: MethodParameterSupplier<T>) {
        methodParameterSupplierMap[parameterType] = supplier
    }

    fun getMethodParameterSupplier(parameterType: Class<*>): MethodParameterSupplier<*> {
        return methodParameterSupplierMap[parameterType]!!
    }

    val isHelpDisabled: Boolean
        get() = helpConsumer != null

    fun setUncaughtExceptionHandler(exceptionHandler: ExceptionHandler?) {
        uncaughtExceptionHandler = exceptionHandler
    }

    override fun getUncaughtExceptionHandler(): ExceptionHandler? {
        return uncaughtExceptionHandler
    }

    override fun getTestGuildIds(): TLongSet {
        return testGuildIds
    }

    fun addTestGuildIds(vararg ids: Long) {
        testGuildIds.addAll(ids)
    }

    fun getAutocompletionTransformer(type: KType): AutocompletionTransformer<*> {
        return autocompletionTransformers[type]!!
    }

    //TODO use KType
    fun <T> registerAutocompletionTransformer(type: Class<T>?, autocompletionTransformer: AutocompletionTransformer<T>) {
        autocompletionTransformers[ofClass(type!!).type] = autocompletionTransformer
    }

    fun enableOnlineAppCommandCheck() {
        isOnlineAppCommandCheckEnabled = true
    }

    companion object {
        private val LOGGER = Logging.getLogger()
    }
}