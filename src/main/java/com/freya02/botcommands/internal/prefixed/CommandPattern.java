package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.internal.utils.Utils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandPattern {
	public static Pattern of(TextCommandInfo commandInfo) {
		final List<? extends TextCommandParameter> optionParameters = commandInfo.getOptionParameters();
		final boolean hasMultipleQuotable = com.freya02.botcommands.internal.prefixed.Utils.hasMultipleQuotable(optionParameters);

		final List<ParameterPattern> patterns = optionParameters.stream()
				.map(p -> new ParameterPattern(p.getResolver(), p.isOptional(), hasMultipleQuotable))
				.toList();
		final Pattern pattern = joinPatterns(patterns);

		//Try to match the built pattern to a built example string,
		// if this fails then the pattern (and the command) is deemed too complex to be used
		final String exampleStr = optionParameters.stream()
				.filter(p -> !p.isOptional())
				.map(p -> p.getResolver().getTestExample())
				.collect(Collectors.joining(" "));
		if (!pattern.matcher(exampleStr).matches())
			throw new IllegalArgumentException("Failed building pattern for method " + Utils.formatMethodShort(commandInfo.getMethod()) + " with pattern '" + pattern + "' and example '" + exampleStr + "'\n" +
					"You can try to either rearrange the arguments as to make a parsable command, especially moving parameters which are parsed from strings, or, use slash commands");

		return pattern;
	}

	public static class ParameterPattern {
		private final Pattern pattern;
		private final boolean optional;

		public ParameterPattern(RegexParameterResolver resolver, boolean optional, boolean hasMultipleQuotable) {
			this.optional = optional;
			this.pattern = hasMultipleQuotable
					? resolver.getPreferredPattern() //Might be a quotable pattern
					: resolver.getPattern();
		}

		public String toString(boolean includeSpace) {
			if (optional) {
				if (includeSpace) {
					return "(?:" + pattern.toString() + "\\s+" + ")?";
				} else {
					return "(?:" + pattern.toString() + ")?";
				}
			} else {
				if (includeSpace) {
					return pattern.toString() + "\\s+";
				} else {
					return pattern.toString();
				}
			}
		}
	}

	public static Pattern joinPatterns(List<ParameterPattern> patterns) {
		final StringBuilder builder = new StringBuilder(16 * patterns.size());
		builder.append("^");

		for (int i = 0, patternsSize = patterns.size(); i < patternsSize; i++) {
			ParameterPattern pattern = patterns.get(i);

			boolean includeSpace = i <= patternsSize - 2; //TODO should be positioned on left if last index
			builder.append(pattern.toString(includeSpace));
		}

		return Pattern.compile(builder.toString());
	}
}
