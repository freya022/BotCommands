package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Parameter;
import java.util.Optional;

public class TextParameterData {
	private final String optionalName, optionalExample;

	public TextParameterData(Parameter parameter) {
		final TextOption option = parameter.getAnnotation(TextOption.class);

		if (option.name().isBlank()) {
			optionalName = null;
		} else {
			optionalName = option.name();
		}

		if (option.example().isBlank()) {
			optionalExample = null;
		} else {
			optionalExample = option.example();
		}
	}

	@NotNull
	public Optional<String> getOptionalName() {
		return Optional.ofNullable(optionalName);
	}

	@NotNull
	public Optional<String> getOptionalExample() {
		return Optional.ofNullable(optionalExample);
	}
}
