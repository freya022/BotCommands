package dev.freya02.botcommands.typesafe.messages.internal.codegen.utils

import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceContext
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.localization.Localization
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.constant.ClassDesc
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.reflect.KClass

internal val CD_MessageSourceContext = classDesc<MessageSourceContext>()
internal val CD_Localization_Entry = classDesc<Localization.Entry>()
internal val CD_DiscordLocale = classDesc<DiscordLocale>()
internal val CD_Locale = classDesc<Locale>()

internal inline fun <reified T : Any> classDesc(): ClassDesc {
    return ClassDesc.of(T::class.java.name)
}

internal fun KClass<*>.toClassDesc(): ClassDesc = java.toClassDesc()

internal fun Class<*>.toClassDesc(): ClassDesc {
    return describeConstable().getOrElse { error("Could not convert ${this.shortQualifiedName} to ClassDesc") }
}
