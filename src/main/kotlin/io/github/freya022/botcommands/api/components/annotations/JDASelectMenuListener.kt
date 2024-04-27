package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver

/**
 * Declares this function as a select menu listener with the given name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Handler][Handler] or [@Command][Command].
 * - The annotation value to have same name as the one given to [IPersistentActionableComponent.bindTo].
 * - First parameter must be [StringSelectEvent]/[EntitySelectEvent].
 * - Have all your consecutive user data, passed when creating the select menu.
 *
 * ### Option types
 * - User data: No annotation, the order must match the data passed when creating the select menu,
 * supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [ComponentParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options and services: No annotation, additional types can be added by implementing [ICustomResolver].
 *
 * @see Components
 * @see Aggregate @Aggregate
 *
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDASelectMenuListener(
    /**
     * Name of the select menu listener.<br>
     * This is used to find back the handler method after a select menu has been clicked
     */
    @get:JvmName("value") val name: String
) 