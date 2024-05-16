package io.github.freya022.botcommands.api.components.builder

import dev.minn.jda.ktx.util.ref
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.components.handler.ComponentHandler
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.function.Consumer
import javax.annotation.CheckReturnValue
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * Allows components to have handlers bound to them.
 */
interface IActionableComponent<T : IActionableComponent<T>> : BuilderInstanceHolder<T> {
    val context: BContext

    val handler: ComponentHandler?

    /**
     * List of filters applied to this component.
     *
     * ### Requirements
     * - The filter must not be [ComponentInteractionFilter.global].
     * - The filter must be available via dependency injection.
     *
     * @see ComponentInteractionFilter
     */
    val filters: MutableList<ComponentInteractionFilter<*>>

    val rateLimitGroup: String?

    /**
     * Sets the rate limiter of this component to one declared by [RateLimitProvider].
     *
     * An exception will be thrown when constructing the button if the group is invalid.
     *
     * @see RateLimitReference @RateLimitReference
     */
    @CheckReturnValue
    fun rateLimitReference(group: String): T

    /**
     * Applies a filter to this component.
     *
     * ### Requirements
     * - The filter must not be [ComponentInteractionFilter.global].
     * - The filter must be available via dependency injection.
     *
     * @see ComponentInteractionFilter
     */
    @CheckReturnValue
    fun addFilter(filter: ComponentInteractionFilter<*>): T = instance.also {
        filters += filter
    }

    /**
     * Applies a filter to this component.
     *
     * ### Requirements
     * - The filter must not be [ComponentInteractionFilter.global].
     * - The filter must be available via dependency injection.
     *
     * @see ComponentInteractionFilter
     */
    @CheckReturnValue
    fun addFilter(filterType: Class<out ComponentInteractionFilter<*>>): T = addFilter(context.getService(filterType))
}

/**
 * Retrieves an existing component filter from the context.
 *
 * This is equivalent to `context.getService<T>()`.
 */
inline fun <reified T : ComponentInteractionFilter<*>> IActionableComponent<*>.filter(): T {
    return context.getService<T>()
}

/**
 * Allows components to have persistent handlers bound to them.
 *
 * These handlers are represented by a method with a [JDAButtonListener] or [JDASelectMenuListener] annotation on it,
 * and will still exist after a restart.
 */
interface IPersistentActionableComponent<T : IPersistentActionableComponent<T>> : IActionableComponent<T> {
    /**
     * Binds the given handler name with its arguments to this component.
     *
     * ### Handler data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
     *
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [JDAButtonListener] or [JDASelectMenuListener]
     * @param data The data to pass to the component handler
     */
    @CheckReturnValue
    fun bindTo(handlerName: String, vararg data: Any?): T = bindTo(handlerName, data.asList())

    /**
     * Binds the given handler name with its arguments to this component.
     *
     * ### Handler data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
     *
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [JDAButtonListener] or [JDASelectMenuListener]
     * @param data The data to pass to the component handler
     */
    @CheckReturnValue
    fun bindTo(handlerName: String, data: List<Any?>): T = bindTo(handlerName) { passData(data) }

    /**
     * Binds the given handler name with its arguments to this component.
     *
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [JDAButtonListener] or [JDASelectMenuListener]
     */
    @CheckReturnValue
    fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>): T
}

/**
 * Allows components to have ephemeral handlers bound to them.
 *
 * These handlers will not exist anymore after a restart.
 */
interface IEphemeralActionableComponent<T : IEphemeralActionableComponent<T, E>, E : GenericComponentInteractionCreateEvent> : IActionableComponent<T> {
    /**
     * Binds the given handler to this component.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * @param handler The handler to run when the button is clicked
     */
    @CheckReturnValue
    fun bindTo(handler: Consumer<E>): T = bindTo(handler = { handler.accept(it) })

    /**
     * Binds the given handler to this component.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * @param handler The handler to run when the button is clicked
     */
    @CheckReturnValue
    fun bindTo(handler: Consumer<E>, block: ReceiverConsumer<EphemeralHandlerBuilder<E>>): T = bindTo(handler = { handler.accept(it) }, block)

    /**
     * Binds the given handler to this component.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * You can still use [User.ref] and such from JDA-KTX to attenuate this issue,
     * even though it will return you an outdated object if the entity cannot be found anymore.
     *
     * @param handler The handler to run when the button is clicked
     */
    @JvmSynthetic
    fun bindTo(handler: suspend (E) -> Unit): T = bindTo(handler, ReceiverConsumer.noop())

    /**
     * Binds the given handler to this component.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * You can still use [User.ref] and such from JDA-KTX to attenuate this issue,
     * even though it will return you an outdated object if the entity cannot be found anymore.
     *
     * @param handler The handler to run when the button is clicked
     */
    @JvmSynthetic
    fun bindTo(handler: suspend (E) -> Unit, block: ReceiverConsumer<EphemeralHandlerBuilder<E>>): T
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent> T.bindTo(func: suspend (event: E) -> Unit, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, emptyList(), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent> T.bindTo(func: (event: E) -> Unit, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, emptyList(), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1> T.bindTo(func: suspend (event: E, T1) -> Unit, arg1: T1, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf<Any?>(arg1), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1> T.bindTo(func: (event: E, T1) -> Unit, arg1: T1, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf<Any?>(arg1), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2> T.bindTo(func: suspend (event: E, T1, T2) -> Unit, arg1: T1, arg2: T2, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2> T.bindTo(func: (event: E, T1, T2) -> Unit, arg1: T1, arg2: T2, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> T.bindTo(func: suspend (event: E, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> T.bindTo(func: (event: E, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> T.bindTo(func: suspend (event: E, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> T.bindTo(func: (event: E, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> T.bindTo(func: suspend (event: E, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> T.bindTo(func: (event: E, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> T.bindTo(func: suspend (event: E, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> T.bindTo(func: (event: E, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> T.bindTo(func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> T.bindTo(func: (event: E, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> T.bindTo(func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> T.bindTo(func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.bindTo(func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.bindTo(func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.bindTo(func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.bindTo(func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10), block)
}

private fun <T : IPersistentActionableComponent<T>> T.bindToCallable(func: KFunction<*>, data: List<Any?>, block: ReceiverConsumer<PersistentHandlerBuilder>): T {
    return this.bindTo(findHandlerName(func)) {
        apply(block)
        passData(data)
    }
}

private fun findHandlerName(func: KFunction<*>): String {
    val buttonName = func.findAnnotation<JDAButtonListener>()?.name
    val selectMenuName = func.findAnnotation<JDASelectMenuListener>()?.name

    if (buttonName != null && selectMenuName != null)
        throwUser(func, "Cannot have the same function with the two annotation")
    else if (buttonName != null)
        return buttonName
    else if (selectMenuName != null)
        return selectMenuName

    throwUser(func, "Could not find ${annotationRef<JDAButtonListener>()} or ${annotationRef<JDASelectMenuListener>()}")
}