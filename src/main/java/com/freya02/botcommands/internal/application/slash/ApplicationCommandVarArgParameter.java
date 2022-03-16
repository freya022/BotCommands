package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.application.slash.annotations.VarArgs;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.utils.ReflectionUtils;

import java.lang.reflect.Parameter;

public abstract class ApplicationCommandVarArgParameter<RESOLVER> extends ApplicationCommandParameter<RESOLVER> {
	private final int varArgs;

	public ApplicationCommandVarArgParameter(Class<RESOLVER> resolverType, Parameter parameter, int index) {
		super(resolverType, parameter, ReflectionUtils.getCollectionTypeOrBoxedSelfType(parameter), index);

		final VarArgs varArgsAnnot = parameter.getAnnotation(VarArgs.class);
		if (varArgsAnnot != null) {
			this.varArgs = varArgsAnnot.value();

			if (varArgs < 1 || varArgs > 25) {
				throw new IllegalArgumentException("@" + VarArgs.class.getSimpleName() + "'s value need to be between 1 and 25 included");
			}
		} else {
			this.varArgs = -1;
		}
	}

	public boolean isVarArg() {
		return varArgs != -1;
	}

	public int getVarArgs() {
		return varArgs;
	}
}
