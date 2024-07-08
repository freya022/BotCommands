package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.coroutines.await
import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.utils.WriteOnce
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataPath
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RestActionImpl

private val logger = KotlinLogging.logger { }

//private val notifiedRoles = enumSetOf(RoleType.OWNER, RoleType.ADMIN, RoleType.DEVELOPER)
private val notifiedRoles = setOf("admin", "developer")

@BService
class BotOwnersImpl internal constructor(
    config: BConfig,
) : BotOwners {
    private val ownerWriter = WriteOnce<TLongSet>()
    private var owners: TLongSet by ownerWriter

    override val ownerIds: Collection<Long> by lazy {
        owners.toArray().toHashSet().unmodifiableView()
    }

    init {
        val ownerIds = config.ownerIds
        if (ownerIds.isNotEmpty()) {
            logger.debug { "Using predefined bot owners, IDs: ${ownerIds.joinToString()}" }
            owners = TLongHashSet(ownerIds)
        }

        logger.debug { "Reminder that bot owners will bypass cooldowns, user permissions checks, and will have hidden and owner-only commands be displayed and usable" }
    }

    @BEventListener
    internal suspend fun onInjectedJDA(event: InjectedJDAEvent) {
        if (ownerWriter.isInitialized()) return

        lateinit var rawAppInfo: DataObject
        val appInfo = RestActionImpl(event.jda, Route.Applications.GET_BOT_APPLICATION.compile()) { response, _ ->
            rawAppInfo = response.`object`
            (event.jda as JDAImpl).entityBuilder.createApplicationInfo(response.`object`)
        }.await()

        val owners = when (val team = appInfo.team) {
            null -> listOf(appInfo.owner)
            else -> {
                team.members.filterIndexed { index, _ ->
                    DataPath.getString(rawAppInfo, "team.members[$index].role") in notifiedRoles
                }.map { it.user }
            }
        }
        this.owners = owners.map { it.idLong }.let(::TLongHashSet)

        //TODO once JDA merges PR
//        val owners = when (val team = appInfo.team) {
//            null -> listOf(appInfo.owner)
//            else -> team.members.filter { it.roleType in notifiedRoles }.map { it.user }
//        }
//        this.owners = owners.map { it.idLong }.let(::TLongHashSet)

        if (logger.isTraceEnabled()) {
            logger.trace {
                val ownerList = owners.joinAsList { "${it.name} (${it.id})" }
                "Registered ${owners.size} bot owners:\n${ownerList.prependIndent()}"
            }
        } else {
            logger.debug { "Registered ${owners.size} bot owners: ${owners.joinToString { it.name }}" }
        }
    }

    override fun isOwner(user: UserSnowflake): Boolean = user.idLong in owners
}