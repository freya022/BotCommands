package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.modals.ModalBuilder
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

/**
 * Declares this function as a modal handler for the specified modal name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Handler][Handler] or [@Command][Command].
 * - The annotation value to have same name as the one given to [ModalBuilder.bindTo].
 * - First parameter must be [ModalInteractionEvent].
 * - Optionally: Have all your consecutive [@ModalData][ModalData], specified in [ModalBuilder.bindTo].
 *
 * ### Option types
 * - Input options: Uses [@ModalInput][ModalInput], the annotation's value must match the name given in [Modals.createTextInput],
 * supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [ModalParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options: No annotation, additional types can be added by implementing [ICustomResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * @see ModalData @ModalData
 * @see ModalInput @ModalInput
 * @see Aggregate @Aggregate
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalHandler(
    /**
     * The name of the handler,
     * which must be the same handler name as in [Modals.create]
     */
    @get:JvmName("value") val name: String
)
