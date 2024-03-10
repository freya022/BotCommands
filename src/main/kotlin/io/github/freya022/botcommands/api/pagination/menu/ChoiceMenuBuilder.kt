package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.utils.ButtonContent

/**
 * Builds a [ChoiceMenu].
 *
 * The callback and the button content supplier must be set!
 *
 * @param E Type of the entries
 */
class ChoiceMenuBuilder<E>(
    componentsService: Components,
    entries: List<E>
) : BasicMenuBuilder<E, ChoiceMenuBuilder<E>, ChoiceMenu<E>>(componentsService, entries) {
    lateinit var callback: ChoiceCallback<E>
        private set
    lateinit var buttonContentSupplier: ButtonContentSupplier<E>
        private set

    /**
     * Sets the callback for this menu
     *
     * @param callback The [ChoiceCallback] to call when the user makes their choice
     *
     * @return This builder for chaining convenience
     */
    fun setCallback(callback: ChoiceCallback<E>): ChoiceMenuBuilder<E> = config {
        this.callback = callback
    }

    /**
     * Sets the button content supplier for this menu, allowing you to use custom buttons (text / emoji)
     *
     * You get handed the object the button is bound to, as well as the object's index in the current page
     *
     * So if you have a maximum of 5 items per page, your index is between 0 (included) and 5 (excluded)
     *
     * @param buttonContentSupplier The function which accepts an item of type **T** and an **item index** (local to the current page), and returns a [ButtonContent]
     *
     * @return This builder for chaining convenience
     *
     * @see ButtonContent.withString
     * @see ButtonContent.withEmoji
     */
    fun setButtonContentSupplier(buttonContentSupplier: ButtonContentSupplier<E>): ChoiceMenuBuilder<E> = config {
        this.buttonContentSupplier = buttonContentSupplier
    }

    override fun build(): ChoiceMenu<E> = ChoiceMenu(
        componentsService,
        this
    )
}
