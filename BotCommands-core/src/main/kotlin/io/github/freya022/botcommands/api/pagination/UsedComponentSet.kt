package io.github.freya022.botcommands.api.pagination

import dev.freya02.botcommands.jda.ktx.components.findAll
import dev.freya02.botcommands.jda.ktx.components.toDefaultComponentTree
import gnu.trove.set.hash.TIntHashSet
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.IdentifiableComponent
import io.github.freya022.botcommands.internal.utils.any
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.components.ActionComponent
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.tree.ComponentTree
import kotlin.reflect.KProperty

private val logger = KotlinLogging.logger { }

/**
 * Utility class to manage component ids in paginators.
 */
class UsedComponentSet(private val componentsService: Components, private val cleanAfterRefresh: Boolean) {
    private lateinit var currentIds: TIntHashSet

    @Deprecated(
        message = "Replaced with setComponent(ComponentTree), if you have a message, you should get the ComponentTree directly from it",
        replaceWith = ReplaceWith(
            expression = "setComponents(components.toList().toDefaultComponentTree())",
            imports = arrayOf("dev.minn.jda.ktx.interactions.components.toDefaultComponentTree")
        )
    )
    fun setComponents(components: Iterable<Component>) {
        return setComponents(components.toList().toDefaultComponentTree())
    }

    fun setComponents(componentTree: ComponentTree<*>) {
        val newIds = TIntHashSet().apply {
            componentTree
                .findAll<ActionComponent>()
                .forEach { component ->
                    if (component.customId == null) return@forEach

                    val bcComponent = component as? IdentifiableComponent
                        ?: return@forEach logger.warn { "Attempted to use a non-BC component, id: '${component.customId}'" }

                    add(bcComponent.internalId)
                }
        }

        if (::currentIds.isInitialized.not()) {
            // If there are no IDs yet, use the new set
            currentIds = newIds
        } else if (cleanAfterRefresh) {
            // If there are IDs and a refresh cleans components, check that none are reused
            val isReusingIds = newIds.any { it in currentIds }

            if (isReusingIds) {
                logger.warn {
                    val property: KProperty<*> = AbstractPaginationBuilder<*, *>::cleanAfterRefresh
                    "Paginators cannot reuse components when ${property.reference} is enabled"
                }
            } else {
                componentsService.deleteComponentsByIdsJava(*currentIds.toArray())
                currentIds = newIds
            }
        } else {
            // If there are IDs and no clean up is required, add the components
            // They will be cleaned up when the pagination expires
            currentIds.addAll(newIds)
        }
    }

    suspend fun cleanup() {
        componentsService.deleteComponentsByIds(*currentIds.toArray())
    }
}
