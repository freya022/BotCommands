package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload
import net.dv8tion.jda.api.interactions.FileType
import net.dv8tion.jda.api.interactions.IFilterableFileTypes

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

    /**
     * The file types this attachment upload is accepting, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES].
     *
     * @see FileType
     */
    val fileTypes = FileTypeAccumulator()

    /**
     * Adds up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun addFileTypeExtensions(extensions: List<String>) {
        fileTypes += extensions
    }

    /**
     * Adds up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun addFileTypeExtensions(vararg extensions: String) {
        fileTypes += extensions.asList()
    }

    /**
     * Sets up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     * Leave the arguments empty to remove file type filtering.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun setFileTypeExtensions(extensions: List<String>) {
        fileTypes.clear()
        fileTypes += extensions
    }

    /**
     * Sets up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     * Leave the arguments empty to remove file type filtering.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun setFileTypeExtensions(vararg extensions: String) {
        fileTypes.clear()
        fileTypes += extensions.asList()
    }

    /** See [AttachmentUpload.Builder.build] */
    fun build(): AttachmentUpload {
        return builder.build()
    }

    inner class FileTypeAccumulator {
        @JvmName("plusAssignExtensions")
        operator fun plusAssign(extensions: Collection<String>) {
            builder.addFileTypeExtensions(extensions)
        }

        operator fun plusAssign(extension: String) {
            builder.addFileTypeExtensions(extension)
        }

        @JvmName("plusAssignFileTypes")
        operator fun plusAssign(extensions: Collection<FileType>) {
            builder.addFileTypes(extensions)
        }

        operator fun plusAssign(extension: FileType) {
            builder.addFileTypes(extension)
        }

        fun clear() {
            builder.setFileTypes()
        }
    }
}

/**
 * Component accepting files from users, see [AttachmentUpload][net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload].
 *
 * @param customId  The custom ID of the input, see [AttachmentUpload.Builder.setCustomId]
 * @param uniqueId  Unique identifier of this component, see [Component.withUniqueId]
 * @param required  Whether the user must upload files, see [AttachmentUpload.Builder.setRequired]
 * @param range     Minimum and maximum amount of files a user can send, see [AttachmentUpload.Builder.setRequiredRange]
 * @param fileTypes The file types to accept, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
 * @param block     Lambda allowing further configuration
 */
inline fun AttachmentUpload(
    customId: String,
    uniqueId: Int = -1,
    required: Boolean = true,
    range: IntRange? = null,
    fileTypes: Collection<FileType> = emptyList(),
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
            this.fileTypes += fileTypes
            block()
        }
        .build()
}

/**
 * Sets the minimum and maximum amount of files a user can send, see [AttachmentUpload.Builder.setRequiredRange].
 */
fun AttachmentUpload.Builder.setRequiredRange(range: IntRange) = setRequiredRange(range.first, range.last)
