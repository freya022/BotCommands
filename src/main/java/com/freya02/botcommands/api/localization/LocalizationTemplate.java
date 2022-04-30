package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

/**
 * Allows different implementation of localization templates.
 * <br>Those templates can have different specifications if you wish, but the default one is in {@link DefaultLocalizationTemplate}.
 *
 * @see DefaultLocalizationTemplate
 */
public interface LocalizationTemplate {
	@NotNull String localize(Localization.Entry... args);
}
