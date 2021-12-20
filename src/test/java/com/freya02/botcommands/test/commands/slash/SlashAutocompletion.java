package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.CommandAutoCompleteEvent;

import java.util.List;

public class SlashAutocompletion extends ApplicationCommand {
	@JDASlashCommand(name = "auto")
	public void auto(GuildSlashEvent event,
	                 @AppOption(autocomplete = "autoStr") String str,
	                 @AppOption(autocomplete = "autoInt") long integer,
	                 @AppOption(autocomplete = "autoDou") double number
	) {
		event.reply(str).queue();
	}

	@AutocompletionHandler(name = "autoStr", mode = AutocompletionMode.CONTINUITY)
	public List<String> autoStr(CommandAutoCompleteEvent event) {
		return List.of("a", "ab", "abc", "Abc Def");
	}

	@AutocompletionHandler(name = "autoInt")
	public List<Long> autoLong(CommandAutoCompleteEvent event, @AppOption(name = "str") String autoStr) {
		return List.of(1L, 12L, 123L);
	}

	@AutocompletionHandler(name = "autoDou")
	public List<Double> autoDou(CommandAutoCompleteEvent event,
	                            @AppOption String str,
	                            JDA jda,
	                            @AppOption long integer,
	                            @AppOption double number) {
		System.out.println(event.getOptions());
		System.out.println("str = " + str);
		System.out.println("jda = " + jda);
		System.out.println("integer = " + integer);
		System.out.println("number = " + number);

		return List.of(1.1, 12.12, 123.123);
	}
}
