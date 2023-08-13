package com.freya02.botcommands.test.filters;

import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

//@BService
public class MyTextCommandFilter implements TextCommandFilter {
    @Override
    public boolean isAccepted(@NotNull MessageReceivedEvent event, @NotNull TextCommandInfo commandInfo, @NotNull String args) {
        return event.getChannel().getIdLong() == 722891685755093076L;
    }
}
