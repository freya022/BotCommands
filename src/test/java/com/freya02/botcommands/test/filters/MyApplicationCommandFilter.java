package com.freya02.botcommands.test.filters;

import com.freya02.botcommands.api.commands.application.ApplicationCommandFilter;
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

//@BService
public class MyApplicationCommandFilter implements ApplicationCommandFilter {
    @Override
    public boolean isAccepted(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
        if (event.getChannel() == null || event.getChannel().getIdLong() != 722891685755093076L) {
            event.reply("Commands are not allowed in this channel").setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}
