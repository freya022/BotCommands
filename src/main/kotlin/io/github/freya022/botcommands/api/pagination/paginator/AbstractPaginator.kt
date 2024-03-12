package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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
            check(value in 0..<maxPages) {
                "Page needs to be between 0 and $maxPages (excluded)"
            }
            field = value
        }

    val isFirstPage: Boolean
        get() = page == 0
    val isLastPage: Boolean
        get() = page >= maxPages - 1

    private var firstButton: Button
    private var previousButton: Button
    private var nextButton: Button
    private var lastButton: Button
    private var deleteButton: Button? = null

    init {
        firstButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, builder.firstContent)
            .bindTo { e: ButtonEvent ->
                page = 0
                e.editMessage(getCurrentMessage()).queue()
            }
            .constraints(constraints)
            .build()

        previousButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, builder.previousContent)
            .bindTo { e: ButtonEvent ->
                page = (page - 1).coerceAtLeast(0)
                e.editMessage(getCurrentMessage()).queue()
            }
            .constraints(constraints)
            .build()

        nextButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, builder.nextContent)
            .bindTo { e: ButtonEvent ->
                page = (page + 1).coerceAtMost(maxPages - 1)
                e.editMessage(getCurrentMessage()).queue()
            }
            .constraints(constraints)
            .build()

        lastButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, builder.lastContent)
            .bindTo { e: ButtonEvent ->
                page = maxPages - 1
                e.editMessage(getCurrentMessage()).queue()
            }
            .constraints(constraints)
            .build()

        if (builder.hasDeleteButton) {
            //Unique use in the case the message isn't ephemeral
            this.deleteButton = this.componentsService.ephemeralButton(ButtonStyle.DANGER, builder.deleteContent)
                .bindTo(::onDeleteClicked)
                .constraints(constraints)
                .oneUse(true)
                .build()
        } else {
            this.deleteButton = null
        }
    }

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
        if (isFirstPage) {
            previousButton = previousButton.asDisabled()
            firstButton = firstButton.asDisabled()
        } else {
            previousButton = previousButton.asEnabled()
            firstButton = firstButton.asEnabled()
        }

        if (isLastPage) {
            nextButton = nextButton.asDisabled()
            lastButton = lastButton.asDisabled()
        } else {
            nextButton = nextButton.asEnabled()
            lastButton = lastButton.asEnabled()
        }

        val deleteButton = deleteButton
        val row = if (deleteButton != null) {
            ActionRow.of(firstButton, previousButton, nextButton, lastButton, deleteButton)
        } else {
            ActionRow.of(firstButton, previousButton, nextButton, lastButton)
        }
        builder.addComponents(row)
    }
}
