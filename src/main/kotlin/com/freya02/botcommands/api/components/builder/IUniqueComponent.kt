package com.freya02.botcommands.api.components.builder

/**
 * Allows components to be used once before being deleted
 *
 * After the component is used, it will be deleted from the database.
 *
 * If the component is a group, then all of its owned components will also be deleted.
 *
 * If the component is inside a group, then all the group's components will also be deleted.
 */
interface IUniqueComponent {
    /**
     * Sets this component as being usable only once before being deleted
     */
    var oneUse: Boolean
}