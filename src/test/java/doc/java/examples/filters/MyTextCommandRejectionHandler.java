package doc.java.examples.filters;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import io.github.freya022.botcommands.test.switches.TestService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@BService
@TestService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyTextCommandRejectionHandler {

    public void handle(@NotNull MessageReceivedEvent event, @NotNull String reason) {
        event.getMessage().reply(reason).queue();
    }
}
