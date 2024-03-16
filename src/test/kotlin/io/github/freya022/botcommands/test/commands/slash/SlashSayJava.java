package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.components.Button;
import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies;
import io.github.freya022.botcommands.api.utils.EmojiUtils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Duration;

@Command
@Dependencies(Components.class) // Disables the command if components are not enabled
public class SlashSayJava extends ApplicationCommand {
    private final Components components;

    public SlashSayJava(Components components) {
        this.components = components;
    }

    @JDASlashCommand(name = "say_java", description = "Sends a message in a channel")
    public void onSlashSay(
            GuildSlashEvent event,
            @SlashOption(description = "Channel to send the message in") TextChannel channel,
            @SlashOption(description = "What to say") String content
    ) {
        event.reply("Done!")
                .setEphemeral(true)
                .delay(Duration.ofSeconds(5))
                .flatMap(InteractionHook::deleteOriginal)
                .queue();

        final Button deleteButton = components.dangerButton(EmojiUtils.resolveJDAEmoji("wastebasket")).ephemeral()
                .bindTo(buttonEvent -> {
                    buttonEvent.deferEdit().queue();
                    buttonEvent.getHook().deleteOriginal().queue();
                })
                .build();
        channel.sendMessage(content)
                .addActionRow(deleteButton)
                .queue();
    }
}
