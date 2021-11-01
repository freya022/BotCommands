package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.annotations.*;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.prefixed.annotations.Hidden;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.CooldownStrategy;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class AnnotationUtils {
	@SuppressWarnings("unchecked")
	public static <T, A extends Annotation> T getAnnotationValue(A annotation, String methodName) {
		try {
			return (T) annotation.annotationType().getMethod(methodName).invoke(annotation);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Unable to get annotation value from " + annotation.annotationType().getName() + "#" + methodName, e);
		}
	}

	public static <T, A extends Annotation> T getAnnotationValue(Class<?> type, Class<A> annotationType, String methodName) {
		return getAnnotationValue(type.getAnnotation(annotationType), methodName);
	}

	public static CooldownStrategy getEffectiveCooldownStrategy(Method method) {
		final Cooldown annotation = method.getAnnotation(Cooldown.class);
		if (annotation == null) return new CooldownStrategy(0, TimeUnit.MILLISECONDS, CooldownScope.USER);

		return new CooldownStrategy(
				annotation.cooldown(),
				annotation.unit(),
				annotation.cooldownScope()
		);
	}

	@SuppressWarnings("DuplicatedCode")
	public static EnumSet<Permission> getEffectiveUserPermissions(Method method) {
		final EnumSet<Permission> permSet = EnumSet.noneOf(Permission.class);
		final Permission[] classPerms;

		final UserPermissions methodAnnotation = method.getAnnotation(UserPermissions.class);
		final UserPermissions classAnnotation = method.getDeclaringClass().getAnnotation(UserPermissions.class);
		if (classAnnotation != null) {
			classPerms = classAnnotation.value();
		} else classPerms = new Permission[0];

		if (methodAnnotation != null) {
			final Permission[] methodPerms = methodAnnotation.value();

			Collections.addAll(permSet, methodPerms);

			if (methodAnnotation.mode() == AppendMode.ADD) {
				Collections.addAll(permSet, classPerms);
			}
		} else {
			Collections.addAll(permSet, classPerms);
		}

		return permSet;
	}

	@SuppressWarnings("DuplicatedCode")
	public static EnumSet<Permission> getEffectiveBotPermissions(Method method) {
		final EnumSet<Permission> permSet = EnumSet.noneOf(Permission.class);
		final Permission[] classPerms;

		final BotPermissions methodAnnotation = method.getAnnotation(BotPermissions.class);
		final BotPermissions classAnnotation = method.getDeclaringClass().getAnnotation(BotPermissions.class);
		if (classAnnotation != null) {
			classPerms = classAnnotation.value();
		} else classPerms = new Permission[0];

		if (methodAnnotation != null) {
			final Permission[] methodPerms = methodAnnotation.value();

			Collections.addAll(permSet, methodPerms);

			if (methodAnnotation.mode() == AppendMode.ADD) {
				Collections.addAll(permSet, classPerms);
			}
		} else {
			Collections.addAll(permSet, classPerms);
		}

		return permSet;
	}

	public static boolean getEffectiveHiddenState(Method method) {
		if (method.isAnnotationPresent(Hidden.class)) {
			return true;
		} else {
			return method.getDeclaringClass().isAnnotationPresent(Hidden.class);
		}
	}

	public static boolean getEffectiveRequireOwnerState(Method method) {
		if (method.isAnnotationPresent(RequireOwner.class)) {
			return true;
		} else {
			return method.getDeclaringClass().isAnnotationPresent(RequireOwner.class);
		}
	}

	public static boolean isOption(Parameter parameter) {
		return parameter.isAnnotationPresent(TextOption.class) || parameter.isAnnotationPresent(AppOption.class);
	}

	@Nullable
	public static <A extends Annotation> A getEffectiveAnnotation(@NotNull Method method, @NotNull Class<A> annotationType) {
		final boolean methodAnnot = method.getAnnotation(annotationType) != null;

		if (methodAnnot) {
			return method.getAnnotation(annotationType);
		} else {
			return method.getDeclaringClass().getAnnotation(annotationType);
		}
	}
}
