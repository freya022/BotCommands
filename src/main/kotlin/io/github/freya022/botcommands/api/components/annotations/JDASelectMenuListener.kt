package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver

/**
 * Annotation for defining a selection menu listener,
 * this has to be the same name as the one given to [IPersistentActionableComponent.bindTo].
 *
 * The options of the listener need to be in the same order as passed in [IPersistentActionableComponent.bindTo],
 * they do not need any annotation.
 *
 * **Requirements:**
 *  - Select menu listeners must be in the [search path][BConfigBuilder.addSearchPath]
 *  - These handlers also need to have a [StringSelectEvent] or [EntitySelectEvent] as their first argument
 *
 * Supported parameters are in [ParameterResolver],
 * additional types can be added by implementing [ComponentParameterResolver].
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
annotation class JDASelectMenuListener(
    /**
     * Name of the select menu listener.<br>
     * This is used to find back the handler method after a select menu has been clicked
     */
    @get:JvmName("value") val name: String
) 