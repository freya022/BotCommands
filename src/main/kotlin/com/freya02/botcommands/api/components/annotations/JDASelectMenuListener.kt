package com.freya02.botcommands.api.components.annotations

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.annotations.RateLimit
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.api.core.annotations.Handler
import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.api.core.options.annotations.Aggregate
import com.freya02.botcommands.api.parameters.ParameterResolver

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
annotation class JDASelectMenuListener(
    /**
     * Name of the select menu listener.<br>
     * This is used to find back the handler method after a select menu has been clicked
     */
    val name: String
) 