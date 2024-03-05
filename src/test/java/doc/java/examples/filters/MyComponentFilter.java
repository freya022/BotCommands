package doc.java.examples.filters;

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyComponentFilter implements ComponentInteractionFilter<String> {
    private final BContext context;

    public MyComponentFilter(BContext context) {
        this.context = context;
    }

    @Nullable
    @Override
    public String check(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
        if (event.getChannel().getIdLong() == 932902082724380744L && context.isOwner(event.getUser().getIdLong())) {
            return "Only owners are allowed to use components in <#932902082724380744>";
        }
        return null;
    }
}
