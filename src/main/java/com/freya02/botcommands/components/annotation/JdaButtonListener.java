package com.freya02.botcommands.components.annotation;

import com.freya02.botcommands.application.slash.ApplicationCommand;
import com.freya02.botcommands.components.Components;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.parameters.ParameterResolvers;
import com.freya02.botcommands.prefixed.Command;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a button listener, this has to be the same name as the one given to {@link Components#button(ButtonStyle, String, Object...)} <br><br>
 * Requirements:
 * <ul>
 *     <li><b>Button listeners can only be put on methods that are inside a class that extends {@link Command} or {@link ApplicationCommand}</b></li>
 *     <li><b>These handlers also need to have a {@link ButtonEvent} as their first argument</b></li>
 * </ul>
 *
 * <p>
 * <i>Supported parameters in {@link ParameterResolvers}</i>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JdaButtonListener {
	/**
	 * Name of the button listener, this is used to find back the handler method after a button has been clicked
	 *
	 * @return Name of the button listener
	 */
	String name();
}