package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.Usability
import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import java.util.*

internal class UsabilityImpl private constructor(override val unusableReasons: Set<UnusableReason>) : Usability {
    override val isUsable: Boolean
        get() = unusableReasons.isEmpty()

    override val isNotUsable: Boolean
        get() = !isUsable

    override val isVisible: Boolean = unusableReasons.all { it.isVisible }

    override val isNotVisible: Boolean
        get() = !isVisible

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