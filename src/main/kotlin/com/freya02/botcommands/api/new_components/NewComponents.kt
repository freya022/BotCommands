package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.data.DataEntityTimeout
import com.freya02.botcommands.internal.data.DataStoreService
import com.freya02.botcommands.internal.data.PartialDataEntity
import com.freya02.botcommands.internal.new_components.EphemeralHandlers
import com.freya02.botcommands.internal.new_components.NewComponentsListener
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.referenceString
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.components.ActionComponent
import java.util.concurrent.TimeUnit

@ConditionalService
class NewComponents internal constructor(private val dataStore: DataStoreService, private val ephemeralHandlers: EphemeralHandlers) {
    private val logger = KotlinLogging.logger { }

    @JvmOverloads
    fun newGroup(oneUse: Boolean = false, vararg components: ActionComponent): ComponentGroup =
        createComponentGroup(oneUse, null, components)

    fun newGroup(
        oneUse: Boolean,
        timeout: Long,
        timeoutUnit: TimeUnit,
        vararg components: ActionComponent
    ): ComponentGroup = createComponentGroup(oneUse, DataEntityTimeout(timeout, timeoutUnit, null), components)

    fun newGroup(
        oneUse: Boolean,
        timeout: Long,
        timeoutUnit: TimeUnit,
        timeoutHandlerName: String? = null,
        vararg components: ActionComponent
    ): ComponentGroup = createComponentGroup(oneUse, DataEntityTimeout(timeout, timeoutUnit, timeoutHandlerName), components)

    fun primaryButton(): ButtonBuilder = ButtonBuilder(dataStore, ephemeralHandlers)

    private fun createComponentGroup(
        oneUse: Boolean,
        groupTimeout: DataEntityTimeout?,
        components: Array<out ActionComponent>
    ): ComponentGroup {
        val dataEntityTimeout = groupTimeout?.let { DataEntityTimeout(it.duration, NewComponentsListener.TIMEOUT_HANDLER_NAME) }
        val componentsIds = components.map { it.id ?: throwUser("Cannot put components without IDs in groups") }
        return ComponentGroup(oneUse, groupTimeout, componentsIds).also { group ->
            runBlocking {
                dataStore.putData(PartialDataEntity.ofPersistent(group, dataEntityTimeout)) {
                    //Try to find components with timeouts
                    if (groupTimeout == null) return@putData

                    preparedStatement("select id from bc_data where id = any(?)") {
                        executeQuery(componentsIds.toTypedArray()).forEach { result ->
                            logger.warn {
                                "Grouped components cannot have timeouts set, component: ${components.find { it.id == result.get<String>("id") }}"
                            }
                        }
                    }
                }
            }
        }
    }

    internal companion object : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext): String? {
            val config = (context as BContextImpl).getService<BComponentsConfig>()
            if (config.useComponents) {
                return null
            }

            return "Components needs to be enabled, see ${BComponentsConfig::useComponents.referenceString}"
        }
    }
}
