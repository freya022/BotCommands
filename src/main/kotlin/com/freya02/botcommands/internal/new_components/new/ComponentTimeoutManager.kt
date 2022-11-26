package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.new_components.new.repositories.ComponentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import mu.KotlinLogging

@BService
internal class ComponentTimeoutManager(
    private val serviceContainer: ServiceContainer,
    private val scopesConfig: BCoroutineScopesConfig,
    private val componentRepository: ComponentRepository
) {
    private val logger = KotlinLogging.logger { }
    private val componentController: ComponentController by lazy { serviceContainer.getService() }
    private val timeoutMap = hashMapOf<Int, Job>()

    fun scheduleTimeout(id: Int, timeout: ComponentTimeout) {
        timeoutMap[id] = scopesConfig.dataTimeoutScope.launch {
            delay(timeout.expirationTimestamp - Clock.System.now())

            //Remove the ID from the timeout map even if the component doesn't exist (might have been cleaned earlier)
            timeoutMap.remove(id)

            val component = componentRepository.getComponent(id)
            if (component == null) {
                logger.warn("Component $id was still timeout scheduled after being deleted")
                return@launch
            }

            //Will also cancel timeouts of related components
            componentController.deleteComponent(component)
        }
    }

    fun cancelTimeout(id: Int) {
        timeoutMap.remove(id)?.cancel()
    }
}