@file:OptIn(ExperimentalContracts::class)

package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import javax.annotation.CheckReturnValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

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

fun Modals.create(title: String, block: context(Modals) ModalBuilder.() -> Unit): Modal {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return create(title).apply { block(this@create, this) }.build()
}

context(Modals)
fun ModalBuilder.textInput(inputName: String, label: String, inputStyle: TextInputStyle, block: TextInputBuilder.() -> Unit = {}): TextInput =
    createTextInput(inputName, label, inputStyle)
        .apply(block)
        .build()
        .also { addActionRow(it) }

context(Modals)
fun ModalBuilder.shortTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}): TextInput =
    textInput(inputName, label, TextInputStyle.SHORT, block)

context(Modals)
fun ModalBuilder.paragraphTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}): TextInput =
    textInput(inputName, label, TextInputStyle.PARAGRAPH, block)
