package doc.java.examples.filters;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@BService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyApplicationCommandRejectionHandler implements ApplicationCommandRejectionHandler<String> {
    @Override
    public void handle(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo, @NotNull String userData) {
        event.reply(userData).setEphemeral(true).queue();
    }
}
