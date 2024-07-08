package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import java.util.*

/**
 * Represents whether a command can be used, and if it should be visible.
 */
class Usability private constructor(val unusableReasons: Set<UnusableReason>) {
    /**
     * Returns `true` if the command can be executed.
     */
    val isUsable: Boolean
        get() = unusableReasons.isEmpty()

    /**
     * Returns `true` if the command cannot be executed.
     */
    val isNotUsable: Boolean
        get() = !isUsable

    /**
     * Returns `true` if the command should be visible, even if unusable (in help content, for example).
     */
    val isVisible: Boolean = unusableReasons.all { it.isVisible }

    /**
     * @return `true` if the command should **not** be visible (in help content, for example).
     */
    val isNotVisible: Boolean
        get() = !isVisible

    /**
     * Returns the most important un-usability reason.
     */
    val bestReason: UnusableReason
        get() = unusableReasons.maxBy { it.priority }

    enum class UnusableReason(
        internal val priority: Int,
        /**
         * Returns `true` if the command should be visible, even if unusable (in help content, for example).
         */
        val isVisible: Boolean
    ) {
        HIDDEN          (priority = 4, isVisible = false),
        OWNER_ONLY      (priority = 3, isVisible = false),
        USER_PERMISSIONS(priority = 2, isVisible = false),
        BOT_PERMISSIONS (priority = 1, isVisible = true),
        NSFW_ONLY       (priority = 0, isVisible = false)
    }

    internal companion object {
        @JvmSynthetic
        internal inline fun build(crossinline block: EnumSet<UnusableReason>.() -> Unit) =
            enumSetOf<UnusableReason>()
                .apply(block)
                .unmodifiableView()
                .let(::Usability)
    }
}