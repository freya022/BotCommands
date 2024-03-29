package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.parameters.CustomResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.StringUtils;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

public abstract class CommandParameter<RESOLVER> {
	private final RESOLVER resolver;
	private final Class<?> boxedType;
	private final CustomResolver customResolver;

	private final Parameter parameter;
	private final int index;

	private final boolean optional, isPrimitive;

	protected abstract List<Class<? extends Annotation>> getOptionAnnotations();

	/**
	 * Returns the list of annotations that must be resolvable with a {@link ParameterResolver}
	 * <br>If an option is not annotated with one of these but is still a valid option, it is up to the handler to fill up the parameters accordingly, without use the resolver nor the custom resolver
	 */
	protected List<Class<? extends Annotation>> getResolvableAnnotations() {
		return getOptionAnnotations();
	}

	public CommandParameter(@Nullable Class<RESOLVER> resolverType, Parameter parameter, int index) {
		this(resolverType, parameter, Utils.getBoxedType(parameter.getType()), index);
	}

	public CommandParameter(@Nullable Class<RESOLVER> resolverType, Parameter parameter, Class<?> boxedType, int index) {
		this.parameter = parameter;
		this.boxedType = boxedType;
		this.index = index;

		this.optional = ReflectionUtils.isOptional(parameter);
		this.isPrimitive = parameter.getType().isPrimitive();

		final ParameterResolver resolver = ParameterResolvers.of(this.boxedType);
		final List<Class<? extends Annotation>> allowedAnnotation = getOptionAnnotations();
		final List<Class<? extends Annotation>> resolvableAnnotation = getResolvableAnnotations();
		if (allowedAnnotation.stream().anyMatch(parameter::isAnnotationPresent)) { //If the parameter has at least one valid annotation
			//If the parameter is not resolvable, but is still an option, then let the handler put the values itself
			if (resolvableAnnotation.stream().noneMatch(parameter::isAnnotationPresent)) {
				this.resolver = null;
				this.customResolver = null;

				return;
			}

			if (resolverType == null) {
				throw new IllegalArgumentException("Parameter of type " + boxedType.getName() + " is an annotated as an option but doesn't have a resolver type attached, please report to devs");
			}

			if (resolver == null) {
				throw new IllegalArgumentException("Unknown interaction command option type: " + boxedType.getName() + " for target resolver " + resolverType.getName());
			} else if (!(resolverType.isAssignableFrom(resolver.getClass()))) {
				throw new IllegalArgumentException("Unsupported interaction command option type: " + boxedType.getName() + " for target resolver " + resolverType.getName());
			}

			this.resolver = (RESOLVER) resolver;
			this.customResolver = null;
		} else {
			this.resolver = null;

			if (resolver instanceof CustomResolver) {
				this.customResolver = (CustomResolver) resolver;
			} else {
				throw new IllegalArgumentException("Unsupported custom parameter: %s, did you forget to use %s on non-custom options ?".formatted(boxedType.getName(),
						StringUtils.naturalJoin("or", allowedAnnotation.stream().map(Class::getSimpleName).toList()))
				);
			}
		}
	}

	public CustomResolver getCustomResolver() {
		return customResolver;
	}

	public RESOLVER getResolver() {
		return resolver;
	}

	@NotNull
	public Class<?> getBoxedType() {
		return boxedType;
	}

	@NotNull
	public Parameter getParameter() {
		return parameter;
	}

	public int getIndex() {
		return index;
	}

	public boolean isOption() {
		return resolver != null;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public boolean isOptional() {
		return optional;
	}
}
