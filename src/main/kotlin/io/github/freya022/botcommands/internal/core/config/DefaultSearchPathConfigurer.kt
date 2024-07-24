package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScans
import org.springframework.context.annotation.Configuration

@Configuration
internal open class DefaultSearchPathConfigurer(private val applicationContext: ApplicationContext) : BConfigConfigurer {
    override fun configure(builder: BConfigBuilder) {
        val scans = getAllAnnotations<ComponentScan>()
        val groupScans = getAllAnnotations<ComponentScans>().flatMap { componentScans -> componentScans.value.asIterable() }

        (scans + groupScans).forEach { builder.packages += it.packages }
    }

    private inline fun <reified A : Annotation> getAllAnnotations(): List<A> {
        return applicationContext.getBeansWithAnnotation<A>().keys
            .flatMap { applicationContext.findAllAnnotationsOnBean(it, A::class.java, true) }
    }

    private val ComponentScan.packages: Set<String>
        get() = buildSet(basePackages.size + basePackageClasses.size) {
            this += basePackages
            basePackageClasses.forEach { this += it.java.packageName }
        }
}