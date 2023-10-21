package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import org.jetbrains.annotations.Nullable

/**
 * Sets a parameter as a text command option from the Discord message.
 *
 * This also can set name and example of [text commands][JDATextCommand] parameters.
 *
 * @see Optional @Optional
 * @see Nullable @Nullable
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextOption(
    /**
     * The name of this option displayed on the help content.
     *
     * This is optional if the parameter name is available,
     * see [the wiki](https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/) for more details.
     */
    //TODO keep an eye out for this wiki link
    val name: String = "",

    /**
     * The example input of this option displayed on the help content.
     */
    val example: String = ""
)
