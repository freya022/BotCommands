package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.utils.toEditData
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.time.Duration.Companion.minutes

private class Series(val name: String)

// https://ptb.discord.com/channels/125227483518861312/1341787170884681769
@Command
class StateExample(
    private val buttons: Buttons,
) : ApplicationCommand() {

    private val states: MutableMap<Long, SeriesState> = hashMapOf()

    private interface SeriesState {
        val interactionId: Long

        suspend fun getMessage(): MessageCreateData

        fun nextPage(): SeriesState
        fun previousPage(): SeriesState

        fun homePage(): SeriesState
        fun viewCharacters(): SeriesState
        fun viewEvents(): SeriesState
    }

    // idr the name of the pattern
    // the idea is to never have to re-store the new state,
    // i.e., we're encapsulating the state inside a state, then delegating calls to the actual state.
    private class SeriesStateWrapper(
        private var state: SeriesState,
    ) : SeriesState {

        override val interactionId: Long get() = state.interactionId

        override suspend fun getMessage(): MessageCreateData = state.getMessage()

        override fun nextPage(): SeriesState {
            state = state.nextPage()
            return state
        }

        override fun previousPage(): SeriesState {
            state = state.previousPage()
            return state
        }

        override fun homePage(): SeriesState {
            state = state.homePage()
            return state
        }

        override fun viewCharacters(): SeriesState {
            state = state.viewCharacters()
            return state
        }

        override fun viewEvents(): SeriesState {
            state = state.viewEvents()
            return state
        }
    }

    private inner class SeriesHomeState(
        override val interactionId: Long,
        private val series: Series,
    ) : SeriesState {
        override suspend fun getMessage(): MessageCreateData {
            return MessageCreate {
                content = "This is the home content for ${series.name}"

                val viewCharacters = buttons.secondary("View characters").ephemeral {
                    singleUse = true
                    noTimeout() // Group handles that

                    bindTo { event ->
                        event.editMessage(viewCharacters().getMessage().toEditData()).queue()
                    }
                }

                val viewEvents = buttons.secondary("View events").ephemeral {
                    singleUse = true
                    noTimeout() // Group handles that

                    bindTo { event ->
                        event.editMessage(viewEvents().getMessage().toEditData()).queue()
                    }
                }

                // Group them so they all get deleted when one is used
                buttons.group(viewCharacters, viewEvents).ephemeral {
                    timeout(15.minutes) {
                        states.remove(interactionId)
                    }
                }

                components += row(viewCharacters, viewEvents)
            }
        }

        override fun nextPage(): SeriesState = throw IllegalStateException("Cannot go to next page")

        override fun previousPage(): SeriesState = throw IllegalStateException("Cannot go to previous page")

        override fun homePage(): SeriesState = this

        override fun viewCharacters(): SeriesState = SeriesViewCharactersState(interactionId, series)

        override fun viewEvents(): SeriesState = SeriesViewEventsState(interactionId, series)
    }

    private inner class SeriesViewCharactersState(
        override val interactionId: Long,
        private val series: Series,
    ) : SeriesState {

        private var page = 0

        override suspend fun getMessage(): MessageCreateData = MessageCreate {
            content = "This is the characters page #$page for series ${series.name}"

            val back = buttons.secondary("Back").ephemeral {
                singleUse = true
                noTimeout() // Group handles that

                bindTo { event ->
                    event.editMessage(homePage().getMessage().toEditData()).queue()
                }
            }

            val previousPage = buttons.secondary("Previous page").ephemeral {
                singleUse = true
                noTimeout() // Group handles that

                bindTo { event ->
                    event.editMessage(previousPage().getMessage().toEditData()).queue()
                }
            }

            val nextPage = buttons.secondary("Next page").ephemeral {
                singleUse = true
                noTimeout() // Group handles that

                bindTo { event ->
                    event.editMessage(nextPage().getMessage().toEditData()).queue()
                }
            }

            // Group them so they all get deleted when one is used
            buttons.group(back, previousPage, nextPage).ephemeral {
                timeout(15.minutes) {
                    states.remove(interactionId)
                }
            }

            components += row(back, previousPage, nextPage)
        }

        override fun nextPage(): SeriesState {
            page++
            return this
        }

        override fun previousPage(): SeriesState {
            page--
            return this
        }

        override fun homePage(): SeriesState = SeriesHomeState(interactionId, series)

        override fun viewCharacters(): SeriesState = this

        override fun viewEvents(): SeriesState = throw IllegalStateException("Cannot go to view events")
    }

    private inner class SeriesViewEventsState(
        override val interactionId: Long,
        private val series: Series
    ) : SeriesState {

        private var page = 0

        override suspend fun getMessage(): MessageCreateData = MessageCreate {
            content = "This is the events page #$page for series ${series.name}"

            // Same code as SeriesViewCharactersState
            val back = buttons.secondary("Back").ephemeral {
                singleUse = true
                noTimeout() // Group handles that

                bindTo { event ->
                    event.editMessage(homePage().getMessage().toEditData()).queue()
                }
            }

            val previousPage = buttons.secondary("Previous page").ephemeral {
                singleUse = true
                noTimeout() // Group handles that

                bindTo { event ->
                    event.editMessage(previousPage().getMessage().toEditData()).queue()
                }
            }

            val nextPage = buttons.secondary("Next page").ephemeral {
                singleUse = true
                noTimeout() // Group handles that

                bindTo { event ->
                    event.editMessage(nextPage().getMessage().toEditData()).queue()
                }
            }

            // Group them so they all get deleted when one is used
            buttons.group(back, previousPage, nextPage).ephemeral {
                timeout(15.minutes) {
                    states.remove(interactionId)
                }
            }

            components += row(back, previousPage, nextPage)
        }

        override fun nextPage(): SeriesState {
            page++
            return this
        }

        override fun previousPage(): SeriesState {
            page--
            return this
        }

        override fun homePage(): SeriesState = SeriesHomeState(interactionId, series)

        override fun viewCharacters(): SeriesState = throw IllegalStateException("Cannot go to view characters")

        override fun viewEvents(): SeriesState = this
    }

    @JDASlashCommand(name = "series", description = "View a series")
    suspend fun onSlashSeries(
        event: GuildSlashEvent,
        @SlashOption seriesTitle: String
    ) {

        val series: Series = searchSeries(seriesTitle)
            ?: return event.reply_("Not found :(", ephemeral = true).queue()

        val state = SeriesStateWrapper(SeriesHomeState(event.idLong, series))
        states[event.idLong] = state
        event.reply(state.getMessage()).queue()
    }

    private fun searchSeries(seriesTitle: String): Series? {
        return Series("Spooderman")
    }
}