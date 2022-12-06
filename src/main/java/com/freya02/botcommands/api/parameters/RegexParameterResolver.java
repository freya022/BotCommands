package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
public interface RegexParameterResolver {
	/**
	 * Returns a resolved object from this text command interaction
	 *
	 * @param context The {@link BContext} of this bot
	 * @param info    The text command info of the command being executed
	 * @param event   The event of this received message
	 * @param args    The text arguments of this command, extracted with {@link #getPattern()}
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String[] args);

	/**
	 * Returns the pattern required to recognize this parameter
	 * <br>This is used to compose a larger pattern which will represent an entire command syntax
	 *
	 * <p>
	 * If you wish to apply flags to this pattern, please enable them inside the regular expression instead of on the pattern,
	 * as the pattern aggregator will not take any flags into account.
	 * <br>You can enable regex flags using the {@code (?[flags])} notation,
	 * such as {@code (?i)} to enable case-insensitive matching, and {@code (?-i)} to disable it.
	 * <br>Make sure to disable your modifiers when you are done using them, as they could affect other patterns.
	 * <br>You can find more information about regex modifiers <a href="https://www.regular-expressions.info/modifiers.html" target="_blank">here</a>
	 *
	 * @return The {@link Pattern} for this parameter
	 */
	@NotNull
	Pattern getPattern();

	/**
	 * Returns an example string for this parameter
	 * <br>This is only used to construct an example command internally and test the whole command's regex against it
	 * <br>If the regex does not match the constructed example command, the framework will throw as the regex is deemed "too complex", see {@link Optional}
	 *
	 * @return An example string for validation purposes
	 * @see Optional
	 */
	@NotNull
	String getTestExample();

	default Pattern getPreferredPattern() {
		if (this instanceof QuotableRegexParameterResolver) {
			return ((QuotableRegexParameterResolver) this).getQuotedPattern();
		} else {
			return getPattern();
		}
	}
}
