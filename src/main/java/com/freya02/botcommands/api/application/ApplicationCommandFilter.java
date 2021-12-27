package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.BContext;

import java.util.function.Predicate;

/**
 * Filters application command execution
 *
 * @see Predicate
 * @see BContext#addApplicationFilter(ApplicationCommandFilter)
 */
public interface ApplicationCommandFilter {
	boolean isAccepted(ApplicationFilteringData data);
}
