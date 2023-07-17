package com.freya02.botcommands.api.annotations

/**
 * Simple marker annotation to enable you to suppress unused warnings.
 *
 * IDEs such as IntelliJ will suggest you a quick-fix to ignore unused warnings if annotated with `@CommandMarker`.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CommandMarker  