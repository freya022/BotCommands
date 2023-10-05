package io.github.freya022.botcommands.test.filters;

import io.github.freya022.botcommands.api.commands.prefixed.TextCommandFilter;
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

//@BService
public class MyTextCommandFilter implements TextCommandFilter {
    @Override
    public boolean isAccepted(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation commandVariation, @NotNull String args) {
        return event.getChannel().getIdLong() == 722891685755093076L;
    }
}
