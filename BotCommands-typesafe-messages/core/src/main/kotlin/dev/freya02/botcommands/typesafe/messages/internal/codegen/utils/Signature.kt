package dev.freya02.botcommands.typesafe.messages.internal.codegen.utils

import io.github.freya022.botcommands.api.core.utils.mapToArray
import java.lang.classfile.Signature
import java.lang.constant.ClassDesc

internal fun ClassDesc.createSignature(vararg typeArguments: Class<*>): Signature.ClassTypeSig {
    return Signature.ClassTypeSig.of(
        this,
        *typeArguments.mapToArray {
            Signature.TypeArg.of(Signature.ClassTypeSig.of(it.toClassDesc()))
        }
    )
}
