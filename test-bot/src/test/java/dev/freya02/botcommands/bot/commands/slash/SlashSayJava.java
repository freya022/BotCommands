package dev.freya02.botcommands.bot.commands.slash;

import dev.freya02.jda.emojis.unicode.UnicodeEmojis;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.components.Button;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Duration;

@Command
@RequiresComponents // (Optional) Disables the command if components are not enabled
public class SlashSayJava {

    private final Buttons buttons; // Factory for buttons

    public SlashSayJava(Buttons buttons) {
        this.buttons = buttons;
    }

    // The descriptions can also be moved to localization files, reducing noise
    @JDASlashCommand(name = "say_java", description = "Sends a message in a channel")
    public void onSlashSay(
            GuildSlashEvent event,
            @SlashOption(description = "Channel to send the message in") TextChannel channel,
            @SlashOption(description = "What to say") String content
    ) {
        final Button deleteButton = buttons.danger(UnicodeEmojis.WASTEBASKET).ephemeral()
                .bindTo(buttonEvent -> {
                    buttonEvent.deferEdit().queue();
                    buttonEvent.getHook().deleteOriginal().queue();
                })
                .build();

        event.reply("Done!")
                .setEphemeral(true)
                .delay(Duration.ofSeconds(5))
                .flatMap(InteractionHook::deleteOriginal)
                .queue();

        channel.sendMessage(content)
                .addComponents(ActionRow.of(deleteButton))
                .queue();
    }
}
