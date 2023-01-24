package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.lockable
import com.freya02.botcommands.internal.throwUser
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executors
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@InjectedService
class BCoroutineScopesConfig internal constructor(private val config: BConfig) {
    var defaultScopeSupplier: (String, Int) -> CoroutineScope = { coroutineName, coreSize ->
        val executor = Executors.newScheduledThreadPool(coreSize) {
            Thread(it).apply {
                name = coroutineName
            }
        }

        getDefaultScope(pool = executor, context = CoroutineName(coroutineName))
    }

    var commandUpdateScope: CoroutineScope by ScopeDelegate("Command update coroutine", 0) //Not used much
    /**
     * Only used for [parallel event execution][EventDispatcher.dispatchEventAsync], including if [BEventListener.async] is enabled, all JDA events are executed sequentially on the same scope as the supplied [CoroutineEventManager]
     */
    var eventDispatcherScope: CoroutineScope by ScopeDelegate("Event dispatcher coroutine", 4) //Only used by EventDispatcher#dispatchEventAsync
    var cooldownScope: CoroutineScope by ScopeDelegate("Cooldown coroutine", 2) //Spends time waiting
    var textCommandsScope: CoroutineScope by ScopeDelegate("Text command coroutine", 2) //Commands that should not block threads with cpu intensive tasks
    var applicationCommandsScope: CoroutineScope by ScopeDelegate("Application command coroutine", 2)  //Interactions that should not block threads with cpu intensive tasks
    var componentsScope: CoroutineScope by ScopeDelegate("Component handling coroutine", 2)  //Interactions that should not block threads with cpu intensive tasks
    var modalsScope: CoroutineScope by ScopeDelegate("Modal handling coroutine", 2) //Interactions that should not block threads with cpu intensive tasks
    var componentTimeoutScope: CoroutineScope by ScopeDelegate("Component timeout coroutine", 2) //Spends time waiting

    private inner class ScopeDelegate(private val name: String, private val coreSize: Int) : ReadWriteProperty<BCoroutineScopesConfig, CoroutineScope> {
        private var scope: CoroutineScope? by Delegates.lockable(config)

        //To avoid allocating the scopes if the user wants to replace them
        override fun getValue(thisRef: BCoroutineScopesConfig, property: KProperty<*>): CoroutineScope {
            if (scope == null) scope = defaultScopeSupplier(name, coreSize)
            return scope!!
        }

        override fun setValue(thisRef: BCoroutineScopesConfig, property: KProperty<*>, value: CoroutineScope) {
            if (scope != null) throwUser("Cannot set a CoroutineScope more than once")
            scope = value
        }
    }
}
