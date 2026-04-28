package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceContext
import dev.freya02.botcommands.typesafe.messages.internal.TextCommandLocaleProviderAdapter
import dev.freya02.botcommands.typesafe.messages.internal.annotations.DynamicCall
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.lang.invoke.MethodHandle
import java.util.*

internal abstract class AbstractMessageSourceFactory<out T : IMessageSource> @DynamicCall internal constructor(
    private val params: Params,
) : IMessageSourceFactory<T> {

    override val bundleName: String get() = params.bundle
    override val locales: Set<Locale> = params.locales

    override fun create(interaction: Interaction): T {
        val (localizationService, bundle, _, _, guildLocaleProvider, userLocaleProvider, sourceHandle) = params

        val messageSourceContext = MessageSourceContext(
            localizationService = localizationService,
            localizationBundle = bundle,
            guildLocaleSupplier = { guildLocaleProvider.getLocale(interaction) },
            userLocaleSupplier = { userLocaleProvider.getLocale(interaction) },
        )

        return MessageSourceGenerator.instantiate(sourceHandle, messageSourceContext)
    }

    override fun create(event: MessageReceivedEvent): T {
        val (localizationService, bundle, _, textCommandLocaleProvider, _, _, sourceHandle) = params

        checkNotNull(textCommandLocaleProvider) {
            "The text commands module must be present to use this"
        }

        val messageSourceContext = MessageSourceContext(
            localizationService = localizationService,
            localizationBundle = bundle,
            guildLocaleSupplier = { textCommandLocaleProvider.getLocale(event) },
            userLocaleSupplier = null,
        )

        return MessageSourceGenerator.instantiate(sourceHandle, messageSourceContext)
    }

    override fun create(guildLocale: Locale, userLocale: Locale?): T {
        val (localizationService, bundle, _, _, _, _, sourceHandle) = params

        val messageSourceContext = MessageSourceContext(
            localizationService = localizationService,
            localizationBundle = bundle,
            guildLocaleSupplier = { guildLocale },
            userLocaleSupplier = if (userLocale == null) null else ({ userLocale }),
        )

        return MessageSourceGenerator.instantiate(sourceHandle, messageSourceContext)
    }

    internal data class Params(
        internal val localizationService: LocalizationService,
        internal val bundle: String,
        internal val locales: Set<Locale>,
        internal val textCommandLocaleProvider: TextCommandLocaleProviderAdapter?,
        internal val guildLocaleProvider: GuildLocaleProvider,
        internal val userLocaleProvider: UserLocaleProvider,
        internal val sourceHandle: MethodHandle,
    )
}
