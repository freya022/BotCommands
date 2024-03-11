package io.github.freya022.botcommands.api.pagination.wrapper

import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.internal.utils.Checks
import okhttp3.internal.toImmutableList

/**
 * @param T Type of the implementor
 */
abstract class AbstractPaginationWrapper<T : AbstractPaginationWrapper<T, W>, W : AbstractPagination<W>> protected constructor(
    context: BContext,
    builder: AbstractPaginationWrapperBuilder<*, T, W>
) : AbstractPagination<T>(
    context,
    builder
) {
    protected val editor: PaginationWrapperPageEditor<T>? = builder.editor

    protected val items: List<WrappedPaginationItem<W>> = builder.items.toImmutableList()
    private val selectOptions = items.mapIndexed { i, item -> item.content.toSelectOption(i.toString()) }

    var selectedItemIndex: Int = 0
        protected set
    val selectedItem: WrappedPaginationItem<W>
        get() = items[selectedItemIndex]

    init {
        check(items.isNotEmpty()) { "No wrapped paginator have been added" }

        setSelectedItem(0)
    }

    protected open fun createSelectMenu(): StringSelectMenu {
        val options = selectOptions.onEachIndexed { i, it -> it.withDefault(i == selectedItemIndex) }

        return componentsService.ephemeralStringSelectMenu()
            .bindTo { event: StringSelectEvent -> this.onItemSelected(event) }
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

    override fun restartTimeout() = forEachInitializedPagination { it.restartTimeout() }

    override fun cancelTimeout() = forEachInitializedPagination { it.cancelTimeout() }

    private inline fun forEachInitializedPagination(block: (W) -> Unit) = items.forEach {
        if ((it::wrappedPagination.getDelegate() as Lazy<*>).isInitialized()) {
            block(it.wrappedPagination)
        }
    }

    override fun writeMessage(builder: MessageCreateBuilder) {
        builder.applyData(selectedItem.wrappedPagination.getInitialMessage())

        putComponents(builder)

        @Suppress("UNCHECKED_CAST")
        editor?.accept(this as T, builder)
    }

    protected open fun putComponents(builder: MessageCreateBuilder) {
        builder.addActionRow(createSelectMenu())
    }
}
