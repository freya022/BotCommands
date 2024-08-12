package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.internal.utils.throwState
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege
import net.dv8tion.jda.api.requests.RestAction
import java.time.OffsetDateTime

/**
 * Represents a top-level application command (i.e. not a subcommand, nor a group).
 *
 * Contains additional info only available on top-level commands.
 */
interface TopLevelApplicationCommandInfo : ApplicationCommandInfo, TopLevelApplicationCommandMetadata, ISnowflake {
    /**
     * The interaction contexts in which this command is executable in,
     * think of it as 'Where can I use this command in the Discord client'.
     */
    val contexts: Set<InteractionContextType>

    /**
     * The integration types in which this command can be installed in.
     */
    val integrationTypes: Set<IntegrationType>

    /**
     * Whether this application command is (initially) locked to administrators.
     *
     * Administrators can then set up who can use the application command.
     */
    val isDefaultLocked: Boolean

    /**
     * Whether this application command is usable only in guilds (i.e., no DMs).
     */
    val isGuildOnly: Boolean
        get() = contexts.singleOrNull() == InteractionContextType.GUILD

    /**
     * Whether this application commands is usable only in [NSFW channels][IAgeRestrictedChannel].
     */
    val nsfw: Boolean

    /**
     * Discord's metadata about this application command.
     */
    val metadata: TopLevelApplicationCommandMetadata

    override fun getIdLong(): Long = metadata.id

    override val version: Long
        get() = metadata.version
    override val id: Long
        get() = metadata.id
    override val timeModified: OffsetDateTime
        get() = metadata.timeModified
    override val guildId: Long?
        get() = metadata.guildId

    /**
     * Retrieves the [IntegrationPrivileges][IntegrationPrivilege] for this application command.
     *
     * Moderators of a guild can modify these privileges through the `Integrations` menu.
     *
     * @throws IllegalStateException If the command is registered globally, or the guild could not be found
     *
     * @return [RestAction] - Type: [List] of [IntegrationPrivilege]
     */
    fun retrieveIntegrationPrivileges(): RestAction<List<IntegrationPrivilege>> {
        val guildId = guildId
        checkNotNull(guildId) {
            "Cannot get integration privileges on a global command"
        }

        val guild = context.jda.getGuildById(guildId)
            ?: throwState("Could not find guild with id $guildId")
        return guild.retrieveIntegrationPrivilegesById(idLong)
    }
}