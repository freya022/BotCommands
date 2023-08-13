package com.freya02.botcommands.test.filters;

import com.freya02.botcommands.api.commands.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.core.config.BConfig;
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

//@BService
public class MyComponentCommandFilter implements ApplicationCommandFilter {
    private final BConfig config;

    public MyComponentCommandFilter(BConfig config) {
        this.config = config;
    }

    @Override
    public boolean isAccepted(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
        if (!config.isOwner(event.getUser().getIdLong())) {
            event.reply("Only owners are allowed to use components").setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}
