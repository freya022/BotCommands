package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.localization.LocalizationPath;
import com.freya02.botcommands.api.localization.annotations.LocalizationPrefix;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LocalizationManager {
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\.");
	private final Map<Method, LocalizationPath> map = Collections.synchronizedMap(new HashMap<>());

	public LocalizationPath getLocalizationPrefix(Method method) {
		return map.computeIfAbsent(method, x -> {
			final LocalizationPrefix methodPrefix = method.getAnnotation(LocalizationPrefix.class);
			if (methodPrefix != null) return new LocalizationPath(SPLIT_PATTERN.split(methodPrefix.value()));

			final LocalizationPrefix classPrefix = method.getDeclaringClass().getAnnotation(LocalizationPrefix.class);
			if (classPrefix != null) return new LocalizationPath(SPLIT_PATTERN.split(classPrefix.value()));

			return new LocalizationPath();
		});
	}

	public LocalizationPath getLocalizationBundle(Method method) {
		return map.computeIfAbsent(method, x -> { //TODO
			final LocalizationPrefix methodPrefix = method.getAnnotation(LocalizationPrefix.class);
			if (methodPrefix != null) return new LocalizationPath(SPLIT_PATTERN.split(methodPrefix.value()));

			final LocalizationPrefix classPrefix = method.getDeclaringClass().getAnnotation(LocalizationPrefix.class);
			if (classPrefix != null) return new LocalizationPath(SPLIT_PATTERN.split(classPrefix.value()));

			return new LocalizationPath();
		});
	}
}
