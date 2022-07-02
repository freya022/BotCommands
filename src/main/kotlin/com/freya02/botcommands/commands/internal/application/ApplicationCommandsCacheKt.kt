package com.freya02.botcommands.commands.internal.application

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.utils.data.DataArray
import java.nio.file.Files
import java.nio.file.Path

internal class ApplicationCommandsCacheKt(jda: JDA) {
    private val cachePath = Path.of(System.getProperty("java.io.tmpdir"), "${jda.selfUser.id}slashcommands")
    val globalCommandsPath = cachePath.resolve("globalCommands.json")

    init {
        Files.createDirectories(cachePath)
    }

    fun getGuildCommandsPath(guild: Guild): Path {
        return cachePath.resolve(guild.id).resolve("commands.json")
    }

    companion object {
        fun getCommandsBytes(commandData: Collection<CommandData>): ByteArray {
            return DataArray.empty().addAll(commandData).toJson()
        }
    }
}