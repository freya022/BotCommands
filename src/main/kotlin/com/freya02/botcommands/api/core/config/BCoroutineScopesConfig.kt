package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.utils.namedDefaultScope
import com.freya02.botcommands.internal.core.config.ConfigDSL
import com.freya02.botcommands.internal.utils.throwUser
import dev.minn.jda.ktx.events.CoroutineEventManager
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@InjectedService
interface BCoroutineScopesConfig {
    val commandUpdateScope: CoroutineScope //Not used much
    /**
     * Only used for [parallel event execution][EventDispatcher.dispatchEventAsync], including if [BEventListener.async] is enabled, all JDA events are executed sequentially on the same scope as the supplied [CoroutineEventManager]
     */
    val eventDispatcherScope: CoroutineScope        //Only used by EventDispatcher#dispatchEventAsync
    val textCommandsScope: CoroutineScope           //Commands that should not block threads with cpu intensive tasks
    val applicationCommandsScope: CoroutineScope    //Interactions that should not block threads with cpu intensive tasks
    val componentsScope: CoroutineScope             //Interactions that should not block threads with cpu intensive tasks
    val modalsScope: CoroutineScope                 //Interactions that should not block threads with cpu intensive tasks
    val componentTimeoutScope: CoroutineScope       //Spends time waiting
}

@ConfigDSL
class BCoroutineScopesConfigBuilder internal constructor() : BCoroutineScopesConfig {
    var defaultScopeSupplier: (coroutineName: String, corePoolSize: Int) -> CoroutineScope = { coroutineName, corePoolSize ->
        namedDefaultScope(coroutineName, corePoolSize)
    }

    override var commandUpdateScope by ScopeDelegate("Command update coroutine", 0) //Not used much
    override var eventDispatcherScope by ScopeDelegate("Event dispatcher coroutine", 4) //Only used by EventDispatcher#dispatchEventAsync
    override var textCommandsScope by ScopeDelegate("Text command coroutine", 2) //Commands that should not block threads with cpu intensive tasks
    override var applicationCommandsScope by ScopeDelegate("Application command coroutine", 2)  //Interactions that should not block threads with cpu intensive tasks
    override var componentsScope by ScopeDelegate("Component handling coroutine", 2)  //Interactions that should not block threads with cpu intensive tasks
    override var modalsScope by ScopeDelegate("Modal handling coroutine", 2) //Interactions that should not block threads with cpu intensive tasks
    override var componentTimeoutScope by ScopeDelegate("Component timeout coroutine", 2) //Spends time waiting

    @JvmSynthetic
    internal fun build() = object : BCoroutineScopesConfig {
        override val commandUpdateScope = this@BCoroutineScopesConfigBuilder.commandUpdateScope
        override val eventDispatcherScope = this@BCoroutineScopesConfigBuilder.eventDispatcherScope
        override val textCommandsScope = this@BCoroutineScopesConfigBuilder.textCommandsScope
        override val applicationCommandsScope = this@BCoroutineScopesConfigBuilder.applicationCommandsScope
        override val componentsScope = this@BCoroutineScopesConfigBuilder.componentsScope
        override val modalsScope = this@BCoroutineScopesConfigBuilder.modalsScope
        override val componentTimeoutScope = this@BCoroutineScopesConfigBuilder.componentTimeoutScope
    }

    private inner class ScopeDelegate(private val name: String, private val corePoolSize: Int) : ReadWriteProperty<BCoroutineScopesConfig, CoroutineScope> {
        private var scope: CoroutineScope? = null

        //To avoid allocating the scopes if the user wants to replace them
        override fun getValue(thisRef: BCoroutineScopesConfig, property: KProperty<*>): CoroutineScope {
            if (scope == null) scope = defaultScopeSupplier(name, corePoolSize)
            return scope!!
        }

        override fun setValue(thisRef: BCoroutineScopesConfig, property: KProperty<*>, value: CoroutineScope) {
            if (scope != null) throwUser("Cannot set a CoroutineScope more than once")
            scope = value
        }
    }
}
