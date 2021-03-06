package com.freya02.botcommands;

import java.util.List;

public interface BGuildSettings {
	/**
	 * Returns the list of prefix this Guild should use <br>
	 * <b>If the returned list is null or empty, the global prefixes will be used</b>
	 *
	 * @return The list of prefixes
	 */
	List<String> getPrefixes();
}
