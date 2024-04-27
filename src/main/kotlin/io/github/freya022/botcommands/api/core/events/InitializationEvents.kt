package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext

internal sealed interface InitializationEvent : BGenericEvent

/**
 * Indicates the framework status changed to [BContext.Status.PRE_LOAD].
 */
class PreLoadEvent internal constructor(context: BContext) : BEvent(context), InitializationEvent

/**
 * Indicates the framework status changed to [BContext.Status.LOAD].
 */
class LoadEvent internal constructor(context: BContext) : BEvent(context), InitializationEvent

/**
 * Indicates the framework status changed to [BContext.Status.POST_LOAD].
 */
class PostLoadEvent internal constructor(context: BContext) : BEvent(context), InitializationEvent

/**
 * Indicates the framework status changed to [BContext.Status.READY].
 */
class BReadyEvent internal constructor(context: BContext) : BEvent(context), InitializationEvent