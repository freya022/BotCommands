package com.freya02.bot.extensionsbot.commands;

import com.freya02.botcommands.annotation.CommandMarker;
import com.freya02.botcommands.slash.SlashCommand;
import com.freya02.botcommands.slash.SlashEvent;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.utils.Timestamp;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@CommandMarker //So it doesn't appear unused
public class SlashTimestamp extends SlashCommand {
	@JdaSlashCommand(
			name = "timestamp",
			description = "Converts a Discord timestamp into a medium date time"
	)
	public void run(SlashEvent event, Timestamp timestamp /*This parameter works as it has been registered in ExtensionsBotMain with a TimestampResolver */) {
		event.reply(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).localizedBy(Locale.ROOT).format(timestamp.toInstant().atOffset(ZoneOffset.UTC)))
				.setEphemeral(true)
				.queue();
	}
}
