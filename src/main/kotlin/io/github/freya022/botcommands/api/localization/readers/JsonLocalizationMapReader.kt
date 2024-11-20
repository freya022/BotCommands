package io.github.freya022.botcommands.api.localization.readers

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import java.io.InputStream
import java.util.*

/**
 * Reads localization bundles in a JSON format and extracts the [LocalizationTemplates][LocalizationTemplate].
 *
 * The bundles are read from the specified [folderName], and must be valid, use the `.json` extension,
 * and be a standard **JSON object**.
 *
 * The name of the files must end with locale, as specified by [ResourceBundle.Control.toBundleName],
 * typically `_fr` (`_language`) or `_en_US` (`_language_COUNTRY`).
 *
 * ### Example
 * Given a file in `src/main/resources/bc_localization/Commands.json`:
 * ```json
 * {
 *     "my_command": {
 *         "name": "my_command_in_en_US",
 *         "description": "My command description in US english"
 *     }
 * }
 * ```
 *
 * ### Creating instances
 * While this reader already exists with its default values,
 * you can create multiple instances of this class to accommodate to your needs,
 * such as reading from a different folder, by registering them as a service,
 * for example:
 *
 * #### Example - Kotlin
 * ```kotlin
 * @BConfiguration
 * object LocalizationProviders {
 *     @BService // Creates a new LocalizationMapReader which finds its JSON files in the "doxxy" folder
 *     fun doxxyLocalizationReader(context: BContext): LocalizationMapReader {
 *         return JsonLocalizationMapReader(context, "doxxy")
 *     }
 * }
 * ```
 *
 * #### Example - Java
 * ```java
 * @BConfiguration
 * public class LocalizationProviders {
 *     @BService // Creates a new LocalizationMapReader which finds its JSON files in the "doxxy" folder
 *     public LocalizationMapReader doxxyLocalizationReader(BContext context) {
 *         return new JsonLocalizationMapReader(context, "doxxy");
 *     }
 * }
 * ```
 *
 * @param folderName       Path where the files can be found, this is not read recursively
 * @param templateFunction Function returning a [LocalizationTemplate] from the template string and locale
 */
class JsonLocalizationMapReader @JvmOverloads constructor(
    context: BContext,
    private val folderName: String = "/bc_localization",
    templateFunction: LocalizationTemplateFunction = LocalizationTemplateFunction.createDefault(context),
) : AbstractJacksonLocalizationMapReader(
    context,
    ObjectMapper(),
    templateFunction
) {

    override fun getInputStream(request: LocalizationMapRequest): InputStream? {
        return this.javaClass.getResourceAsStream("/$folderName/${request.bundleName}.json")
    }
}