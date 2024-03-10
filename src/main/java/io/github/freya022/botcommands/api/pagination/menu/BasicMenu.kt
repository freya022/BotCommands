package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginator
import io.github.freya022.botcommands.api.pagination.transformer.EntryTransformer
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.internal.utils.Checks

/**
 * @param E Type of the menu elements
 * @param T Type of the implementor
 */
abstract class BasicMenu<E, T : BasicMenu<E, T>> protected constructor(
    componentsService: Components,
    builder: BasicMenuBuilder<*, *, T>,
    protected val pages: Map<Int, MenuPage<E>>
) : BasicPaginator<T>(
    componentsService,
    builder
) {
    override var maxPages: Int = pages.size

    override fun getEmbed(): MessageEmbed {
        val builder = when {
            supplier != null -> EmbedBuilder(supplier.get(this as T, messageBuilder, components, page))
            else -> EmbedBuilder()
        }

        val menuPage = pages[page]!!

        builder.appendDescription(menuPage.content)

        return builder.build()
    }

    companion object {
        @JvmStatic
        protected fun <E> makePages(
            entries: List<E>,
            transformer: EntryTransformer<E>,
            rowPrefixSupplier: RowPrefixSupplier,
            maxEntriesPerPage: Int
        ): Map<Int, MenuPage<E>> {
            val pages: MutableMap<Int, MenuPage<E>> = HashMap()

            var page = 0
            var oldEntry = 0
            val builder = StringBuilder()

            var i = 0
            val entriesSize = entries.size
            while (i < entriesSize) {
                val entry = entries[i]

                val s = transformer.toString(entry)
                Checks.notLonger(s, MessageEmbed.TEXT_MAX_LENGTH - 8, "Entry #$i string")

                if (i - oldEntry >= maxEntriesPerPage || builder.length + s.length > MessageEmbed.TEXT_MAX_LENGTH - 8) {
                    pages[page] = MenuPage(builder.toString(), entries.subList(oldEntry, i))

                    page++
                    oldEntry = i

                    builder.setLength(0)
                }

                builder.append(rowPrefixSupplier.apply(i - oldEntry + 1, maxEntriesPerPage)).append(s).append('\n')
                i++
            }

            pages[page] = MenuPage(builder.toString(), entries.subList(oldEntry, entries.size))

            return pages
        }
    }
}
