package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.entities.ChannelType
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

    val minValue: Number //TODO use ClosedRange<*> ?
    val maxValue: Number
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

        minValue = Int.MIN_VALUE //TODO option builder
        maxValue = Int.MAX_VALUE //TODO option builder

        channelTypes = enumSetOf() //TODO option builder
    }
}