@file:OptIn(ExperimentalContracts::class)

package io.github.freya022.botcommands.api.modals

import dev.minn.jda.ktx.interactions.components.row
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.modals.Modals.Companion.defaultTimeout
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
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
 */
@InterfacedService(acceptMultiple = false)
interface Modals {
    /**
     * Creates a new modal.
     *
     * The modal expires after [a default timeout][defaultTimeout],
     * which can be overridden, or set by [ModalBuilder.timeout].
     *
     * @param title The title of the modal
     */
    @CheckReturnValue
    fun create(title: String): ModalBuilder

    /**
     * Creates a new text input component.
     *
     * @param inputName The name of the input, set in [@ModalInput][ModalInput]
     * @param label     The label to display on top of the text field
     * @param style     The style of the text field
     */
    @CheckReturnValue
    fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder

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

fun Modals.create(title: String, block: ModalBuilder.() -> Unit): Modal {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return create(title).apply(block).build()
}

fun ModalBuilder.textInput(inputName: String, label: String, inputStyle: TextInputStyle, block: TextInputBuilder.() -> Unit = {}): TextInput {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return modals.createTextInput(inputName, label, inputStyle)
        .apply(block)
        .build()
        .also { addComponents(row(it)) }
}

fun ModalBuilder.shortTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}): TextInput {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return textInput(inputName, label, TextInputStyle.SHORT, block)
}

fun ModalBuilder.paragraphTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}): TextInput {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return textInput(inputName, label, TextInputStyle.PARAGRAPH, block)
}
