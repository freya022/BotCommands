package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * Paginator where pages are made from a list of entries, also adds buttons to choose an entry.
 *
 * @param E Type of the entries
 *
 * @see Paginator
 * @see Menu
 */
class ChoiceMenu<E> internal constructor(
    context: BContext,
    builder: ChoiceMenuBuilder<E>
) : BasicMenu<E, ChoiceMenu<E>>(
    context,
    builder,
    makePages(builder.entries, builder.transformer, builder.rowPrefixSupplier, builder.maxEntriesPerPage)
) {
    private val buttonContentSupplier: ButtonContentSupplier<E> = builder.buttonContentSupplier
    private val callback: ChoiceCallback<E> = builder.callback

    override fun putComponents(builder: MessageCreateBuilder) {
        super.putComponents(builder)

        val page = pages[page]!!
        page.entries.chunked(5) { items ->
            items.mapIndexed { i, item ->
                val content = buttonContentSupplier.apply(item, i)
                componentsService.ephemeralButton(ButtonStyle.PRIMARY, content)
                    .bindTo { event: ButtonEvent ->
                        this.cleanup()
                        callback.accept(event, item)
                    }
                    .constraints(constraints)
                    .build()
            }
        }
    }
}
