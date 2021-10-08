package com.freya02.bot.extensionsbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.utils.Timestamp;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@CommandMarker //So it doesn't appear unused
public class SlashTimestamp extends ApplicationCommand {
	@JdaSlashCommand(
			name = "timestamp",
			description = "Converts a Discord timestamp into a medium date time"
	)
	public void run(GuildSlashEvent event, @AppOption Timestamp timestamp /*This parameter works as it has been registered in ExtensionsBotMain with a TimestampResolver */) {
		event.reply(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).localizedBy(Locale.ROOT).format(timestamp.toInstant().atOffset(ZoneOffset.UTC)))
				.setEphemeral(true)
				.queue();
	}
}
