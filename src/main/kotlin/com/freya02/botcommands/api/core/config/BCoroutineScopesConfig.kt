package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.core.annotations.LateService
import com.freya02.botcommands.internal.lockable
import com.freya02.botcommands.internal.throwUser
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@LateService
class BCoroutineScopesConfig internal constructor(private val config: BConfig) {
    var defaultScopeSupplier: () -> CoroutineScope = { getDefaultScope() }

    var miscScope: CoroutineScope by ScopeDelegate()
    var eventDispatcherScope: CoroutineScope by ScopeDelegate()
    var cooldownScope: CoroutineScope by ScopeDelegate()
    var textCommandsScope: CoroutineScope by ScopeDelegate()
    var applicationCommandsScope: CoroutineScope by ScopeDelegate()
    var componentsScope: CoroutineScope by ScopeDelegate()
    var modalsScope: CoroutineScope by ScopeDelegate()

    private inner class ScopeDelegate : ReadWriteProperty<BCoroutineScopesConfig, CoroutineScope> {
        private var scope: CoroutineScope? by Delegates.lockable(config)

        //To avoid allocating the scopes if the user wants to replace them
        override fun getValue(thisRef: BCoroutineScopesConfig, property: KProperty<*>): CoroutineScope {
            if (scope == null) scope = defaultScopeSupplier()
            return scope!!
        }

        override fun setValue(thisRef: BCoroutineScopesConfig, property: KProperty<*>, value: CoroutineScope) {
            if (scope != null) throwUser("Cannot set a CoroutineScope more than once")
            scope = value
        }
    }
}
