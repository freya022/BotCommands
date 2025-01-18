package doc.java.examples.filters;

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter;
import io.github.freya022.botcommands.api.core.BotOwners;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import io.github.freya022.botcommands.test.switches.TestService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
@TestService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyComponentFilter implements ComponentInteractionFilter {

    private final MyComponentRejectionHandler rejectionHandler;
    private final BotOwners botOwners;

    public MyComponentFilter(MyComponentRejectionHandler rejectionHandler, BotOwners botOwners) {
        this.rejectionHandler = rejectionHandler;
        this.botOwners = botOwners;
    }

    @Nullable
    @Override
    public String check(@NotNull GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
        if (event.getChannel().getIdLong() == 932902082724380744L && !botOwners.isOwner(event.getUser())) {
            rejectionHandler.handle(event, "Only owners are allowed to use components in <#932902082724380744>");
            return "Not an owner";
        }
        return null;
    }
}
