package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.util.*;

public class SlashAutocomplete extends ApplicationCommand {
	@JDASlashCommand(name = "auto")
	public void auto(GuildSlashEvent event,
	                 @SlashOption(autocomplete = "autoStr") String str,
	                 @SlashOption(autocomplete = "autoInt") long integer,
	                 @SlashOption(autocomplete = "autoDou") double number
	) {
		event.reply(str).queue();
	}

	@AutocompleteHandler(name = "autoStr", mode = AutocompleteMode.CONTINUITY, showUserInput = false)
	public Queue<String> autoStr(CommandAutoCompleteInteractionEvent event) {
		System.out.println(event.getFocusedOption().getValue());

		return new ArrayDeque<>(List.of("Anaheim Ducks",
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

	@AutocompleteHandler(name = "autoInt")
	public Set<Long> autoLong(CommandAutoCompleteInteractionEvent event,
	                          @SlashOption(name = "str") String autoStr,
	                          @SlashOption long integer) {
		return new HashSet<>(List.of(1L, 12L, 123L));
	}

	@AutocompleteHandler(name = "autoDou")
	public Collection<Double> autoDou(CommandAutoCompleteInteractionEvent event,
	                                  @SlashOption String str,
	                                  JDA jda,
	                                  @SlashOption long integer,
	                                  @SlashOption double number) {
		System.out.println(event.getOptions());
		System.out.println("str = " + str);
		System.out.println("jda = " + jda);
		System.out.println("integer = " + integer);
		System.out.println("number = " + number);

		return List.of(1.1, 12.12, 123.123);
	}
}
