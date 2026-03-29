package doc.java.examples.resolvers;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverManager;
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverProvider;
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.TextCommandEnumResolver;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

@BService
public class TimeUnitResolverProvider implements ResolverProvider {
    @Override
    public void declare(@Nonnull ResolverManager manager) {
        // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
        manager.registerEnum(TimeUnit.class, builder -> {
            // Add support for text commands, you can add support for more handler types in a similar way
            builder.with(
                    // If you don't need further configuration, use "of"
                    TextCommandEnumResolver.builder(TimeUnit.class)
                            // Further configuration
                            .build()
            );

            builder.setValues(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES);

            // Further configuration
        });
    }
}
