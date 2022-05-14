package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import kotlin.reflect.KParameter;

public abstract class ApplicationCommandVarArgParameter<RESOLVER> extends ApplicationCommandParameter<RESOLVER> {
	private final int varArgs = 0, numRequired = 0; //TODO varargs

	public ApplicationCommandVarArgParameter(Class<RESOLVER> resolverType, KParameter parameter, int index) {
		super(resolverType, parameter, parameter.getType(), index);
	}

	public boolean isVarArg() {
		return varArgs != -1;
	}

	public int getVarArgs() {
		return varArgs;
	}

	public boolean isRequiredVararg(int varArgNum) {
		if (!isVarArg()) return !isOptional(); //Default if not a vararg

		return varArgNum < numRequired;
	}
}
