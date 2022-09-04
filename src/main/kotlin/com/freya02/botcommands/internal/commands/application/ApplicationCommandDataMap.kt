package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.internal.enumMapOf
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import java.util.function.Function
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType

internal class ApplicationCommandDataMap {
    //The String is CommandPath's base name
    private val typeMap: MutableMap<CommandType, MutableMap<String, CommandData>> = enumMapOf()

    val allCommandData: Collection<CommandData>
        get() = typeMap
            .values
            .flatMap { it.values }

    fun computeIfAbsent(type: CommandType, path: CommandPath, mappingFunction: Function<String, CommandData>): CommandData {
        return getTypeMap(type).computeIfAbsent(path.name, mappingFunction)
    }

    operator fun set(type: CommandType, name: String, value: CommandData) {
        getTypeMap(type)[name] = value
    }

    private fun getTypeMap(type: CommandType): MutableMap<String, CommandData> {
        return typeMap.computeIfAbsent(type) { hashMapOf() }
    }
}