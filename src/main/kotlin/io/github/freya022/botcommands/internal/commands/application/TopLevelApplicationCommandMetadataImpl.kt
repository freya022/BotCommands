package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.TimeUtil
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import java.time.OffsetDateTime

internal class TopLevelApplicationCommandMetadataImpl private constructor(
    internal val name: String, // For matching purposes
    override val version: Long,
    override val id: Long,
) : TopLevelApplicationCommandMetadata {
    override val timeModified: OffsetDateTime get() = TimeUtil.getTimeCreated(version)

    internal fun toData(): DataObject = DataObject.empty()
        .put("name", name)
        .put("version", version)
        .put("id", id)

    internal companion object {
        internal fun fromCommand(command: Command) =
            TopLevelApplicationCommandMetadataImpl(command.name, command.version, command.idLong)

        internal fun fromData(array: DataArray): List<TopLevelApplicationCommandMetadataImpl> = buildList(array.length()) {
            for (i in 0..<array.length()) {
                val obj = array.getObject(i)
                val name = obj.getString("name")
                val version = obj.getLong("version")
                val id = obj.getLong("id")

                add(TopLevelApplicationCommandMetadataImpl(name, version, id))
            }
        }
    }
}