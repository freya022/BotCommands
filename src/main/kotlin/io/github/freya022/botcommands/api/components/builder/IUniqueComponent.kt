package io.github.freya022.botcommands.api.components.builder

import javax.annotation.CheckReturnValue

/**
 * Allows components to be used once before being deleted
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * This will also cause cancellation of any associated timeout.
 */
interface IUniqueComponent<T : IUniqueComponent<T>> : BuilderInstanceHolder<T> {
    /**
     * Sets this component as being usable once.
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * This will also cause cancellation of any associated timeout.
     */
    @Deprecated(message = "Renamed to 'singleUse'", ReplaceWith("singleUse"))
    var oneUse: Boolean
        get() = singleUse
        set(value) {
            singleUse = value
        }

    /**
     * Sets this component as being usable once.
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * This will also cause cancellation of any associated timeout.
     */
    var singleUse: Boolean

    /**
     * Sets this component as being usable once.
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * This will also cause cancellation of any associated timeout.
     */
    @Deprecated(message = "Renamed to 'singleUse'", ReplaceWith("singleUse(oneUse)"))
    @CheckReturnValue
    fun oneUse(oneUse: Boolean): T = instance.also {
        this.singleUse = oneUse
    }

    /**
     * Sets this component as being usable once.
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * This will also cause cancellation of any associated timeout.
     */
    @CheckReturnValue
    fun singleUse(singleUse: Boolean): T = instance.also {
        this.singleUse = singleUse
    }
}