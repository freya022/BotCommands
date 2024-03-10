package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

/**
 * Paginator where pages are made from a list of entries, also adds buttons to choose an entry.
 *
 * @param E Type of the entries
 *
 * @see Paginator
 * @see Menu
 */
class ChoiceMenu<E> internal constructor(
    componentsService: Components,
    builder: ChoiceMenuBuilder<E>
) : BasicMenu<E, ChoiceMenu<E>>(
    componentsService,
    builder,
    makePages(builder.entries, builder.transformer, builder.rowPrefixSupplier, builder.maxEntriesPerPage)
) {
    private val buttonContentSupplier: ButtonContentSupplier<E> = builder.buttonContentSupplier
    private val callback: ChoiceCallback<E> = builder.callback

    override fun putComponents() {
        super.putComponents()

        val page = pages[page]!!
        val entries = page.entries

        entries.forEachIndexed { i, item ->
            val content = buttonContentSupplier.apply(item, i)
            val choiceButton: Button = componentsService.ephemeralButton(ButtonStyle.PRIMARY, content)
                .bindTo { event: ButtonEvent ->
                    this.cleanup()
                    callback.accept(event, item)
                }
                .constraints(constraints)
                .build()

            components.addComponents(1 + (i / 5), choiceButton)
        }
    }
}
