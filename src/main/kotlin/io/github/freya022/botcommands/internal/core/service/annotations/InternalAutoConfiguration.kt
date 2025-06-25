package io.github.freya022.botcommands.internal.core.service.annotations

import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.springframework.boot.autoconfigure.AutoConfiguration

@AutoConfiguration
@BService
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
internal annotation class InternalAutoConfiguration