package io.github.freya022.botcommands.api.localization.readers

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.InputStream

private val logger = KotlinLogging.logger { }

/**
 * [LocalizationMapReader] based on an [ObjectMapper].
 *
 * ### Usage
 * Declare a [service factory][BService] returning an instance of this class, using the factory methods.
 *
 * You may supply a [LocalizationTemplateFunction] to customize the templates being filled,
 * if the [default provider][LocalizationTemplateFunction.createDefault] is not enough.
 *
 * ### Example - YAML reader
 * You must add the [jackson-dataformat-yaml](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml)
 * dependency, make sure to match the version that JDA uses to avoid incompatibilities.
 *
 * Note that the following example works the same for any data type,
 * just replace `YAMLFactory` by `JSONFactory` or any other formats.
 *
 * #### Kotlin
 * ```kotlin
 * @BConfiguration
 * object LocalizationMapReaderProvider {
 *
 *     @BService // Creates a new LocalizationMapReader which finds its YAML files in the "doxxy" folder
 *     fun doxxyLocalizationReader(context: BContext): LocalizationMapReader {
 *         return JacksonLocalizationMapReader.createWithDefaultTemplate(
 *             context,
 *             ObjectMapper(YAMLFactory()),
 *             folderName = "doxxy",
 *             extensions = listOf("yaml", "yml")
 *         )
 *     }
 * }
 * ```
 *
 * #### Java
 * ```java
 * @BConfiguration
 * public class LocalizationMapReaderProvider {
 *
 *     @BService // Creates a new LocalizationMapReader which finds its YAML files in the "doxxy" folder
 *     public static LocalizationMapReader doxxyLocalizationReader(BContext context) {
 *         return JacksonLocalizationMapReader.createWithDefaultTemplate(
 *                 context,
 *                 new ObjectMapper(new YAMLFactory()),
 *                 /* folderName */ "doxxy",
 *                 /* extensions */ List.of("yaml", "yml")
 *         );
 *     }
 * }
 * ```
 *
 * ### Default implementation
 * A built-in instance is available, and reads JSON bundles in the `bc_localization` folder,
 * however, you are encouraged to register your own reader, to properly separate bundles and eliminate collisions.
 */
class JacksonLocalizationMapReader private constructor(
    objectMapper: ObjectMapper,
    templateFunction: LocalizationTemplateFunction,
    private val folderName: String,
    private val extensions: List<String>,
    private val classLoader: ClassLoader,
) : AbstractJacksonLocalizationMapReader(objectMapper, templateFunction) {

    private val notFounds = hashSetOf<String>()

    init {
        require(!folderName.startsWith("/")) {
            "The folder name cannot start with /"
        }
        require(extensions.isNotEmpty()) {
            "The extension cannot be empty"
        }
    }

    override fun getInputStream(request: LocalizationMapRequest): InputStream? {
        for (extension in extensions) {
            val path = "$folderName/${request.bundleName}.$extension"
            if (path in notFounds)
                continue

            val stream = classLoader.getResourceAsStream(path)
            if (stream != null) {
                return stream
            } else {
                notFounds.add(path)
                logger.trace { "Found no bundle at '$path' in class loader named '${classLoader.name}' ($classLoader)" }
            }
        }
        return null
    }

    companion object {

        /**
         * Constructs a [JacksonLocalizationMapReader] with the given template function.
         *
         * The bundles are read using the [folderName] and [extensions], in the specified [classLoader],
         * note that folders are not explored recursively.
         *
         * @param objectMapper     Object mapper with support for any [data format](https://github.com/FasterXML/jackson?tab=readme-ov-file#data-format-modules) (such as JSON, YAML and TOML)
         * @param templateFunction Function returning a [LocalizationTemplate] from the template string and locale
         * @param folderName       Path where the bundles can be found, cannot start with `/`
         * @param extensions       Extensions which the bundles can use
         * @param classLoader      Where to load the resources from, useful if the bundles are in a different named module
         *
         * @throws IllegalArgumentException If [folderName] starts with a `/`, or [extensions] is empty
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            objectMapper: ObjectMapper,
            templateFunction: LocalizationTemplateFunction,
            folderName: String,
            extensions: List<String>,
            classLoader: ClassLoader = JacksonLocalizationMapReader::class.java.classLoader,
        ): JacksonLocalizationMapReader {
            return JacksonLocalizationMapReader(objectMapper, templateFunction, folderName, extensions, classLoader)
        }

        /**
         * Constructs a [JacksonLocalizationMapReader] with the default [LocalizationTemplateFunction].
         *
         * The bundles are read using the [folderName] and [extensions], in the specified [classLoader],
         * note that folders are not explored recursively.
         *
         * @param context      Main framework context
         * @param objectMapper Object mapper with support for any [data format](https://github.com/FasterXML/jackson?tab=readme-ov-file#data-format-modules) (such as JSON, YAML and TOML)
         * @param folderName   Path where the bundles can be found, cannot start with `/`
         * @param extensions   Extensions which the bundles can use
         * @param classLoader  Where to load the resources from, useful if the bundles are in a different named module
         *
         * @throws IllegalArgumentException If [folderName] starts with a `/`, or [extensions] is empty
         */
        @JvmStatic
        @JvmOverloads
        fun createWithDefaultTemplate(
            context: BContext,
            objectMapper: ObjectMapper,
            folderName: String,
            extensions: List<String>,
            classLoader: ClassLoader = JacksonLocalizationMapReader::class.java.classLoader,
        ): JacksonLocalizationMapReader {
            return JacksonLocalizationMapReader(objectMapper, LocalizationTemplateFunction.createDefault(context), folderName, extensions, classLoader)
        }
    }
}