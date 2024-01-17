package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.commands.text.annotations.Hidden
import io.github.freya022.botcommands.api.commands.text.annotations.RequireOwner
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.putServiceAs
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.toImmutableSet
import io.github.freya022.botcommands.api.core.waiter.EventWaiter
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.messages.MessageCreateData

@InjectedService
interface BConfig {
    /**
     * User IDs of the bot owners, allowing bypassing cooldowns, user permission checks,
     * and having [hidden commands][Hidden] shown.
     */
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
     * This could be useful when testing methods that use autocomplete caching while using hotswap.
     *
     * Default: `false`
     */
    val disableAutocompleteCache: Boolean

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
    val databaseConfig: BDatabaseConfig
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

    @set:JvmName("disableExceptionsInDMs")
    override var disableExceptionsInDMs = false
    @set:DevConfig
    @set:JvmName("disableAutocompleteCache")
    override var disableAutocompleteCache = false

    override val ignoredIntents: MutableSet<GatewayIntent> = enumSetOf()

    override val ignoredEventIntents: MutableSet<Class<out Event>> = hashSetOf()

    override val classGraphProcessors: MutableList<ClassGraphProcessor> = arrayListOf()

    override val debugConfig = BDebugConfigBuilder()
    override val serviceConfig = BServiceConfigBuilder()
    override val databaseConfig = BDatabaseConfigBuilder()
    override val textConfig = BTextConfigBuilder()
    override val applicationConfig = BApplicationConfigBuilder(serviceConfig)
    override val componentsConfig = BComponentsConfigBuilder()
    @get:JvmSynthetic
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

    /**
     * Adds this package for class discovery.
     * All services, commands, handlers, listeners, etc... will be read from these packages.
     *
     * **Tip:**: you can have your package structure such as:
     *
     * ```text
     * commands/
     * ├─ common/
     * │  ├─ fun/
     * │  │  ├─ CommonFish.java
     * │  │  ├─ CommonMeme.java
     * │  ├─ moderation/
     * │  │  ├─ CommonBan.java
     * ├─ slash/
     * │  ├─ fun/
     * │  │  ├─ SlashFish.java
     * │  │  ├─ SlashMeme.java
     * │  ├─ moderation/
     * │  │  ├─ SlashBan.java
     * ├─ text/
     * │  ├─ fun/
     * │  │  ├─ TextFish.java
     * │  │  ├─ TextMeme.java
     * │  ├─ moderation/
     * │  │  ├─ TextBan.java
     * ```
     *
     * The `common` package would have code that works for both the text and the slash commands,
     * such as the methods that take the event's data (the command caller, guild, channel, parameters... instead of the event itself),
     * and then return a [MessageCreateData] that lets you generate the message output, without actually knowing how to send the reply.
     *
     * This is only beneficial if you plan on having the same logic
     * for multiple input types (text / slash commands, for example).
     *
     * @param commandPackageName The package name such as `io.github.freya022.bot.commands`
     *
     * @see addClass
     */
    fun addSearchPath(commandPackageName: String) {
        packages.add(commandPackageName)
    }

    fun addClass(clazz: Class<*>) {
        classes.add(clazz)
    }

    @JvmSynthetic
    inline fun <reified T : Any> addClass() {
        addClass(T::class.java)
    }

    fun services(block: ReceiverConsumer<BServiceConfigBuilder>) {
        serviceConfig.apply(block)
    }

    @JvmSynthetic
    fun coroutineScopes(block: ReceiverConsumer<BCoroutineScopesConfigBuilder>) {
        coroutineScopesConfig.apply(block)
    }

    fun database(block: ReceiverConsumer<BDatabaseConfigBuilder>) {
        databaseConfig.apply(block)
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
        override val ignoredIntents = this@BConfigBuilder.ignoredIntents.toImmutableSet()
        override val ignoredEventIntents = this@BConfigBuilder.ignoredEventIntents.toImmutableSet()
        override val classGraphProcessors = this@BConfigBuilder.classGraphProcessors.toImmutableList()
        override val debugConfig = this@BConfigBuilder.debugConfig.build()
        override val serviceConfig = this@BConfigBuilder.serviceConfig.build()
        override val databaseConfig = this@BConfigBuilder.databaseConfig.build()
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
    serviceContainer.putServiceAs(databaseConfig)
    serviceContainer.putServiceAs(applicationConfig)
    serviceContainer.putServiceAs(componentsConfig)
    serviceContainer.putServiceAs(coroutineScopesConfig)
    serviceContainer.putServiceAs(debugConfig)
    serviceContainer.putServiceAs(textConfig)
}
