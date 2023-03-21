package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.slash.annotations.VarArgs;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public abstract class ApplicationCommandVarArgParameter<RESOLVER> extends ApplicationCommandParameter<RESOLVER> {
	private final int varArgs, numRequired;

	public ApplicationCommandVarArgParameter(BContext context, CommandPath path, Class<RESOLVER> resolverType, Parameter parameter, int index) {
		super(context, path, resolverType, parameter, ReflectionUtils.getCollectionTypeOrBoxedSelfType(parameter), index);

		final VarArgs varArgsAnnot = parameter.getAnnotation(VarArgs.class);
		if (varArgsAnnot != null) {
			this.varArgs = varArgsAnnot.value();
			this.numRequired = varArgsAnnot.numRequired();

			if (!List.class.isAssignableFrom(parameter.getType())) {
				throw new IllegalArgumentException("Parameter #%d at %s must be a List since it is annotated with @%s".formatted(index, Utils.formatMethodShort((Method) parameter.getDeclaringExecutable()), VarArgs.class.getSimpleName()));
			}

			if (varArgs < 1 || varArgs > 25) {
				throw new IllegalArgumentException("@" + VarArgs.class.getSimpleName() + "'s value need to be between 1 and 25 included");
			}
		} else {
			this.varArgs = numRequired = -1;
		}
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
