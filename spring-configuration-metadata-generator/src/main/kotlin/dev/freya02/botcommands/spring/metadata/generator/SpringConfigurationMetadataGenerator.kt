package dev.freya02.botcommands.spring.metadata.generator

import dev.freya02.botcommands.spring.metadata.generator.data.ClassReferenceHint
import dev.freya02.botcommands.spring.metadata.generator.data.PropertyMetadata
import dev.freya02.botcommands.spring.metadata.generator.data.SpringMetadata
import dev.freya02.botcommands.spring.metadata.generator.utils.get
import dev.freya02.botcommands.spring.metadata.generator.utils.getIfSet
import dev.freya02.botcommands.spring.metadata.generator.utils.getOrDefault
import dev.freya02.botcommands.spring.metadata.generator.utils.tryAppendDot
import io.github.classgraph.*
import kotlinx.serialization.json.Json
import java.io.File

private typealias SpringConfigurationMetadataJson = String

private val configurationValueName = AnnotationName("io.github.freya022.botcommands.internal.core.config", "ConfigurationValue")
private val deprecatedValueName = AnnotationName("io.github.freya022.botcommands.internal.core.config", "DeprecatedValue")

private val json = Json {
    prettyPrint = true
    explicitNulls = false
    encodeDefaults = true
}

private val classPattern = Regex("java.lang.Class<(.+)>")
private val collectionPattern = Regex("java\\.util\\.(?:Set|List)<(.+)>$")
private val mapPattern = Regex("java\\.util\\.Map<(.+), (.+)>$")

class SpringConfigurationMetadataGenerator private constructor() {

    private val metadata = SpringMetadata()

    private fun generate(scan: ScanResult): SpringConfigurationMetadataJson {
        scan.allClasses
            .flatMap { it.declaredMethodInfo }
            .forEach { method ->
                processMethod(method)
            }

        metadata.properties.sortBy { it.name }
        return json.encodeToString(metadata)
    }

    private fun processMethod(method: MethodInfo) {
        val annotations = method.annotationInfo
        val configurationValueAnnotation = annotations.firstOrNull { it.name == configurationValueName.name }
        if (configurationValueAnnotation == null) {
            return
        }

        val path: String = configurationValueAnnotation.get("path")
        val description: String = configurationValueAnnotation.get("description")
        val defaultValue: String? = configurationValueAnnotation.getIfSet("defaultValue")

        // Get the type string if explicitly set, otherwise resolve
        val typeStr: String = getTypeString(method, configurationValueAnnotation)

        tryPutClassReferenceHint(path, typeStr)

        val deprecation = annotations.firstOrNull { it.name == deprecatedValueName.name }?.let { deprecatedValueAnnotation ->
            val reason = deprecatedValueAnnotation.get<String>("reason").tryAppendDot()
            val level = deprecatedValueAnnotation.getOrDefault<AnnotationEnumValue>("level").name.lowercase()
            val replacement = deprecatedValueAnnotation.getIfSet<String>("replacement")
            PropertyMetadata.Deprecation(reason, level, replacement)
        }

        metadata.properties += PropertyMetadata(
            name = path,
            defaultValue = defaultValue,
            type = typeStr,
            sourceType = method.className,
            description = description,
            deprecation = deprecation
        )
    }

    private fun getTypeString(method: MethodInfo, configurationValueAnnotation: AnnotationInfo): String {
        configurationValueAnnotation.getIfSet<String>("type")?.let {
            return it
                // Wildcard is invalid for spring metadata, we ignore wildcards for the reference hint too
                .replace("<?>", "")
        }

        // Kotlin stores annotations of interface properties on a separate synthetic method
        return method.typeSignatureOrTypeDescriptor.resultType
            .toString()
            .toBoxedType()
            // Subclasses
            .replace('$', '.')
            // Wildcard is invalid for spring metadata, we ignore wildcards for the reference hint too
            .replace("<?>", "")
    }

    private fun tryPutClassReferenceHint(name: String, typeStr: String) {
        classPattern.matchEntire(typeStr)?.let { matchResult ->
            val classTypeStr = matchResult.groupValues[1]
            metadata.hints += ClassReferenceHint(name, classTypeStr)
            return
        }

        collectionPattern.matchEntire(typeStr)?.let { matchResult ->
            val elementTypeStr = matchResult.groupValues[1]
            // May or may not be a class
            tryPutClassReferenceHint(name, elementTypeStr)
            return
        }

        mapPattern.matchEntire(typeStr)?.let { matchResult ->
            val keyTypeStr = matchResult.groupValues[1]
            val valueTypeStr = matchResult.groupValues[2]
            // May or may not be a class
            tryPutClassReferenceHint("$name.keys", keyTypeStr)
            tryPutClassReferenceHint("$name.values", valueTypeStr)
            return
        }
    }

    private fun String.toBoxedType(): String = when (this) {
        "boolean" -> "java.lang.Boolean"
        "int" -> "java.lang.Integer"
        "long" -> "java.lang.Long"
        "double" -> "java.lang.Double"
        "float" -> "java.lang.Float"
        "char" -> "java.lang.Character"
        "string" -> "java.lang.String"
        else -> this
    }

    companion object {
        fun generate(classesRoot: File): SpringConfigurationMetadataJson {
            return ClassGraph()
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo()
                .overrideClasspath(classesRoot)
                .rejectPaths("**/internal/**")
                .scan()
                .use { scanResult ->
                    SpringConfigurationMetadataGenerator().generate(scanResult)
                }
        }
    }
}
