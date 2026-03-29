package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface implemented to declare resolvers.
 *
 * You can use this to declare them in bulk and to [declare enum resolvers][ResolverManager.registerEnum].
 *
 * This is an alternative to [@ResolverFactory][io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory] service factories.
 *
 * ### Example
 * #### Kotlin
 * ```kotlin
 * @BService
 * object TimeUnitResolverProvider : ResolverProvider {
 *     override fun declare(manager: ResolverManager) {
 *         // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
 *         manager.registerEnum<TimeUnit> {
 *             // Add support for text commands, you can add support for more handler types in a similar way
 *             withTextCommands {
 *                 // Further configuration
 *             }
 *
 *             setValues(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)
 *
 *             // Further configuration
 *         }
 *     }
 * }
 * ```
 *
 * #### Java
 * ```java
 * @BService
 * public class TimeUnitResolverProvider implements ResolverProvider {
 *     @Override
 *     public void declare(@Nonnull ResolverManager manager) {
 *         // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
 *         manager.registerEnum(TimeUnit.class, builder -> {
 *             // Add support for text commands, you can add support for more handler types in a similar way
 *             builder.with(
 *                     // If you don't need further configuration, use "of"
 *                     TextCommandEnumResolver.builder(TimeUnit.class)
 *                             // Further configuration
 *                             .build()
 *             );
 *
 *             builder.setValues(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES);
 *
 *             // Further configuration
 *         });
 *     }
 * }
 * ```
 */
@InterfacedService(acceptMultiple = true)
interface ResolverProvider {
    fun declare(manager: ResolverManager)
}
