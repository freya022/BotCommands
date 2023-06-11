package com.freya02.botcommands.api.commands.prefixed;

import com.freya02.botcommands.api.core.config.BTextConfigBuilder;

import java.util.function.Predicate;

/**
 * Filters text command execution
 *
 * @see Predicate
 * @see BTextConfigBuilder#getTextFilters()
 * @see #isAccepted(TextFilteringData)
 */
public interface TextCommandFilter {
	/**
	 * Tells whether the text command should run
	 *
	 * @param data The filtering data of the interaction
	 * @return <code>true</code> if the text command can run, <code>false</code> if it must not run
	 */
	boolean isAccepted(TextFilteringData data);
}
