package io.github.freya022.botcommands.api.modals.annotations

/**
 * Set this parameter as a modal input.
 *
 * The specified input custom ID must be the same as the custom ID of a component in that modal.
 *
 * Supported types for each component:
 * - [TextInput][net.dv8tion.jda.api.components.textinput.TextInput]: `String`
 * - [StringSelectMenu][net.dv8tion.jda.api.components.selections.StringSelectMenu]: `List<String>`
 *
 * @see ModalData @ModalData
 * @see ModalHandler @ModalHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalInput(
    /**
     * The custom ID this modal input will match against.
     */
    @get:JvmName("value") val customId: String
)
