package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupFactory
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking
import javax.annotation.CheckReturnValue

abstract class AbstractComponentFactory internal constructor(internal val componentController: ComponentController) {
    @CheckReturnValue
    fun group(vararg components: IGroupHolder): ComponentGroupFactory =
        ComponentGroupFactory(componentController, components)

    @JvmName("deleteComponents")
    fun deleteComponentsJava(vararg components: IdentifiableComponent) = deleteComponentsJava(components.asList())

    @JvmSynthetic
    suspend fun deleteComponents(vararg components: IdentifiableComponent) = deleteComponents(components.asList())

    @JvmName("deleteComponents")
    fun deleteComponentsJava(components: Collection<IdentifiableComponent>) = runBlocking { deleteComponents(components) }

    @JvmSynthetic
    suspend fun deleteComponents(components: Collection<IdentifiableComponent>) =
        deleteComponentsByIds(components.map { it.internalId })

    @JvmName("deleteComponentsByIds")
    fun deleteComponentsByIdsJava(vararg ids: Int) = deleteComponentsByIdsJava(ids.asList())

    @JvmSynthetic
    suspend fun deleteComponentsByIds(vararg ids: Int) = deleteComponentsByIds(ids.asList())

    @JvmName("deleteComponentsByIds")
    fun deleteComponentsByIdsJava(ids: Collection<Int>) = runBlocking { deleteComponentsByIds(ids) }

    @JvmSynthetic
    suspend fun deleteComponentsByIds(ids: Collection<Int>) {
        componentController.deleteComponentsById(ids, throwTimeouts = false)
    }
}