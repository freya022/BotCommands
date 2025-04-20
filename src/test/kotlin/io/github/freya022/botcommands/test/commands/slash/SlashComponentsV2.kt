package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.core.utils.readResource
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.FileUpload

@Command
class SlashComponentsV2(
    private val buttons: Buttons,
    private val selectMenus: SelectMenus,
) : ApplicationCommand() {

    private val kotlinIcon = FileUpload.fromData(readResource("/emojis/kotlin.png"), "kotlin.png")
    private val rustAnimation = FileUpload.fromData(readResource("/rust.webp"), "rust.webp")

    @TopLevelSlashCommandData(
        contexts = [InteractionContextType.GUILD],
        integrationTypes = [IntegrationType.USER_INSTALL],
    )
    @JDASlashCommand(name = "components_v2", description = "Yippie")
    suspend fun onSlashComponentsV2(event: GuildSlashEvent) {
        val ephemeral = true

        event.replyComponents(
            Container(accentColor = hex("00FF00")) {
                +MediaGallery {
                    +item("https://cdn.discordapp.com/attachments/964253122547552349/1336440069892083712/7Q3S.gif")
                    +item(rustAnimation)
                }

                +Section(
                    accessory = Thumbnail(kotlinIcon)
                ) {
                    +TextDisplay("kotlin")
                }

                +FileDisplay(FileUpload.fromData("abc".encodeToByteArray(), "abc.txt"))

                +Section(
                    accessory = buttons.success("Button in a section").ephemeral {
                        bindTo { buttonEvent ->
                            buttonEvent.reply_(
                                components = listOf(TextDisplay("My reference ID is ${buttonEvent.component.uniqueId}")),
                                useComponentsV2 = true,
                                ephemeral = true
                            ).await()
                        }
                    }
                ) {
                    +TextDisplay("""
                        # Yippie
                        This container is built using some very fancy (not rly) abstraction and extension methods.
                    """.trimIndent())
                }

                +Separator(isDivider = true, spacing = Separator.Spacing.LARGE)

                +Section(
                    accessory = Thumbnail("https://cdn.discordapp.com/attachments/556235929443106828/1339901053813919764/wires.png")
                ) {
                    +TextDisplay("""
                        And another section with a totally-not-a-rickroll [link](https://www.youtube.com/watch?v=dQw4w9WgXcQ)
                        -# *and a thumbnail from attachments*
                    """.trimIndent())
                }

                +ActionRow {
                    +buttons.link("https://www.youtube.com/watch?v=dQw4w9WgXcQ", "Link? ain't no way")
                }
                +ActionRow {
                    +selectMenus.stringSelectMenu().ephemeral {
                        options += SelectOption("foo", "bar")
                    }
                }
                +ActionRow {
                    +buttons.danger("Button").ephemeral {
                        bindTo { buttonEvent ->
                            buttonEvent.reply_("My reference ID is ${buttonEvent.component.uniqueId}")
                        }
                    }
                    +buttons.success("No way... A second one").ephemeral {
                        bindTo { buttonEvent ->
                            buttonEvent.reply_("My reference ID is ${buttonEvent.component.uniqueId}", ephemeral = true).await()
                        }
                    }
                }
            }
        ).useComponentsV2().setEphemeral(ephemeral).queue()

        event.hook.sendFiles(rustAnimation).setEphemeral(ephemeral).queue()
        event.hook.sendMessageEmbeds(Embed {
            image = "attachment://rust.webp"
        }).addFiles(rustAnimation).setEphemeral(ephemeral).queue()
    }
}
