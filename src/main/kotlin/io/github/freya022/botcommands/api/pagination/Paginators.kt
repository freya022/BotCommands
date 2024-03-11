package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.pagination.interactive.InteractiveMenuBuilder
import io.github.freya022.botcommands.api.pagination.menu.ChoiceMenuBuilder
import io.github.freya022.botcommands.api.pagination.menu.MenuBuilder
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import io.github.freya022.botcommands.api.pagination.paginator.PaginatorBuilder
import io.github.freya022.botcommands.api.utils.ButtonContent

@BService
@Dependencies(Components::class)
class Paginators(private val context: BContext) {
    fun paginator(pageEditor: PageEditor<Paginator>): PaginatorBuilder =
        PaginatorBuilder(context, pageEditor)

    fun <E> menu(entries: List<E>): MenuBuilder<E> =
        MenuBuilder(context, entries)

    fun <E> choiceMenu(entries: List<E>): ChoiceMenuBuilder<E> =
        ChoiceMenuBuilder(context, entries)

    fun interactionMenu(): InteractiveMenuBuilder =
        InteractiveMenuBuilder(context)

    object Defaults {
        @JvmStatic
        var firstPageButtonContent: ButtonContent = ButtonContent.withShortcode("rewind")
        @JvmStatic
        var previousPageButtonContent: ButtonContent = ButtonContent.withShortcode("arrow_backward")
        @JvmStatic
        var nextPageButtonContent: ButtonContent = ButtonContent.withShortcode("arrow_forward")
        @JvmStatic
        var lastPageButtonContent: ButtonContent = ButtonContent.withShortcode("fast_forward")
        @JvmStatic
        var deleteButtonContent: ButtonContent = ButtonContent.withShortcode("wastebasket")
    }
}