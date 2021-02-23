package com.freya02.botcommands.regex;

public class ArgumentFunction {
	public final String pattern;
	public final int groups;
	public final PatternSolver function;

	ArgumentFunction(String pattern, int groups, PatternSolver function) {
		this.pattern = pattern;
		this.groups = groups;
		this.function = function;
	}

	ArgumentFunction optimize(String newPattern) {
		return new ArgumentFunction(newPattern, groups, function);
	}
}
