package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import org.jspecify.annotations.Nullable

/**
 * Sets a parameter as a text command option from the Discord message.
 *
 * This also can set name and example of [text commands][JDATextCommandVariation] parameters.
 *
 * The supported data types can be seen in [TextParameterResolver], more types can be supported by implementing it.
 *
 * ### Varargs
 *
 * You can define a single vararg parameter per text command variation, using [@VarArgs][VarArgs].
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
     * see [the wiki](https://bc.freya02.dev/3.X/setup/parameter-names/) for more details.
     */
    val name: String = "",

    /**
     * The example input of this option displayed on the help content.
     */
    val example: String = ""
)
