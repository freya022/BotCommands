package io.github.freya022.botcommands.api.commands.prefixed.annotations

import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.SettingsProvider


/**
 * Marks a text command as being usable in NSFW channels only.
 *
 * NSFW commands will be shown in help content only if called in an NSFW channel,
 * DM consent is **not** checked as text commands are guild-only.
 *
 * **Note:** For application commands, see the `nsfw` parameter of your annotation
 *
 * @see TextCommandBuilder.nsfw DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NSFW(
    /**
     * Specifies whether this NSFW command should work in guild channels.
     *
     * @return `true` if the command should run on guild channels
     */
    val guild: Boolean = true,

    /**
     * Specifies whether this NSFW command should work in a user's DMs.
     *
     * The user also needs to [consent to NSFW DMs][SettingsProvider.doesUserConsentNSFW]
     *
     * @return `true` if the command should run in user DMs
     *
     * @see SettingsProvider.doesUserConsentNSFW
     */
    val dm: Boolean = false
) 