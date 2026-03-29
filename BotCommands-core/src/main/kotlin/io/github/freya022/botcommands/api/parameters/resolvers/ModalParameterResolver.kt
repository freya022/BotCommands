package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload
import net.dv8tion.jda.api.components.checkbox.Checkbox
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup
import net.dv8tion.jda.api.components.radiogroup.RadioGroup
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Resolver for parameters annotated with [@ModalInput][ModalInput].
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * ### Types supported by default
 * **Note:** For `null` to be supported, the parameter must be explicitly nullable.
 *
 * #### [TextInput]
 * - `String` (can be empty, supports `null` when empty)
 *
 * #### [StringSelectMenu]
 * - `String` when a single value can be selected (supports `null` when none selected)
 * - `List<String>` (can be empty)
 *
 * #### [EntitySelectMenu]
 * - [Mentions]
 * - `T` (supports `null` when none selected)
 * - `List<T>` (can be empty)
 *
 * Where `T` is one of:
 * [IMentionable], [Role], [User], [InputUser], [Member], [GuildChannel]
 *
 * #### [AttachmentUpload]
 * - `List` of [Message.Attachment] (can be empty)
 * - [Message.Attachment] (supports `null` when none selected)
 *
 * #### [RadioGroup]
 * - `String` (supports `null` when none selected)
 *
 * #### [CheckboxGroup]
 * - `List<String>` (can be empty)
 * - `String` when a single value can be selected  (supports `null` when none selected)
 *
 * #### [Checkbox]
 * - (primitive) `Boolean`
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface ModalParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : ModalParameterResolver<T, R> {
    /**
     * Returns a resolved object for this [ModalMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param option       The option currently being resolved
     * @param event        The corresponding event
     * @param modalMapping The [ModalMapping] to be resolved
     */
    fun resolve(option: ModalOption, event: ModalEvent, modalMapping: ModalMapping): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this [ModalMapping].
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param option       The option currently being resolved
     * @param event        The corresponding event
     * @param modalMapping The [ModalMapping] to be resolved
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: ModalOption, event: ModalEvent, modalMapping: ModalMapping) =
        resolve(option, event, modalMapping)
}
