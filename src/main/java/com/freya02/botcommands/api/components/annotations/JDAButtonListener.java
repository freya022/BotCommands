package com.freya02.botcommands.api.components.annotations

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.core.options.annotations.Aggregate;
import com.freya02.botcommands.api.parameters.ParameterResolver;

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
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDAButtonListener(
    /**
     * Name of the button listener.<br>
     * This is used to find back the handler method after a button has been clicked.
     */
    val name: String
) 