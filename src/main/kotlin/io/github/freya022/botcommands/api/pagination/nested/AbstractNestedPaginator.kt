package io.github.freya022.botcommands.api.pagination.nested

import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginator
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.internal.utils.Checks

/**
 * @param T Type of the implementor
 */
abstract class AbstractNestedPaginator<T : AbstractNestedPaginator<T>> protected constructor(
    context: BContext,
    builder: AbstractNestedPaginatorBuilder<*, T>
) : AbstractPaginator<T>(
    context,
    builder
) {
    val usePaginatorControls: Boolean = builder.usePaginatorControls

    private val items: List<NestedPaginationItem<T>> = builder.items.toImmutableList()
    private val selectOptions = items.mapIndexed { i, item -> item.content.toSelectOption(i.toString()) }

    var selectedItemIndex: Int = 0
        protected set(value) {
            selectedItem.page = page
            field = value
            page = selectedItem.page
            maxPages = selectedItem.maxPages
        }
    val selectedItem: NestedPaginationItem<T>
        get() = items[selectedItemIndex]

    override var maxPages: Int = selectedItem.maxPages

    init {
        require(items.isNotEmpty()) { "No wrapped paginator have been added" }

        setSelectedItem(0)
    }

    protected open fun createSelectMenu(): StringSelectMenu {
        val options = selectOptions.mapIndexed { i, it -> it.withDefault(i == selectedItemIndex) }

        return selectMenus.stringSelectMenu().ephemeral()
            .bindTo(this::onItemSelected)
            .oneUse(true)
            .constraints(constraints)
            .addOptions(options)
            .build()
    }

    private fun onItemSelected(event: StringSelectEvent) {
        selectedItemIndex = event.values[0].toInt()
        event.editMessage(getCurrentMessage()).queue()
    }

    /**
     * Sets the selected wrapped pagination.
     *
     * This does not update the active pagination,
     * you can use [getCurrentMessage] and [InteractionHook.editOriginal] in order to update the message.
     *
     * @param index Index of the wrapped pagination, from `0` to `[the number of paginators] - 1`
     *
     * @return This instance for chaining convenience
     */
    fun setSelectedItem(index: Int): T {
        Checks.check(index >= 0, "Item index cannot be negative")
        Checks.check(index < items.size, "Item index cannot be higher than max items count (%d)", items.size)

        this.selectedItemIndex = index

        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /**
     * Sets the selected wrapped pagination.
     *
     * This does not update the active pagination,
     * you can use [getCurrentMessage] and [InteractionHook.editOriginal] in order to update the message.
     *
     * @param itemLabel Label which must be matched against [SelectContent.label]
     *
     * @return This instance for chaining convenience
     */
    fun setSelectedItem(itemLabel: String): T {
        Checks.notEmpty(itemLabel, "Item name cannot be empty")

        items.forEachIndexed { i, item ->
            if (item.content.label == itemLabel) {
                return setSelectedItem(i)
            }
        }

        throw IllegalArgumentException("Item named '$itemLabel' cannot be found in this pagination wrapper")
    }

    override fun writeMessage(builder: MessageCreateBuilder) {
        super.writeMessage(builder)

        val embedBuilder = EmbedBuilder()
        @Suppress("UNCHECKED_CAST")
        selectedItem.pageEditor.accept(this as T, builder, embedBuilder, page)
        builder.setEmbeds(embedBuilder.build())
    }

    override fun putComponents(builder: MessageCreateBuilder) {
        if (usePaginatorControls) {
            super.putComponents(builder)
        }

        builder.addActionRow(createSelectMenu())
    }
}
