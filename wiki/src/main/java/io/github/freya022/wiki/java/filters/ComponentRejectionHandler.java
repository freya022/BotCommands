package io.github.freya022.wiki.java.filters;

import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:component_rejection_handler-java]
@BService
public class ComponentRejectionHandler implements ComponentInteractionRejectionHandler<String/*(1)!*/> {
    @Override
    public void handle(@NotNull GenericComponentInteractionCreateEvent event,
                       @Nullable String handlerName,
                       @NotNull String userData) {
        event.reply(userData).setEphemeral(true).queue();
    }
}
// --8<-- [end:component_rejection_handler-java]
