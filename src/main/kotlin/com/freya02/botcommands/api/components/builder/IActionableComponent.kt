package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.annotations.JDASelectMenuListener
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.components.ComponentHandler
import dev.minn.jda.ktx.util.ref
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.function.Consumer

/**
 * Allows components to have handlers bound to them.
 */
interface IActionableComponent {
    val handler: ComponentHandler?
}

/**
 * Allows components to have persistent handlers bound to them.
 *
 * These handlers are represented by a method with a [JDAButtonListener] or [JDASelectMenuListener] annotation on it,
 * and will still exist after a restart.
 */
interface IPersistentActionableComponent : IActionableComponent {
    /**
     * Binds the given handler name with its arguments to this component.
     *
     * The data passed is transformed with [toString][Object.toString] except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * **As always**, the data can only be reconstructed if a suitable [ComponentParameterResolver] exists for the type.
     *
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [JDAButtonListener] or [JDASelectMenuListener]
     * @param data The data to pass to the component handler
     */
    fun bindTo(handlerName: String, vararg data: Any?)
}

/**
 * Allows components to have ephemeral handlers bound to them.
 *
 * These handlers will not exist anymore after a restart.
 */
interface IEphemeralActionableComponent<E : GenericComponentInteractionCreateEvent> : IActionableComponent {
    /**
     * Binds the given handler to this component.
     *
     * **Be sure not to capture JDA entities in such handlers
     * as [their lifetime could have expired](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)**.
     *
     * @param handler The handler to run when the button is clicked
     */
    fun bindTo(handler: Consumer<E>) = bindTo { handler.accept(it) }

    /**
     * Binds the given handler to this component.
     *
     * **Be sure not to capture JDA entities in such handlers
     * as [their lifetime could have expired](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)**.
     *
     * You can still use [User.ref] and such from JDA-KTX to circumvent this issue.
     *
     * @param handler The handler to run when the button is clicked
     */
    @JvmSynthetic
    fun bindTo(handler: suspend (E) -> Unit)
}
