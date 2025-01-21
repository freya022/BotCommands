package doc.java.examples.filters;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

@BService
@TestService
public class MyComponentRejectionHandler {

    public void handle(@NotNull GenericComponentInteractionCreateEvent event, @NotNull String reason) {
        event.reply(reason).setEphemeral(true).queue();
    }
}
