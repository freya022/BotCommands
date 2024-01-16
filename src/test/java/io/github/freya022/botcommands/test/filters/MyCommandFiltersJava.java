package io.github.freya022.botcommands.test.filters;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter;
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo;
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BService
@TestLanguage(TestLanguage.Language.JAVA)
public class MyCommandFiltersJava implements TextCommandFilter<String>, ApplicationCommandFilter<String> {
    @Nullable
    @Override
    public String check(@NotNull MessageReceivedEvent event, @NotNull TextCommandVariation commandVariation, @NotNull String args) {
        return check(event.getGuildChannel());
    }

    @Nullable
    @Override
    public String check(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
        return check(event.getGuildChannel());
    }

    @Nullable
    private String check(@NotNull Channel channel) {
        if (channel.getIdLong() != 722891685755093076L) {
            return "Can only run commands in <#722891685755093076>";
        }
        return null;
    }
}
