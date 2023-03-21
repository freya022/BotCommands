package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.internal.utils.LocalizationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

public class ApplicationOptionData {
	private final String effectiveName, effectiveDescription;
	private final String autocompletionHandlerName;

	public ApplicationOptionData(BContext context, CommandPath path, Parameter parameter) {
		final AppOption option = parameter.getAnnotation(AppOption.class);

		if (option.name().isBlank()) {
			effectiveName = getOptionName(parameter);
		} else {
			effectiveName = option.name();
		}

		effectiveDescription = getEffectiveDescription(context, path, option);

		if (option.autocomplete().isBlank()) {
			autocompletionHandlerName = null;
		} else {
			autocompletionHandlerName = option.autocomplete();
		}
	}

	@NotNull
	private String getEffectiveDescription(@NotNull BContext context, CommandPath path, @NotNull AppOption option) {
		//Not in autocomplete
		if (path != null) {
			final String joinedPath = path.getFullPath('.');
			final String rootLocalization = LocalizationUtils.getCommandRootLocalization((BContextImpl) context, "%s.options.%s.description".formatted(joinedPath, effectiveName));
			if (rootLocalization != null)
				return rootLocalization;
		}

		if (option.description().isBlank()) {
			return "No description";
		} else {
			return option.description();
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
