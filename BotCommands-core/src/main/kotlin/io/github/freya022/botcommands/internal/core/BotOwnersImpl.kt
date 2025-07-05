package io.github.freya022.botcommands.internal.core

import dev.minn.jda.ktx.coroutines.await
import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.utils.WriteOnce
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.TeamMember.RoleType
import net.dv8tion.jda.api.entities.UserSnowflake

private val logger = KotlinLogging.loggerOf<BotOwners>()

private val notifiedRoles = enumSetOf(RoleType.OWNER, RoleType.ADMIN, RoleType.DEVELOPER)

@BService
internal class BotOwnersImpl internal constructor(
    config: BConfig,
) : BotOwners {
    private val ownerWriter = WriteOnce<TLongSet>(wait = true)
    private var owners: TLongSet by ownerWriter

    override val ownerIds: Collection<Long> by lazy {
        owners.toArray().toHashSet().unmodifiableView()
    }

    init {
        val ownerIds = config.predefinedOwnerIds
        if (ownerIds.isNotEmpty()) {
            logger.debug { "Using predefined bot owners, IDs: ${ownerIds.joinToString()}" }
            owners = TLongHashSet(ownerIds)
        }

        logger.debug { "Reminder that bot owners will bypass cooldowns, user permissions checks, and will have hidden and owner-only commands be displayed and usable" }
    }

    override fun isOwner(user: UserSnowflake): Boolean = user.idLong in owners

    @BEventListener
    internal suspend fun onInjectedJDA(event: InjectedJDAEvent) {
        if (ownerWriter.isInitialized()) return

        val appInfo = event.jda.retrieveApplicationInfo().await()
        val owners = when (val team = appInfo.team) {
            null -> listOf(appInfo.owner)
            else -> team.members.filter { it.roleType in notifiedRoles }.map { it.user }
        }
        this.owners = owners.map { it.idLong }.let(::TLongHashSet)

        if (logger.isTraceEnabled()) {
            logger.trace {
                val ownerList = owners.joinAsList { "${it.name} (${it.id})" }
                "Registered ${owners.size} bot owners:\n${ownerList.prependIndent()}"
            }
        } else {
            logger.debug { "Registered ${owners.size} bot owners: ${owners.joinToString { it.name }}" }
        }
    }
}