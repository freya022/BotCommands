package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.ExperimentalCoreApi
import io.github.freya022.botcommands.api.core.requests.PriorityGlobalRestRateLimiter
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.toImmutableSet
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestRateLimiter
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.intellij.lang.annotations.Language
import kotlin.reflect.KClass

@InjectedService
interface BConfig : IConfig, BConfigProps {

    override val configType get() = BConfig::class.java

    val eventManagerConfig: BEventManagerConfig
    val serviceConfig: BServiceConfig
    val coroutineScopesConfig: BCoroutineScopesConfig

    /**
     * An immutable collection of all [registered][BConfigBuilder.registerModule] configuration objects.
     */
    val configs: Collection<IConfig>

    /**
     * Returns the configuration object of the provided type, or `null` if none were [registered][BConfigBuilder.registerModule].
     *
     * @param type The type of configuration to get.
     */
    fun <T : IConfig> getConfigOrNull(type: Class<T>): T?
}

interface BConfigProps {

    /**
     * Predefined user IDs of the bot owners, allowing bypassing cooldowns, user permission checks,
     * and having hidden text commands shown.
     *
     * If not set, the application owners will be used, with roles "Developer" and above.
     *
     * **Note:** Prefer using [BotOwners] to get the effective bot owners, regardless of if this property is set or not.
     *
     * Spring property: `botcommands.core.predefinedOwnerIds`
     */
    @get:ConfigurationValue(
        path = "botcommands.core.predefinedOwnerIds",
        description = "Predefined user IDs of the bot owners, allowing bypassing cooldowns, user permission checks, and having [Hidden] commands shown. See the documentation for more details.",
    )
    val predefinedOwnerIds: Set<Long>

    /**
     * The packages the framework will scan through for services, commands, handlers...
     *
     * Spring property: `botcommands.core.packages`
     */
    @get:ConfigurationValue(
        path = "botcommands.core.packages",
        description = "The packages the framework will scan through for services, commands, handlers...",
    )
    val packages: Set<String>

    /**
     * Additional classes the framework will scan through for services, commands, handlers...
     *
     * Spring property: `botcommands.core.classes`
     */
    @get:ConfigurationValue(
        path = "botcommands.core.classes",
        description = "Additional classes the framework will scan through for services, commands, handlers...",
        type = "java.util.Set<java.lang.Class<?>>",
    )
    val classes: Set<Class<*>>

    /**
     * Instructs the classpath scanner to use a predefined list of library classes.
     * This speeds up startup, but may log a few false positive exceptions,
     * which do not affect the functionality of your application.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.core.usePreprocessedLibClassList`
     */
    @get:ConfigurationValue(
        path = "botcommands.core.usePreprocessedLibClassList",
        description = "Instructs the classpath scanner to use a predefined list of library classes. This speeds up startup, but may log a few false positive exceptions, which do not affect the functionality of your application.",
        defaultValue = "false",
    )
    val usePreprocessedLibClassList: Boolean

    /**
     * Disables sending exceptions to the bot owners.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.core.disableExceptionsInDMs`
     */
    @get:ConfigurationValue(
        path = "botcommands.core.disableExceptionsInDMs",
        description = "Disables sending exceptions to the bot owners.",
        defaultValue = "false",
    )
    val disableExceptionsInDMs: Boolean

    /**
     * Enables *bot* owners to bypass certain limits.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.core.enableOwnerBypass`
     *
     * @see BotOwners
     */
    @get:ConfigurationValue(
        path = "botcommands.core.enableOwnerBypass",
        description = "Enables *bot* owners to bypass certain limits.",
        defaultValue = "false",
    )
    val enableOwnerBypass: Boolean

    /**
     * Gateway intents to ignore when checking for required intents of [event listeners][BEventListener].
     *
     * Spring property: `botcommands.core.ignoredIntents`
     *
     * @see BEventListener.ignoreIntents
     */
    @get:ConfigurationValue(
        path = "botcommands.core.ignoredIntents",
        description = "Gateway intents to ignore when checking for required intents of event listeners.",
    )
    val ignoredIntents: Set<GatewayIntent>

    /**
     * Suppresses warnings about the default [RestRateLimiter] being used for large bots.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.core.ignoreRestRateLimiter`
     *
     * @see PriorityGlobalRestRateLimiter
     */
    @get:ConfigurationValue(
        path = "botcommands.core.ignoreRestRateLimiter",
        description = "Suppresses warnings about the default [RestRateLimiter] being used for large bots.",
        defaultValue = "false",
    )
    val ignoreRestRateLimiter: Boolean

    /**
     * Whether to use a [shutdown hook][Runtime.addShutdownHook] to call [BContext.shutdownNow] when the JVM is exiting **gracefully**.
     *
     * If disabled you'll have to shut it down yourself.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.core.enableShutdownHook`
     */
    @get:ConfigurationValue(
        path = "botcommands.core.enableShutdownHook",
        description = "Whether to use a shutdown hook to call [BContext.shutdownNow] when the JVM is exiting **gracefully**.",
        defaultValue = "true",
    )
    val enableShutdownHook: Boolean
}

/**
 * Returns the configuration object of the provided type, or `null` if none were [registered][BConfigBuilder.registerModule].
 *
 * @param type The type of configuration to get.
 */
fun <T : IConfig> BConfig.getConfigOrNull(type: KClass<T>): T? = getConfigOrNull(type.java)

/**
 * Returns the configuration object of the provided type, or `null` if none were [registered][BConfigBuilder.registerModule].
 *
 * @param T The type of configuration to get.
 */
inline fun <reified T : IConfig> BConfig.getConfigOrNull(): T? = getConfigOrNull(T::class.java)

@ConfigDSL
class BConfigBuilder : BConfigProps {

    override val packages: MutableSet<String> = HashSet()
    override val classes: MutableSet<Class<*>> = HashSet()

    @ExperimentalCoreApi
    override var usePreprocessedLibClassList: Boolean = false

    override val predefinedOwnerIds: MutableSet<Long> = HashSet()

    @set:JvmName("disableExceptionsInDMs")
    override var disableExceptionsInDMs = false

    @set:JvmName("enableOwnerBypass")
    override var enableOwnerBypass = false

    override val ignoredIntents: MutableSet<GatewayIntent> = enumSetOf()

    override var ignoreRestRateLimiter: Boolean = false

    override var enableShutdownHook: Boolean = true

    private val _configs: MutableMap<Class<out IConfig>, IConfig> = hashMapOf()

    val eventManagerConfig = BEventManagerConfigBuilder()
    val serviceConfig = BServiceConfigBuilder()
    val coroutineScopesConfig = BCoroutineScopesConfigBuilder()

    /**
     * Predefined user IDs of the bot owners, allowing bypassing cooldowns, user permission checks,
     * and having hidden text commands shown.
     *
     * If not set, the application owners will be used, with roles "Developer" and above.
     *
     * @param ownerIds IDs of the bot owners
     *
     * @see BotOwners
     */
    fun addPredefinedOwners(vararg ownerIds: Long) = addPredefinedOwners(ownerIds.asList())

    /**
     * Predefined user IDs of the bot owners, allowing bypassing cooldowns, user permission checks,
     * and having hidden text commands shown.
     *
     * If not set, the application owners will be used, with roles "Developer" and above.
     *
     * @param ownerIds IDs of the bot owners
     *
     * @see BotOwners
     */
    fun addPredefinedOwners(ownerIds: Collection<Long>) {
        this.predefinedOwnerIds += ownerIds
    }

    /**
     * Adds this package for class discovery.
     * All services, commands, handlers, listeners, etc... will be read from these packages.
     *
     * **Tip:** For your commands, you can have your package structure such as:
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
     * @param packageName The package name such as `io.github.freya022.bot.commands`
     *
     * @see addClass
     */
    fun addSearchPath(@Language("Java", prefix = "/** @see ", suffix = " */") packageName: String) {
        packages.add(packageName)
    }

    /**
     * Adds a specific class containing services, commands, handlers, listeners, etc...
     *
     * @see addSearchPath
     */
    fun addClass(clazz: Class<*>) {
        classes.add(clazz)
    }

    /**
     * Adds a specific class containing services, commands, handlers, listeners, etc...
     *
     * @see BConfigBuilder.addSearchPath
     */
    @JvmSynthetic
    inline fun <reified T : Any> addClass() {
        addClass(T::class.java)
    }

    fun eventManager(block: ReceiverConsumer<BEventManagerConfigBuilder>) {
        eventManagerConfig.apply(block)
    }

    fun services(block: ReceiverConsumer<BServiceConfigBuilder>) {
        serviceConfig.apply(block)
    }

    fun coroutineScopes(block: ReceiverConsumer<BCoroutineScopesConfigBuilder>) {
        coroutineScopesConfig.apply(block)
    }

    /**
     * Registers a configuration for the relevant module, enabling the features provided by the module.
     *
     * Once configured, it cannot be modified or overwritten.
     *
     * @param configuration The configuration of the registered module
     *
     * @throws IllegalStateException If the module was already registered
     */
    fun registerModule(configuration: IConfig) {
        _configs.putIfAbsentOrThrow(configuration.configType, configuration) { _ ->
            "Module was already registered, please configure the module then register once"
        }
    }

    fun build(): BConfig {
        val logger = KotlinLogging.loggerOf<BConfig>()
        if (disableExceptionsInDMs)
            logger.info { "Disabled sending exception in bot owners DMs" }

        return object : BConfig {
            override val predefinedOwnerIds = this@BConfigBuilder.predefinedOwnerIds.toImmutableSet()
            override val packages = this@BConfigBuilder.packages.toImmutableSet()
            override val classes = this@BConfigBuilder.classes.toImmutableSet()
            override val usePreprocessedLibClassList = this@BConfigBuilder.usePreprocessedLibClassList
            override val disableExceptionsInDMs = this@BConfigBuilder.disableExceptionsInDMs
            override val enableOwnerBypass = this@BConfigBuilder.enableOwnerBypass
            override val ignoredIntents = this@BConfigBuilder.ignoredIntents.toImmutableSet()
            override val ignoreRestRateLimiter = this@BConfigBuilder.ignoreRestRateLimiter
            override val enableShutdownHook = this@BConfigBuilder.enableShutdownHook
            override val eventManagerConfig = this@BConfigBuilder.eventManagerConfig.build()
            override val serviceConfig = this@BConfigBuilder.serviceConfig.build()
            override val coroutineScopesConfig = this@BConfigBuilder.coroutineScopesConfig.build()
            private val _configs = (this@BConfigBuilder._configs + mapOf(
                this.configType to this,
                eventManagerConfig.configType to eventManagerConfig,
                serviceConfig.configType to serviceConfig,
                coroutineScopesConfig.configType to coroutineScopesConfig,
            )).unmodifiableView()
            override val configs get() = _configs.values

            override fun <T : IConfig> getConfigOrNull(type: Class<T>): T? {
                val config = _configs[type] ?: return null
                if (!type.isInstance(config)) {
                    throwInternal("$config (${config.javaClass.name}) is not an instance of ${type.name}")
                }

                return type.cast(config)
            }
        }
    }
}
