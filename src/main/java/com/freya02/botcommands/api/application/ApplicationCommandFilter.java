package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.BContext;

import java.util.function.Predicate;

/**
 * Filters application command execution
 *
 * @see Predicate
 * @see BContext#addApplicationFilter(ApplicationCommandFilter)
 * @see #isAccepted(ApplicationFilteringData)
 */
public interface ApplicationCommandFilter {
	/**
	 * Tells whether the application command should run
	 * <br><b>You still have to acknowledge the interaction if you don't let it run</b>
	 *
	 * @param data The filtering data of the interaction
	 * @return <code>true</code> if the application command can run, <code>false</code> if it must not run
	 */
	boolean isAccepted(ApplicationFilteringData data);
}
