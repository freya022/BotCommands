package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.filedisplay.FileDisplay
import net.dv8tion.jda.api.utils.FileUpload

class InlineFileDisplay(
    private var fileDisplay: FileDisplay,
) : InlineComponent {

    override var uniqueId: Int
        get() = fileDisplay.uniqueId
        set(value) {
            fileDisplay = fileDisplay.withUniqueId(value)
        }

    /** Hides the file until the user clicks on it */
    var spoiler: Boolean
        get() = fileDisplay.isSpoiler
        set(value) {
            fileDisplay = fileDisplay.withSpoiler(value)
        }

    fun build(): FileDisplay = fileDisplay
}

/**
 * See [FileDisplay.fromFile].
 *
 * This requires [Components V2][net.dv8tion.jda.api.utils.messages.MessageRequest.useComponentsV2] to be enabled.
 *
 * @param uniqueId    Unique identifier of this component
 * @param spoiler     Hides the file until the user clicks on it
 * @param block       Lambda allowing further configuration
 */
fun FileDisplay(file: FileUpload, uniqueId: Int = -1, spoiler: Boolean = false, block: InlineFileDisplay.() -> Unit = {}): FileDisplay =
    InlineFileDisplay(FileDisplay.fromFile(file))
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (spoiler)
                this.spoiler = true
            block()
        }
        .build()

/**
 * See [FileDisplay.fromFileName].
 *
 * This requires [Components V2][net.dv8tion.jda.api.utils.messages.MessageRequest.useComponentsV2] to be enabled.
 *
 * @param uniqueId    Unique identifier of this component
 * @param spoiler     Hides the file until the user clicks on it
 * @param block       Lambda allowing further configuration
 */
fun FileDisplay(fileName: String, uniqueId: Int = -1, spoiler: Boolean = false, block: InlineFileDisplay.() -> Unit = {}): FileDisplay =
    InlineFileDisplay(FileDisplay.fromFileName(fileName))
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (spoiler)
                this.spoiler = true
            block()
        }
        .build()
