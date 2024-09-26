package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupFactory
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import javax.annotation.CheckReturnValue

abstract class AbstractComponentFactory internal constructor(internal val componentController: ComponentController) {
    val context: BContext get() = componentController.context

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

    // no need for a vararg, the use case is mostly to delete components when JDA gives them to you,
    // which is only in a List
    @JvmName("deleteRows")
    fun deleteRowsJava(components: Collection<LayoutComponent>) = runBlocking { deleteRows(components) }

    @JvmSynthetic
    suspend fun deleteRows(components: Collection<LayoutComponent>) =
        components.flatMap { it.actionComponents }
            .mapNotNull { it.id }
            .mapNotNull { IdentifiableComponent.fromIdOrNull(it) }
            .let { deleteComponents(it) }

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

    /**
     * Creates a reference to a rate limiter previously declared by a [RateLimitProvider],
     * alongside a discriminator which differentiates this component from others under the same [group].
     *
     * Each rate limit reference is unique, the uniqueness can be qualified as a
     * composite primary key with both the [group] and [discriminator].
     *
     * @param group         The name of the rate limiter declared by a [RateLimitProvider]
     * @param discriminator Differentiates this component from others using the same [group], must be unique within the [group]
     *
     * @throws IllegalStateException If a reference already exists with the same [group] and [discriminator]
     */
    fun createRateLimitReference(group: String, discriminator: String): ComponentRateLimitReference {
        return componentController.createRateLimitReference(group, discriminator)
    }

    /**
     * Gets an existing reference to a [ComponentRateLimitReference].
     *
     * @param group         The name of the rate limiter declared by a [RateLimitProvider]
     * @param discriminator Differentiates this component from others using the same [group], must be unique within the [group]
     */
    fun getRateLimitReference(group: String, discriminator: String): ComponentRateLimitReference? {
        return componentController.getRateLimitReference(group, discriminator)
    }
}