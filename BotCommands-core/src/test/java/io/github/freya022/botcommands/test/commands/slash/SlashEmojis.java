package io.github.freya022.botcommands.test.commands.slash;

import dev.freya02.jda.emojis.unicode.Emojis;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Command
public class SlashEmojis extends ApplicationCommand {

    @JDASlashCommand(name = "emojis")
    public void onSlashEmojis(GuildSlashEvent event) {
        // For jda-emojis demonstration purposes, this does not work with BC
        Button approve = Button.primary("approve:data", "Approve").withEmoji(Emojis.WHITE_CHECK_MARK);
        StringSelectMenu choices = StringSelectMenu.create("choices:data")
                .addOptions(
                        SelectOption.of("First choice", "1").withEmoji(Emojis.ONE)
                )
                .setDefaultValues("1")
                .build();

        MessageCreateData messageData = new MessageCreateBuilder()
                .addActionRow(approve)
                .addActionRow(choices)
                .build();

        event.reply(messageData)
                // This emoji's name is slightly different as fields can't start with digits
                .flatMap(hook -> hook.getCallbackResponse().getMessage().addReaction(Emojis.HUNDRED_POINTS))
                .queue();
    }
}
