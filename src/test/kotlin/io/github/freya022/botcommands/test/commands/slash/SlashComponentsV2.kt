package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.SelectOption
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
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.filedisplay.FileDisplay
import net.dv8tion.jda.api.components.mediagallery.MediaGallery
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent
import net.dv8tion.jda.api.components.section.SectionContentComponent
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Color

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
            Container(accentColor = rgb(0, 255, 0)) {
                +MediaGallery {
                    +url("https://cdn.discordapp.com/attachments/964253122547552349/1336440069892083712/7Q3S.gif")
                    +file(rustAnimation)
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

fun rgb(red: Int, green: Int, blue: Int): Int = Color(red, green, blue).rgb
fun hsb(hue: Float, saturation: Float, brightness: Float): Int = Color.HSBtoRGB(hue, saturation, brightness)

abstract class InlineComponentWithChildren<T> {
    var components = arrayListOf<T>()

    operator fun T.unaryPlus() {
        components += this
    }

    operator fun Collection<T>.unaryPlus() {
        components += this
    }
}

class InlineThumbnail(
    private val factory: () -> Thumbnail,
    var uniqueId: Int?,
    var description: String?,
    var spoiler: Boolean,
) {

    fun build(): Thumbnail {
        var thumbnail = factory()
            .withSpoiler(spoiler)
            .withDescription(description)
        if (uniqueId != null)
            thumbnail = thumbnail.withUniqueId(uniqueId!!)
        return thumbnail
    }
}

fun Thumbnail(url: String, uniqueId: Int? = null, description: String? = null, spoiler: Boolean = false, block: InlineThumbnail.() -> Unit = {}): Thumbnail =
    InlineThumbnail({ Thumbnail.fromUrl(url) }, uniqueId, description, spoiler).apply(block).build()

fun Thumbnail(file: FileUpload, uniqueId: Int? = null, description: String? = null, spoiler: Boolean = false, block: InlineThumbnail.() -> Unit = {}): Thumbnail =
    InlineThumbnail({ Thumbnail.fromFile(file) }, uniqueId, description, spoiler).apply(block).build()

class InlineFileDisplay(
    private val factory: () -> FileDisplay,
    var uniqueId: Int?,
    var spoiler: Boolean,
) {

    fun build(): FileDisplay {
        var fileDisplay = factory()
            .withSpoiler(spoiler)
        if (uniqueId != null)
            fileDisplay = fileDisplay.withUniqueId(uniqueId!!)
        return fileDisplay
    }
}

fun FileDisplay(file: FileUpload, uniqueId: Int? = null, spoiler: Boolean = false, block: InlineFileDisplay.() -> Unit = {}): FileDisplay =
    InlineFileDisplay({ FileDisplay.fromFile(file) }, uniqueId, spoiler).apply(block).build()

interface InlineActionRowChildComponentContainer

class InlineActionRow(
    var uniqueId: Int?,
) : InlineComponentWithChildren<ActionRowChildComponent>(), InlineActionRowChildComponentContainer {

    fun build(): ActionRow {
        var row = ActionRow.of(components)
        if (uniqueId != null)
            row = row.withUniqueId(uniqueId!!)
        return row
    }
}

inline fun ActionRow(uniqueId: Int? = null, block: InlineActionRow.() -> Unit): ActionRow =
    InlineActionRow(uniqueId).apply(block).build()

class InlineSeparator(
    var isDivider: Boolean,
    var spacing: Separator.Spacing,
    var uniqueId: Int?,
) {

    fun build(): Separator {
        var separator = Separator.create(isDivider, spacing)
        if (uniqueId != null)
            separator = separator.withUniqueId(uniqueId!!)
        return separator
    }
}

inline fun Separator(isDivider: Boolean, spacing: Separator.Spacing, uniqueId: Int? = null, block: InlineSeparator.() -> Unit = {}): Separator =
    InlineSeparator(isDivider, spacing, uniqueId).apply(block).build()

class InlineTextDisplay(
    var content: String,
    var uniqueId: Int?,
) {

    fun build(): TextDisplay {
        var textDisplay = TextDisplay.create(content)
        if (uniqueId != null)
            textDisplay = textDisplay.withUniqueId(uniqueId!!)
        return textDisplay
    }
}

inline fun TextDisplay(content: String, uniqueId: Int? = null, block: InlineTextDisplay.() -> Unit = {}): TextDisplay =
    InlineTextDisplay(content, uniqueId).apply(block).build()

interface InlineSectionComponentContainer {

}

class InlineSection(
    var uniqueId: Int?,
    var accessory: SectionAccessoryComponent,
) : InlineComponentWithChildren<SectionContentComponent>(), InlineSectionComponentContainer {

    fun build(): Section {
        var section = Section.of(accessory, components)
        if (uniqueId != null)
            section = section.withUniqueId(uniqueId!!)
        return section
    }
}

inline fun Section(accessory: SectionAccessoryComponent, uniqueId: Int? = null, block: InlineSection.() -> Unit): Section =
    InlineSection(uniqueId, accessory).apply(block).build()

interface InlineMediaGalleryComponentContainer {
    fun url(url: String): MediaGalleryItem = MediaGalleryItem.fromUrl(url)

    fun file(file: FileUpload): MediaGalleryItem = MediaGalleryItem.fromFile(file)
}

class InlineMediaGallery(
    var uniqueId: Int?,
) : InlineComponentWithChildren<MediaGalleryItem>(), InlineMediaGalleryComponentContainer {

    fun build(): MediaGallery {
        var gallery = MediaGallery.of(components)
        if (uniqueId != null)
            gallery = gallery.withUniqueId(uniqueId!!)
        return gallery
    }
}

inline fun MediaGallery(uniqueId: Int? = null, block: InlineMediaGallery.() -> Unit): MediaGallery {
    return InlineMediaGallery(uniqueId).apply(block).build()
}

class InlineContainer(
    var uniqueId: Int?,
    var accentColor: Int?,
    var spoiler: Boolean,
) : InlineComponentWithChildren<ContainerChildComponent>() {

    fun build(): Container {
        var container = Container.of(components)
        if (uniqueId != null)
            container = container.withUniqueId(uniqueId!!)
        if (accentColor != null)
            container = container.withAccentColor(accentColor!!)
        container.withSpoiler(spoiler)
        return container
    }
}

inline fun Container(uniqueId: Int? = null, accentColor: Int? = null, spoiler: Boolean = false, block: InlineContainer.() -> Unit): Container =
    InlineContainer(uniqueId, accentColor, spoiler).apply(block).build()