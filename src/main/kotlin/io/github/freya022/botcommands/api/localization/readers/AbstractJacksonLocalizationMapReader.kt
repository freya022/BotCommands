package io.github.freya022.botcommands.api.localization.readers

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.typeReferenceOf
import io.github.freya022.botcommands.api.localization.DefaultLocalizationMap
import io.github.freya022.botcommands.api.localization.LocalizationMap
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import io.github.freya022.botcommands.internal.utils.throwArgument
import java.io.InputStream

/**
 * Helps to create a [LocalizationMapReader] based on an [ObjectMapper].
 *
 * You may supply a [LocalizationTemplateFunction] to customize the templates being filled,
 * if the [default provider][LocalizationTemplateFunction.createDefault] is not enough.
 *
 * **Note:** For most cases, using [JacksonLocalizationMapReader] is more than enough.
 *
 * ### Example - YAML reader
 * You must add the [jackson-dataformat-yaml](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml)
 * dependency, make sure to match the version that JDA uses to avoid incompatibilities.
 *
 * #### Kotlin
 * ```kotlin
 * class YamlLocalizationMapReader(
 *     localizationTemplateFunction: LocalizationTemplateFunction,
 *     private val folderName: String,
 * ) : AbstractJacksonLocalizationMapReader(
 *     ObjectMapper(YAMLFactory()), // Give it a YAMLFactory instead of the default JSONFactory
 *     localizationTemplateFunction,
 * ) {
 *
 *     override fun getInputStream(request: LocalizationMapRequest): InputStream? {
 *         return this.javaClass.getResourceAsStream("/$folderName/${request.bundleName}.yml")
 *             ?: this.javaClass.getResourceAsStream("/$folderName/${request.bundleName}.yaml")
 *     }
 * }
 * ```
 *
 * #### Java
 * ```java
 * public class YamlLocalizationMapReader extends AbstractJacksonLocalizationMapReader {
 *
 *     private final String folderName;
 *
 *     public YamlLocalizationMapReader(@NotNull LocalizationTemplateFunction templateFunction, @NotNull String folderName) {
 *         super(new ObjectMapper(new YAMLFactory()), templateFunction);
 *         this.folderName = folderName;
 *     }
 *
 *     @Nullable
 *     @Override
 *     public InputStream getInputStream(@NotNull LocalizationMapRequest request) {
 *         final InputStream stream = getClass().getResourceAsStream("/%s/%s.yml".formatted(folderName, request.bundleName()));
 *         if (stream != null) return stream;
 *
 *         return getClass().getResourceAsStream("/%s/%s.yaml".formatted(folderName, request.bundleName()));
 *     }
 * }
 * ```
 *
 * @param objectMapper     Object mapper with support for any [data format](https://github.com/FasterXML/jackson?tab=readme-ov-file#data-format-modules) (such as JSON, YAML and TOML)
 * @param templateFunction Function returning a [LocalizationTemplate] from the template string and locale
 *
 * @see JacksonLocalizationMapReader
 */
abstract class AbstractJacksonLocalizationMapReader(
    private val objectMapper: ObjectMapper,
    private val templateFunction: LocalizationTemplateFunction,
) : LocalizationMapReader {

    /**
     * Constructs a [AbstractJacksonLocalizationMapReader] with the default [LocalizationTemplateFunction].
     *
     * @param context      Main framework context
     * @param objectMapper Object mapper with support for any [data format](https://github.com/FasterXML/jackson?tab=readme-ov-file#data-format-modules) (such as JSON, YAML and TOML)
     */
    constructor(
        context: BContext,
        objectMapper: ObjectMapper,
    ) : this(objectMapper, LocalizationTemplateFunction.createDefault(context))

    /**
     * Retrieves the [InputStream] from the requested localization map,
     * may return `null` if none can be found.
     */
    abstract fun getInputStream(request: LocalizationMapRequest): InputStream?

    final override fun readLocalizationMap(request: LocalizationMapRequest): LocalizationMap? {
        val input = getInputStream(request) ?: return null
        return DefaultLocalizationMap(request) {
            input.use { input ->
                val map: Map<String, *> = objectMapper.readValue(input, typeReferenceOf())
                discoverEntries(request, map.entries)
            }
        }
    }

    private fun MutableMap<String, LocalizationTemplate>.discoverEntries(request: LocalizationMapRequest, entries: Set<Map.Entry<String, *>>, currentPath: String = "") {
        entries.forEach { (prefix, value) ->
            val key = appendPath(currentPath, prefix)

            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                discoverEntries(request, (value as Map<String, *>).entries, currentPath = key)
            } else if (value is String) {
                val template = templateFunction.apply(value, request.requestedLocale)
                putIfAbsentOrThrow(key, template) { oldValue ->
                    "Two localization templates exist at the same key '$key'"
                }
            } else {
                throwArgument("Key '$key' in bundle ${request.baseName} (locale '${request.requestedLocale}') can only be a String or a Map (an object if you prefer), found ${value?.javaClass?.name}")
            }
        }
    }
}