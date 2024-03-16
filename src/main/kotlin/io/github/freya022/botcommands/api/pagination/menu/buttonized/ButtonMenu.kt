package io.github.freya022.botcommands.api.pagination.menu.buttonized

import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators
import io.github.freya022.botcommands.api.pagination.menu.AbstractMenu
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * A paginator where each page is filled with a list of entries.
 *
 * @param E Type of the entries
 *
 * @see Paginators.buttonMenu
 */
class ButtonMenu<E> internal constructor(
    context: BContext,
    builder: ButtonMenuBuilder<E>
) : AbstractMenu<E, ButtonMenu<E>>(
    context,
    builder,
    makePages(builder.entries, builder.transformer, builder.rowPrefixSupplier, builder.maxEntriesPerPage)
) {
    /**
     * A [ButtonContent] supplier for use in different paginators,
     * allowing you to use your own text and emojis on buttons.
     *
     * @param T Item type
     *
     * @see ButtonContent.fromLabel
     * @see ButtonContent.fromEmoji
     */
    fun interface ButtonContentSupplier<T> {
        /**
         * Returns a [ButtonContent] based on the given item and the current page number of the paginator
         *
         * @param item  The item bound to this button
         * @param index The index of this item on the current page number of the paginator
         * @return The [ButtonContent] of this item
         */
        fun apply(item: T, index: Int): ButtonContent
    }

    private val buttonContentSupplier: ButtonContentSupplier<E> = builder.buttonContentSupplier
    private val callback: SuspendingChoiceCallback<E> = builder.callback
    private val reusable: Boolean = builder.reusable

    override fun putComponents(builder: MessageCreateBuilder) {
        super.putComponents(builder)

        pages[page]!!.entries
            .mapIndexed { i, item ->
                val styledContent = buttonContentSupplier.apply(item, i)
                buttons.button(styledContent).ephemeral()
                    .bindTo { event: ButtonEvent ->
                        if (reusable) {
                            restartTimeout()
                        } else {
                            cleanup()
                        }
                        callback(event, item)
                    }
                    .constraints(constraints)
                    .build()
            }
            .chunked(5, ActionRow::of)
            .also(builder::addComponents)
    }

    object Defaults {
        /** @see ButtonMenuBuilder.setReusable */
        @JvmStatic
        var reusable: Boolean = false
    }
}
