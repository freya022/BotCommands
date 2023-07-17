package com.freya02.botcommands.api.annotations

/**
 * Simple marker annotation to enable you to suppress unused warnings.
 *
 * IDEs such as IntelliJ will suggest you a quick-fix to ignore unused warnings if annotated with `@CommandMarker`.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CommandMarker