package doc.java.examples.filters;

import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyComponentRejectionHandler implements ComponentInteractionRejectionHandler<String> {
    @Override
    public void handle(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName, @NotNull String userData) {
        event.reply(userData).setEphemeral(true).queue();
    }
}
