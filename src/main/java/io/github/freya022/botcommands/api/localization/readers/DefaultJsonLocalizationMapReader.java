package io.github.freya022.botcommands.api.localization.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.localization.DefaultLocalizationTemplate;
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest;
import io.github.freya022.botcommands.api.localization.LocalizationTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Implementation for {@link LocalizationTemplate} mappings readers.
 *
 * <h3>Default behavior</h3>
 * Localization templates are loaded from the {@code /bc_localization} folder (i.e., the {@code bc_localization} in your jar's root)
 * <br>Your localization bundle must be a valid JSON file and use the {@code .json} extension.
 * <br>The localization bundle can use any name, but <b>must</b> be suffixed with the same locale formatting as {@link ResourceBundle.Control#toBundleName(String, Locale)},
 * typically {@code _fr} ({@code _language}) or {@code _en_US} ({@code _language_COUNTRY}).
 *
 * <p>The JSON content root must be an object, where the keys must either be delimited by dots, or by using nested objects.
 * <h3>Example</h3>
 * <pre><code>
 *     {
 *         "myCommand": {
 *             "name": "my_command_in_en_US",
 *             "description": "My command description in US english"
 *         }
 *     }
 * </code></pre>
 *
 * <p>This reader uses the default localization templates, see {@link DefaultLocalizationTemplate} for more details.
 *
 * <h3>Customization</h3>
 * You can create more {@link DefaultJsonLocalizationMapReader} instances with different folder names,
 * by creating a service factory, with a different folder name given in the constructor.
 *
 * <h4>Example - Kotlin</h4>
 * <pre><code>
 *     {@code @BConfiguration}
 *     object LocalizationProviders {
 *         {@code @BService} // Creates a new LocalizationMapReader which finds its JSON files in the "locales" folder
 *         fun localesLocalizationReader(context: BContext): LocalizationMapReader {
 *             return DefaultJsonLocalizationMapReader(context, "locales")
 *         }
 *     }
 * </code></pre>
 *
 * <h4>Example - Java</h4>
 * <pre><code>
 *     {@code @BConfiguration}
 *     public class LocalizationProviders {
 *         {@code @BService} // Creates a new LocalizationMapReader which finds its JSON files in the "locales" folder
 *         public LocalizationMapReader localesLocalizationReader(BContext context) {
 *             return new DefaultJsonLocalizationMapReader(context, "locales");
 *         }
 *     }
 * </code></pre>
 *
 * @see DefaultLocalizationTemplate
 */
public class DefaultJsonLocalizationMapReader extends AbstractJacksonLocalizationMapReader {
    private final String folderName;

    /**
     * Constructs a new {@link DefaultJsonLocalizationMapReader}.
     *
     * <p>Note that the files are not walked recursively,
     * and the folder name is found at the {@code resources} root,
     * meaning the path is always {@code /$folderName/$bundleName.json}.
     *
     * @param context    The main context
     * @param folderName The folder in which to find the localization files
     */
    public DefaultJsonLocalizationMapReader(BContext context, String folderName) {
        super(context, new ObjectMapper());
        this.folderName = folderName;
    }

    @Nullable
    @Override
    public InputStream getInputStream(@NotNull LocalizationMapRequest request) {
        return DefaultJsonLocalizationMapReader.class.getResourceAsStream("/" + folderName + "/" + request.bundleName() + ".json");
    }
}
