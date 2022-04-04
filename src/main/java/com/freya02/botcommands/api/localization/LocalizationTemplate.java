package com.freya02.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

public interface LocalizationTemplate {
	@NotNull String localize(Localization.Entry... args);
}
