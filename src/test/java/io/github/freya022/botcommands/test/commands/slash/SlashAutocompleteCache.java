package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.Logging;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class SlashAutocompleteCache extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	@JDASlashCommand(name = "auto_cache")
	public void auto(GuildSlashEvent event,
	                 @SlashOption(autocomplete = "autoCacheStr") String str,
	                 @SlashOption(autocomplete = "autoCacheInt") long integer,
	                 @SlashOption(autocomplete = "autoCacheDou") double number
	) {
		event.reply(str).queue();
	}

	@CacheAutocomplete(guildLocal = true, channelLocal = true, userLocal = true)
	@AutocompleteHandler(name = "autoCacheStr", mode = AutocompleteMode.CONTINUITY, showUserInput = false)
	public Queue<String> autoStr(CommandAutoCompleteInteractionEvent event) throws InterruptedException {
		LOGGER.warn("Computing constant");

		Thread.sleep(2000); //Simulate a long API request to show cache working

		return new ArrayDeque<>(List.of(event.getChannel().getName(),
				"Anaheim Ducks",
				"Arizona Coyotes",
				"Boston Bruins",
				"Buffalo Sabres",
				"Calgary Flames",
				"Carolina Hurricanes",
				"Chicago Blackhawks",
				"Colorado Avalanche",
				"Columbus Blue Jackets",
				"Dallas Stars",
				"Detroit Red Wings",
				"Edmonton Oilers",
				"Florida Panthers",
				"Los Angeles Kings",
				"Minnesota Wild",
				"Montréal Canadiens",
				"Nashville Predators",
				"New Jersey Devils",
				"New York Islanders",
				"New York Rangers",
				"Ottawa Senators",
				"Philadelphia Flyers",
				"Pittsburgh Penguins",
				"San Jose Sharks",
				"Seattle Kraken",
				"St. Louis Blues",
				"Tampa Bay Lightning",
				"Toronto Maple Leafs",
				"Vancouver Canucks",
				"Vegas Golden Knights",
				"Washington Capitals",
				"Winnipeg Jets"));
	}

	@CacheAutocomplete(compositeKeys = {"str", "integer"})
	@AutocompleteHandler(name = "autoCacheInt")
	public Set<Long> autoLong(CommandAutoCompleteInteractionEvent event,
	                          @SlashOption(name = "str") String autoStr,
	                          @SlashOption long integer) throws InterruptedException {
		LOGGER.warn("Computing constant by key");

		Thread.sleep(2000); //Simulate a long API request to show cache working

		return LongStream.rangeClosed(0, 500).boxed().collect(Collectors.toSet());
	}

	@AutocompleteHandler(name = "autoCacheDou")
	public Collection<Double> autoDou(CommandAutoCompleteInteractionEvent event,
	                                  @SlashOption String str,
	                                  JDA jda,
	                                  @SlashOption long integer,
	                                  @SlashOption double number) throws InterruptedException {
		LOGGER.warn("Computing key");

		Thread.sleep(2000); //Simulate a long API request to show cache working

		return List.of(1.1, 12.12, 123.123);
	}
}
