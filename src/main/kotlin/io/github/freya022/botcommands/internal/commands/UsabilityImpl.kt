package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.Usability
import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import java.util.*

/**
 * Represents whether a command can be used, and if it should be visible.
 */
internal class UsabilityImpl private constructor(override val unusableReasons: Set<UnusableReason>) : Usability {
    /**
     * Returns `true` if the command can be executed.
     */
    override val isUsable: Boolean
        get() = unusableReasons.isEmpty()

    /**
     * Returns `true` if the command cannot be executed.
     */
    override val isNotUsable: Boolean
        get() = !isUsable

    /**
     * Returns `true` if the command should be visible, even if unusable (in help content, for example).
     */
    override val isVisible: Boolean = unusableReasons.all { it.isVisible }

    /**
     * @return `true` if the command should **not** be visible (in help content, for example).
     */
    override val isNotVisible: Boolean
        get() = !isVisible

    /**
     * Returns the most important un-usability reason.
     */
    override val bestReason: UnusableReason
        get() = unusableReasons.maxBy { it.priority }

    internal companion object {
        internal inline fun build(crossinline block: EnumSet<UnusableReason>.() -> Unit) =
            enumSetOf<UnusableReason>()
                .apply(block)
                .unmodifiableView()
                .let(::UsabilityImpl)
    }
}