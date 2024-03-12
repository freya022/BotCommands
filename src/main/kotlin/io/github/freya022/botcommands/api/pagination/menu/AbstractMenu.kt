package io.github.freya022.botcommands.api.pagination.menu

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.PageEditor
import io.github.freya022.botcommands.api.pagination.menu.transformer.EntryTransformer
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginator
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.internal.utils.Checks

/**
 * @param E Type of the menu elements
 * @param T Type of the implementor
 */
abstract class AbstractMenu<E, T : AbstractMenu<E, T>> protected constructor(
    context: BContext,
    builder: AbstractMenuBuilder<*, *, T>,
    protected val pages: Map<Int, MenuPage<E>>
) : AbstractPaginator<T>(
    context,
    builder
) {
    private val editor: PageEditor<T>? = builder.pageEditor

    override var maxPages: Int = pages.size

    override fun writeMessage(builder: MessageCreateBuilder) {
        super.writeMessage(builder)

        val menuPage = pages[page]!!
        val embedBuilder = EmbedBuilder()
        embedBuilder.setDescription(menuPage.content)
        @Suppress("UNCHECKED_CAST")
        editor?.accept(this as T, builder, embedBuilder, page)

        builder.setEmbeds(embedBuilder.build())
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

            entries.forEachIndexed { i, entry ->
                val s = transformer.toString(entry)
                Checks.notLonger(s, MessageEmbed.TEXT_MAX_LENGTH - 8, "Entry #$i string")

                if (i - oldEntry >= maxEntriesPerPage || builder.length + s.length > MessageEmbed.TEXT_MAX_LENGTH - 8) {
                    pages[page] = MenuPage(builder.toString(), entries.subList(oldEntry, i))

                    page++
                    oldEntry = i

                    builder.setLength(0)
                }

                builder.append(rowPrefixSupplier.apply(i - oldEntry, maxEntriesPerPage)).append(s).append('\n')
            }

            pages[page] = MenuPage(builder.toString(), entries.subList(oldEntry, entries.size))

            return pages
        }
    }
}
