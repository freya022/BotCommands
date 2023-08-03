package com.freya02.botcommands.api.localization

import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.localization.arguments.FormattableArgument
import com.freya02.botcommands.api.localization.arguments.JavaFormattableArgument
import com.freya02.botcommands.api.localization.arguments.MessageFormatArgument
import com.freya02.botcommands.internal.localization.LocalizableArgument
import com.freya02.botcommands.internal.localization.RawArgument
import com.freya02.botcommands.internal.utils.throwUser
import java.text.MessageFormat
import java.util.*

private val bracketsRegex = Regex("""\{.*?}""")
private val templateRegex = Regex("""\{(\w+?)(?::(%.+))?}""")
private val messageFormatRegex = Regex("""\{(\w+)(,?.*?)}""")

/**
 * Default implementation for [LocalizationTemplate].
 *
 * This is effectively [MessageFormat], but with named parameters.
 *
 * To declare a variable inside your localization template, you may use `{variable_name}`.
 *
 * As this supports [MessageFormat], you can also specify format types, such as: `{variable_name, number}`,
 * and format styles, such as: `{user_amount, choice, 0#users|1#user|1<users}`.
 *
 * Full example: `"There are {user_amount} {user_amount, choice, 0#users|1#user|1<users} and my up-time is {uptime, number} seconds"`
 */
class DefaultLocalizationTemplate(private val template: String, locale: Locale) : LocalizationTemplate {
    private val localizableArguments: MutableList<LocalizableArgument> = ArrayList()

    init {
        var start = 0
        bracketsRegex.findAll(template).forEach { templateMatch ->
            val matchStart = templateMatch.range.first
            addRawArgument(template.substring(start, matchStart))

            //TODO use LocalizableString factories and loop
            // users will be able to easily add different LocalizableString(s)
            val group = templateMatch.value
            templateRegex.matchEntire(group)?.let { javaFormatMatch ->
                val (formatterName, formatter) = javaFormatMatch.groupValues
                localizableArguments.add(JavaFormattableArgument(formatterName, formatter))
                return@forEach
            }

            val messageFormatMatcher = messageFormatRegex.matchEntire(group)
                ?: throwUser("Invalid MessageFormat format '$group' in template '$template'")
            val (_, formatterName, formatterFormat) = messageFormatMatcher.groupValues
            //Replace named index by integer index
            val messageFormatter = "{0$formatterFormat}"
            localizableArguments.add(MessageFormatArgument(formatterName, messageFormatter, locale))

            start = templateMatch.range.last
        }
        addRawArgument(template.substring(start))
    }

    private fun addRawArgument(substring: String) {
        if (substring.isEmpty()) return
        localizableArguments.add(RawArgument(substring))
    }

    override fun localize(vararg args: Localization.Entry): String {
        return localizableArguments.joinToString("") { localizableArgument ->
            when (localizableArgument) {
                is RawArgument -> localizableArgument.get()
                is FormattableArgument -> formatFormattableString(args, localizableArgument)
                else -> throwUser("Unknown localizable argument type: ${localizableArgument::class.simpleNestedName}")
            }
        }
    }

    private fun formatFormattableString(args: Array<out Localization.Entry>, formattableArgument: FormattableArgument): String {
        val value = getValueByFormatterName(args, formattableArgument.formatterName)
        return try {
            formattableArgument.format(value)
        } catch (e: Exception) { //For example, if the user provided a string to a number format
            throw RuntimeException("Could not get localized string from ${formattableArgument::class.simpleNestedName} '${formattableArgument.formatterName}' with value '$value'", e)
        }
    }

    private fun getValueByFormatterName(args: Array<out Localization.Entry>, formatterName: String): Any {
        return args.find { it.key == formatterName }
            ?: throw IllegalArgumentException("Could not find format '$formatterName' in template: '$template'")
    }

    override fun toString(): String = template
}
