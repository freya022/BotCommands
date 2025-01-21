package doc.java.examples.filters;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestService;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@BService
@TestService
public class MyApplicationCommandRejectionHandler {

    public void handle(@NotNull GenericCommandInteractionEvent event, @NotNull String reason) {
        event.reply(reason).setEphemeral(true).queue();
    }
}
