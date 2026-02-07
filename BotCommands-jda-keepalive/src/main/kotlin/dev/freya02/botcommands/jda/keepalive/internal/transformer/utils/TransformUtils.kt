package dev.freya02.botcommands.jda.keepalive.internal.transformer.utils

import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile.ACC_SYNTHETIC
import java.lang.classfile.ClassModel
import java.lang.classfile.MethodModel
import java.lang.reflect.AccessFlag
import kotlin.jvm.optionals.getOrNull

internal fun Int.withVisibility(visibility: AccessFlag?): Int {
    var flags = this
    flags = flags and (AccessFlag.PUBLIC.mask() or AccessFlag.PROTECTED.mask() or AccessFlag.PRIVATE.mask()).inv()
    if (visibility != null) // null = package-private
        flags = flags or visibility.mask()
    return flags
}

internal fun MethodModel.matches(name: String, signature: String): Boolean {
    return methodName().equalsString(name) && methodType().equalsString(signature)
}

internal fun ClassModel.findMethod(name: String, signature: String): MethodModel {
    return this.methods().firstOrNull { it.matches(name, signature) }
        ?: error("Could not find ${this.thisClass().name().stringValue()}#$name$signature")
}

context(classBuilder: ClassBuilder)
internal fun MethodModel.transferCodeTo(targetMethodName: String, visibility: AccessFlag = AccessFlag.PRIVATE) {
    classBuilder.withMethodBody(
        classBuilder.constantPool().utf8Entry(targetMethodName),
        methodType(),
        visibility.mask() or ACC_SYNTHETIC // Synthetic so this doesn't require a mock
    ) { codeBuilder ->
        val codeModel = code().orElseThrow { IllegalArgumentException("Method ${this.toFullyQualifiedString()} does not have code") }
        codeModel.forEach { codeBuilder.with(it) }
    }
}

internal fun MethodModel.toFullyQualifiedString(): String {
    val className = parent().getOrNull()?.thisClass()?.asSymbol()?.displayName() ?: "<no parent>"
    return "$className#${methodName().stringValue()}${methodTypeSymbol().displayDescriptor()}"
}
