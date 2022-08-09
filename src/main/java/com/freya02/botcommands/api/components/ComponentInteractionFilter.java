package com.freya02.botcommands.api.components;

import com.freya02.botcommands.core.api.config.BComponentsConfig;

import java.util.function.Predicate;

/**
 * Filters component interaction execution
 *
 * @see Predicate
 * @see BComponentsConfig#addComponentFilter(ComponentInteractionFilter)
 * @see #isAccepted(ComponentFilteringData)
 */
public interface ComponentInteractionFilter {
	/**
	 * Tells whether the component interaction should run
	 * <br><b>You still have to acknowledge the interaction if you don't let it run</b>
	 *
	 * @param data The filtering data of the interaction
	 * @return <code>true</code> if the component interaction command can run, <code>false</code> if it must not run
	 */
	boolean isAccepted(ComponentFilteringData data);
}
