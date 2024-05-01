package io.github.freya022.wiki.java.filters;

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:component_filter-java]
@BService
public class GeneralChannelFilter implements ComponentInteractionFilter<String/*(1)!*/> {
    private static final long CHANNEL_ID = 722891685755093076L;

    @Override
    public boolean getGlobal() {
        // So we can apply this filter on specific components
        return false;
    }

    @Nullable
    @Override
    public String check(@NotNull GenericComponentInteractionCreateEvent event,
                        @Nullable String handlerName) {
        if (event.getChannelIdLong() == CHANNEL_ID)
            return "This button can only be used in <#" + CHANNEL_ID + ">";
        return null;
    }
}
// --8<-- [end:component_filter-java]
