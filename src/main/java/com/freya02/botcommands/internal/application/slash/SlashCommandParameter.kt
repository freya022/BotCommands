package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.annotations.api.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.annotations.api.application.slash.annotations.DoubleRange
import com.freya02.botcommands.annotations.api.application.slash.annotations.LongRange
import com.freya02.botcommands.annotations.api.prefixed.annotations.TextOption
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.enumSetOf
import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class SlashCommandParameter(
    parameter: KParameter, index: Int
) : ApplicationCommandVarArgParameter<SlashParameterResolver>(
    SlashParameterResolver::class, parameter, index
) {
    var minValue: Number? = null //TODO use ClosedRange<*> ?
    var maxValue: Number? = null
    val channelTypes: EnumSet<ChannelType> = enumSetOf()
    val defaultOptionSupplierMap: TLongObjectMap<DefaultValueSupplier> = TLongObjectHashMap()

    init {
        require(!parameter.hasAnnotation<TextOption>()) {
            String.format(
                "Slash command parameter #%d cannot be annotated with @TextOption",
                index
            )
        }
        val longRange = parameter.findAnnotation<LongRange>() //TODO move to annotation module
        if (longRange != null) {
            minValue = longRange.from
            maxValue = longRange.to
        } else {
            val doubleRange = parameter.findAnnotation<DoubleRange>()
            if (doubleRange != null) {
                minValue = doubleRange.from
                maxValue = doubleRange.to
            } else {
                if (boxedType == Int::class.java) {
                    minValue = Int.MIN_VALUE
                    maxValue = Int.MAX_VALUE
                } else {
                    minValue = OptionData.MIN_NEGATIVE_NUMBER
                    maxValue = OptionData.MAX_POSITIVE_NUMBER
                }
            }
        }

        channelTypes += parameter.findAnnotation<ChannelTypes>()?.value ?: emptyArray()
    }
}