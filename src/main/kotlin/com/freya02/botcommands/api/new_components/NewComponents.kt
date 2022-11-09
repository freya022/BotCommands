package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.data.DataEntity
import com.freya02.botcommands.internal.data.DataEntityTimeout
import com.freya02.botcommands.internal.data.DataStoreService
import com.freya02.botcommands.internal.data.PartialDataEntity
import com.freya02.botcommands.internal.data.annotations.DataStoreTimeoutHandler
import com.freya02.botcommands.internal.new_components.EphemeralHandlers
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.referenceString
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.ActionComponent
import java.util.concurrent.TimeUnit

@ConditionalService
class NewComponents internal constructor(private val dataStore: DataStoreService, private val ephemeralHandlers: EphemeralHandlers) {
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
        val dataEntityTimeout = groupTimeout?.let { DataEntityTimeout(it.duration, TIMEOUT_HANDLER_NAME) }
        val componentsIds = components.map { it.id ?: throwUser("Cannot put components without IDs in groups") }
        return ComponentGroup(oneUse, groupTimeout, componentsIds).also { group ->
            runBlocking {
                dataStore.putData(PartialDataEntity.ofPersistent(group, dataEntityTimeout))
            }
        }
    }

    @DataStoreTimeoutHandler(TIMEOUT_HANDLER_NAME)
    internal fun onComponentTimeout(dataEntity: DataEntity) {
        println("timeout occurred for ${dataEntity.id}")
    }

    //TODO finish reconfiguring existing services
    // Then reimplement ServiceContainer
    internal companion object : ConditionalServiceChecker {
        internal const val TIMEOUT_HANDLER_NAME = "NewComponents: timeoutHandler"

        override fun checkServiceAvailability(context: BContext): String? {
            val config = (context as BContextImpl).getService<BComponentsConfig>()
            if (config.useComponents) {
                return null
            }

            return "Components needs to be enabled, see ${BComponentsConfig::useComponents.referenceString}"
        }
    }
}
