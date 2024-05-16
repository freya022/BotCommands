package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver

/**
 * Declares this function as a button listener with the given name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Handler][Handler] or [@Command][Command].
 * - The annotation value to have same name as the one given to [PersistentButtonBuilder.bindTo].
 * - First parameter must be [ButtonEvent].
 *
 * ### Option types
 * - User data: Uses [@ComponentData][ComponentData], the order must match the data passed when creating the button,
 * supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [ComponentParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options: No annotation, additional types can be added by implementing [ICustomResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * @see Components
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