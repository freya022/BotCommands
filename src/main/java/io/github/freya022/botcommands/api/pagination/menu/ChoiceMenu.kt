package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier
import io.github.freya022.botcommands.api.pagination.TimeoutInfo
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import io.github.freya022.botcommands.api.pagination.transformer.EntryTransformer
import io.github.freya022.botcommands.api.utils.ButtonContent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.internal.utils.Checks

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
    constraints: InteractionConstraints,
    timeout: TimeoutInfo<ChoiceMenu<E>>?,
    hasDeleteButton: Boolean,
    firstContent: ButtonContent,
    previousContent: ButtonContent,
    nextContent: ButtonContent,
    lastContent: ButtonContent,
    deleteContent: ButtonContent,
    entries: List<E>,
    maxEntriesPerPage: Int,
    transformer: EntryTransformer<in E>,
    rowPrefixSupplier: RowPrefixSupplier,
    supplier: PaginatorSupplier<ChoiceMenu<E>>?,
    buttonContentSupplier: ButtonContentSupplier<E>,
    callback: ChoiceCallback<E>
) : BasicMenu<E, ChoiceMenu<E>>(
    componentsService,
    constraints,
    timeout,
    hasDeleteButton,
    firstContent,
    previousContent,
    nextContent,
    lastContent,
    deleteContent,
    makePages(entries, transformer, rowPrefixSupplier, maxEntriesPerPage),
    supplier
) {
    private val buttonContentSupplier: ButtonContentSupplier<E>
    private val callback: ChoiceCallback<E>

    init {
        Checks.notNull(buttonContentSupplier, "Button content supplier")
        Checks.notNull(callback, "Callback")

        this.buttonContentSupplier = buttonContentSupplier
        this.callback = callback
    }

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
