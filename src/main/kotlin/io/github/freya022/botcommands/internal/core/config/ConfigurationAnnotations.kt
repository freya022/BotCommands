package io.github.freya022.botcommands.internal.core.config

import org.intellij.lang.annotations.Language

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
internal annotation class ConfigurationValue(
    val path: String,
    val defaultValue: String = "",
    @Language("Java", prefix = "", suffix = " x = null;") val type: String = "java.lang.Byte",
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
internal annotation class DeprecatedValue(
    val reason: String,
    val level: DeprecationLevel = DeprecationLevel.WARNING,
    val replacement: String = "",
) {
    enum class DeprecationLevel {
        WARNING,
        ERROR,
    }
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
internal annotation class IgnoreDefaultValue