package io.github.freya022.bot.commands.slash;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.annotations.GeneratedOption;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.parameters.ParameterType;
import io.github.freya022.bot.switches.WikiCommandProfile;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:create_time-java]
@Command
public class SlashCreateTimeJava extends ApplicationCommand {
    @NotNull
    @Override
    public ApplicationGeneratedValueSupplier getGeneratedValueSupplier(
            @Nullable Guild guild,
            @Nullable String commandId,
            @NotNull CommandPath commandPath,
            @NotNull String optionName,
            @NotNull ParameterType parameterType
    ) {
        if (commandPath.getName().equals("create_time")) {
            if (optionName.equals("timestamp")) {
                // Create a snapshot of the instant the command was created
                final Instant now = Instant.now();
                // Give back the instant snapshot, as this will be called every time the command is ran
                return event -> now;
            }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType);
    }

    @JDASlashCommand(name = "create_time", description = "Shows the creation time of this command")
    public void onSlashTimeIn(
            GuildSlashEvent event,
            @GeneratedOption Instant timestamp
    ) {
        event.reply("I was created on " + TimeFormat.DATE_TIME_SHORT.format(timestamp)).queue();
    }
}
// --8<-- [end:create_time-java]
