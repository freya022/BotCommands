package dev.freya02.botcommands.bot.commands.slash;

import dev.freya02.botcommands.bot.services.INamedService;
import dev.freya02.botcommands.bot.services.UnusedInterfacedService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.core.db.BlockingDatabase;
import io.github.freya022.botcommands.api.core.service.LazyService;
import io.github.freya022.botcommands.api.core.service.annotations.RequiresDefaultInjection;
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName;
import io.github.freya022.botcommands.internal.core.ReadyListener;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Command
@RequiresDefaultInjection
public class SlashDIJava extends ApplicationCommand {
    public SlashDIJava(@ServiceName("modifiedNamedService") INamedService namedService,
                       @javax.annotation.Nullable UnusedInterfacedService unusedInterfacedService) {
        System.out.println("Named service: " + namedService);
        System.out.println("UnusedInterfacedService: " + unusedInterfacedService);
    }

    @JDASlashCommand(name = "di_java")
    public void onSlashDi(GuildSlashEvent event,
                          List<ApplicationCommandFilter> filters,
                          LazyService<BlockingDatabase> databaseLazy,
                          @ServiceName("firstReadyListenerNope") @Nullable ReadyListener inexistantListener) {
        event.replyFormat("""
                                Filters: %s
                                DB: %s
                                inexistant listener: %s
                                """,
                        filters,
                        databaseLazy.getValue(),
                        inexistantListener)
                .setEphemeral(true)
                .queue();
    }
}
