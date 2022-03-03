package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.application.annotations.AppOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

public class ApplicationOptionData {
	private final String effectiveName, effectiveDescription;
	private final String autocompletionHandlerName;

	public ApplicationOptionData(Parameter parameter) {
		final AppOption option = parameter.getAnnotation(AppOption.class);

		if (option.name().isBlank()) {
			effectiveName = getOptionName(parameter);
		} else {
			effectiveName = option.name();
		}
		
		if (option.description().isBlank()) {
			effectiveDescription = "No description";
		} else {
			effectiveDescription = option.description();
		}

		if (option.autocomplete().isBlank()) {
			autocompletionHandlerName = null;
		} else {
			autocompletionHandlerName = option.autocomplete();
		}
	}

	private static String getOptionName(Parameter parameter) {
		if (!parameter.isNamePresent())
			throw new RuntimeException("Parameter name cannot be deduced as the option's name is not specified on: " + parameter);

		final String name = parameter.getName();
		final int nameLength = name.length();

		final StringBuilder optionNameBuilder = new StringBuilder(nameLength + 10); //I doubt you'd have a parameter long enough to have more than 10 underscores
		for (int i = 0; i < nameLength; i++) {
			final char c = name.charAt(i);

			if (Character.isUpperCase(c)) {
				optionNameBuilder.append('_').append(Character.toLowerCase(c));
			} else {
				optionNameBuilder.append(c);
			}
		}

		return optionNameBuilder.toString();
	}

	/**
	 * Not localized
	 */
	@NotNull
	public String getEffectiveName() {
		return effectiveName;
	}

	/**
	 * Not localized
	 */
	@NotNull
	public String getEffectiveDescription() {
		return effectiveDescription;
	}

	public boolean hasAutocompletion() {
		return getAutocompletionHandlerName() != null;
	}

	@Nullable
	public String getAutocompletionHandlerName() {
		return autocompletionHandlerName;
	}
}
