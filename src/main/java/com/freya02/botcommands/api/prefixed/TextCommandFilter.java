package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.BContext;

import java.util.function.Predicate;

/**
 * Filters text command execution
 *
 * @see Predicate
 * @see BContext#addTextFilter(TextCommandFilter)
 */
public interface TextCommandFilter {
	boolean isAccepted(TextFilteringData data);
}
