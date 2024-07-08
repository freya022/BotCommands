package io.github.freya022.botcommands.api.commands

/**
 * Represents whether a command is usable (and if it is visible).
 */
interface Usability {
    val unusableReasons: Set<UnusableReason>

    /**
     * Returns `true` if the command can be executed.
     */
    val isUsable: Boolean

    /**
     * Returns `true` if the command cannot be executed.
     */
    val isNotUsable: Boolean

    /**
     * Returns `true` if the command should be visible, even if unusable (in help content, for example).
     */
    val isVisible: Boolean

    /**
     * Returns `true` if the command should **not** be visible (in help content, for example).
     */
    val isNotVisible: Boolean

    /**
     * Returns the most important un-usability reason.
     */
    val bestReason: UnusableReason

    enum class UnusableReason(
        internal val priority: Int,
        /**
         * Returns `true` if the command should be visible, even if unusable (in help content, for example).
         */
        val isVisible: Boolean,
    ) {
        HIDDEN          (priority = 4, isVisible = false),
        OWNER_ONLY      (priority = 3, isVisible = false),
        USER_PERMISSIONS(priority = 2, isVisible = false),
        BOT_PERMISSIONS (priority = 1, isVisible = true),
        NSFW_ONLY       (priority = 0, isVisible = false)
    }
}