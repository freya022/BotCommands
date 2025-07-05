package doc.java.examples.filters;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
public class MyApplicationCommandFilter implements ApplicationCommandFilter {

    @Override
    public boolean getGlobal() {
        return true;
    }

    @Nullable
    @Override
    public String check(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
        if (event.getChannel().getIdLong() != 722891685755093076L) {
            event.reply("Can only run commands in <#722891685755093076>").setEphemeral(true).queue();
            return "Not the right channel";
        }
        return null;
    }
}
