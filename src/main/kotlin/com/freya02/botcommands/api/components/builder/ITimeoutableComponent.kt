package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.api.components.annotations.ComponentTimeoutHandler
import com.freya02.botcommands.api.components.annotations.GroupTimeoutHandler
import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import dev.minn.jda.ktx.util.ref
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import kotlin.time.*
import java.time.Duration as JavaDuration

/**
 * Allows components to have timeouts.
 *
 * After the timeout has expired, the component will be deleted from the database.
 *
 * If the component is a group, then all of its owned components will also be deleted.
 *
 * If the component is inside a group, then all the group's components will also be deleted.
 *
 * **Components inside groups cannot have timeouts**.
 */
interface ITimeoutableComponent {
    val timeout: ComponentTimeout? //No need to use specific types in sub-interfaces as they're internal

    /**
     * Sets the timeout on this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     */
    fun timeout(timeout: Long, timeoutUnit: TimeUnit) =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()))

    /**
     * Sets the timeout on this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * @param timeout The duration of the timeout
     */
    fun timeout(timeout: JavaDuration) =
        timeout(timeout.toKotlinDuration())

    /**
     * Sets the timeout on this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * @param timeout The duration of the timeout
     */
    @JvmSynthetic
    fun timeout(timeout: Duration)
}

/**
 * Allows components to have persistent timeouts.
 *
 * These timeouts are represented by a method with a [ComponentTimeoutHandler] or [GroupTimeoutHandler] annotation on it,
 * and will still exist (and be rescheduled) after a restart.
 *
 * @see ITimeoutableComponent
 */
interface IPersistentTimeoutableComponent : ITimeoutableComponent {
    /**
     * Binds the given timeout handler name with its arguments to this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * The data passed is transformed with [toString][Object.toString] except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * **As always**, the data can only be reconstructed if a suitable [ComponentParameterResolver] exists for the type.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [ComponentTimeoutHandler] or [GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     */
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg data: Any?) =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *data)

    /**
     * Binds the given timeout handler name with its arguments to this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * The data passed is transformed with [toString][Object.toString] except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * **As always**, the data can only be reconstructed if a suitable [ComponentParameterResolver] exists for the type.
     *
     * @param timeout The duration of the timeout
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [ComponentTimeoutHandler] or [GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     */
    fun timeout(timeout: JavaDuration, handlerName: String, vararg data: Any?) =
        timeout(timeout.toKotlinDuration(), handlerName, *data)

    /**
     * Binds the given timeout handler name with its arguments to this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * The data passed is transformed with [toString][Object.toString] except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * **As always**, the data can only be reconstructed if a suitable [ComponentParameterResolver] exists for the type.
     *
     * @param timeout The duration of the timeout
     * @param handlerName The name of the handler to run when the button is clicked, defined by either [ComponentTimeoutHandler] or [GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     */
    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg data: Any?)
}

/**
 * Allows components to have ephemeral timeouts.
 *
 * These timeouts will not exist anymore after a restart.
 *
 * @see ITimeoutableComponent
 */
interface IEphemeralTimeoutableComponent : ITimeoutableComponent {
    /**
     * Binds the given handler to this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * **Be sure not to capture JDA entities in such handlers
     * as [their lifetime could have expired](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)**.
     *
     * @param timeout The duration before timeout
     * @param handler The handler to run when the button is clicked
     */
    fun timeout(timeout: JavaDuration, handler: Runnable) =
        timeout(timeout.toKotlinDuration()) { handler.run() }

    /**
     * Binds the given handler to this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * **Be sure not to capture JDA entities in such handlers
     * as [their lifetime could have expired](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)**.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     * @param handler The handler to run when the button is clicked
     */
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable) =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    /**
     * Binds the given handler to this component.
     *
     * After the timeout has expired, the component will be deleted from the database.
     *
     * If the component is a group, then all of its owned components will also be deleted.
     *
     * If the component is inside a group, then all the group's components will also be deleted.
     *
     * **Components inside groups cannot have timeouts**.
     *
     * **Be sure not to capture JDA entities in such handlers
     * as [their lifetime could have expired](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)**.
     *
     * You can still use [User.ref] and such from JDA-KTX to circumvent this issue.
     *
     * @param timeout The duration of the timeout
     * @param handler The handler to run when the button is clicked
     */
    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit)
}
