package io.github.freya022.botcommands.internal.core.annotations

/**
 * Marker to ensure functions requesting kotlin-reflect instances have a Java equivalent.
 *
 * The test essentially makes sure the contributor did not forget about adding a Java equivalent.
 */
@Retention(AnnotationRetention.BINARY)
internal annotation class SkipJavaReflectionOverload(val reason: String = "")
