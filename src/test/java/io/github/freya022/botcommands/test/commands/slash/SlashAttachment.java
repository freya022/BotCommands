package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

public class SlashAttachment extends ApplicationCommand {
	@JDASlashCommand(
			name = "attachment"
	)
	public void onSlashAttachment(GuildSlashEvent event, @SlashOption Message.Attachment attachment) {
		event.deferReply(true).queue();

		attachment.getProxy()
				.download()
				.thenAccept(stream -> {
			event.getHook()
					.sendFiles(FileUpload.fromData(stream, "reupload_" + attachment.getFileName()))
					.queue();
		});
	}
}