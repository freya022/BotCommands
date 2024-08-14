package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.internal.core.annotations.InternalComponentScan
import io.github.freya022.botcommands.internal.utils.annotationRef
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScans
import org.springframework.context.annotation.Configuration

@Configuration
internal open class DefaultSearchPathConfigurer(private val applicationContext: ApplicationContext) : BConfigConfigurer {
    override fun configure(builder: BConfigBuilder) {
        val scans = getAllAnnotations<ComponentScan>()
        val groupScans = getAllAnnotations<ComponentScans>().flatMap { componentScans -> componentScans.value.asIterable() }

        val allUserPackages = (scans + groupScans).flatMap { it.packages }
        check(allUserPackages.isNotEmpty()) {
            "You must configure at least one package on your ${annotationRef<SpringBootApplication>()} (recommended) or in a ${annotationRef<ComponentScan>()}"
        }
        builder.packages += allUserPackages
    }

    private inline fun <reified A : Annotation> getAllAnnotations(): List<A> {
        // Internal packages are added with InternalComponentScan, so we only get the user packages
        return (applicationContext.getBeansWithAnnotation<A>().keys - applicationContext.getBeansWithAnnotation<InternalComponentScan>().keys)
            .flatMap { applicationContext.findAllAnnotationsOnBean(it, A::class.java, true) }
    }

    private val ComponentScan.packages: Set<String>
        get() = buildSet(basePackages.size + basePackageClasses.size) {
            this += basePackages
            basePackageClasses.forEach { this += it.java.packageName }
        }
}