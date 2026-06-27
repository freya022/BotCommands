package io.github.freya022.botcommands.api.commands.application.slash.annotations

/**
 * Sets the desired file types accepted by an [Attachment][net.dv8tion.jda.api.entities.Message.Attachment] [@SlashOption][SlashOption].
 *
 * @see net.dv8tion.jda.api.interactions.FileType FileType
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FileTypes(
    /**
     * Extensions to filter for.
     */
    vararg val extensions: String,
    /**
     * Accepts any kind of image file supported by the Discord client.
     */
    val image: Boolean = false,
    /**
     * Accepts any kind of video file supported by the Discord client.
     */
    val video: Boolean = false,
    /**
     * Accepts any kind of audio file supported by the Discord client.
     */
    val audio: Boolean = false,
)
