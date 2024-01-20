package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

/**
 * Methods for modals and modal inputs
 */
@InterfacedService(acceptMultiple = false)
interface Modals {
    /**
     * Creates a new modal.
     *
     * @param title The title of the modal
     */
    fun create(title: String): ModalBuilder

    /**
     * Creates a new text input component.
     *
     * @param inputName The name of the input, set in [@ModalInput][ModalInput]
     * @param label     The label to display on top of the text field
     * @param style     The style of the text field
     */
    fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder
}
