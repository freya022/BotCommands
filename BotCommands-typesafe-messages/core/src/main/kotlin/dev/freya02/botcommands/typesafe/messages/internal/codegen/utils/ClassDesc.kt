package dev.freya02.botcommands.typesafe.messages.internal.codegen.utils

import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.constant.ClassDesc
import kotlin.jvm.optionals.getOrElse
import kotlin.reflect.KClass

internal val CD_LocalizationContext = classDesc<LocalizationContext>()
internal val CD_Localization_Entry = classDesc<Localization.Entry>()
internal val CD_DiscordLocale = classDesc<DiscordLocale>()

internal inline fun <reified T : Any> classDesc(): ClassDesc {
    return ClassDesc.of(T::class.java.name)
}

internal fun KClass<*>.toClassDesc(): ClassDesc = java.toClassDesc()

internal fun Class<*>.toClassDesc(): ClassDesc {
    return describeConstable().getOrElse { error("Could not convert ${this.shortQualifiedName} to ClassDesc") }
}
