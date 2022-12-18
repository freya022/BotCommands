package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.Nullable;

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

	private enum SpacePosition {
		LEFT,
		RIGHT
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

		public String toString(@Nullable SpacePosition position) {
			if (optional) {
				if (position == SpacePosition.LEFT) {
					return "(?:" + "\\s+" + pattern.toString() + ")?";
				} else if (position == SpacePosition.RIGHT) {
					return "(?:" + pattern.toString() + "\\s+" + ")?";
				} else {
					return "(?:" + pattern.toString() + ")?";
				}
			} else {
				if (position == SpacePosition.LEFT) {
					return "\\s+" + pattern.toString();
				} else if (position == SpacePosition.RIGHT) {
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

		// The space must stick to the optional part when in between, while being the nearest from the middle point
		// So if arg0 is optional, but arg1 is not, the space goes on the right part of the regex of arg0
		// If arg0 is required, but arg1 is optional, the space goes on the left part of the regex of arg1
		final SpacePosition[] positions = new SpacePosition[patterns.size()];
		for (int i = 0; i < patterns.size() - 1; i++) {
			final ParameterPattern arg0 = patterns.get(i);
			final ParameterPattern arg1 = patterns.get(i + 1);

			if (arg0.optional && !arg1.optional) {
				positions[i] = SpacePosition.RIGHT;
			} else if (!arg0.optional && arg1.optional) {
				positions[i + 1] = SpacePosition.LEFT;
			} else if (arg0.optional /*&& arg1.optional*/) {
				positions[i + 1] = SpacePosition.LEFT;
			} else { //Both are required
				positions[i + 1] = SpacePosition.LEFT;
			}
		}

		for (int i = 0, patternsSize = patterns.size(); i < patternsSize; i++) {
			final ParameterPattern pattern = patterns.get(i);
			final SpacePosition position = positions[i];
			builder.append(pattern.toString(position));
		}

		return Pattern.compile(builder.toString());
	}
}
