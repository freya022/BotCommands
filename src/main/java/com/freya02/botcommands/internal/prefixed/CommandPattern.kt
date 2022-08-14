package com.freya02.botcommands.internal.prefixed;

import java.util.regex.Pattern;

public class CommandPattern {
	public static Pattern of(TextCommandInfo commandInfo) {
		throw new UnsupportedOperationException();

//		final StringBuilder exampleBuilder = new StringBuilder(commandInfo.getParameters().getOptionCount() * 16);
//		final StringBuilder patternBuilder = new StringBuilder(commandInfo.getParameters().getOptionCount() * 16);
//		patternBuilder.append('^');
//
//		List<? extends TextCommandParameter> optionParameters = commandInfo.getOptionParameters();
//		final boolean hasMultipleQuotable = com.freya02.botcommands.internal.prefixed.Utils.hasMultipleQuotable(optionParameters);
//
//		for (int i = 0, optionParametersSize = optionParameters.size(); i < optionParametersSize; i++) {
//			final TextCommandParameter parameter = optionParameters.get(i);
//			final Pattern pattern = hasMultipleQuotable
//					? parameter.getResolver().getPreferredPattern() //Might be a quotable pattern
//					: parameter.getResolver().getPattern();
//
//			final String optionalSpacePattern = i == 0 ? "" : "\\s+";
//			if (parameter.isOptional()) {
//				patternBuilder.append("(?:").append(optionalSpacePattern).append(pattern.toString()).append(")?");
//			} else {
//				patternBuilder.append(optionalSpacePattern).append(pattern.toString());
//
//				exampleBuilder.append(parameter.getResolver().getTestExample()).append(' ');
//			}
//		}
//
//		final Pattern pattern = Pattern.compile(patternBuilder.toString());
//
//		//Try to match the built pattern to a built example string,
//		// if this fails then the pattern (and the command) is deemed too complex to be used
//		final String exampleStr = exampleBuilder.toString().trim();
//		if (!pattern.matcher(exampleStr).matches())
//			throw new IllegalArgumentException("Failed building pattern for method " + Utils.formatMethodShort(commandInfo.getMethod()) + " with pattern '" + pattern + "' and example '" + exampleStr + "'\n" +
//					"You can try to either rearrange the arguments as to make a parsable command, especially moving parameters which are parsed from strings, or, use slash commands");
//
//		return pattern;
	}
}
