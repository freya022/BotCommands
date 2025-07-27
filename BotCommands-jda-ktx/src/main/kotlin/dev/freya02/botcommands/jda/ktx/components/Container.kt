package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.hex
import dev.freya02.botcommands.jda.ktx.hsb
import dev.freya02.botcommands.jda.ktx.rgb
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent
import net.dv8tion.jda.api.components.section.SectionContentComponent
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Color

private val DUMMY_CONTAINER = Container.of(TextDisplay.of("a"))

class InlineContainer : InlineComponentWithChildren<ContainerChildComponent> {

    private var container = DUMMY_CONTAINER

    override var uniqueId: Int
        get() = container.uniqueId
        set(value) {
            container = container.withUniqueId(value)
        }

    /** Color of the container's left side, you can use [rgb], [hsb] or [hex] for it */
    var accentColorRaw: Int?
        get() = container.accentColorRaw
        set(value) {
            container = container.withAccentColor(value)
        }

    /** Color of the container's left side, you can use [rgb], [hsb] or [hex] for it */
    var accentColor: Color?
        get() = container.accentColor
        set(value) {
            container = container.withAccentColor(value)
        }

    /** Hides the file until the user clicks on it */
    var spoiler: Boolean
        get() = container.isSpoiler
        set(value) {
            container = container.withSpoiler(value)
        }

    override val components = mutableListOf<ContainerChildComponent>()

    inline fun actionRow(
        vararg components: ActionRowChildComponent,
        uniqueId: Int = -1,
        block: InlineActionRow.() -> Unit = {},
    ) {
        this.components += ActionRow(uniqueId) {
            this.components += components
            block()
        }
    }

    inline fun actionRow(
        components: Collection<ActionRowChildComponent> = emptyList(),
        uniqueId: Int = -1,
        block: InlineActionRow.() -> Unit = {},
    ) {
        this.components += ActionRow(uniqueId) {
            this.components += components
            block()
        }
    }

    inline fun section(
        accessory: SectionAccessoryComponent? = null,
        vararg components: SectionContentComponent,
        uniqueId: Int = -1,
        block: InlineSection.() -> Unit = {},
    ) {
        this.components += Section(accessory, uniqueId) {
            this.components += components
            block()
        }
    }

    inline fun section(
        accessory: SectionAccessoryComponent? = null,
        components: Collection<SectionContentComponent> = emptyList(),
        uniqueId: Int = -1,
        block: InlineSection.() -> Unit = {},
    ) {
        this.components += Section(accessory, uniqueId) {
            this.components += components
            block()
        }
    }

    inline fun textDisplay(
        content: String? = null,
        uniqueId: Int = -1,
        block: InlineTextDisplay.() -> Unit = {},
    ) {
        this.components += TextDisplay(content, uniqueId, block)
    }

    inline fun mediaGallery(
        vararg items: MediaGalleryItem,
        uniqueId: Int = -1,
        block: InlineMediaGallery.() -> Unit = {},
    ) {
        this.components += MediaGallery(uniqueId) {
            this.items += items
            block()
        }
    }

    inline fun mediaGallery(
        items: Collection<MediaGalleryItem> = emptyList(),
        uniqueId: Int = -1,
        block: InlineMediaGallery.() -> Unit = {},
    ) {
        this.components += MediaGallery(uniqueId) {
            this.items += items
            block()
        }
    }

    inline fun separator(
        uniqueId: Int = -1,
        isDivider: Boolean = true,
        spacing: Separator.Spacing = Separator.Spacing.SMALL,
        block: InlineSeparator.() -> Unit = {},
    ) {
        this.components += Separator(uniqueId, isDivider, spacing, block)
    }

    fun fileDisplay(
        file: FileUpload,
        uniqueId: Int = -1,
        spoiler: Boolean = false,
        block: InlineFileDisplay.() -> Unit = {},
    ) {
        this.components += FileDisplay(file, uniqueId, spoiler, block)
    }

    fun fileDisplay(
        fileName: String,
        uniqueId: Int = -1,
        spoiler: Boolean = false,
        block: InlineFileDisplay.() -> Unit = {},
    ) {
        this.components += FileDisplay(fileName, uniqueId, spoiler, block)
    }

    fun build(): Container {
        return container.withComponents(components)
    }
}

/**
 * See [Container][net.dv8tion.jda.api.components.container.Container].
 *
 * This requires [Components V2][net.dv8tion.jda.api.utils.messages.MessageRequest.useComponentsV2] to be enabled.
 *
 * @param uniqueId    Unique identifier of this component
 * @param accentColor Color of the container's left side, you can use [rgb], [hsb] or [hex] for it
 * @param spoiler     Hides the file until the user clicks on it
 * @param block       Lambda allowing further configuration
 *
 * @see ContainerChildComponent
 */
inline fun Container(uniqueId: Int = -1, accentColor: Int? = null, spoiler: Boolean = false, block: InlineContainer.() -> Unit): Container =
    InlineContainer()
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (accentColor != null)
                this.accentColorRaw = accentColor
            if (spoiler)
                this.spoiler = true
            block()
        }
        .build()
