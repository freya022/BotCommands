package doc.kotlin.examples.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverManager
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverProvider
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.withTextCommands
import io.github.freya022.botcommands.api.parameters.resolvers.registerEnum
import java.util.concurrent.TimeUnit

@BService
object TimeUnitResolverProvider : ResolverProvider {
    override fun declare(manager: ResolverManager) {
        // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
        manager.registerEnum<TimeUnit> {
            // Add support for text commands, you can add support for more handler types in a similar way
            withTextCommands {
                // Further configuration
            }

            setValues(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)

            // Further configuration
        }
    }
}
