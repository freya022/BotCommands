package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.DefaultMessages
import com.freya02.botcommands.api.ExceptionHandler
import com.freya02.botcommands.api.ExceptionHandlerAdapter
import com.freya02.botcommands.api.SettingsProvider
import com.freya02.botcommands.api.commands.annotations.RequireOwner
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.DefaultMessagesFunction
import com.freya02.botcommands.internal.LockableVar
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.lockableNotNull
import com.freya02.botcommands.internal.toDelegate
import com.freya02.botcommands.internal.utils.Utils
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import java.sql.Connection
import java.util.function.Function
import java.util.function.Supplier
import kotlin.properties.Delegates

@InjectedService
class BConfig internal constructor() {
    @get:JvmSynthetic
    internal var locked = false
        private set

    internal val packages: MutableSet<String> = HashSet() //TODO backed collection as mutable, exposed collection as immutable
    //TODO backed collection as mutable, exposed collection as immutable
    internal val classes: MutableSet<Class<*>> = HashSet() //TODO treat as being potential classes, not all of them would be valid to use

    internal val ownerIds: MutableSet<Long> = HashSet() //TODO backed collection as mutable, exposed collection as immutable

    /**
     * Enabling dev mode only disables exception DMs currently
     */
    var devMode = false

    var defaultMessageProvider: Function<DiscordLocale, DefaultMessages> = DefaultMessagesFunction()

    val debugConfig = BDebugConfig()

    val serviceConfig = BServiceConfig()

    val textConfig = BTextConfig()

    val applicationConfig = BApplicationConfig(this)

    val componentsConfig = BComponentsConfig(this)

    val coroutineScopesConfig = BCoroutineScopesConfig(this)

    /**
     * Used to take guild-specific settings such as prefixes
     */
    var settingsProvider: SettingsProvider by Delegates.lockableNotNull(this, "Settings provider needs to be set !")
    fun hasSettingsProvider() = ::settingsProvider.toDelegate<LockableVar<*>>().hasValue()

    /**
     * Used by the thread pools such of command handlers / components
     *
     * Notes: You will need to handle things such as already acknowledged interactions (in the case of interaction events, where the exception happened after the interaction has been acknowledged), see [Interaction.isAcknowledged]
     *
     * @see Utils.getException
     * @see ExceptionHandlerAdapter
     */
    var uncaughtExceptionHandler: ExceptionHandler by Delegates.lockableNotNull(this, "Uncaught exception handler needs to be set !")
    fun hasUncaughtExceptionHandler() = ::uncaughtExceptionHandler.toDelegate<LockableVar<*>>().hasValue()

    var connectionProvider: Supplier<Connection> by Delegates.lockableNotNull(this, "Connection provider needs to be set!")
    fun hasConnectionProvider() = ::connectionProvider.toDelegate<LockableVar<*>>().hasValue()

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

    fun textCommands(block: BTextConfig.() -> Unit) {
        block(textConfig)
    }

    fun applicationCommands(block: BApplicationConfig.() -> Unit) {
        block(applicationConfig)
    }

    fun components(block: BComponentsConfig.() -> Unit) {
        block(componentsConfig)
    }

    internal fun putConfigInServices(serviceContainer: ServiceContainer) {
        serviceContainer.putService(serviceConfig)
        serviceContainer.putService(applicationConfig)
        serviceContainer.putService(componentsConfig)
        serviceContainer.putService(coroutineScopesConfig)
        serviceContainer.putService(debugConfig)
        serviceContainer.putService(textConfig)
    }

    internal fun lock() {
        locked = true
    }

    fun isOwner(id: Long) = id in ownerIds
}
