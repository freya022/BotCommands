package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.builder.filter
import io.github.freya022.botcommands.api.components.builder.timeout
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.test.filters.InVoiceChannel
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import kotlin.time.Duration.Companion.seconds

@Command
@RequiresComponents
class SlashNewSelects(
    private val selectMenus: SelectMenus
) : ApplicationCommand() {
    @JDASlashCommand(name = "new_selects")
    suspend fun onSlashNewButtons(event: GuildSlashEvent) {
        val persistentSelect = persistentGroupTest(event)
        val ephemeralSelect = ephemeralGroupTest(event)
        val voiceSelect = selectMenus.entitySelectMenu(SelectTarget.CHANNEL).ephemeral {
            setChannelTypes(ChannelType.VOICE)
            filters += filter<InVoiceChannel>()
            bindTo {
                it.guild!!.moveVoiceMember(it.member!!, it.values.first() as AudioChannel).await()
                it.deferEdit().await()
            }
        }

        //These *should* be able to store continuations and throw a TimeoutException once the timeout is met
//        val groupEvent: GenericComponentInteractionCreateEvent = firstGroup.await()
//        val buttonEvent: ButtonEvent = firstButton.await()

        event.replyComponents(row(persistentSelect), row(ephemeralSelect), row(voiceSelect)).queue()
    }

    private suspend fun persistentGroupTest(event: GuildSlashEvent): StringSelectMenu {
        val firstSelect = selectMenus.stringSelectMenu().persistent {
            oneUse = true //Cancels whole group if used
            constraints += UserSnowflake.fromId(1234L)
            constraints += Permission.ADMINISTRATOR
            bindTo(::onFirstSelectClicked)

            addOption("Test", "Test")
            addOption("Foo", "Foo")
            addOption("Bar", "Bar")
        }

        val secondSelect = selectMenus.stringSelectMenu().persistent {
            oneUse = true //Cancels whole group if used
            constraints {
                addUserIds(1234L)
                allowingPermissions += Permission.ADMINISTRATOR
            }
            bindTo(::onFirstSelectClicked)

            addOption("Test", "Test")
            addOption("Foo", "Foo")
            addOption("Bar", "Bar")
        }

        selectMenus.group(firstSelect, secondSelect).persistent {
            timeout(10.seconds, ::onFirstGroupTimeout)
        }
        return firstSelect
    }

    private suspend fun ephemeralGroupTest(event: GuildSlashEvent): EntitySelectMenu {
        val firstSelect = selectMenus.entitySelectMenu(SelectTarget.ROLE).ephemeral {
            noTimeout()
            oneUse = true //Cancels whole group if used
            constraints {
                addUserIds(1234L)
                allowingPermissions += Permission.ADMINISTRATOR
            }
            bindTo { evt -> evt.reply_("Ephemeral select menu clicked", ephemeral = true).queue() }
        }

        selectMenus.group(firstSelect).ephemeral {
            timeout(15.seconds) {
                event.hook.retrieveOriginal()
                    .flatMap { event.hook.editOriginalComponents(it.components.asDisabled()) }
                    .queue()
            }
        }
        return firstSelect
    }

    @JDASelectMenuListener
    fun onFirstSelectClicked(event: StringSelectEvent) {
        event.reply_("Persistent select menu clicked", ephemeral = true).queue()
    }

    @GroupTimeoutHandler
    fun onFirstGroupTimeout(data: GroupTimeoutData) {
        println(data)
    }
}