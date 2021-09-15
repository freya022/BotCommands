package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.annotations.Option;
import com.freya02.botcommands.api.parameters.CustomResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolvers;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Parameter;

public abstract class ApplicationCommandParameter<RESOLVER> {
	private final RESOLVER resolver;
	private final Class<?> boxedType;
	private final CustomResolver customResolver;
	
	private final Parameter parameter;
	private final int index;
	private final ApplicationOptionData applicationOptionData;

	@SuppressWarnings("unchecked")
	public ApplicationCommandParameter(Class<RESOLVER> resolverType, Parameter parameter, int index) {
		this.parameter = parameter;
		this.boxedType = Utils.getBoxedType(parameter.getType());
		this.index = index;

		final ParameterResolver resolver = ParameterResolvers.of(this.boxedType);
		if (parameter.isAnnotationPresent(Option.class)) {
			this.applicationOptionData = new ApplicationOptionData(parameter);

			if (resolver == null) {
				throw new IllegalArgumentException("Unknown application command option type: " + boxedType.getName() + " for target resolver " + resolverType.getName());
			} else if (!(resolverType.isAssignableFrom(resolver.getClass()))) {
				throw new IllegalArgumentException("Unsupported application command option type: " + boxedType.getName() + " for target resolver " + resolverType.getName());
			}

			this.resolver = (RESOLVER) resolver;
			this.customResolver = null;
		} else {
			this.applicationOptionData = null;
			this.resolver = null;

			if (resolver instanceof CustomResolver) {
				this.customResolver = (CustomResolver) resolver;
			} else {
				throw new IllegalArgumentException("Unsupported custom parameter: " + boxedType.getName() + ", did you forget to use @Option on discord options ?");
			}
		}
	}

	@NotNull
	public Class<?> getBoxedType() {
		return boxedType;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public int getIndex() {
		return index;
	}

	public ApplicationOptionData getApplicationOptionData() {
		return applicationOptionData;
	}
	
	public boolean isOption() {
		return applicationOptionData != null;
	}

	public CustomResolver getCustomResolver() {
		return customResolver;
	}

	public RESOLVER getResolver() {
		return resolver;
	}
}
