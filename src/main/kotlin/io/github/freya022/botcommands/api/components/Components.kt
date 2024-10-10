package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

/**
 * This class lets you create smart components such as buttons, select menus, and groups.
 *
 * Every component can either be persistent or ephemeral, all components can be configured to:
 *  - Be used once
 *  - Have timeouts, a default timeout can be configured,
 *  which can be overridden, or set by the `timeout` methods.
 *  - Have handlers
 *  - Have constraints (checks before the button can be used)
 *
 * Except component groups which can only have their timeout configured,
 * their default timeouts are the same as components.
 *
 * ### Persistent components
 *  - Kept after restart
 *  - Handlers are methods; they can have arguments passed to them
 *  - Timeouts are also methods, additionally, they will be rescheduled when the bot restarts
 *
 * ### Ephemeral components
 *  - Are deleted once the bot restarts
 *  - Handlers are closures, they can capture objects, but you [shouldn't capture JDA entities](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)
 *  - Timeouts are also closures, but are not rescheduled when restarting
 *
 * ### Component groups
 *  - If deleted, all contained components are deleted
 *  - If one of the contained components is deleted, then all of its subsequent groups are also deleted
 *
 * **Note:** Component groups cannot contain components with timeouts,
 * you will need to [disable the timeout on the components][ITimeoutableComponent.noTimeout].
 *
 * @see RequiresComponents @RequiresComponents
 * @see Buttons
 * @see SelectMenus
 */
@BService
@RequiresComponents
class Components internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    companion object {
        /**
         * The default timeout for *ephemeral* components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @Deprecated("Renamed to defaultEphemeralTimeout", ReplaceWith("this.defaultEphemeralTimeout"))
        var defaultTimeout: Duration
            @JvmSynthetic
            get() = defaultEphemeralTimeout!!
            @JvmSynthetic
            set(value) {
                defaultEphemeralTimeout = value
            }

        /**
         * The default timeout for *ephemeral* components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        @Deprecated("Renamed to getDefaultTimeout", ReplaceWith("this.getDefaultTimeout()"))
        fun getDefaultTimeout(): JavaDuration = defaultEphemeralTimeout!!.toJavaDuration()

        /**
         * Sets the default timeout for *ephemeral* components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        @Deprecated("Renamed to setDefaultTimeout", ReplaceWith("this.setDefaultTimeout(timeout)"))
        fun setDefaultTimeout(timeout: JavaDuration) {
            defaultEphemeralTimeout = timeout.toKotlinDuration()
        }

        /**
         * The default timeout for ephemeral components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmSynthetic
        var defaultEphemeralTimeout: Duration? = 15.minutes

        /**
         * The default timeout for ephemeral components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        fun getDefaultEphemeralTimeout(): JavaDuration? = defaultEphemeralTimeout?.toJavaDuration()

        /**
         * Sets the default timeout for ephemeral components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        fun setDefaultEphemeralTimeout(timeout: JavaDuration?) {
            defaultEphemeralTimeout = timeout?.toKotlinDuration()
        }

        /**
         * The default timeout for persistent components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmSynthetic
        var defaultPersistentTimeout: Duration? = null

        /**
         * The default timeout for persistent components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        fun getDefaultPersistentTimeout(): JavaDuration? = defaultPersistentTimeout?.toJavaDuration()

        /**
         * Sets the default timeout for persistent components and component groups.
         *
         * `null`, non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        fun setDefaultPersistentTimeout(timeout: JavaDuration?) {
            defaultPersistentTimeout = timeout?.toKotlinDuration()
        }
    }
}