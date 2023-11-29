package io.github.freya022.botcommands.api.components.builder

import dev.minn.jda.ktx.util.ref
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import kotlin.time.*
import java.time.Duration as JavaDuration

/**
 * Allows components to have timeouts.
 *
 * The component will be deleted from the database on expiration.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 */
interface ITimeoutableComponent<T : ITimeoutableComponent<T>> : BuilderInstanceHolder<T> {
    val timeout: ComponentTimeout?

    /**
     * Sets the timeout on this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     */
    fun timeout(timeout: Long, timeoutUnit: TimeUnit): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()))

    /**
     * Sets the timeout on this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * @param timeout The duration of the timeout
     */
    fun timeout(timeout: JavaDuration): T =
        timeout(timeout.toKotlinDuration())

    /**
     * Sets the timeout on this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * @param timeout The duration of the timeout
     */
    @JvmSynthetic
    fun timeout(timeout: Duration): T
}

/**
 * Allows components to have persistent timeouts.
 *
 * These timeouts are represented by a method with a [ComponentTimeoutHandler] or [GroupTimeoutHandler] annotation on it,
 * and will still exist (and be rescheduled) after a restart.
 *
 * @see ITimeoutableComponent
 */
interface IPersistentTimeoutableComponent<T : IPersistentTimeoutableComponent<T>> : ITimeoutableComponent<T> {
    /**
     * Binds the given timeout handler name with its arguments to this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Timeout data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [ComponentParameterResolver] exists for the handler's parameter type.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [ComponentTimeoutHandler] or [GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     */
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg data: Any?): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *data)

    /**
     * Binds the given timeout handler name with its arguments to this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Timeout data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * Unlike with [PersistentHandlerBuilder.passData], the data passed to the handler will only be [String],
     * no conversion will happen.
     *
     * @param timeout The duration of the timeout
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [ComponentTimeoutHandler] or [GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     */
    fun timeout(timeout: JavaDuration, handlerName: String, vararg data: Any?): T =
        timeout(timeout.toKotlinDuration(), handlerName, *data)

    /**
     * Binds the given timeout handler name with its arguments to this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Timeout data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * Unlike with [PersistentHandlerBuilder.passData], the data passed to the handler will only be [String],
     * no conversion will happen.
     *
     * @param timeout The duration of the timeout
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [ComponentTimeoutHandler] or [GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     */
    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg data: Any?): T
}

/**
 * Allows components to have ephemeral timeouts.
 *
 * These timeouts will not exist anymore after a restart.
 *
 * @see ITimeoutableComponent
 */
interface IEphemeralTimeoutableComponent<T : IEphemeralTimeoutableComponent<T>> : ITimeoutableComponent<T> {
    /**
     * Binds the given handler to this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * @param timeout The duration before timeout
     * @param handler The handler to run when the button is clicked
     */
    fun timeout(timeout: JavaDuration, handler: Runnable): T =
        timeout(timeout.toKotlinDuration()) { handler.run() }

    /**
     * Binds the given handler to this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     * @param handler The handler to run when the button is clicked
     */
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    /**
     * Binds the given handler to this component.
     *
     * The component will be deleted from the database on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * You can still use [User.ref] and such from JDA-KTX to attenuate this issue,
     * even though it will return you an outdated object if the entity cannot be found anymore.
     *
     * @param timeout The duration of the timeout
     * @param handler The handler to run when the button is clicked
     */
    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit): T
}
