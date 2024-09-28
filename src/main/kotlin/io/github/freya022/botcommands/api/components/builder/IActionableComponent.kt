@file:Suppress("DEPRECATION")

package io.github.freya022.botcommands.api.components.builder

import dev.minn.jda.ktx.util.ref
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.annotations.getEffectiveName
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.javaMethodInternal
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.function.Consumer
import javax.annotation.CheckReturnValue
import kotlin.reflect.*

/**
 * Allows components to have handlers bound to them.
 */
interface IActionableComponent<T : IActionableComponent<T>> {
    val context: BContext

    /**
     * List of filters applied to this component.
     *
     * ### Requirements
     * - The filter must not be [ComponentInteractionFilter.global].
     * - The filter must be available via dependency injection.
     *
     * @see ComponentInteractionFilter
     * @see ComponentInteractionRejectionHandler
     */
    val filters: MutableList<ComponentInteractionFilter<*>>

    /**
     * Sets the rate limiter of this component to one declared by a [RateLimitProvider].
     *
     * An exception will be thrown when constructing the button if the [group][ComponentRateLimitReference.group] is invalid.
     *
     * @see RateLimitReference @RateLimitReference
     */
    @CheckReturnValue
    fun rateLimitReference(reference: ComponentRateLimitReference): T

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
    fun addFilter(filter: ComponentInteractionFilter<*>): T

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
    fun addFilter(filterType: Class<out ComponentInteractionFilter<*>>): T
}

/**
 * Convenience extension to load an [ComponentInteractionFilter] service.
 *
 * Typically used as `filters += filter<MyApplicationCommandFilter>()`
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
     * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
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
     * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
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
    @Deprecated("Nothing else to be configured than the data, will be removed, pass your data to bindTo directly")
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
    @Deprecated("Nothing to be configured, will be removed", ReplaceWith("bindTo(handler)"))
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
    @Deprecated("Nothing to be configured, will be removed", ReplaceWith("bindTo(handler)"))
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
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent> T.bindTo(func: KSuspendFunction1<E, Unit>, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, emptyList(), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent> T.bindTo(func: KFunction1<E, Unit>, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, emptyList(), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1> T.bindTo(func: KSuspendFunction2<E, T1, Unit>, arg1: T1, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1> T.bindTo(func: KFunction2<E, T1, Unit>, arg1: T1, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2> T.bindTo(func: KSuspendFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2> T.bindTo(func: KFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> T.bindTo(func: KSuspendFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> T.bindTo(func: KFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> T.bindTo(func: KSuspendFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> T.bindTo(func: KFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> T.bindTo(func: KSuspendFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> T.bindTo(func: KFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> T.bindTo(func: KSuspendFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> T.bindTo(func: KFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> T.bindTo(func: KSuspendFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> T.bindTo(func: KFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> T.bindTo(func: KSuspendFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> T.bindTo(func: KFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.bindTo(func: KSuspendFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.bindTo(func: KFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindToSuspend")
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.bindTo(func: KSuspendFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10), block)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 * 
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@Deprecated("Replaced with bindWith", ReplaceWith("bindWith(func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)"))
fun <T : IPersistentActionableComponent<T>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.bindTo(func: KFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, block: ReceiverConsumer<PersistentHandlerBuilder> = ReceiverConsumer.noop()): T {
    return bindToCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10), block)
}

private fun <T : IPersistentActionableComponent<T>> T.bindToCallable(func: KFunction<*>, data: List<Any?>, block: ReceiverConsumer<PersistentHandlerBuilder>): T {
    return this.bindTo(findHandlerName(func)) {
        apply(block)
        passData(data)
    }
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent> C.bindWith(func: KSuspendFunction1<E, Unit>): C {
    return bindWithBoundCallable(func, emptyList())
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent> C.bindWith(func: KFunction1<E, Unit>): C {
    return bindWithBoundCallable(func, emptyList())
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1> C.bindWith(func: KSuspendFunction2<E, T1, Unit>, arg1: T1): C {
    return bindWithBoundCallable(func, listOf(arg1))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1> C.bindWith(func: KFunction2<E, T1, Unit>, arg1: T1): C {
    return bindWithBoundCallable(func, listOf(arg1))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2> C.bindWith(func: KSuspendFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2> C.bindWith(func: KFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> C.bindWith(func: KSuspendFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> C.bindWith(func: KFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> C.bindWith(func: KSuspendFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> C.bindWith(func: KFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> C.bindWith(func: KSuspendFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> C.bindWith(func: KFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> C.bindWith(func: KSuspendFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> C.bindWith(func: KFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> C.bindWith(func: KSuspendFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> C.bindWith(func: KFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> C.bindWith(func: KSuspendFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> C.bindWith(func: KFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.bindWith(func: KSuspendFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.bindWith(func: KFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallableSuspend")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.bindWith(func: KSuspendFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithBoundCallable")
fun <C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.bindWith(func: KFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return bindWithBoundCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

private fun <C : IPersistentActionableComponent<C>> C.bindWithBoundCallable(func: KFunction<*>, data: List<Any?>): C {
    return this.bindTo(findHandlerName(func), data)
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent> C.bindWith(func: KSuspendFunction2<T, E, Unit>): C {
    return bindWithClassCallable(func, emptyList())
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent> C.bindWith(func: KFunction2<T, E, Unit>): C {
    return bindWithClassCallable(func, emptyList())
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1> C.bindWith(func: KSuspendFunction3<T, E, T1, Unit>, arg1: T1): C {
    return bindWithClassCallable(func, listOf(arg1))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1> C.bindWith(func: KFunction3<T, E, T1, Unit>, arg1: T1): C {
    return bindWithClassCallable(func, listOf(arg1))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2> C.bindWith(func: KSuspendFunction4<T, E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return bindWithClassCallable(func, listOf(arg1, arg2))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2> C.bindWith(func: KFunction4<T, E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return bindWithClassCallable(func, listOf(arg1, arg2))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> C.bindWith(func: KSuspendFunction5<T, E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3> C.bindWith(func: KFunction5<T, E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> C.bindWith(func: KSuspendFunction6<T, E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4> C.bindWith(func: KFunction6<T, E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> C.bindWith(func: KSuspendFunction7<T, E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5> C.bindWith(func: KFunction7<T, E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> C.bindWith(func: KSuspendFunction8<T, E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6> C.bindWith(func: KFunction8<T, E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> C.bindWith(func: KSuspendFunction9<T, E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7> C.bindWith(func: KFunction9<T, E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> C.bindWith(func: KSuspendFunction10<T, E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8> C.bindWith(func: KFunction10<T, E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.bindWith(func: KSuspendFunction11<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.bindWith(func: KFunction11<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallableSuspend")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.bindWith(func: KSuspendFunction12<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Binds the given handler to this component.
 *
 * ### Handler data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
 *
 * Remember the parameters need to be annotated with [@ComponentData][ComponentData].
 */
@JvmName("bindWithClassCallable")
fun <T : Any, C : IPersistentActionableComponent<C>, E : GenericComponentInteractionCreateEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.bindWith(func: KFunction12<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return bindWithClassCallable(func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

private fun <C : IPersistentActionableComponent<C>> C.bindWithClassCallable(func: KFunction<*>, data: List<Any?>): C {
    val paramType = func.javaMethodInternal.parameterTypes[0]
    require(paramType.isSubclassOf<GenericComponentInteractionCreateEvent>()) {
        "The provided function must have a component event as its first parameter, found ${paramType.simpleNestedName}"
    }
    return this.bindTo(findHandlerName(func), data)
}

private fun findHandlerName(func: KFunction<*>): String {
    val buttonAnnotation = func.findAnnotationRecursive<JDAButtonListener>()
    val selectMenuAnnotation = func.findAnnotationRecursive<JDASelectMenuListener>()

    if (buttonAnnotation != null && selectMenuAnnotation != null)
        throwArgument(func, "Cannot have the same function with the two annotation")
    else if (buttonAnnotation != null)
        return buttonAnnotation.getEffectiveName(func)
    else if (selectMenuAnnotation != null)
        return selectMenuAnnotation.getEffectiveName(func)

    throwArgument(func, "Could not find ${annotationRef<JDAButtonListener>()} or ${annotationRef<JDASelectMenuListener>()}")
}