package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.parameters.CustomResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

public class CommandParameter<RESOLVER> {
	private final RESOLVER resolver;
	private final Class<?> boxedType;
	private final CustomResolver customResolver;

	private final Parameter parameter;
	private final int index;

	private final boolean optional, isPrimitive;

	@SuppressWarnings("unchecked")
	public CommandParameter(@Nullable Class<RESOLVER> resolverType, Parameter parameter, int index) {
		this.parameter = parameter;
		this.boxedType = Utils.getBoxedType(parameter.getType());
		this.index = index;

		this.optional = ReflectionUtils.isOptional(parameter);
		this.isPrimitive = parameter.getType().isPrimitive();

		final ParameterResolver resolver = ParameterResolvers.of(this.boxedType);
		if (AnnotationUtils.isOption(parameter)) {
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
				throw new IllegalArgumentException("Unsupported custom parameter: " + boxedType.getName() + ", did you forget to use @TextOption or @AppOption on non-custom options ?");
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
