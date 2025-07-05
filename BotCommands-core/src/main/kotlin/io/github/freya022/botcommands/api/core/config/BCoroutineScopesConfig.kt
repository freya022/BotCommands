package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.hooks.EventDispatcher
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executor

@InjectedService
interface BCoroutineScopesConfig {
    val commandUpdateScope: CoroutineScope          //Not used much
    /**
     * Only used for [parallel event execution][EventDispatcher.dispatchEventAsync],
     * and for [ASYNC][BEventListener.RunMode.ASYNC] run modes.
     */
    val eventDispatcherScope: CoroutineScope        //Only used by EventDispatcher#dispatchEventAsync
    val textCommandsScope: CoroutineScope           //Should not be long-running
    val applicationCommandsScope: CoroutineScope    //Should not be long-running
    val componentScope: CoroutineScope              //Should not be long-running
    val componentTimeoutScope: CoroutineScope       //Should not be long-running, spends time waiting
    val modalScope: CoroutineScope                  //Should not be long-running
    val modalTimeoutScope: CoroutineScope           //Should not be long-running, spends time waiting
    val paginationTimeoutScope: CoroutineScope      //Should not be long-running
}

fun interface CoroutineScopeFactory {
    fun create(): CoroutineScope
}

@ConfigDSL
class BCoroutineScopesConfigBuilder internal constructor() : BCoroutineScopesConfig {
    override val commandUpdateScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val eventDispatcherScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val textCommandsScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val applicationCommandsScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val componentScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val componentTimeoutScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val modalScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val modalTimeoutScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")
    override val paginationTimeoutScope: Nothing get() = throwArgument("Cannot get a coroutine scope from the builder")

    var commandUpdateScopeFactory: CoroutineScopeFactory = defaultFactory("Command updater", 0)
    var eventDispatcherScopeFactory: CoroutineScopeFactory = defaultFactory("Event dispatcher", 4)
    var textCommandsScopeFactory: CoroutineScopeFactory = defaultFactory("Text command handler", 2)
    var applicationCommandsScopeFactory: CoroutineScopeFactory = defaultFactory("App command handler", 2)
    var componentScopeFactory: CoroutineScopeFactory = defaultFactory("Component handler", 2)
    var componentTimeoutScopeFactory: CoroutineScopeFactory = defaultFactory("Component timeout handler", 2)
    var modalScopeFactory: CoroutineScopeFactory = defaultFactory("Modal handler", 2)
    var modalTimeoutScopeFactory: CoroutineScopeFactory = defaultFactory("Modal timeout handler", 2)
    var paginationTimeoutScopeFactory: CoroutineScopeFactory = defaultFactory("Pagination timeout handler", 2)

    /**
     * Creates a new coroutine scope factory out of an executor.
     *
     * @param coroutineName The name of the coroutines
     * @param executor      The executor running the coroutines
     *
     * @see namedDefaultScope
     */
    fun defaultFactory(coroutineName: String, executor: Executor) = CoroutineScopeFactory {
        namedDefaultScope(coroutineName, executor)
    }

    /**
     * Creates a new coroutine scope factory.
     *
     * @param name         The base name of the threads and coroutines, will be prefixed by the number if [corePoolSize] > 1
     * @param corePoolSize The number of threads to keep in the pool, even if they are idle
     *
     * @see namedDefaultScope
     */
    fun defaultFactory(name: String, corePoolSize: Int) = CoroutineScopeFactory {
        namedDefaultScope(name, corePoolSize)
    }

    @JvmSynthetic
    internal fun build() = object : BCoroutineScopesConfig {
        override val commandUpdateScope = commandUpdateScopeFactory.create()
        override val eventDispatcherScope = eventDispatcherScopeFactory.create()
        override val textCommandsScope = textCommandsScopeFactory.create()
        override val applicationCommandsScope = applicationCommandsScopeFactory.create()
        override val componentScope = componentScopeFactory.create()
        override val componentTimeoutScope = componentTimeoutScopeFactory.create()
        override val modalScope = modalScopeFactory.create()
        override val modalTimeoutScope = modalTimeoutScopeFactory.create()
        override val paginationTimeoutScope = paginationTimeoutScopeFactory.create()
    }
}
