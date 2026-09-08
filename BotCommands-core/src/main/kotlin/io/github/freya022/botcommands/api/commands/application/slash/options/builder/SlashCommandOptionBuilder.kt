package io.github.freya022.botcommands.api.commands.application.slash.options.builder

import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.SlashOptionChoiceProvider
import io.github.freya022.botcommands.api.commands.application.ValueRange
import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.annotations.DoubleRange
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
import net.dv8tion.jda.api.interactions.FileType
import net.dv8tion.jda.api.interactions.IFilterableFileTypes
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import kotlin.reflect.KFunction

interface SlashCommandOptionBuilder : ApplicationCommandOptionBuilder {

    /**
     * The name of this option as shown on Discord.
     */
    val optionName: String

    /**
     * Description of the option.<br>
     * Must follow the Discord specifications,
     * see the [OptionData] constructor for details.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.<br>
     * If none is found then it is defaulted to `No Description`.
     *
     * This can be localized, see [LocalizationFunction] on how options are mapped, example: `ban.options.user.description`.
     * This is optional if the parameter is not a slash command parameter.
     *
     * @see SlashOption.usePredefinedChoices
     */
    var description: String?

    /**
     * Enables using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @return `true` to enable using choices from [SlashParameterResolver.getPredefinedChoices].
     *
     * @throws IllegalStateException If [choices] are set.
     *
     * @see SlashOption.usePredefinedChoices
     */
    var usePredefinedChoices: Boolean

    /**
     * The option's choices.
     *
     * The choices returned by this method will have their name localized
     * if they are present in the [localization bundles][BApplicationConfigBuilder.addLocalizations].
     *
     * @throws IllegalStateException If [usePredefinedChoices] is enabled.
     *
     * @see SlashParameterResolver.getPredefinedChoices
     * @see SlashOptionChoiceProvider
     */
    var choices: List<Choice>?

    /**
     * Sets the minimum and maximum values on the specified option.
     *
     * **Note:** This is only for floating point number types!
     *
     * @see DoubleRange
     * @see LongRange
     */
    var valueRange: ValueRange?

    /**
     * Sets the minimum and maximum string length on the specified option.
     *
     * **Note:** This is only for string types!
     *
     * @see Length
     */
    var lengthRange: LengthRange?

    /**
     * The file types this [Attachment][net.dv8tion.jda.api.entities.Message.Attachment] option is accepting, if it is one;
     * up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES].
     *
     * The extensions must match `[\w\-.]+`. For example: `zip`, `tar.zst`.
     *
     * @see FileType
     */
    val fileTypes: FileTypeAccumulator

    /**
     * Adds up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     *
     * The extensions must match `[\w\-.]+`. For example: `zip`, `tar.zst`.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun addFileTypeExtensions(extensions: List<String>) {
        fileTypes += extensions
    }

    /**
     * Adds up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     *
     * The extensions must match `[\w\-.]+`. For example: `zip`, `tar.zst`.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun addFileTypeExtensions(vararg extensions: String) {
        fileTypes += extensions.asList()
    }

    /**
     * Sets up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     * Leave the arguments empty to remove file type filtering.
     *
     * The extensions must match `[\w\-.]+`. For example: `zip`, `tar.zst`.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun setFileTypeExtensions(extensions: List<String>) {
        fileTypes.clear()
        fileTypes += extensions
    }

    /**
     * Sets up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] file extensions to filter for.
     * Leave the arguments empty to remove file type filtering.
     *
     * The extensions must match `[\w\-.]+`. For example: `zip`, `tar.zst`.
     *
     * @param  extensions The extensions, up to [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES]
     *
     * @throws IllegalArgumentException There are more than [MAX_FILE_TYPES][IFilterableFileTypes.MAX_FILE_TYPES] extensions,
     *                                  or, an extension is empty or isn't alphanumeric
     *
     * @see FileType
     */
    fun setFileTypeExtensions(vararg extensions: String) {
        fileTypes.clear()
        fileTypes += extensions.asList()
    }

    /**
     * Uses an existing autocomplete handler with the specified [name][AutocompleteHandler.name].
     *
     * Must match an autocomplete handler created from a named [@AutocompleteHandler][AutocompleteHandler]
     * or [AutocompleteManager.autocomplete].
     *
     * @see AutocompleteHandler @AutocompleteHandler
     */
    fun autocompleteByName(name: String)

    /**
     * Uses an existing autocomplete handler with the specified function.
     *
     * Must match an autocomplete handler created from [@AutocompleteHandler][AutocompleteHandler]
     * or [AutocompleteManager.autocomplete].
     */
    @SkipJavaReflectionOverload
    fun autocompleteByFunction(function: KFunction<Collection<Any>>)

    class FileTypeAccumulator internal constructor() {
        internal val list: List<FileType>
            field = arrayListOf<FileType>()

        @JvmName("plusAssignExtensions")
        operator fun plusAssign(extensions: Collection<String>) {
            require(list.size + extensions.size <= OptionData.MAX_FILE_TYPES) {
                "Cannot filter with more than ${OptionData.MAX_FILE_TYPES} file types"
            }
            list += extensions.map(FileType::ofExtension)
        }

        operator fun plusAssign(extension: String) {
            this += listOf(extension)
        }

        @JvmName("plusAssignFileTypes")
        operator fun plusAssign(extensions: Collection<FileType>) {
            require(list.size + extensions.size <= OptionData.MAX_FILE_TYPES) {
                "Cannot filter with more than ${OptionData.MAX_FILE_TYPES} file types"
            }
            list += extensions
        }

        operator fun plusAssign(extension: FileType) {
            this += listOf(extension)
        }

        fun clear() {
            list.clear()
        }
    }
}
