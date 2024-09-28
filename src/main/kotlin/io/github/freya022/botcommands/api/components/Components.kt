package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.Components.Companion.defaultTimeout
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
 *  - Have timeouts, [a default timeout][defaultTimeout] is set **on ephemeral components**,
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
         * The default timeout for components and component groups.
         *
         * Non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmSynthetic
        var defaultTimeout: Duration = 15.minutes

        @JvmStatic
        fun getDefaultTimeout(): JavaDuration = defaultTimeout.toJavaDuration()

        /**
         * Sets the default timeout for components and component groups.
         *
         * Non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        fun setDefaultTimeout(defaultTimeout: JavaDuration) {
            this.defaultTimeout = defaultTimeout.toKotlinDuration()
        }
    }
}