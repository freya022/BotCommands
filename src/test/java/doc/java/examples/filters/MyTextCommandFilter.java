package doc.java.examples.filters;

import io.github.freya022.botcommands.api.commands.text.TextCommandFilter;
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import io.github.freya022.botcommands.test.switches.TestService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
@TestService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyTextCommandFilter implements TextCommandFilter {

    @Override
    public boolean getGlobal() {
        return true;
    }

    @Nullable
    @Override
    public String check(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation commandVariation, @NotNull String args) {
        if (event.getChannel().getIdLong() != 722891685755093076L) {
            event.getMessage().reply("Can only run commands in <#722891685755093076>").queue();
            return "Wrong channel";
        }
        return null;
    }
}
