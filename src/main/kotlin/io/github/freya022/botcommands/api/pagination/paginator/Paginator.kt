package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.pagination.PageEditor
import io.github.freya022.botcommands.api.pagination.menu.Menu
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * Classic paginator, pages are supplied from [paginator suppliers][PaginatorBuilder.setPaginatorSupplier].
 *
 * You provide the pages, it displays them one by one.
 *
 * Initial page is page 0, there is navigation buttons and an optional delete button
 *
 * **The button IDs used by this paginator and those registered by the [PaginatorComponents] in the [PageEditor] are cleaned up once the embed is removed with the button**
 *
 * When the message is deleted, you would also have to call [cleanup]
 *
 * @see Menu
 */
class Paginator internal constructor(
    componentsService: Components,
    builder: PaginatorBuilder
) : BasicPaginator<Paginator>(
    componentsService,
    builder
) {
    private val editor: PageEditor<Paginator> = builder.pageEditor

    override var maxPages: Int = builder.maxPages

    override fun writeMessage(builder: MessageCreateBuilder) {
        super.writeMessage(builder)

        val embedBuilder = EmbedBuilder()
        editor.accept(this, builder, embedBuilder, page)
        builder.setEmbeds(embedBuilder.build())
    }
}
