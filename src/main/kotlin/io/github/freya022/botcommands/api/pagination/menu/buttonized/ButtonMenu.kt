package io.github.freya022.botcommands.api.pagination.menu.buttonized

import io.github.freya022.botcommands.api.components.event.ButtonEvent
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
    private val styledButtonContentSupplier: StyledButtonContentSupplier<E> = builder.styledButtonContentSupplier
    private val callback: SuspendingChoiceCallback<E> = builder.callback

    override fun putComponents(builder: MessageCreateBuilder) {
        super.putComponents(builder)

        pages[page]!!.entries
            .mapIndexed { i, item ->
                val styledContent = styledButtonContentSupplier.apply(item, i)
                componentsService.ephemeralButton(styledContent.style, styledContent.content)
                    .bindTo { event: ButtonEvent ->
                        this.cleanup()
                        callback(event, item)
                    }
                    .constraints(constraints)
                    .build()
            }
            .chunked(5, ActionRow::of)
            .also(builder::addComponents)
    }
}
