package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.modals.Modals.Companion.defaultTimeout
import java.time.Duration as JavaDuration
import javax.annotation.CheckReturnValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

/**
 * Methods for modals and modal inputs
 *
 * More details available on the [wiki](https://bc.freya02.dev/3.X/using-modals).
 */
@InterfacedService(acceptMultiple = false)
interface Modals {
    /**
     * Creates a new modal.
     *
     * You can add compatible JDA components in this builder,
     * see [ModalTopLevelComponent][net.dv8tion.jda.api.components.ModalTopLevelComponent].
     *
     * The modal expires after [a default timeout][defaultTimeout],
     * which can be overridden, or set by [ModalBuilder.timeout].
     *
     * @param title The title of the modal
     */
    @CheckReturnValue
    fun create(title: String): ModalBuilder

    companion object {
        @JvmSynthetic
        var defaultTimeout: Duration = 15.minutes

        @JvmStatic
        fun getDefaultTimeout(): JavaDuration = defaultTimeout.toJavaDuration()

        @JvmStatic
        fun setDefaultTimeout(defaultTimeout: JavaDuration) {
            this.defaultTimeout = defaultTimeout.toKotlinDuration()
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Modals.create(title: String, block: InlineModal.() -> Unit): Modal {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return InlineModal(create(title)).apply(block).build()
}
