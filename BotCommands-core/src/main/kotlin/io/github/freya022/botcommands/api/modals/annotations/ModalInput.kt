package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.core.entities.InputUser
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel

/**
 * Set this parameter as a modal input.
 *
 * The specified input custom ID must be the same as the custom ID of a component in that modal.
 *
 * ### Types supported by default
 * - [TextInput] : `String`
 * - [StringSelectMenu] : `List<String>`
 * - [EntitySelectMenu] : [Mentions], `T` and `List<T>` where `T` is one of:
 * [IMentionable], [Role], [User], [InputUser], [Member], [GuildChannel]
 *
 * @see ModalData @ModalData
 * @see ModalHandler @ModalHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalInput(
    /**
     * The custom ID this modal input will match against.
     */
    @get:JvmName("value") val customId: String
)
