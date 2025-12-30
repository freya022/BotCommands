package doc.java.examples.filters;

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter;
import io.github.freya022.botcommands.api.core.BotOwners;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@BService
@NullMarked
public class MyComponentFilter implements ComponentInteractionFilter {

    private final BotOwners botOwners;

    public MyComponentFilter(BotOwners botOwners) {
        this.botOwners = botOwners;
    }

    @Override
    public boolean getGlobal() {
        return true;
    }

    @Nullable
    @Override
    public String check(GenericComponentInteractionCreateEvent event, @Nullable String handlerName) {
        if (event.getChannel().getIdLong() == 932902082724380744L && !botOwners.isOwner(event.getUser())) {
            event.reply("Only owners are allowed to use components in <#932902082724380744>").setEphemeral(true).queue();
            return "Not an owner";
        }
        return null;
    }
}
