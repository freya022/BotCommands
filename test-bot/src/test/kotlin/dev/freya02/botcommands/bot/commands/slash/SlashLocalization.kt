package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.bot.messages.TestBotMessages
import dev.freya02.botcommands.jda.ktx.components.SelectOption
import dev.freya02.botcommands.jda.ktx.components.TextInput
import dev.freya02.botcommands.jda.ktx.components.row
import dev.freya02.botcommands.jda.ktx.durations.before
import dev.freya02.botcommands.jda.ktx.messages.MessageCreate
import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.localize
import io.github.freya022.botcommands.api.localization.context.localizeGuild
import io.github.freya022.botcommands.api.localization.context.localizeUser
import io.github.freya022.botcommands.api.localization.interaction.*
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.TimeFormat
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.milliseconds

@Command
@RequiresModals
class SlashLocalization {

    @JDASlashCommand(name = "localization")
    fun onSlashLocalization(
        event: GuildSlashEvent,
        @LocalizationBundle("Test", prefix = "commands.localization") ctx: AppLocalizationContext,
    ) {
        val content = """
            User localized (${ctx.userLocale}): %s
            Guild localized (${ctx.guildLocale}): %s
            German localized: %s
        """.trimIndent().format(
            ctx.localizeUser(
                localizationPath = "response",
                "guild_users" to event.guild.memberCount,
                "uptime" to 3.141519
            ),
            ctx.localizeGuild(
                localizationPath = "response",
                "guild_users" to event.guild.memberCount,
                "uptime" to 3.141519
            ),
            ctx.localize(
                DiscordLocale.GERMAN,
                localizationPath = "response",
                "guild_users" to event.guild.memberCount,
                "uptime" to 3.141519
            )
        )

        event.reply_(content, ephemeral = true).queue()
    }

    @JDASlashCommand(name = "type_safe_localization")
    fun onSlashTypeSafeLocalization(event: GuildSlashEvent, messages: TestBotMessages) {
        val content = """
            Default localized: ${messages.response(guildUsers = event.guild.memberCount, uptime = 3.14159)}
        """.trimIndent()

        event.reply_(content, ephemeral = true).queue()
    }

    @JDASlashCommand(name = "localization_by_event")
    suspend fun onSlashLocalizationByEvent(event: GuildSlashEvent, buttons: Buttons, selectMenus: SelectMenus, modals: Modals) {
        event.localizationPrefix = "commands.localization"

        val message = MessageCreate {
            content = event.getUserMessage("response", *userData)

            val userLocaleButton = buttons.primary("User locale").ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.replyUser("commands.localization.response", *userData)
                        .setEphemeral(true)
                        .queue()
                }
            }
            val guildLocaleButton = buttons.primary("Guild locale").ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.localizationPrefix = "commands.localization"
                    buttonEvent.replyGuild("response", *guildData)
                        .setEphemeral(true)
                        .queue()
                }
            }
            val customLocaleButton = buttons.primary("Custom locale").ephemeral {
                bindTo { buttonEvent ->
                    buttonEvent.replyLocalized(DiscordLocale.GERMAN, "commands.localization.response", *customData)
                        .setEphemeral(true)
                        .queue()
                }
            }

            val modalButton = buttons.primary("Open modal").ephemeral {
                bindTo { buttonEvent ->
                    val modal = modals.create("Sample title") {
                        label("Sample label") {
                            child = TextInput("name", TextInputStyle.SHORT)
                        }
                    }

                    buttonEvent.replyModal(modal).queue()

                    val modalEvent = modal.await()
                    modalEvent.localizationPrefix = "commands.localization"

                    modalEvent.replyUser("response", *userData).setEphemeral(true).queue()
                    modalEvent.hook.editUser("response", *userData).queue()
                    modalEvent.hook.editGuild("response", *guildData).queue()
                    modalEvent.hook.editLocalized(DiscordLocale.GERMAN, "response", *customData).queue()
                }
            }

            val selectMenu = selectMenus.stringSelectMenu().ephemeral {
                bindTo { selectEvent ->
                    selectEvent.localizationPrefix = "commands.localization"
                    when (selectEvent.values.single()) {
                        "user" -> selectEvent.replyUser("response", *userData)
                        "guild" -> selectEvent.replyGuild("response", *guildData)
                        "custom" -> selectEvent.replyLocalized(DiscordLocale.GERMAN, "response", *customData)
                        else -> throw AssertionError()
                    }.setEphemeral(true).queue()
                }

                options += SelectOption("User", "user")
                options += SelectOption("Guild", "guild")
                options += SelectOption("Custom", "custom")
            }

            components += row(userLocaleButton, guildLocaleButton, customLocaleButton, modalButton)
            components += row(selectMenu)
        }

        event.reply(message).setEphemeral(true).queue()
        event.hook.editUser("response", *userData).queue()

        event.hook.sendGuild("response", *guildData)
            .setEphemeral(true)
            .queue()
        event.hook.editGuild("response", *guildData).queue()

        event.hook.sendLocalized(DiscordLocale.GERMAN, "response", *customData)
            .setEphemeral(true)
            .queue()
        event.hook.editLocalized(DiscordLocale.GERMAN, "response", *customData).queue()
    }

    private val userData get() = arrayOf("guild_users" to 20, "uptime" to uptime)
    private val guildData get() = arrayOf("guild_users" to 40, "uptime" to uptime)
    private val customData get() = arrayOf("guild_users" to 60, "uptime" to uptime)

    private val uptime get() = TimeFormat.RELATIVE.before(ManagementFactory.getRuntimeMXBean().uptime.milliseconds)
}
