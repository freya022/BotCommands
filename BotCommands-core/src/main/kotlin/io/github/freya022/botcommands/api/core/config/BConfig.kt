package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.text.annotations.Hidden
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.requests.PriorityGlobalRestRateLimiter
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.core.waiter.EventWaiter
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
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
    val databaseConfig: BDatabaseConfig
    val localizationConfig: BLocalizationConfig
    val appEmojisConfig: BAppEmojisConfig
    val textConfig: BTextConfig
    val applicationConfig: BApplicationConfig
    val modalsConfig: BModalsConfig
    val componentsConfig: BComponentsConfig
    val coroutineScopesConfig: BCoroutineScopesConfig

    /**
     * An immutable collection of all [registered][BConfigBuilder.withConfig] configuration objects.
     */
    val configs: Collection<IConfig>

    /**
     * Gets a configuration of the provided type, or `null` if none were [registered][BConfigBuilder.withConfig].
     *
     * @param type The type of configuration to get, based on [configType].
     */
    fun <T : IConfig> getConfigOrNull(type: Class<T>): T?
}

interface BConfigProps {
    /**
     * Predefined user IDs of the bot owners, allowing bypassing cooldowns, user permission checks,
     * and having [hidden commands][Hidden] shown.
     *
     * If not set, the application owners will be used, with roles "Developer" and above.
     *
     * **Note:** Prefer using [BotOwners] to get the effective bot owners, regardless of if this property is set or not.
     *
     * Spring property: `botcommands.core.predefinedOwnerIds`
     */
    @ConfigurationValue(path = "botcommands.core.predefinedOwnerIds")
    val predefinedOwnerIds: Set<Long>

    /**
     * The packages the framework will scan through for services, commands, handlers...
     *
     * Spring property: `botcommands.core.packages`
     */
    @ConfigurationValue(path = "botcommands.core.packages")
    val packages: Set<String>
    /**
     * Additional classes the framework will scan through for services, commands, handlers...
     *
     * Spring property: `botcommands.core.classes`
     */
    @ConfigurationValue(path = "botcommands.core.classes", type = "java.util.Set<java.lang.Class<?>>")
    val classes: Set<Class<*>>

    /**
     * Disables sending exceptions to the bot owners
     *
     * Default: `false`
     *
     * Spring property: `botcommands.core.disableExceptionsInDMs`
     */
    @ConfigurationValue(path = "botcommands.core.disableExceptionsInDMs", defaultValue = "false")
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
    @ConfigurationValue(path = "botcommands.core.enableOwnerBypass", defaultValue = "false")
    val enableOwnerBypass: Boolean

    /**
     * Gateway intents to ignore when checking for [event listeners][BEventListener] intents.
     *
     * Spring property: `botcommands.core.ignoredIntents`
     *
     * @see BEventListener.ignoreIntents
     */
    @ConfigurationValue(path = "botcommands.core.ignoredIntents")
    val ignoredIntents: Set<GatewayIntent>

    /**
     * Events for which the [event waiter][EventWaiter] must ignore intent requirements.
     *
     * If not ignored, the event would still be being listened to, but a warning would have been logged.
     *
     * Spring property: `botcommands.core.ignoredEventIntents`
     */
    @ConfigurationValue(path = "botcommands.core.ignoredEventIntents", type = "java.util.Set<java.lang.Class<net.dv8tion.jda.api.events.Event>>")
    val ignoredEventIntents: Set<Class<out Event>>

    /**
     * Suppresses warnings about the default [RestRateLimiter] being used for large bots.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.core.ignoreRestRateLimiter`
     *
     * @see PriorityGlobalRestRateLimiter
     */
    @ConfigurationValue("botcommands.core.ignoreRestRateLimiter", defaultValue = "false")
    val ignoreRestRateLimiter: Boolean

    val classGraphProcessors: List<ClassGraphProcessor>

    /**
     * Whether to use a [shutdown hook][Runtime.addShutdownHook] to call [BContext.shutdownNow] when the JVM is exiting **gracefully**.
     *
     * If disabled you'll have to shut it down yourself.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.core.enableShutdownHook`
     */
    @ConfigurationValue("botcommands.core.enableShutdownHook", defaultValue = "true")
    val enableShutdownHook: Boolean
}

/**
 * Gets a configuration of the provided type, or `null` if none were [registered][BConfigBuilder.withConfig].
 *
 * @param type The type of configuration to get, based on [configType][BConfig.configType].
 */
fun <T : IConfig> BConfig.getConfigOrNull(type: KClass<T>): T? = getConfigOrNull(type.java)

/**
 * Gets a configuration of the provided type, or `null` if none were [registered][BConfigBuilder.withConfig].
 *
 * @param T The type of configuration to get, based on [configType][BConfig.configType].
 */
inline fun <reified T : IConfig> BConfig.getConfigOrNull(): T? = getConfigOrNull(T::class.java)

@ConfigDSL
class BConfigBuilder : BConfigProps {
    override val packages: MutableSet<String> = HashSet()
    override val classes: MutableSet<Class<*>> = HashSet()

    override val predefinedOwnerIds: MutableSet<Long> = HashSet()

    @set:JvmName("disableExceptionsInDMs")
    override var disableExceptionsInDMs = false
    @set:JvmName("enableOwnerBypass")
    override var enableOwnerBypass = false

    override val ignoredIntents: MutableSet<GatewayIntent> = enumSetOf()

    override val ignoredEventIntents: MutableSet<Class<out Event>> = hashSetOf()

    override var ignoreRestRateLimiter: Boolean = false

    override val classGraphProcessors: MutableList<ClassGraphProcessor> = arrayListOf()

    override var enableShutdownHook: Boolean = true

    private val _configs: MutableMap<Class<out IConfig>, IConfig> = hashMapOf()

    val eventManagerConfig = BEventManagerConfigBuilder()
    val serviceConfig = BServiceConfigBuilder()
    val databaseConfig = BDatabaseConfigBuilder()
    val localizationConfig = BLocalizationConfigBuilder()
    val appEmojisConfig = BAppEmojisConfigBuilder()
    val textConfig = BTextConfigBuilder()
    val applicationConfig = BApplicationConfigBuilder()
    val modalsConfig = BModalsConfigBuilder()
    val componentsConfig = BComponentsConfigBuilder()
    val coroutineScopesConfig = BCoroutineScopesConfigBuilder()

    /**
     * Predefined user IDs of the bot owners, allowing bypassing cooldowns, user permission checks,
     * and having [hidden commands][Hidden] shown.
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
     * and having [hidden commands][Hidden] shown.
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

    fun database(block: ReceiverConsumer<BDatabaseConfigBuilder>) {
        databaseConfig.apply(block)
    }

    fun localization(block: ReceiverConsumer<BLocalizationConfigBuilder>) {
        localizationConfig.apply(block)
    }

    fun appEmojis(block: ReceiverConsumer<BAppEmojisConfigBuilder>) {
        appEmojisConfig.apply(block)
    }

    fun textCommands(block: ReceiverConsumer<BTextConfigBuilder>) {
        textConfig.apply(block)
    }

    fun applicationCommands(block: ReceiverConsumer<BApplicationConfigBuilder>) {
        applicationConfig.apply(block)
    }

    fun modals(block: ReceiverConsumer<BModalsConfigBuilder>) {
        modalsConfig.apply(block)
    }

    fun components(block: ReceiverConsumer<BComponentsConfigBuilder>) {
        componentsConfig.apply(block)
    }

    /**
     * Registers the provided configuration, once configured, it cannot be modified or overwritten.
     *
     * @param newConfig The new configuration
     *
     * @throws IllegalStateException If a config of the same type was already registered
     */
    fun withConfig(newConfig: IConfig) {
        _configs.putIfAbsentOrThrow(newConfig.configType, newConfig) { _ ->
            "Cannot reassign configuration of ${newConfig.configType.simpleNestedName}, please configure it entirely then assign once"
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
            override val disableExceptionsInDMs = this@BConfigBuilder.disableExceptionsInDMs
            override val enableOwnerBypass = this@BConfigBuilder.enableOwnerBypass
            override val ignoredIntents = this@BConfigBuilder.ignoredIntents.toImmutableSet()
            override val ignoredEventIntents = this@BConfigBuilder.ignoredEventIntents.toImmutableSet()
            override val ignoreRestRateLimiter = this@BConfigBuilder.ignoreRestRateLimiter
            override val classGraphProcessors = this@BConfigBuilder.classGraphProcessors.toImmutableList()
            override val enableShutdownHook = this@BConfigBuilder.enableShutdownHook
            override val eventManagerConfig = this@BConfigBuilder.eventManagerConfig.build()
            override val serviceConfig = this@BConfigBuilder.serviceConfig.build()
            override val databaseConfig = this@BConfigBuilder.databaseConfig.build()
            override val localizationConfig = this@BConfigBuilder.localizationConfig.build()
            override val appEmojisConfig = this@BConfigBuilder.appEmojisConfig.build()
            override val textConfig = this@BConfigBuilder.textConfig.build()
            override val applicationConfig = this@BConfigBuilder.applicationConfig.build()
            override val modalsConfig = this@BConfigBuilder.modalsConfig.build()
            override val componentsConfig = this@BConfigBuilder.componentsConfig.build()
            override val coroutineScopesConfig = this@BConfigBuilder.coroutineScopesConfig.build()
            private val _configs = (this@BConfigBuilder._configs + mapOf(
                this.configType to this,
                eventManagerConfig.configType to eventManagerConfig,
                serviceConfig.configType to serviceConfig,
                databaseConfig.configType to databaseConfig,
                localizationConfig.configType to localizationConfig,
                appEmojisConfig.configType to appEmojisConfig,
                textConfig.configType to textConfig,
                applicationConfig.configType to applicationConfig,
                modalsConfig.configType to modalsConfig,
                componentsConfig.configType to componentsConfig,
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
