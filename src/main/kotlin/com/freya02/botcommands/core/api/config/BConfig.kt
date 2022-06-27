package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.annotations.api.annotations.RequireOwner
import com.freya02.botcommands.api.ExceptionHandler
import com.freya02.botcommands.api.ExceptionHandlerAdapter
import com.freya02.botcommands.api.SettingsProvider
import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.components.DefaultComponentManager
import com.freya02.botcommands.internal.notNull
import com.freya02.botcommands.internal.utils.Utils
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.properties.Delegates

//TODO immutable config
class BConfig internal constructor() {
    internal val packages: MutableSet<String> = HashSet()
    internal val classes: MutableSet<Class<*>> = HashSet() //TODO treat as being potential classes, not all of them would be valid to use

    private val ownerIds: MutableSet<Long> = HashSet()
    private val prefixes: MutableSet<String> = HashSet()

    val serviceConfig = BServiceConfig()

    val applicationConfig = BApplicationConfig()

    /**
     * Used to take guild-specific settings such as prefixes
     */
    var settingsProvider: SettingsProvider by Delegates.notNull("")

    /**
     * Used by the thread pools such of command handlers / components
     *
     * Notes: You will need to handle things such as already acknowledged interactions (in the case of interaction events, where the exception happened after the interaction has been acknowledged), see [Interaction.isAcknowledged]
     *
     * @see Utils.getException
     * @see ExceptionHandlerAdapter
     */
    var uncaughtExceptionHandler: ExceptionHandler by Delegates.notNull("")

    /**
     * Used to handle storing/retrieving persistent/lambda components handlers
     *
     * @see DefaultComponentManager
     */
    var componentManager: ComponentManager by Delegates.notNull("")

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
}
