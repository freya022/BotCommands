package com.freya02.botcommands.api.localization

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.getInterfacedServices
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.localization.arguments.FormattableArgument
import com.freya02.botcommands.api.localization.arguments.factories.FormattableArgumentFactory
import com.freya02.botcommands.internal.localization.LocalizableArgument
import com.freya02.botcommands.internal.localization.RawArgument
import com.freya02.botcommands.internal.localization.SimpleArgument
import com.freya02.botcommands.internal.utils.throwUser
import java.text.MessageFormat
import java.util.*

private val argumentRegex = Regex("""\{(.*?)}""")
private val alphanumericRegex = Regex("""\w+""")

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
class DefaultLocalizationTemplate(context: BContext, private val template: String, locale: Locale) : LocalizationTemplate {
    private val localizableArguments: MutableList<LocalizableArgument> = ArrayList()

    init {
        val formattableArgumentFactories = context.getInterfacedServices<FormattableArgumentFactory>()

        var start = 0
        argumentRegex.findAll(template).forEach argumentsLoop@{ argumentMatch ->
            val matchStart = argumentMatch.range.first
            addRawArgument(template.substring(start, matchStart))

            val formattableArgument = argumentMatch.groups[1]?.value!!
            // Try to match against each factory
            formattableArgumentFactories.forEach { factory ->
                factory.regex.matchEntire(formattableArgument)?.let {
                    localizableArguments += factory.get(it, locale)
                    start = argumentMatch.range.last
                    return@argumentsLoop
                }
            }

            // If the entire thing looks like a simple argument name
            if (formattableArgument.matches(alphanumericRegex)) {
                localizableArguments += SimpleArgument(formattableArgument)
                start = argumentMatch.range.last
                return@argumentsLoop
            }

            throwUser("Could not match formattable argument '$formattableArgument' against ${formattableArgumentFactories.map { it.javaClass.simpleNestedName }}")
        }
        addRawArgument(template.substring(start))
    }

    private fun addRawArgument(substring: String) {
        if (substring.isEmpty()) return
        localizableArguments += RawArgument(substring)
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
