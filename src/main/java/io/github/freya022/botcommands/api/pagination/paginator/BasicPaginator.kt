package io.github.freya022.botcommands.api.pagination.paginator

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.pagination.BasicPagination
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier
import io.github.freya022.botcommands.api.pagination.TimeoutInfo
import io.github.freya022.botcommands.api.utils.ButtonContent
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageEditData
import kotlin.math.max

/**
 * @param T Type of the implementor
 */
abstract class BasicPaginator<T : BasicPaginator<T>> protected constructor(
    componentsService: Components,
    constraints: InteractionConstraints,
    timeout: TimeoutInfo<T>?,
    protected val supplier: PaginatorSupplier<T>?,
    hasDeleteButton: Boolean,
    firstContent: ButtonContent,
    previousContent: ButtonContent,
    nextContent: ButtonContent,
    lastContent: ButtonContent,
    deleteContent: ButtonContent
) : BasicPagination<T>(componentsService, constraints, timeout) {
    abstract var maxPages: Int
        protected set
    /**
     * The page number, after changing this value, you can update the message with the new content from [get].
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
        firstButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, firstContent)
            .bindTo { e: ButtonEvent ->
                page = 0
                e.editMessage(get()).queue()
            }
            .constraints(constraints)
            .build()

        previousButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, previousContent)
            .bindTo { e: ButtonEvent ->
                page = max(0.0, (page - 1).toDouble()).toInt()
                e.editMessage(get()).queue()
            }
            .constraints(constraints)
            .build()

        nextButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, nextContent)
            .bindTo { e: ButtonEvent ->
                page = (page + 1).coerceAtMost(maxPages - 1)
                e.editMessage(get()).queue()
            }
            .constraints(constraints)
            .build()

        lastButton = this.componentsService.ephemeralButton(ButtonStyle.PRIMARY, lastContent)
            .bindTo { e: ButtonEvent ->
                page = maxPages - 1
                e.editMessage(get()).queue()
            }
            .constraints(constraints)
            .build()

        if (hasDeleteButton) {
            //Unique use in the case the message isn't ephemeral
            this.deleteButton = this.componentsService.ephemeralButton(ButtonStyle.DANGER, deleteContent)
                .bindTo(::onDeleteClicked)
                .constraints(constraints)
                .oneUse(true)
                .build()
        } else {
            this.deleteButton = null
        }
    }

    private fun onDeleteClicked(e: ButtonEvent) {
        cancelTimeout()

        e.deferEdit().queue()
        e.hook.deleteOriginal().queue()

        cleanup()
    }

    override fun get(): MessageEditData {
        onPreGet()

        putComponents()

        val embed = getEmbed()

        messageBuilder.setEmbeds(embed)

        val rows = components.actionRows
        messageBuilder.setComponents(rows)

        onPostGet()

        return messageBuilder.build()
    }

    protected open fun getEmbed(): MessageEmbed =
        //TODO sussy
        supplier!!.get(this as T, messageBuilder, components, page)

    protected open fun putComponents() {
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

        if (deleteButton != null) {
            components.addComponents(
                firstButton,
                previousButton,
                nextButton,
                lastButton,
                deleteButton
            )
        } else {
            components.addComponents(
                firstButton,
                previousButton,
                nextButton,
                lastButton
            )
        }
    }
}
