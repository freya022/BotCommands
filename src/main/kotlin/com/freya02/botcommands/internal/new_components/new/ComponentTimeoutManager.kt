package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.internal.new_components.new.repositories.ComponentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import mu.KotlinLogging

@BService
internal class ComponentTimeoutManager(
    private val scopesConfig: BCoroutineScopesConfig,
    private val componentRepository: ComponentRepository
) {
    private val logger = KotlinLogging.logger { }
    private val timeoutMap = hashMapOf<Int, Job>()

    fun scheduleTimeout(id: Int, componentBuilder: ComponentBuilder) {
        val timeout = componentBuilder.timeout ?: return
        timeoutMap[id] = scopesConfig.dataTimeoutScope.launch {
            delay(timeout.expirationTimestamp - Clock.System.now())

            val component = componentRepository.getComponent(id)
            if (component == null) {
                logger.warn("Component $id was still timeout scheduled after being deleted")
                return@launch
            }

            componentRepository.deleteComponent(component)
            timeoutMap.remove(id)
        }
    }

    fun cancelTimeout(id: Int) {
        timeoutMap.remove(id)
    }
}