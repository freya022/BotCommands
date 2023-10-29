package io.github.freya022.botcommands.test.filters;

import io.github.freya022.botcommands.api.commands.text.TextCommandRejectionHandler;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@BService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyTextCommandRejectionHandlerJava implements TextCommandRejectionHandler<String> {
    @Override
    public void handle(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation variation, @NotNull String args, @NotNull String userData) {
        event.getMessage().reply(userData).queue();
    }
}
