package doc.java.examples.filters;

import io.github.freya022.botcommands.api.commands.text.TextCommandFilter;
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@BService
@NullMarked
public class MyTextCommandFilter implements TextCommandFilter {

    @Override
    public boolean getGlobal() {
        return true;
    }

    @Nullable
    @Override
    public String check(MessageReceivedEvent event, TextCommandVariation commandVariation, String args) {
        if (event.getChannel().getIdLong() != 722891685755093076L) {
            event.getMessage().reply("Can only run commands in <#722891685755093076>").queue();
            return "Wrong channel";
        }
        return null;
    }
}
