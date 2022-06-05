package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.application.ValueRange
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import java.util.*
import kotlin.reflect.KParameter

class SlashCommandParameter(
    parameter: KParameter, optionBuilder: SlashCommandOptionBuilder, val resolver: SlashParameterResolver
) : AbstractSlashCommandParameter(
    parameter, optionBuilder
) {
    val description: String = optionBuilder.description
    override val isOptional: Boolean
    val autocompleteInfo: Any = Any() //TODO autocompleteInfo

    val choices: List<Choice>? = optionBuilder.choices
    val range: ValueRange? = optionBuilder.valueRange

    val channelTypes: EnumSet<ChannelType>
    val defaultOptionSupplierMap: TLongObjectMap<DefaultValueSupplier> = TLongObjectHashMap()

    init {
        val optionOptional = optionBuilder.optional
        this.isOptional = when {
            optionOptional != null -> {
                if (optionOptional != kParameter.isNullable) {
                    throwUser("Option '${optionBuilder.name}' does not have the same nullability as it's function parameter")
                }

                optionOptional
            }
            else -> kParameter.isNullable
        }

        channelTypes = enumSetOf() //TODO option builder
    }
}