package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.TimeUtil
import net.dv8tion.jda.api.utils.data.DataObject
import java.time.OffsetDateTime

internal class TopLevelApplicationCommandMetadataImpl private constructor(
    internal val name: String, // For matching purposes
    override val version: Long,
    override val id: Long,
    override val guildId: Long?
) : TopLevelApplicationCommandMetadata {
    override val timeModified: OffsetDateTime get() = TimeUtil.getTimeCreated(version)

    internal fun toData(): DataObject = DataObject.empty()
        .put("name", name)
        .put("version", version)
        .put("id", id)
        .put("guild_id", guildId)

    internal companion object {
        internal fun fromCommand(guild: Guild?, command: Command) =
            TopLevelApplicationCommandMetadataImpl(command.name, command.version, command.idLong, guild?.idLong)

        internal fun fromData(obj: DataObject): TopLevelApplicationCommandMetadataImpl {
            val name = obj.getString("name")
            val version = obj.getLong("version")
            val id = obj.getLong("id")
            val guildId = if (obj.isNull("guild_id")) null else obj.getLong("guild_id")

            return TopLevelApplicationCommandMetadataImpl(name, version, id, guildId)
        }
    }
}