package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import org.jetbrains.annotations.Nullable

/**
 * Sets a parameter as a text command option from the Discord message.
 *
 * This also can set name and example of [text commands][JDATextCommandVariation] parameters.
 *
 * @see Optional @Optional
 * @see Nullable @Nullable
 * @see ID @ID
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextOption(
    /**
     * The name of this option displayed on the help content.
     *
     * This is optional if the parameter name is available,
     * see [the wiki](https://freya022.github.io/BotCommands/3.X/using-botcommands/parameter-names/) for more details.
     */
    val name: String = "",

    /**
     * The example input of this option displayed on the help content.
     */
    val example: String = ""
)
