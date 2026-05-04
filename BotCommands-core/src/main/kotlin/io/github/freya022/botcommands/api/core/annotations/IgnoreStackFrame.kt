package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.core.DeclarationSite

/**
 * Ignores stack frames of this class.
 *
 * ## When declaring commands
 * When an error happens while declaring a command (such as when one is duplicated),
 * the declaration site is used to tell where a command was created at.
 *
 * See [DeclarationSite].
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreStackFrame
