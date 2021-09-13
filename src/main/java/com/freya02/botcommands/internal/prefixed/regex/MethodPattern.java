package com.freya02.botcommands.internal.prefixed.regex;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class MethodPattern {
	public final Method method;
	public final Pattern pattern;
	public final ArgumentFunction[] argumentsArr;

	MethodPattern(Method method, Pattern pattern, ArgumentFunction[] argumentsArr) {
		this.method = method;
		this.pattern = pattern;
		this.argumentsArr = argumentsArr;
	}
}
