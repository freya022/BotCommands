package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.annotations.JDASelectMenuListener
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.api.core.utils.isSubclassOfAny
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.components.ComponentHandler
import com.freya02.botcommands.internal.utils.requireUser
import com.freya02.botcommands.internal.utils.throwUser
import dev.minn.jda.ktx.util.ref
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

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
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [JDAButtonListener] or [JDASelectMenuListener]
     */
    fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>)

    /**
     * Binds the given handler name with its arguments to this component.
     *
     * The data passed is transformed with [toString][Object.toString]
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * **As always**, the data can only be reconstructed if a suitable [ComponentParameterResolver] exists for the type.
     *
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [JDAButtonListener] or [JDASelectMenuListener]
     * @param data The data to pass to the component handler
     */
    fun bindTo(handlerName: String, data: List<Any?>) = bindTo(handlerName) { passData(data) }

    /**
     * Binds the given handler name with its arguments to this component.
     *
     * The data passed is transformed with [toString][Object.toString]
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * **As always**, the data can only be reconstructed if a suitable [ComponentParameterResolver] exists for the type.
     *
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [JDAButtonListener] or [JDASelectMenuListener]
     * @param data The data to pass to the component handler
     */
    fun bindTo(handlerName: String, vararg data: Any?) = bindTo(handlerName, data.asList())
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
    fun bindTo(handler: Consumer<E>) = bindTo(handler = { handler.accept(it) })

    /**
     * Binds the given handler to this component.
     *
     * **Be sure not to capture JDA entities in such handlers
     * as [their lifetime could have expired](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)**.
     *
     * @param handler The handler to run when the button is clicked
     */
    fun bindTo(handler: Consumer<E>, block: ReceiverConsumer<EphemeralHandlerBuilder<E>>) = bindTo(handler = { handler.accept(it) }, block)

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
    fun bindTo(handler: suspend (E) -> Unit, block: ReceiverConsumer<EphemeralHandlerBuilder<E>> = ReceiverConsumer.noop())
}

inline fun <reified E : GenericComponentInteractionCreateEvent> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E) -> Unit, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, emptyList(), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent> IPersistentActionableComponent.bindTo(noinline func: (event: E) -> Unit, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, emptyList(), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1) -> Unit, arg1: T1, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf<Any?>(arg1), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1) -> Unit, arg1: T1, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf<Any?>(arg1), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2) -> Unit, arg1: T1, arg2: T2, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2) -> Unit, arg1: T1, arg2: T2, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10), block)
}

inline fun <reified E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()) {
    bindToCallable(func as KFunction<*>, E::class, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10), block)
}

@PublishedApi
internal fun IPersistentActionableComponent.bindToCallable(func: KFunction<*>, eventType: KClass<out GenericComponentInteractionCreateEvent>, data: List<Any?>, block: ReceiverConsumer<PersistentHandlerBuilder>) {
    val name = findHandlerName(func, eventType)
        ?: throwUser(func, "Could not find @${JDAButtonListener::class.simpleName} or @${JDASelectMenuListener::class.simpleName}")
    this.bindTo(handlerName = name) {
        apply(block)
        passData(data)
    }
}

private fun findHandlerName(func: KFunction<*>, eventType: KClass<out GenericComponentInteractionCreateEvent>): String? {
    func.findAnnotation<JDAButtonListener>()?.let {
        requireUser(eventType.isSubclassOf(ButtonEvent::class), func) {
            "Function must have a subclass of ButtonEvent as the first argument"
        }
        return it.name
    }

    func.findAnnotation<JDASelectMenuListener>()?.let {
        requireUser(eventType.isSubclassOfAny(StringSelectEvent::class, EntitySelectEvent::class), func) {
            "Function must have a subclass of StringSelectEvent/EntitySelectEvent as the first argument"
        }
        return it.name
    }

    return null
}