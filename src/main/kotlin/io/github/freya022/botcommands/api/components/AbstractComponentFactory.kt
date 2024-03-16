package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupFactory
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking
import javax.annotation.CheckReturnValue

abstract class AbstractComponentFactory internal constructor(internal val componentController: ComponentController) {
    private val logger = Logging.currentLogger()

    @CheckReturnValue
    fun group(vararg components: IdentifiableComponent): ComponentGroupFactory =
        ComponentGroupFactory(componentController, components)

    @JvmName("deleteComponentsById")
    fun deleteComponentsByIdJava(ids: Collection<String>) = runBlocking { deleteComponentsById(ids) }

    @JvmSynthetic
    suspend fun deleteComponentsById(ids: Collection<String>) {
        val parsedIds = ids
            .filter {
                if (ComponentController.isCompatibleComponent(it)) {
                    true
                } else {
                    logger.warn { "Tried to delete an incompatible component ID '$it'" }
                    false
                }
            }
            .map { ComponentController.parseComponentId(it) }

        componentController.deleteComponentsById(parsedIds, throwTimeouts = false)
    }
}