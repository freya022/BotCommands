package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.*
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import com.freya02.botcommands.internal.throwUser
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

//TODO decline into persistent/ephemeral builders
@OptIn(ExperimentalTime::class)
class ButtonBuilder internal constructor(private val style: ButtonStyle, private val componentController: ComponentController) : ComponentBuilder {
    override val componentType: ComponentType = ComponentType.BUTTON
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

    override var oneUse: Boolean = false
        private set
    override var constraints: InteractionConstraints = InteractionConstraints()
        private set
    override var timeout: ComponentTimeout? = null
        private set
    override var handler: ComponentHandler? = null
        private set

    fun oneUse(): ButtonBuilder = this.also { oneUse = true }

    fun constraints(block: InteractionConstraints.() -> Unit): ButtonBuilder = this.also { constraints.apply(block) }

    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): ButtonBuilder = timeout(timeout, timeoutUnit) {
        runBlocking { handler.run() }
    }

    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit): ButtonBuilder = this.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }

    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg args: Any?): ButtonBuilder =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *args)

    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): ButtonBuilder = this.also {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    }

    fun bindTo(handlerName: String, vararg data: Any?): ButtonBuilder = this.also { handler = PersistentHandler(handlerName, data) }

    fun bindTo(handler: (ButtonEvent) -> Unit): ButtonBuilder = this.also { it.handler = EphemeralHandler(handler) }

    fun build(label: String): Button = build(label, null)
    fun build(emoji: Emoji): Button = build(null, emoji)
    fun build(label: String?, emoji: Emoji?): Button {
        require(handler != null) {
            throwUser("A component handler needs to be set using #bindTo methods")
        }
//        val data = when (handler) {
//            null -> throwUser("A component handler needs to be set using #bindTo methods")
//            is PersistentHandler ->
//                PersistentComponentData(ComponentType.BUTTON, oneUse, constraints, timeoutInfo, handler as PersistentHandler)
//            is EphemeralHandler<*> -> {
//                val handlerId = ephemeralHandlers.put(handler as EphemeralHandler<*>)
//                EphemeralComponentData(ComponentType.BUTTON, oneUse, constraints, timeoutInfo, handlerId)
//            }
//            else -> throwInternal("Unknown handler type: ${handler!!::class.simpleNestedName}")
//        }
//        val entityTimeout = timeoutInfo?.let { DataEntityTimeout(it.duration, NewComponentsListener.TIMEOUT_HANDLER_NAME) }
//        return runBlocking {
//            val id = dataStore.putData(PartialDataEntity.ofType(handler!!.lifetimeType, data, entityTimeout))
//            return@runBlocking Button.of(ButtonStyle.PRIMARY, id, label, emoji)
//        }
        return Button.of(style, componentController.createComponent(this), label, emoji)
    }
}

suspend fun Button.await(): ButtonEvent = TODO()