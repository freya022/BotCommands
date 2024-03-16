package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupFactory
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
import javax.annotation.CheckReturnValue

abstract class AbstractComponentFactory internal constructor(internal val componentController: ComponentController) {
    private val logger = Logging.currentLogger()

    @CheckReturnValue
    fun group(vararg components: IdentifiableComponent): ComponentGroupFactory =
        ComponentGroupFactory(componentController, components)

    @JvmName("deleteComponentsByIds")
    fun deleteComponentsByIdsJava(ids: Collection<String>) = runBlocking { deleteComponentsByIds(ids) }

    @JvmSynthetic
    suspend fun deleteComponentsByIds(ids: Collection<String>) {
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

    @Deprecated("Use deleteComponentsByIds", replaceWith = ReplaceWith("deleteComponentsByIdsJava(ids)"))
    @JvmName("deleteComponentsById")
    @ScheduledForRemoval
    fun deleteComponentsByIdJava(ids: Collection<String>) = deleteComponentsByIdsJava(ids)

    @Deprecated("Use deleteComponentsByIds", replaceWith = ReplaceWith("deleteComponentsByIds(ids)"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend fun deleteComponentsById(ids: Collection<String>) = deleteComponentsByIds(ids)
}