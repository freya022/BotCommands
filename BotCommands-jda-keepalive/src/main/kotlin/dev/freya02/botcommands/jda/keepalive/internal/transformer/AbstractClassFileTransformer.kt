package dev.freya02.botcommands.jda.keepalive.internal.transformer

import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

internal abstract class AbstractClassFileTransformer protected constructor(
    private val target: String
) : ClassFileTransformer {

    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray? {
        if (className == target) return try {
            transform(classfileBuffer)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
        return null
    }

    protected abstract fun transform(classData: ByteArray): ByteArray
}
