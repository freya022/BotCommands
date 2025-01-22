package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.builder.filter
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.commands.text.builder.filter
import io.github.freya022.botcommands.api.components.builder.IActionableComponent
import io.github.freya022.botcommands.api.components.builder.filter
import io.github.freya022.botcommands.api.core.IFilterFactory
import kotlin.reflect.KClass

/**
 * References a factory for interaction filters.
 *
 * **Note:** If you seek a more advanced usage of filters, for example, where they differ based on the guild,
 * then I recommend making your commands without annotations, using Kotlin.
 *
 * ### Usage
 * 1. Create an annotation
 * 2. Add the [FUNCTION][AnnotationTarget.FUNCTION] annotation target
 * 3. (On Java) Add the [RUNTIME][AnnotationRetention.RUNTIME] annotation retention
 * 4. Apply [@FilterFactory][FilterFactory] on your annotation
 * 5. Give it the factories it will use to generate filters
 * 6. Use your newly created annotation on your functions
 *
 * #### Example
 * ```java
 * @FilterFactory(MyFilterFactoryImpl.class)
 * public @interface MyFilterFactory { }
 *
 * @BService
 * public class MyFilterFactoryImpl implements IFilterFactory<FilterFactory> {
 *
 *     @Override
 *     public ApplicationCommandFilter create(KFunction<?> function, MyFilterFactory annotation) {
 *         // create filter
 *     }
 * }
 * ```
 *
 * ### Requirements
 * The factory must return a filter implementing at least one of the corresponding interfaces:
 * - [@JDASlashCommand][JDASlashCommand], [@JDAUserCommand][JDAUserCommand], [@JDAMessageCommand][JDAMessageCommand] -> [ApplicationCommandFilter]
 * - [@JDATextCommandVariation][JDATextCommandVariation] -> [TextCommandFilter]
 *
 * **Note:** This **cannot** be used on component handlers,
 * use the appropriate methods on the builders instead like [IActionableComponent.filter] / [IActionableComponent.addFilter].
 *
 * ### Merging
 * This annotation can be merged if found with other meta-annotations.
 * Keep in mind that a *direct* annotation overrides all meta-annotations.
 *
 * @see ApplicationCommandFilter
 * @see TextCommandFilter
 *
 * @see ApplicationCommandBuilder.filter DSL equivalent (application commands)
 * @see TextCommandVariationBuilder.filter DSL equivalent (text commands)
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FilterFactory(@get:JvmName("value") vararg val classes: KClass<out IFilterFactory<*>>)
