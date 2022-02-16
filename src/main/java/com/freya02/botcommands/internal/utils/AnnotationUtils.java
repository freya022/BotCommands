package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.annotations.*;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.annotations.Test;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.modals.annotations.ModalData;
import com.freya02.botcommands.api.modals.annotations.ModalInput;
import com.freya02.botcommands.api.prefixed.annotations.Hidden;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.CooldownStrategy;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class AnnotationUtils {
	@SuppressWarnings("unchecked")
	@NotNull
	public static <T, A extends Annotation> T getAnnotationValue(@NotNull A annotation, @NotNull String methodName) {
		try {
			return (T) annotation.annotationType().getMethod(methodName).invoke(annotation);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Unable to get annotation value from " + annotation.annotationType().getName() + "#" + methodName, e);
		}
	}

	@Nullable
	public static <T, A extends Annotation> T getAnnotationValue(@NotNull AnnotatedElement element, Class<A> annotationType, String methodName) {
		final A annotation = element.getAnnotation(annotationType);

		if (annotation == null) return null;

		return getAnnotationValue(annotation, methodName);
	}

	@NotNull
	public static <T, A extends Annotation> T getAnnotationValue(@NotNull AnnotatedElement element, Class<A> annotationType, String methodName, T defaultVal) {
		final A annotation = element.getAnnotation(annotationType);

		if (annotation == null) return defaultVal;

		return getAnnotationValue(annotation, methodName);
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

	//List order is from deepest to most effective
	//aka class --> method
	public static <A extends Annotation> List<A> getEffectiveAnnotations(Method method, Class<A> annotation) {
		List<A> annotations = new ArrayList<>();

		final A classAnnotation = method.getDeclaringClass().getAnnotation(annotation);
		if (classAnnotation != null) {
			annotations.add(classAnnotation);
		}

		final A methodAnnotation = method.getAnnotation(annotation);
		if (methodAnnotation != null) {
			annotations.add(methodAnnotation);
		}

		return annotations;
	}

	public static TLongSet getEffectiveTestGuildIds(BContext context, Method method) {
		TLongSet testIds = new TLongHashSet(context.getTestGuildIds());

		final List<Test> effectiveAnnotations = getEffectiveAnnotations(method, Test.class);

		for (Test test : effectiveAnnotations) {
			final long[] ids = test.guildIds();
			final AppendMode mode = test.mode();

			if (mode == AppendMode.SET) {
				testIds.clear();
				testIds.addAll(ids);

				return testIds;
			} else if (mode == AppendMode.ADD) {
				testIds.addAll(ids);
			}
		}

		return testIds;
	}

	public static boolean isOption(Parameter parameter) {
		return Stream.of(TextOption.class, AppOption.class, ModalData.class, ModalInput.class).anyMatch(parameter::isAnnotationPresent);
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

	public static ChannelType[] getEffectiveChannelTypes(Parameter parameter) {
		final ChannelTypes annotation = parameter.getAnnotation(ChannelTypes.class);
		if (annotation == null) return new ChannelType[0];

		return annotation.value();
	}

	public static boolean getEffectiveTestState(Method method) {
		return !getEffectiveAnnotations(method, Test.class).isEmpty();
	}
}
