package com.freya02.botcommands.api.components.annotations;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a button listener,
 * this has to be the same name as the one given to {@link Components#button(ButtonStyle, String, Object...)}.
 *
 * <p>
 * <b>Requirements:</b>
 * <ul>
 *     <li>Button listeners must be in the {@link CommandsBuilder#addSearchPath(String) search path}</li>
 *     <li>These handlers also need to have a {@link ButtonEvent} as their first argument</li>
 * </ul>
 *
 * Supported parameters are in {@link ParameterResolvers}.
 *
 * @see Components
 * @see ParameterResolvers
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JDAButtonListener {
	/**
	 * Name of the button listener, this is used to find back the handler method after a button has been clicked
	 *
	 * @return Name of the button listener
	 */
	String name();
}