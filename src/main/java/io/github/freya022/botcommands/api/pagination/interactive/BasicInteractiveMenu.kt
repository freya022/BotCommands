package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier
import io.github.freya022.botcommands.api.pagination.TimeoutInfo
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginator
import io.github.freya022.botcommands.api.utils.ButtonContent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.utils.Checks

/**
 * @param T Type of the implementor
 */
abstract class BasicInteractiveMenu<T : BasicInteractiveMenu<T>> protected constructor(
    componentsService: Components,
    constraints: InteractionConstraints,
    timeout: TimeoutInfo<T>?,
    hasDeleteButton: Boolean,
    firstContent: ButtonContent,
    previousContent: ButtonContent,
    nextContent: ButtonContent,
    lastContent: ButtonContent,
    deleteContent: ButtonContent,
    @JvmField
    protected val items: List<InteractiveMenuItem<T>>,
    @JvmField
    protected val usePaginator: Boolean
) : BasicPaginator<T>(
    componentsService,
    constraints,
    timeout,
    0,
    PaginatorSupplier { _, _, _, _ -> EmbedBuilder().build() },
    hasDeleteButton,
    firstContent,
    previousContent,
    nextContent,
    lastContent,
    deleteContent
) {
    var selectedItem: Int = 0
        protected set
    val selectedItemContent: SelectContent
        get() = items[selectedItem].content

    init {
        check(items.isNotEmpty()) { "No interactive menu items has been added" }

        setSelectedItem(0)
    }

    protected fun createSelectMenu(): StringSelectMenu {
        val options = arrayOfNulls<SelectOption>(items.size)
        var i = 0
        val itemsSize = items.size
        while (i < itemsSize) {
            val item = items[i]

            var option = item.content.toSelectOption(i.toString())
            if (i == selectedItem) option = option.withDefault(true)

            options[i] = option
            i++
        }

        return componentsService.ephemeralStringSelectMenu()
            .bindTo { event: StringSelectEvent -> this.onItemSelected(event) }
            .oneUse(true)
            .constraints(constraints)
            .addOptions(*options)
            .build()
    }

    private fun onItemSelected(event: StringSelectEvent) {
        selectedItem = event.values[0].toInt()

        event.editMessage(get()).queue()
    }

    /**
     * Sets the interactive menu item number, **this does not update the embed in any way**,
     * you can use [get] with an [InteractionHook.editOriginal] in order to update the embed on Discord
     *
     * @param itemIndex Index of the item, from `0` to `[the number of menus] - 1`
     *
     * @return This instance for chaining convenience
     */
    fun setSelectedItem(itemIndex: Int): T {
        Checks.check(itemIndex >= 0, "Item index cannot be negative")
        Checks.check(itemIndex < items.size, "Item index cannot be higher than max items count (%d)", items.size)

        this.selectedItem = itemIndex
        maxPages = items[itemIndex].maxPages
        page = 0

        return this as T
    }

    /**
     * Sets the interactive menu item number, via it's label (O(n) search), **this does not update the embed in any way**,
     * you can use [get] with an [InteractionHook.editOriginal] in order to update the embed on Discord
     *
     * @param itemLabel Label of the item, must be a valid label from any of the interactive menu items
     *
     * @return This instance for chaining convenience
     */
    fun setSelectedItem(itemLabel: String): T {
        Checks.notEmpty(itemLabel, "Item name cannot be empty")

        for (i in items.indices) {
            val label = items[i].content.label

            if (label == itemLabel) {
                return setSelectedItem(i)
            }
        }

        throw IllegalArgumentException("Item name '$itemLabel' cannot be found in this interactive menu")
    }

    override fun get(): MessageEditData {
        onPreGet()

        if (usePaginator) {
            putComponents()
        }

        components.addComponents(createSelectMenu())

        val embed = items[selectedItem].supplier.get(this as T, page, messageBuilder, components)
        messageBuilder.setEmbeds(embed)
        messageBuilder.setComponents(components.actionRows)

        onPostGet()

        return messageBuilder.build()
    }
}
