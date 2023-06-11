package com.freya02.botcommands.api.commands.application;

import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder;

import java.util.function.Predicate;

/**
 * Filters application command execution
 *
 * @see Predicate
 * @see BApplicationConfigBuilder#getApplicationFilters()
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
