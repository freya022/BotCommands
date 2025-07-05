package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import java.util.List;

// Regression test for Kotlin resolvers used for Java types,
// the List<Double> is read as a j.u.List but the resolver only accepts a k.c.List
@Command
public class SlashDynamicTypedResolverJava extends ApplicationCommand {
    @JDASlashCommand(name = "dynamic_typed_resolver_java")
    public void onSlashDynamicTypedResolverJava(
            GuildSlashEvent event,
            @SlashOption List<Double> list
    ) {
        event.reply("works").setEphemeral(true).queue();
    }
}
