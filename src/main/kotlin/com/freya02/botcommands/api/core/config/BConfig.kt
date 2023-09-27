package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.apply
import com.freya02.botcommands.api.commands.annotations.RequireOwner
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.service.ClassGraphProcessor
import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.service.putServiceAs
import com.freya02.botcommands.api.core.utils.enumSetOf
import com.freya02.botcommands.api.core.utils.toImmutableList
import com.freya02.botcommands.api.core.utils.toImmutableSet
import com.freya02.botcommands.api.core.waiter.EventWaiter
import com.freya02.botcommands.internal.core.config.ConfigDSL
import kotlinx.coroutines.debug.DebugProbes
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.requests.GatewayIntent

@InjectedService
interface BConfig {
    val ownerIds: Set<Long>

    val packages: Set<String>
    val classes: Set<Class<*>>

    /**
     * Disables sending exceptions to the bot owners
     *
     * Default: `false`
     */
    val disableExceptionsInDMs: Boolean
    /**
     * Disables autocomplete caching, unless [CacheAutocomplete.forceCache] is set to `true`.
     *
     * This could be useful when testing methods that use autocomplete caching, while using hotswap.
     *
     * Default: `false`
     */
    val disableAutocompleteCache: Boolean

    /**
     * Whether transactions should trigger a coroutine dump & thread dump
     * when running longer than the [max transaction duration][ConnectionSupplier.getMaxTransactionDuration]
     *
     * **Note:** you need to [install the debug probes][DebugProbes.install] in order to dump coroutine debug info,
     * do not forget to turn off [DebugProbes.enableCreationStackTraces] in production environments.
     *
     * @see ConnectionSupplier.getMaxTransactionDuration
     * @see DebugProbes
     * @see DebugProbes.enableCreationStackTraces
     */
    val dumpLongTransactions: Boolean
    /**
     * Determines whether the SQL queries should be logged
     *
     * **SQL queries will be logged on TRACE**
     *
     * Default: `true`
     */
    val logQueries: Boolean
    /**
     * Determines if the SQL query logger will replace query parameters by their value.
     *
     * Default: `true`
     */
    val logQueryParameters: Boolean

    /**
     * Gateway intents to ignore when checking for [event listeners][BEventListener] intents.
     *
     * @see BEventListener.ignoreIntents
     */
    val ignoredIntents: Set<GatewayIntent>

    /**
     * Events for which the [event waiter][EventWaiter] must ignore intent requirements.
     *
     * Either way, the event would still be being listened to, but a warning would have been logged.
     */
    val ignoredEventIntents: Set<Class<out Event>>

    val classGraphProcessors: List<ClassGraphProcessor>

    val debugConfig: BDebugConfig
    val serviceConfig: BServiceConfig
    val textConfig: BTextConfig
    val applicationConfig: BApplicationConfig
    val componentsConfig: BComponentsConfig
    val coroutineScopesConfig: BCoroutineScopesConfig

    fun isOwner(id: Long): Boolean = id in ownerIds
}

@ConfigDSL
class BConfigBuilder internal constructor() : BConfig {
    override val packages: MutableSet<String> = HashSet()
    override val classes: MutableSet<Class<*>> = HashSet()

    override val ownerIds: MutableSet<Long> = HashSet()

    override var disableExceptionsInDMs = false
    override var disableAutocompleteCache = false

    @set:DevConfig
    override var dumpLongTransactions: Boolean = false
    override var logQueries: Boolean = true
    override var logQueryParameters: Boolean = true

    override val ignoredIntents: MutableSet<GatewayIntent> = enumSetOf()

    override val ignoredEventIntents: MutableSet<Class<out Event>> = hashSetOf()

    override val classGraphProcessors: MutableList<ClassGraphProcessor> = arrayListOf()

    override val debugConfig = BDebugConfigBuilder()
    override val serviceConfig = BServiceConfigBuilder()
    override val textConfig = BTextConfigBuilder()
    override val applicationConfig = BApplicationConfigBuilder(serviceConfig)
    override val componentsConfig = BComponentsConfigBuilder()
    override val coroutineScopesConfig = BCoroutineScopesConfigBuilder()

    /**
     * Adds owners, they can access the commands annotated with [RequireOwner] as well as bypass cooldowns.
     *
     * @param ownerIds Owners Long IDs to add
     */
    fun addOwners(vararg ownerIds: Long) = addOwners(ownerIds.asList())

    /**
     * Adds owners, they can access the commands annotated with [RequireOwner] as well as bypass cooldowns.
     *
     * @param ownerIds Owners Long IDs to add
     */
    fun addOwners(ownerIds: Collection<Long>) {
        this.ownerIds += ownerIds
    }

    //TODO take javadoc from master
    /**
     * Adds the commands of this packages in this builder, all classes with valid annotations will be added<br></br>
     * **You can have up to 2 nested sub-folders in the specified package**, this means you can have your package structure like this:
     *
     * ```
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
     * |
     * |__moderation
     * |
     * |__Ban.java
     * Mute.java
     * ...
     * ```
     *
     * @param commandPackageName The package name where all the commands are, ex: com.freya02.bot.commands
     * @return This builder for chaining convenience
     */
    fun addSearchPath(commandPackageName: String) {
        packages.add(commandPackageName)
    }

    fun addClass(clazz: Class<*>) {
        classes.add(clazz)
    }

    @JvmSynthetic
    inline fun <reified T> addClass() {
        addClass(T::class.java)
    }

    fun services(block: ReceiverConsumer<BServiceConfigBuilder>) {
        serviceConfig.apply(block)
    }

    fun coroutineScopes(block: ReceiverConsumer<BCoroutineScopesConfigBuilder>) {
        coroutineScopesConfig.apply(block)
    }

    fun debug(block: ReceiverConsumer<BDebugConfigBuilder>) {
        debugConfig.apply(block)
    }

    fun textCommands(block: ReceiverConsumer<BTextConfigBuilder>) {
        textConfig.apply(block)
    }

    fun applicationCommands(block: ReceiverConsumer<BApplicationConfigBuilder>) {
        applicationConfig.apply(block)
    }

    fun components(block: ReceiverConsumer<BComponentsConfigBuilder>) {
        componentsConfig.apply(block)
    }

    @JvmSynthetic
    internal fun build() = object : BConfig {
        override val ownerIds = this@BConfigBuilder.ownerIds.toImmutableSet()
        override val packages = this@BConfigBuilder.packages.toImmutableSet()
        override val classes = this@BConfigBuilder.classes.toImmutableSet()
        override val disableExceptionsInDMs = this@BConfigBuilder.disableExceptionsInDMs
        override val disableAutocompleteCache = this@BConfigBuilder.disableAutocompleteCache
        override val dumpLongTransactions = this@BConfigBuilder.dumpLongTransactions
        override val logQueries = this@BConfigBuilder.logQueries
        override val logQueryParameters = this@BConfigBuilder.logQueryParameters
        override val ignoredIntents = this@BConfigBuilder.ignoredIntents.toImmutableSet()
        override val ignoredEventIntents = this@BConfigBuilder.ignoredEventIntents.toImmutableSet()
        override val classGraphProcessors = this@BConfigBuilder.classGraphProcessors.toImmutableList()
        override val debugConfig = this@BConfigBuilder.debugConfig.build()
        override val serviceConfig = this@BConfigBuilder.serviceConfig.build()
        override val textConfig = this@BConfigBuilder.textConfig.build()
        override val applicationConfig = this@BConfigBuilder.applicationConfig.build()
        override val componentsConfig = this@BConfigBuilder.componentsConfig.build()
        override val coroutineScopesConfig = this@BConfigBuilder.coroutineScopesConfig.build()
    }
}

@JvmSynthetic
internal fun BConfig.putConfigInServices(serviceContainer: ServiceContainer) {
    serviceContainer.putServiceAs(this)
    serviceContainer.putServiceAs(serviceConfig)
    serviceContainer.putServiceAs(applicationConfig)
    serviceContainer.putServiceAs(componentsConfig)
    serviceContainer.putServiceAs(coroutineScopesConfig)
    serviceContainer.putServiceAs(debugConfig)
    serviceContainer.putServiceAs(textConfig)
}
