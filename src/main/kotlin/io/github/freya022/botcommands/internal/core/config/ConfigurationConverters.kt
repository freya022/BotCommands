package io.github.freya022.botcommands.internal.core.config

import net.dv8tion.jda.api.interactions.DiscordLocale
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.expression.spel.standard.SpelExpressionParser

private typealias RawLocalizationMap = String
private typealias ResolvedLocalizationMap = MutableMap<String, MutableList<DiscordLocale>>

private val spelParser = SpelExpressionParser()

@Configuration
internal open class ConfigurationConverters {
    @Bean
    @ConfigurationPropertiesBinding
    internal open fun localizationMapConverter() = LocalizationMapConverter

    internal data object LocalizationMapConverter : Converter<RawLocalizationMap, ResolvedLocalizationMap> {
        override fun convert(it: RawLocalizationMap): ResolvedLocalizationMap {
            @Suppress("UNCHECKED_CAST")
            val map = spelParser.parseRaw(it).getValue(Map::class.java) as Map<String, List<String>>
            return map.mapValuesTo(hashMapOf()) { (_, locales) -> locales.mapTo(arrayListOf(), DiscordLocale::valueOf) }
        }
    }
}