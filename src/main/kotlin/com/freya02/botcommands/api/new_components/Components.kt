package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.apply
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.api.new_components.builder.ComponentGroupBuilder
import com.freya02.botcommands.api.new_components.builder.button.EphemeralButtonBuilder
import com.freya02.botcommands.api.new_components.builder.button.PersistentButtonBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.new_components.builder.ComponentGroupBuilderImpl
import com.freya02.botcommands.internal.new_components.builder.button.EphemeralButtonBuilderImpl
import com.freya02.botcommands.internal.new_components.builder.button.PersistentButtonBuilderImpl
import com.freya02.botcommands.internal.new_components.builder.select.ephemeral.EphemeralEntitySelectBuilderImpl
import com.freya02.botcommands.internal.new_components.builder.select.ephemeral.EphemeralStringSelectBuilderImpl
import com.freya02.botcommands.internal.new_components.builder.select.persistent.PersistentEntitySelectBuilderImpl
import com.freya02.botcommands.internal.new_components.builder.select.persistent.PersistentStringSelectBuilderImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.referenceString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

@ConditionalService
class Components internal constructor(private val componentController: ComponentController) {
    private val logger = KotlinLogging.logger { }

    fun newGroup(block: ComponentGroupBuilder.() -> Unit, vararg components: ActionComponent): ComponentGroup = runBlocking {
        createGroup(block, *components)
    }

    @JvmSynthetic
    suspend fun newGroup(vararg components: ActionComponent, block: ComponentGroupBuilder.() -> Unit): ComponentGroup = createGroup(block, *components)

    fun persistentButton(style: ButtonStyle): PersistentButtonBuilder = PersistentButtonBuilderImpl(style, componentController)

    //TODO (docs) warn about captured jda entities
    fun ephemeralButton(style: ButtonStyle): EphemeralButtonBuilder = EphemeralButtonBuilderImpl(style, componentController)

    @PublishedApi
    internal fun persistentStringSelectMenu(block: ReceiverConsumer<PersistentStringSelectBuilderImpl>) =
        PersistentStringSelectBuilderImpl(componentController).apply(block).doBuild()
    @PublishedApi
    internal fun persistentEntitySelectMenu(block: ReceiverConsumer<PersistentEntitySelectBuilderImpl>) =
        PersistentEntitySelectBuilderImpl(componentController).apply(block).doBuild()

    @PublishedApi
    internal fun ephemeralStringSelectMenu(block: ReceiverConsumer<EphemeralStringSelectBuilderImpl>) =
        EphemeralStringSelectBuilderImpl(componentController).apply(block).doBuild()
    @PublishedApi
    internal fun ephemeralEntitySelectMenu(block: ReceiverConsumer<EphemeralEntitySelectBuilderImpl>) =
        EphemeralEntitySelectBuilderImpl(componentController).apply(block).doBuild()

    fun deleteComponentsById(ids: List<Int>) = runBlocking { deleteComponentsById_(ids) }

    @JvmSynthetic
    @Suppress("FunctionName")
    suspend fun deleteComponentsById_(ids: List<Int>) {
        componentController.deleteComponentsById(ids)
    }

    private suspend fun createGroup(block: ComponentGroupBuilder.() -> Unit, vararg components: ActionComponent): ComponentGroup {
        requireUser(components.none { it.id == null }) {
            "Cannot make groups with link buttons"
        }

        return components.map { it.id?.toIntOrNull() ?: throwUser("Cannot put external components in groups") }.let { componentIds ->
            ComponentGroupBuilderImpl(componentIds)
                .apply(block)
                .let {
                    withContext(Dispatchers.IO) {
                        componentController.insertGroup(it)
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

suspend fun Button.await(): ButtonEvent = TODO()