package io.github.freya022.botcommands.internal.emojis

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.events.PreFirstGatewayConnectEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry
import io.github.freya022.botcommands.api.emojis.annotations.AppEmoji
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer
import io.github.freya022.botcommands.api.emojis.annotations.RequiresAppEmojis
import io.github.freya022.botcommands.api.emojis.exceptions.EmojiAlreadyExistsException
import io.github.freya022.botcommands.api.emojis.exceptions.NoEmojiResourceException
import io.github.freya022.botcommands.api.emojis.exceptions.NonUniqueEmojiResourceException
import io.github.freya022.botcommands.api.emojis.exceptions.OutOfAppEmojisException
import io.github.freya022.botcommands.internal.emojis.AppEmojisLoader.Companion.register
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import net.dv8tion.jda.internal.utils.Checks
import org.jetbrains.annotations.TestOnly
import kotlin.collections.set
import kotlin.math.abs
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

/**
 * The [register] function is the core of the loading mechanism,
 * by adding a set of basePath/assetPattern/emojiName/identifier,
 * the [retriever/uploader][loadEmojis] can then create the registered emojis,
 * and then assign them to a map of loaded emojis, with the key being the "identifier".
 *
 * When an application emoji container is detected,
 * its fields are reads, can be optionally customized with [@AppEmoji][AppEmoji],
 * and are then registered to be loaded later.
 *
 * Kotlin users can alternatively use [AppEmojisRegistry.lazy] to register their emojis lazily,
 * but cannot mix those with other retrieval methods in the same class.
 *
 * So only when the user requires the application emojis, they are fetched from the loaded emojis.
 */
@BService
@RequiresAppEmojis
internal class AppEmojisLoader internal constructor(
    private val appEmojisConfig: BAppEmojisConfig,
) {

    private val packages: List<String>

    init {
        val emojiContainers = AppEmojiContainerProcessor.emojiClasses
        if (emojiContainers.isEmpty()) {
            logger.warn { "App emojis were enabled by no emoji container was found, check if you used ${annotationRef<AppEmojiContainer>()}, the package it is in and if it is included in your search path" }
        } else {
            logger.trace { "Registering app emoji containers for pre-login retrieval:\n${emojiContainers.joinAsList { it.clazz.shortQualifiedName }}" }
        }

        packages = emojiContainers.map {
            require(it.annotation.basePath.startsWith("/")) { "Base path of ${it.clazz.simpleNestedName} must start with /" }
            require(!it.annotation.basePath.endsWith("/")) { "Base path of ${it.clazz.simpleNestedName} cannot end with /" }
            it.annotation.basePath
        }

        emojiContainers.forEach(::processClass)
    }

    private fun processClass(emojiContainer: AppEmojiContainerData) {
        val clazz = emojiContainer.clazz
        val basePath = emojiContainer.annotation.basePath

        // Separate lazy from eager properties
        // Can't mix both as lazy properties registration would trigger loading of eager properties
        val (lazyProperties, eagerProperties) = (clazz.staticProperties + clazz.declaredMemberProperties)
            .filter { it.returnType.jvmErasure == ApplicationEmoji::class }
            .partition { it.javaField?.type == Lazy::class.java }

        if (lazyProperties.isNotEmpty()) {
            require(eagerProperties.isEmpty()) {
                "Cannot mix lazy and eager properties in '${clazz.simpleNestedName}', eager properties: ${eagerProperties.joinToString { it.name }}"
            }

            val oldCount = toLoad.size
            clazz.objectInstance // Force initialization (and thus registration)
            if (oldCount == toLoad.size) {
                logger.info { "No emojis were registered by ${clazz.simpleNestedName}" }
            }
            return
        }

        eagerProperties.forEach { property ->
            val fieldAnnotation = property.findAnnotationRecursive<AppEmoji>()
            val assetPattern = fieldAnnotation?.assetPattern?.takeIf { it != AppEmoji.DEFAULT }
            val emojiName = fieldAnnotation?.emojiName?.takeIf { it != AppEmoji.DEFAULT }

            registerFromProperty(
                property,
                basePath,
                assetPatternOverride = assetPattern,
                emojiNameOverride = emojiName,
                identifier = "${clazz.simpleNestedName}.${property.name}"
            )
        }
    }

    @BEventListener(mode = RunMode.BLOCKING)
    internal fun onPreGatewayConnect(event: PreFirstGatewayConnectEvent) {
        try {
            loadEmojis(event.jda)
        } catch (e: Throwable) {
            logger.error(e) { "Error while getting application emojis, shutting down the application to avoid later issues" }
            exitProcess(115)
        }
    }

    internal fun loadEmojis(jda: JDA) {
        if (packages.isEmpty()) return // Already logged in init

        if (toLoad.isEmpty()) return logger.debug { "No application emojis to load" }

        logger.debug { "Fetching application emojis" }

        val applicationEmojis = jda.retrieveApplicationEmojis().complete()

        val missingRequests = getMissingEmojis(applicationEmojis)
        if (missingRequests.isEmpty()) {
            logger.debug { "Application emojis loaded, none were created" }
        } else {
            checkRemainingSlots(missingRequests, applicationEmojis)
            uploadEmojis(missingRequests, jda)
            logger.info { "Application emojis loaded, ${missingRequests.size} were created" }
        }

        loaded = true
    }

    private fun getMissingEmojis(applicationEmojis: MutableList<ApplicationEmoji>): List<LoadRequest> = buildList {
        toLoad.forEach { request ->
            val appEmoji = applicationEmojis.find { it.name == request.emojiName }
            if (appEmoji != null) {
                loadedEmojis[request.identifier] = appEmoji
            } else {
                add(request)
            }
        }
    }

    private fun checkRemainingSlots(missingRequests: List<LoadRequest>, applicationEmojis: List<ApplicationEmoji>) {
        val remainingSlots = maxAppEmojis - missingRequests.size - applicationEmojis.size
        if (remainingSlots < 0) {
            val amountToDelete = abs(remainingSlots)

            if (appEmojisConfig.deleteOnOutOfSlots) {
                val deletableEmojis = getDeletableEmojis(applicationEmojis, amountToDelete)
                // If we couldn't take enough emojis to delete, throw
                if (deletableEmojis.size == amountToDelete) {
                    logger.info { "${missingRequests.size} application emojis are missing, $amountToDelete app emojis will be removed to free necessary slots, this may take a while." }
                    deletableEmojis.forEach { it.delete().complete() }
                    return
                }
            }

            throw OutOfAppEmojisException(
                """
                    Not enough slots to push new application emojis
                    Existing: ${applicationEmojis.size}
                    Registered: ${toLoad.size}
                    Missing slots: $amountToDelete
                    Hint: You can set ${BAppEmojisConfig::deleteOnOutOfSlots.reference} to delete the oldest emojis required to push the app emojis
               """.trimIndent()
            )
        } else {
            logger.info { "${missingRequests.size} application emojis are missing, this may take a while." }
        }
    }

    internal fun getDeletableEmojis(applicationEmojis: List<ApplicationEmoji>, amountToDelete: Int): List<ApplicationEmoji> {
        return applicationEmojis
            // Don't delete our app's emojis
            .filter { existingEmoji -> existingEmoji.name !in toLoadEmojiNames }
            // Delete oldest ones first
            .sortedBy { it.timeCreated }
            .take(amountToDelete)
    }

    private fun uploadEmojis(missingRequests: List<LoadRequest>, jda: JDA) {
        withScannedResources(packages) { scan ->
            for ((basePath, assetPattern, emojiName, identifier) in missingRequests) {
                // CG doesn't need / as root
                val wildcardString = "${basePath.drop(1)}/$assetPattern"
                val resources = scan.getResourcesMatchingWildcard(wildcardString)
                requireThrowing(resources.isNotEmpty(), ::NoEmojiResourceException) {
                    "Found no resources for '$identifier', matching '$wildcardString'"
                }
                requireThrowing(resources.size == 1, ::NonUniqueEmojiResourceException) {
                    "Found multiple resources for '$identifier': ${resources.joinToString { it.pathRelativeToClasspathElement }}"
                }

                val icon = resources.single().open().use(Icon::from)
                val applicationEmoji = jda.createApplicationEmoji(emojiName, icon).complete()
                loadedEmojis.putIfAbsentOrThrowInternal(identifier, applicationEmoji)
            }
        }
    }

    private inline fun withScannedResources(packages: Collection<String>, action: (ScanResult) -> Unit) {
        ClassGraph()
            .acceptPackagesNonRecursive(*packages.toTypedArray())
            .acceptClasspathElementsContainingResourcePath(*packages.map { "$it/*" }.toTypedArray())
            .scan()
            .use(action)
    }

    // Identifier is the field name for annotation usages, UUID for "manual" registration
    private data class LoadRequest(val basePath: String, val assetPattern: String, val emojiName: String, val identifier: String)

    internal companion object {

        // This has to be a getter so we can mock this property
        internal val maxAppEmojis get() = ApplicationEmoji.MAX_APPLICATION_EMOJIS

        internal var loaded = false
            private set
        private val toLoadEmojiNames = hashSetOf<String>()
        private val toLoadIdentifiers = hashSetOf<String>()
        private val toLoad = arrayListOf<LoadRequest>()
        private val loadedEmojis = hashMapOf<String, ApplicationEmoji>()

        @TestOnly
        internal fun clear() {
            loaded = false
            toLoadEmojiNames.clear()
            toLoadIdentifiers.clear()
            toLoad.clear()
            loadedEmojis.clear()

            AppEmojiContainerProcessor.clear()
        }

        internal fun getByIdentifierOrNull(identifier: String): ApplicationEmoji? {
            check(loaded) {
                "Emojis were not loaded yet, have you enabled the feature? Try to use your app emoji container only when necessary"
            }

            return loadedEmojis[identifier]
        }

        internal fun register(
            basePath: String,
            assetPattern: String,
            emojiName: String,
            identifier: String,
        ) {
            require(basePath.startsWith("/")) { "The base path must start with '/'" }
            require(!basePath.endsWith("/")) { "The base path must not end with '/'" }
            Checks.inRange(emojiName, 2, ApplicationEmoji.EMOJI_NAME_MAX_LENGTH, "Emoji name")
            Checks.matches(emojiName, Checks.ALPHANUMERIC_WITH_DASH, "Emoji name")

            check(!loaded) {
                "Cannot get an application emoji after they were loaded, did you forget to use ${annotationRef<AppEmojiContainer>()}?"
            }

            requireThrowing(toLoadEmojiNames.size < maxAppEmojis, ::OutOfAppEmojisException) {
                "Too many app emojis were registered, registered ${toLoadEmojiNames.size}, but max is ${ApplicationEmoji.MAX_APPLICATION_EMOJIS}"
            }

            requireThrowing(toLoadEmojiNames.add(emojiName), ::EmojiAlreadyExistsException) {
                "The emoji name '$emojiName' is already in use."
            }

            check(toLoadIdentifiers.add(identifier)) {
                "The identifier '$identifier' is already in use."
            }

            toLoad += LoadRequest(basePath, assetPattern, emojiName, identifier)
        }

        internal fun registerFromProperty(
            property: KProperty<*>,
            basePath: String,
            assetPatternOverride: String?,
            emojiNameOverride: String?,
            identifier: String,
        ) {
            val snakeCaseFieldName = when {
                // If screaming case, only take lowercase
                property.name.filter { it.isLetter() }.all { it.isUpperCase() } -> property.name.lowercase()
                // Else, consider camelCase
                else -> property.name.toDiscordString()
            }
            val assetPattern = assetPatternOverride ?: "$snakeCaseFieldName.**"
            val emojiName = emojiNameOverride ?: snakeCaseFieldName

            register(basePath, assetPattern, emojiName, identifier)
        }
    }
}