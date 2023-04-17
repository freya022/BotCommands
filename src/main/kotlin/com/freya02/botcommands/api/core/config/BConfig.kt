package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.apply
import com.freya02.botcommands.api.commands.annotations.RequireOwner
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.core.ServiceContainer
import com.freya02.botcommands.api.core.annotations.InjectedService

@InjectedService
class BConfig internal constructor() {
    @get:JvmSynthetic
    internal var locked = false
        private set

    @JvmSynthetic
    internal val packages: MutableSet<String> = HashSet()
    @JvmSynthetic
    internal val classes: MutableSet<Class<*>> = HashSet()

    @JvmSynthetic
    internal val ownerIds: MutableSet<Long> = HashSet()

    /**
     * Disables sending exceptions to the bot owners
     */
    var disableExceptionsInDMs = false

    /**
     * Disables autocomplete caching, unless [CacheAutocomplete.forceCache] is set to `true`.
     *
     * This could be useful when testing methods that use autocomplete caching, while using hotswap.
     */
    var disableAutocompleteCache = false

    @JvmSynthetic
    internal val debugConfig = BDebugConfig()

    @JvmSynthetic
    internal val serviceConfig = BServiceConfig()

    @JvmSynthetic
    internal val textConfig = BTextConfig()

    @JvmSynthetic
    internal val applicationConfig = BApplicationConfig(this)

    @JvmSynthetic
    internal val componentsConfig = BComponentsConfig(this)

    @JvmSynthetic
    internal val coroutineScopesConfig = BCoroutineScopesConfig(this)

    /**
     * Determines whether the SQL queries should be logged
     *
     * **SQL queries will be logged on TRACE**
     */
    var logQueries: Boolean = true
    /**
     * Determines if the SQL query logger will replace query parameters by their value.
     */
    var logQueryParameters: Boolean = true

    /**
     * Adds owners, they can access the commands annotated with [RequireOwner]
     *
     * @param ownerIds Owners Long IDs to add
     */
    fun addOwners(vararg ownerIds: Long) {
        this.ownerIds.addAll(ownerIds.asList())
    }

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

    fun services(block: ReceiverConsumer<BServiceConfig>) {
        serviceConfig.apply(block)
    }

    fun coroutineScopes(block: ReceiverConsumer<BCoroutineScopesConfig>) {
        coroutineScopesConfig.apply(block)
    }

    fun debug(block: ReceiverConsumer<BDebugConfig>) {
        debugConfig.apply(block)
    }

    fun textCommands(block: ReceiverConsumer<BTextConfig>) {
        textConfig.apply(block)
    }

    fun applicationCommands(block: ReceiverConsumer<BApplicationConfig>) {
        applicationConfig.apply(block)
    }

    fun components(block: ReceiverConsumer<BComponentsConfig>) {
        componentsConfig.apply(block)
    }

    @JvmSynthetic
    internal fun putConfigInServices(serviceContainer: ServiceContainer) {
        serviceContainer.putService(serviceConfig)
        serviceContainer.putService(applicationConfig)
        serviceContainer.putService(componentsConfig)
        serviceContainer.putService(coroutineScopesConfig)
        serviceContainer.putService(debugConfig)
        serviceContainer.putService(textConfig)
    }

    @JvmSynthetic
    internal fun lock() {
        locked = true
    }

    fun isOwner(id: Long) = id in ownerIds
}
