package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 * @param T Type of the implementor
 */
abstract class AbstractPaginator<T : AbstractPaginator<T>> protected constructor(
    context: BContext,
    builder: AbstractPaginatorBuilder<*, T>
) : AbstractPagination<T>(context, builder) {
    abstract var maxPages: Int
        protected set

    /**
     * The page number, after changing this value, you can update the message with the new content from [getCurrentMessage].
     *
     * The page must be between `0` and [`maxPages - 1`][maxPages]
     */
    var page: Int = 0
        set(value) {
            // 0 <= value < maxPages
            require(value in 0..<maxPages) {
                "Page needs to be between 0 and $maxPages (excluded)"
            }
            field = value
        }

    val isFirstPage: Boolean
        get() = page == 0
    val isLastPage: Boolean
        get() = page >= maxPages - 1

    private val firstButton: ButtonContent = builder.firstContent
    private val previousButton: ButtonContent = builder.previousContent
    private val nextButton: ButtonContent = builder.nextContent
    private val lastButton: ButtonContent = builder.lastContent
    private val deleteButton: ButtonContent? = builder.deleteContent.takeIf { builder.hasDeleteButton }

    private suspend fun onDeleteClicked(e: ButtonEvent) {
        e.deferEdit().queue()
        e.hook.deleteOriginal().queue()

        cancelTimeout()
        cleanup()
    }

    override fun writeMessage(builder: MessageCreateBuilder) {
        putComponents(builder)
    }

    protected open fun putComponents(builder: MessageCreateBuilder) {
        val buttons = buildList {
            this += firstButton.toPageButton(targetPage = 0).withDisabled(isFirstPage)
            this += previousButton.toPageButton(targetPage = page - 1).withDisabled(isFirstPage)

            this += nextButton.toPageButton(targetPage = page + 1).withDisabled(isLastPage)
            this += lastButton.toPageButton(targetPage = maxPages - 1).withDisabled(isLastPage)

            if (deleteButton != null) {
                this += componentsService.ephemeralButton(deleteButton)
                    .bindTo(::onDeleteClicked)
                    .constraints(constraints)
                    .build()
            }
        }

        builder.addActionRow(buttons)
    }

    private fun ButtonContent.toPageButton(targetPage: Int) =
        componentsService.ephemeralButton(this)
            .bindTo { e: ButtonEvent ->
                page = targetPage.coerceIn(0, maxPages - 1)
                e.editMessage(getCurrentMessage()).queue()
            }
            .constraints(constraints)
            .build()
}
