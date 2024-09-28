package io.github.freya022.botcommands.internal.components.builder

/**
 * Since the instances must be returned to provide a builder-like experience for JVM users,
 * we need to get the instance from the implementation itself
 *
 * However, overriding the [instance] property on the implementation does not allow delegates to access it,
 * as the delegates are in their own world.
 *
 * Kotlin does not support `this` in super constructors or in delegates,
 * even though this should be possible by just looking at the decompiled java code
 *
 * To work around this, we introduce [InstanceRetriever], which will hold our instance,
 * and will only be filled in the implementation's constructor, and so, the instance will be available post-construct.
 */
internal sealed class BuilderInstanceHolderImpl<T : Any> : BuilderInstanceHolder<T> {
    protected abstract val instanceRetriever: InstanceRetriever<T>

    override val instance: T
        get() = instanceRetriever.instance
}