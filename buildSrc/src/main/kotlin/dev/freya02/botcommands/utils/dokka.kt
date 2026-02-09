package dev.freya02.botcommands.utils

import org.jetbrains.dokka.gradle.engine.parameters.DokkaSourceSetSpec

fun DokkaSourceSetSpec.registerJDADocs() {
    externalDocumentationLinks.register("JDA") {
        url("https://docs.jda.wiki")
        packageListUrl("https://docs.jda.wiki/element-list")
    }
}

fun DokkaSourceSetSpec.registerJetbrainsAnnotationsDocs() {
    externalDocumentationLinks.register("JetBrainsAnnotations") {
        url("https://javadoc.io/doc/org.jetbrains/annotations/26.0.2")
        packageListUrl("https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/package-list")
    }
}

fun DokkaSourceSetSpec.registerSpringFrameworkDocs() {
    externalDocumentationLinks.register("Spring") {
        url("https://docs.spring.io/spring-framework/docs/current/javadoc-api")
        packageListUrl("https://docs.spring.io/spring-framework/docs/current/javadoc-api/element-list")
    }
}

fun DokkaSourceSetSpec.registerBucket4JDocs() {
    externalDocumentationLinks.register("Bucket4J") {
        url("https://javadoc.io/doc/com.bucket4j/bucket4j_jdk17-core/8.14.0")
        packageListUrl("https://javadoc.io/doc/com.bucket4j/bucket4j_jdk17-core/8.14.0/element-list")
    }
}
