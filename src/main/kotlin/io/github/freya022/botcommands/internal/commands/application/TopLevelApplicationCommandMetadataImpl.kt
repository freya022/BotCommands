package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata
import io.github.freya022.botcommands.internal.core.exceptions.internalErrorMessage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.TimeUtil
import net.dv8tion.jda.api.utils.data.DataObject
import java.time.OffsetDateTime

internal class TopLevelApplicationCommandMetadataImpl private constructor(
    override val type: Command.Type,
    internal val name: String, // For matching purposes
    override val version: Long,
    override val id: Long,
    override val guildId: Long?
) : TopLevelApplicationCommandMetadata {
    override val timeModified: OffsetDateTime get() = TimeUtil.getTimeCreated(version)

    internal fun toData(): DataObject = DataObject.empty()
        .put("type", type.name)
        .put("name", name)
        .put("version", version)
        .put("id", id)
        .put("guild_id", guildId)

    internal companion object {
        internal fun fromCommand(guild: Guild?, command: Command) =
            TopLevelApplicationCommandMetadataImpl(command.type, command.name, command.version, command.idLong, guild?.idLong)

        internal fun fromData(obj: DataObject): TopLevelApplicationCommandMetadataImpl {
            val type = obj.getString("type").let(Command.Type::valueOf)
            if (type == Command.Type.UNKNOWN)
                throw ParsingException(internalErrorMessage("Serialized interaction metadata should not include unknown command types, as they should have been filtered by the updater"))

            val name = obj.getString("name")
            val version = obj.getLong("version")
            val id = obj.getLong("id")
            val guildId = if (obj.isNull("guild_id")) null else obj.getLong("guild_id")

            return TopLevelApplicationCommandMetadataImpl(type, name, version, id, guildId)
        }
    }
}