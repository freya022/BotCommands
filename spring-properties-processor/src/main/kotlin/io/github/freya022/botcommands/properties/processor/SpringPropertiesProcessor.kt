package io.github.freya022.botcommands.properties.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import io.github.freya022.botcommands.properties.processor.utils.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

private val nameName = AnnotationName("org.springframework.boot.context.properties.bind", "Name")
private val configurationPropertiesName = AnnotationName("org.springframework.boot.context.properties", "ConfigurationProperties")
private val configurationValueName = AnnotationName("io.github.freya022.botcommands.internal.core.config", "ConfigurationValue")
private val deprecatedValueName = AnnotationName("io.github.freya022.botcommands.internal.core.config", "DeprecatedValue")
private val ignoreDefaultValueName = AnnotationName("io.github.freya022.botcommands.internal.core.config", "IgnoreDefaultValue")

val json = Json {
    prettyPrint = true
    explicitNulls = false
    encodeDefaults = true
}

private val classPattern = Regex("java.lang.Class<(.+)>")
private val collectionPattern = Regex("java\\.util\\.(?:Set|List)<(.+)>$")
private val mapPattern = Regex("java\\.util\\.Map<(.+), (.+)>$")

class SpringPropertiesProcessor(
    private val log: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private val configurableProperties: MutableSet<String> = hashSetOf()
    private val configuredProperties: MutableSet<String> = hashSetOf()
    private val metadata = SpringMetadata()

    private val processedNodes: MutableList<KSNode> = arrayListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        processedNodes += resolver.getSymbolsWithAnnotation(configurationPropertiesName.name, inDepth = false)
            .filterIsInstance<KSClassDeclaration>()
            .onEach(::processClassDeclaration)

        processedNodes += resolver.getSymbolsWithAnnotation(configurationValueName.name, inDepth = false)
            .filterIsInstance<KSPropertyDeclaration>()
            .onEach(::processPropertyDeclaration)

        return emptyList()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun finish() {
        log.warn("Processed ${processedNodes.size} nodes")

        val propertiesWithoutConfigValue = configurableProperties - configuredProperties
        val propertiesWithoutConstructorParam = configuredProperties - configurableProperties
        if (propertiesWithoutConfigValue.isNotEmpty()) {
            log.warn("Could not find a @ConfigurationValue for $propertiesWithoutConfigValue")
        }
        if (propertiesWithoutConstructorParam.isNotEmpty()) {
            log.warn("Could not find a constructor parameter for $propertiesWithoutConstructorParam")
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                sources = processedNodes.mapNotNull { it.containingFile }.toTypedArray()
            ),
            packageName = "META-INF",
            fileName = "spring-configuration-metadata",
            extensionName = "json"
        ).use {
            json.encodeToStream(metadata, it)
        }
    }

    /**
     * Find all properties based on the @ConfigurationProperties prefix + constructor parameter name.
     *
     * This is used to check that all properties have a @ConfigurationValue assigned and vice versa
     */
    private fun processClassDeclaration(classDeclaration: KSClassDeclaration) {
        val prefix: String = classDeclaration.findAnnotation(configurationPropertiesName).getOrDefault("prefix")
        addPropertyBinds(classDeclaration, prefix)
    }

    /**
     * Add all constructor parameters as configuration properties,
     * handle inner classes recursively
     */
    private fun addPropertyBinds(classDeclaration: KSClassDeclaration, prefix: String) {
        val constructor = classDeclaration.getDeclaredFunctions().single { it.isConstructor() }

        constructor.parameters.forEach { param ->
            /**
             * Get the class declaration of [param],
             * or `null` if the parameter does not represent an inner class of [classDeclaration].
             */
            fun getInnerClassOrNull(): KSClassDeclaration? {
                val paramClassDeclaration = param.type.resolve().declaration as? KSClassDeclaration ?: return null

                val parentDeclaration = paramClassDeclaration.parentDeclaration ?: return null
                if (parentDeclaration.qualifiedName!!.asString() != classDeclaration.qualifiedName!!.asString()) return null

                return paramClassDeclaration
            }

            val innerClass = getInnerClassOrNull()
            val fullBindName = "$prefix.${param.getBindName()}"
            if (innerClass != null && innerClass.classKind == ClassKind.CLASS) {
                addPropertyBinds(innerClass, fullBindName)
            } else {
                configurableProperties.add(fullBindName)
            }
        }
    }

    private fun KSValueParameter.getBindName(): String {
        val nameAnnotation = findAnnotationOrNull(nameName)
        return nameAnnotation?.getOrDefault("value")
            ?: this.name?.asString()
            ?: throw IllegalArgumentException("No name for $this")
    }

    private fun processPropertyDeclaration(propertyDeclaration: KSPropertyDeclaration) {
        val configurationPropertiesAnnotation = propertyDeclaration.findAnnotation(configurationValueName)
        val path: String = configurationPropertiesAnnotation.getOrDefault("path")
        val defaultValue: String? = configurationPropertiesAnnotation.getIfSet("defaultValue")

        if (path !in configurableProperties) {
            log.warn("Metadata was added for '$path' but there is no such parameter in a constructor annotated with @ConfigurationProperties")
        } else {
            configuredProperties += path
        }

        fun KSTypeReference.resolveTypedQualifiedName(from: KSDeclaration): String {
            val type = resolve()
            val qualifiedName = type.declaration.qualifiedName?.asString()?.toJavaType()
                ?: throw IllegalArgumentException("Unknown type for $this in ${from.qualifiedName?.asString()}")

            return when {
                type.arguments.isNotEmpty() -> {
                    val argumentsStr = type.arguments.joinToString {
                        when (it.variance) {
                            Variance.STAR -> "?"
                            else -> it.type!!.resolveTypedQualifiedName(from)
                        }
                    }
                    "$qualifiedName<$argumentsStr>"
                }
                else -> qualifiedName
            }
        }
        // Get the type string if explicitly set, otherwise resolve
        val typeStr = run {
            val typeStr = configurationPropertiesAnnotation.getIfSet("type")
                ?: propertyDeclaration.type.resolveTypedQualifiedName(propertyDeclaration)
            // Wildcard is invalid for spring metadata, we ignore wildcards for the reference hint too
            typeStr.replace("<?>", "")
        }

        val description = propertyDeclaration.docString?.let { docString ->
            if (docString.contains("Default: ") && defaultValue == null && !propertyDeclaration.isAnnotationPresent(ignoreDefaultValueName)) {
                log.warn("Missing default value for ${propertyDeclaration.qualifiedName?.asString()}")
            }

            val parsedTree = MarkdownParser(CommonMarkFlavourDescriptor()).buildMarkdownTreeFromString(docString)
            parsedTree.children
                .asSequence()
                .map { it.getTextInNode(docString) }
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .filterNot { it.startsWith("Spring property:") }
                .filterNot { it.startsWith("@") }
                .joinToString(" ")
                .replace("\n", " ") // New lines in paragraph = structural wrapping
                .tryAppendDot()
        }

        tryPutClassReferenceHint(path, typeStr)

        val deprecation = propertyDeclaration.findAnnotationOrNull(deprecatedValueName)?.let { deprecatedValueAnnotation ->
            val reason = deprecatedValueAnnotation.getOrDefault<String>("reason").tryAppendDot()
            val level = deprecatedValueAnnotation.getOrDefault<KSClassDeclaration>("level").simpleName.asString().lowercase()
            val replacement = deprecatedValueAnnotation.getIfSet<String>("replacement")
            PropertyMetadata.Deprecation(reason, level, replacement)
        }

        metadata.properties += PropertyMetadata(
            name = path,
            defaultValue = defaultValue,
            type = typeStr.toJavaType(),
            sourceType = propertyDeclaration.canonicalName,
            description = description,
            deprecation = deprecation
        )
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

    private fun String.toJavaType() = when (this) {
        "kotlin.collections.List" -> "java.util.List"
        "kotlin.collections.Set" -> "java.util.Set"
        "kotlin.collections.Collection" -> "java.util.Collection"
        "kotlin.collections.Map" -> "java.util.Map"
        "kotlin.Boolean" -> "java.lang.Boolean"
        "kotlin.Int" -> "java.lang.Integer"
        "kotlin.Long" -> "java.lang.Long"
        "kotlin.Double" -> "java.lang.Double"
        "kotlin.String" -> "java.lang.String"
        else -> {
            if (this.startsWith("kotlin."))
                log.warn("Unmapped type: $this")
            this
        }
    }
}