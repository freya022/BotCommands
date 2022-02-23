package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.localization.LocalizationPath;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;
import com.freya02.botcommands.api.localization.annotations.LocalizationPrefix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LocalizationManager {
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\.");
	private final Map<Method, LocalizationPath> prefixMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Method, String> bundleMap = Collections.synchronizedMap(new HashMap<>());

	@NotNull
	public LocalizationPath getLocalizationPrefix(@NotNull Method method) {
		return prefixMap.computeIfAbsent(method, x -> {
			final LocalizationPrefix methodPrefix = method.getAnnotation(LocalizationPrefix.class);
			if (methodPrefix != null) return new LocalizationPath(SPLIT_PATTERN.split(methodPrefix.value()));

			final LocalizationPrefix classPrefix = method.getDeclaringClass().getAnnotation(LocalizationPrefix.class);
			if (classPrefix != null) return new LocalizationPath(SPLIT_PATTERN.split(classPrefix.value()));

			return new LocalizationPath();
		});
	}

	@Nullable
	public String getLocalizationBundle(@NotNull Method method) {
		return bundleMap.computeIfAbsent(method, x -> {
			final LocalizationBundle methodPrefix = method.getAnnotation(LocalizationBundle.class);
			if (methodPrefix != null) return methodPrefix.value();

			final LocalizationBundle classPrefix = method.getDeclaringClass().getAnnotation(LocalizationBundle.class);
			if (classPrefix != null) return classPrefix.value();

			return null;
		});
	}
}
