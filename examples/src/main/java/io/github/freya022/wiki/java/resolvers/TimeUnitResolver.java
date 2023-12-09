package io.github.freya022.wiki.java.resolvers;

import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.Resolvers;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo;
import io.github.freya022.wiki.switches.WikiDetailProfile;
import io.github.freya022.wiki.switches.WikiLanguage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@ServiceName("timeUnitResolverJava")
@WikiLanguage(WikiLanguage.Language.JAVA)
@WikiDetailProfile(WikiDetailProfile.Profile.DETAILED)
// --8<-- [start:time_unit_resolver-detailed-java]
@Resolver
public class TimeUnitResolver
        extends ClassParameterResolver<TimeUnitResolver, TimeUnit>
        implements SlashParameterResolver<TimeUnitResolver, TimeUnit> {

    public TimeUnitResolver() {
        super(TimeUnit.class);
    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

    @NotNull
    @Override
    public Collection<Command.Choice> getPredefinedChoices(@Nullable Guild guild) {
        return Stream.of(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
                // The Resolvers class helps us by providing resolvers for any enum type.
                // We're just using the helper method to change an enum value to a more natural name.
                .map(u -> new Command.Choice(Resolvers.toHumanName(u), u.name()))
                .toList();
    }

    @Nullable
    @Override
    public TimeUnit resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
        return TimeUnit.valueOf(optionMapping.getAsString());
    }
}
// --8<-- [end:time_unit_resolver-detailed-java]
