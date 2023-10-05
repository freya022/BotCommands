package com.freya02.botcommands.test_kt.commands.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.annotations.*;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete;
import com.freya02.botcommands.api.commands.ratelimit.RateLimitScope;
import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static com.freya02.botcommands.test_kt.commands.slash.SlashMyCommand.autocompleteHandlerName;
import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Command
public class SlashMyJavaCommand extends ApplicationCommand {
	@RateLimit(
			scope = RateLimitScope.USER, bandwidths = {
			@Bandwidth(capacity = 5, refill = @Refill(type = RefillType.GREEDY, tokens = 5, period = 1, periodUnit = ChronoUnit.MINUTES)),
			@Bandwidth(capacity = 2, refill = @Refill(type = RefillType.INTERVAL, tokens = 2, period = 5, periodUnit = ChronoUnit.SECONDS))
	})
	@Override
	@NotNull
	public ApplicationGeneratedValueSupplier getGeneratedValueSupplier(@Nullable Guild guild,
	                                                                   @Nullable String commandId,
	                                                                   @NotNull CommandPath commandPath,
	                                                                   @NotNull String optionName,
	                                                                   @NotNull ParameterType parameterType) {
		if (optionName.equals("guild_name")) {
			return event -> event.getGuild().getName();
		}

		return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType);
	}

	@Override
	@NotNull
	public List<Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String optionName) {
		if (optionName.equals("string_option") || optionName.equals("string_annotated")) {
			return List.of(new Choice("a", "a"), new Choice("b", "b"), new Choice("c", "c"));
		} else if (optionName.equals("int_option") || optionName.equals("int_annotated")) {
			return List.of(new Choice("1", 1L), new Choice("2", 2L));
		}

		return super.getOptionChoices(guild, commandPath, optionName);
	}

	@AutocompleteHandler(name = autocompleteHandlerName)
	@CacheAutocomplete(cacheMode = AutocompleteCacheMode.CONSTANT_BY_KEY)
	public Collection<Choice> runAutocompleteJava(CommandAutoCompleteInteractionEvent event, String stringOption, @Nullable Double doubleOption) {
		return List.of(new Choice("test, string: " + stringOption + ", double: " + doubleOption, "test"));
	}

	@JDASlashCommand(name = "my_command_annotated", subcommand = "java", description = "mah desc")
	public void cmd(GuildSlashEvent event,
	                @SlashOption(name = "string_annotated", description = "Option description") String stringOption,
	                @SlashOption(name = "int_annotated", description = "An integer") @LongRange(from = 1, to = 2) int intOption,
	                @SlashOption(name = "user_annotated", description = "An user") User userOption,
	                @SlashOption(name = "channel_annotated") GuildChannel channelOption,
	                @SlashOption(name = "autocomplete_str_annotated", description = "Autocomplete !", autocomplete = autocompleteHandlerName) String autocompleteStr,
	                @SlashOption(name = "double_annotated", description = "A double") @Optional double doubleOption,
	                BContext custom,
	                @GeneratedOption String guildName) {
		event.reply(stringOption + intOption + doubleOption + userOption + custom + channelOption + autocompleteStr + guildName).queue();
	}
}
