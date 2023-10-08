package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.parameters.ParameterResolver

/**
 * Annotation for defining a button listener,
 * this has to be the same name as the one given to [PersistentButtonBuilder.bindTo].
 *
 * The options of the listener need to be in the same order as passed in [PersistentButtonBuilder.bindTo],
 * they do not need any annotation.
 *
 * **Requirements:**
 *  - Button listeners must be in the [search path][BConfigBuilder.addSearchPath]
 *  - These handlers also need to have a [ButtonEvent] as their first argument
 *
 * Supported parameters are in [ParameterResolver].
 *
 * **Requirement:** The declaring class must be annotated with [@Handler][Handler] or [@Command][Command].
 *
 * @see Components
 * @see ParameterResolver
 * @see Aggregate @Aggregate
 *
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDAButtonListener(
    /**
     * Name of the button listener.<br>
     * This is used to find back the handler method after a button has been clicked.
     */
    @get:JvmName("value") val name: String
) 