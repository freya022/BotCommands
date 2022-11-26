package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.api.new_components.builder.ButtonBuilder
import com.freya02.botcommands.api.new_components.builder.ComponentGroupBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import com.freya02.botcommands.internal.new_components.new.repositories.ComponentRepository
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.referenceString
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

@ConditionalService
class NewComponents internal constructor(private val componentRepository: ComponentRepository, private val componentController: ComponentController) {
    private val logger = KotlinLogging.logger { }

    //TODO kotlin should be the impl as to use the command's coroutine context
    fun newGroup(block: ComponentGroupBuilder.() -> Unit, vararg components: ActionComponent): ComponentGroup {
        requireUser(components.none { it.id == null }) {
            "Cannot make groups with link buttons"
        }

        return ComponentGroupBuilder(components.map { it.id!! }).apply(block).build().also {
            runBlocking { //TODO kotlin should be the impl as to use the command's coroutine context
                componentRepository.insertGroup(it)
            }
        }
    }

    @JvmSynthetic
    fun newGroup(vararg components: ActionComponent, block: ComponentGroupBuilder.() -> Unit): ComponentGroup = newGroup(block, *components)

    fun primaryButton(): ButtonBuilder = ButtonBuilder(ButtonStyle.PRIMARY, componentController)

    private fun createComponentGroup(
        oneUse: Boolean,
        groupTimeout: PersistentTimeout?,
        components: Array<out ActionComponent>
    ): ComponentGroup {
//        val dataEntityTimeout = groupTimeout?.let { DataEntityTimeout(it.duration, NewComponentsListener.TIMEOUT_HANDLER_NAME) }
//        val componentsIds = components.map { it.id ?: throwUser("Cannot put components without IDs in groups") }
//        return ComponentGroup(oneUse, groupTimeout, componentsIds).also { group ->
//            runBlocking {
//                dataStore.putData(PartialDataEntity.ofPersistent(group, dataEntityTimeout)) {
//                    //Try to find components with timeouts
//                    if (groupTimeout == null) return@putData
//
//                    preparedStatement("select id from bc_data where id = any(?)") {
//                        executeQuery(componentsIds.toTypedArray()).forEach { result ->
//                            logger.warn {
//                                "Grouped components cannot have timeouts set, component: ${components.find { it.id == result.get<String>("id") }}"
//                            }
//                        }
//                    }
//                }
//            }
//        }
        TODO()
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
