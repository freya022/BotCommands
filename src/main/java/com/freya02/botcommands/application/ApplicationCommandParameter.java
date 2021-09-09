package com.freya02.botcommands.application;

import com.freya02.botcommands.application.slash.annotations.Option;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.parameters.CustomResolver;
import com.freya02.botcommands.parameters.ParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolvers;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Parameter;

public abstract class ApplicationCommandParameter<RESOLVER> {
	private final RESOLVER resolver;
	private final Class<?> type;
	private final CustomResolver customResolver;
	
	private final Parameter parameter;
	private final int index;
	private final ApplicationOptionData applicationOptionData;

	@SuppressWarnings("unchecked")
	public ApplicationCommandParameter(Class<RESOLVER> resolverType, Parameter parameter, int index) {
		this.parameter = parameter;
		this.type = Utils.getBoxedType(parameter.getType());
		this.index = index;

		final ParameterResolver resolver = ParameterResolvers.of(this.type);
		if (parameter.isAnnotationPresent(Option.class)) {
			this.applicationOptionData = new ApplicationOptionData(parameter);

			if (resolver == null) {
				throw new IllegalArgumentException("Unknown application command option type: " + type.getName() + " for target resolver " + resolverType.getName());
			} else if (!(resolverType.isAssignableFrom(resolver.getClass()))) {
				throw new IllegalArgumentException("Unsupported application command option type: " + type.getName() + " for target resolver " + resolverType.getName());
			}

			this.resolver = (RESOLVER) resolver;
			this.customResolver = null;
		} else {
			this.applicationOptionData = null;
			this.resolver = null;

			if (resolver instanceof CustomResolver) {
				this.customResolver = (CustomResolver) resolver;
			} else {
				throw new IllegalArgumentException("Unsupported custom parameter: " + type.getName());
			}
		}
	}

	@NotNull
	public Class<?> getType() {
		return type;
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
