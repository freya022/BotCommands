package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload

class InlineAttachmentUpload(
    val builder: AttachmentUpload.Builder,
) : InlineComponent {

    override var uniqueId: Int
        get() = builder.uniqueId
        set(value) {
            builder.setUniqueId(value)
        }

    /** The custom ID, can be used to pass data, then read in an interaction, see [AttachmentUpload.Builder.setCustomId] */
    var customId: String
        get() = builder.customId
        set(value) {
            builder.setCustomId(value)
        }

    /** Whether the user must upload files, see [AttachmentUpload.Builder.setRequired] */
    var required: Boolean
        get() = builder.isRequired
        set(value) {
            builder.setRequired(value)
        }

    /** Minimum and maximum amount of files a user can send, see [AttachmentUpload.Builder.setRequiredRange] */
    var range: IntRange
        get() = builder.minValues..builder.maxValues
        set(value) {
            builder.setRequiredRange(value.first, value.last)
        }

    /** Minimum amount of attachments the user has to send, see [AttachmentUpload.Builder.setMinValues] */
    var minValues: Int
        get() = builder.minValues
        set(value) {
            builder.setMinValues(value)
        }

    /** Maximum amount of attachments the user can send, see [AttachmentUpload.Builder.setMaxValues] */
    var maxValues: Int
        get() = builder.maxValues
        set(value) {
            builder.setMaxValues(value)
        }

    /** See [AttachmentUpload.Builder.build] */
    fun build(): AttachmentUpload {
        return builder.build()
    }
}

/**
 * Component accepting files from users, see [AttachmentUpload][net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload].
 *
 * @param customId The custom ID of the input, see [AttachmentUpload.Builder.setCustomId]
 * @param uniqueId Unique identifier of this component, see [Component.withUniqueId]
 * @param required Whether the user must upload files, see [AttachmentUpload.Builder.setRequired]
 * @param range    Minimum and maximum amount of files a user can send, see [AttachmentUpload.Builder.setRequiredRange]
 * @param block    Lambda allowing further configuration
 */
inline fun AttachmentUpload(
    customId: String,
    uniqueId: Int = -1,
    required: Boolean = true,
    range: IntRange? = null,
    block: InlineAttachmentUpload.() -> Unit = {},
): AttachmentUpload {
    return AttachmentUpload.create(customId)
        .let(::InlineAttachmentUpload)
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (!required)
                this.required = false
            if (range != null)
                this.range = range
            block()
        }
        .build()
}

/**
 * Sets the minimum and maximum amount of files a user can send, see [AttachmentUpload.Builder.setRequiredRange].
 */
fun AttachmentUpload.Builder.setRequiredRange(range: IntRange) = setRequiredRange(range.first, range.last)
