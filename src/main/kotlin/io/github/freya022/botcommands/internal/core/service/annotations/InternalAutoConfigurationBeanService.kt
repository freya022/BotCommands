package io.github.freya022.botcommands.internal.core.service.annotations

import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.springframework.context.annotation.Bean

@Bean
@BService
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
internal annotation class InternalAutoConfigurationBeanService
