package com.freya02.botcommands.api.components.annotations;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.event.EntitySelectionEvent;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a selection menu listener,
 * this has to be the same name as the one given to {@link Components#stringSelectionMenu(String, Object...)}
 * or {@link Components#entitySelectionMenu(EntitySelectMenu.SelectTarget, String, Object...)}.
 *
 * <p>
 * <b>Requirements:</b>
 * <ul>
 *     <li>Selection menu listeners must be in the {@link CommandsBuilder#addSearchPath(String) search path}</li>
 *     <li>These handlers also need to have a {@link StringSelectionEvent} or {@link EntitySelectionEvent} as their first argument</li>
 * </ul>
 *
 * Supported parameters are in {@link ParameterResolvers}.
 *
 * @see Components
 * @see ParameterResolvers
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JDASelectionMenuListener {
	/**
	 * Name of the selection menu listener, this is used to find back the handler method after a button has been clicked
	 *
	 * @return Name of the selection menu listener
	 */
	String name();
}