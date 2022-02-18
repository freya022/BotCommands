package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.CacheAutocompletion;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.CompositeKey;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class SlashAutocompletionCache extends ApplicationCommand {
	private static final Logger LOGGER = Logging.getLogger();

	@JDASlashCommand(name = "auto_cache")
	public void auto(GuildSlashEvent event,
	                 @AppOption(autocomplete = "autoCacheStr") String str,
	                 @AppOption(autocomplete = "autoCacheInt") long integer,
	                 @AppOption(autocomplete = "autoCacheDou") double number
	) {
		event.reply(str).queue();
	}

	@CacheAutocompletion(guildLocal = true, channelLocal = true, userLocal = true)
	@AutocompletionHandler(name = "autoCacheStr", mode = AutocompletionMode.CONTINUITY, showUserInput = false)
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

	@CacheAutocompletion
	@AutocompletionHandler(name = "autoCacheInt")
	public Set<Long> autoLong(CommandAutoCompleteInteractionEvent event,
	                          @CompositeKey @AppOption(name = "str") String autoStr,
	                          @CompositeKey @AppOption long integer) throws InterruptedException {
		LOGGER.warn("Computing constant by key");

		Thread.sleep(2000); //Simulate a long API request to show cache working

		return LongStream.rangeClosed(0, 500).boxed().collect(Collectors.toSet());
	}

	@AutocompletionHandler(name = "autoCacheDou")
	public Collection<Double> autoDou(CommandAutoCompleteInteractionEvent event,
	                                  @AppOption String str,
	                                  JDA jda,
	                                  @AppOption long integer,
	                                  @AppOption double number) throws InterruptedException {
		LOGGER.warn("Computing key");

		Thread.sleep(2000); //Simulate a long API request to show cache working

		return List.of(1.1, 12.12, 123.123);
	}
}
