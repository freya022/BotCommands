package dev.freya02.botcommands.jda.keepalive.internal.transformer

import dev.freya02.botcommands.jda.keepalive.internal.BufferingEventManager
import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderConfiguration
import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderSession
import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.classDesc
import org.intellij.lang.annotations.Language
import java.lang.constant.ClassDesc

internal val CD_Function0 = classDescOf("kotlin.jvm.functions.Function0")

internal val CD_IllegalStateException = classDescOf("java.lang.IllegalStateException")
internal val CD_Runnable = classDescOf("java.lang.Runnable")
internal val CD_Supplier = classDescOf("java.util.function.Supplier")

internal val CD_BContext = classDescOf("io.github.freya022.botcommands.api.core.BContext")
internal val CD_BContextImpl = classDescOf("io.github.freya022.botcommands.internal.core.BContextImpl")
internal val CD_JDAService = classDescOf("io.github.freya022.botcommands.api.core.JDAService")
internal val CD_BReadyEvent  = classDescOf("io.github.freya022.botcommands.api.core.events.BReadyEvent")

internal val CD_JDA = classDescOf("net.dv8tion.jda.api.JDA")
internal val CD_JDAImpl = classDescOf("net.dv8tion.jda.internal.JDAImpl")
internal val CD_JDABuilder = classDescOf("net.dv8tion.jda.api.JDABuilder")
internal val CD_IEventManager = classDescOf("net.dv8tion.jda.api.hooks.IEventManager")

internal val CD_BufferingEventManager = classDesc<BufferingEventManager>()
internal val CD_JDABuilderSession = classDesc<JDABuilderSession>()
internal val CD_JDABuilderConfiguration = classDesc<JDABuilderConfiguration>()

private fun classDescOf(@Language("java", prefix = "import ", suffix = ";") name: String): ClassDesc {
    return ClassDesc.of(name)
}
