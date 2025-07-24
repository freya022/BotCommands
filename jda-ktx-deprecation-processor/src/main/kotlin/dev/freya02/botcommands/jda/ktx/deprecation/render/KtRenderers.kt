package dev.freya02.botcommands.jda.ktx.deprecation.render

import com.google.devtools.ksp.isDefault
import com.google.devtools.ksp.symbol.*
import dev.freya02.botcommands.jda.ktx.deprecation.utils.ifNotEmpty
import ksp.org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import ksp.org.jetbrains.kotlin.analysis.api.contracts.description.KaContractCallsInPlaceContractEffectDeclaration
import ksp.org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import ksp.org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol

fun KSDeclaration.renderDocString(): String {
    val docString = docString ?: return ""
    return buildString {
        appendLine("/**")
        for (line in docString.lines().drop(1).dropLast(1)) {
            appendLine(" *$line")
        }
        append(" */")
    }
}

fun KSModifierListOwner.renderModifiers(): String {
    if (modifiers.isEmpty()) return ""

    return modifiers.joinToString { it.name.lowercase().removePrefix("java_") }
}

fun KSFunctionDeclaration.renderExtensionReceiver(): String {
    val extensionReceiver = extensionReceiver
    if (extensionReceiver == null) return ""

    return buildString {
        append(extensionReceiver.resolve().toNestedTypeString())
        append(".")
    }
}

fun KSFunctionDeclaration.renderParameters(): String = buildString {
    append("(")
    append(parameters.joinToString(separator = ", ") { parameter ->
        val modifiers = buildString {
            if (parameter.isVararg) append("vararg ")
            if (parameter.isNoInline) append("noinline ")
            if (parameter.isCrossInline) append("crossinline ")
        }

        "$modifiers${parameter.name!!.asString()}: ${parameter.type.resolve()}"
    })
    append(")")
}

@OptIn(KaExperimentalApi::class)
fun KaNamedFunctionSymbol.renderContract(): String {
    if (contractEffects.isEmpty()) return ""

    return buildString {
        append("contract {\n")
        append(contractEffects.joinToString("\n") { effect ->
            when (effect) {
                is KaContractCallsInPlaceContractEffectDeclaration ->
                    "callsInPlace(${(effect.valueParameterReference.symbol as KaVariableSymbol).name.asString()}, InvocationKind.${effect.occurrencesRange})"

                else -> error("Unhandled contract ${effect::class.qualifiedName}")
            }
        }.prependIndent())
        append("\n}")
    }
}

fun KSDeclaration.renderTypeParameters(): String {
    if (typeParameters.isEmpty()) return ""

    return typeParameters.joinToString(prefix = "<", separator = ", ", postfix = ">") { typeParameter ->
        buildString {
            if (typeParameter.isReified) append("reified ")
            append(typeParameter.name.asString())
            // "Any?" bounds are synthetic, remove those as they are the default bound
            typeParameter.bounds.filterNot { it.origin == Origin.SYNTHETIC }.toList().ifNotEmpty { bounds ->
                append(" : ")
                append(bounds.single().resolve().toNestedTypeString())
            }
        }
    }
}

fun KSAnnotation.render(): String {
    return "@${this.shortName.asString()}(${this.arguments.filterNot { it.isDefault() }.joinToString(", ") { it.name!!.asString() + " = " + getCompileValue(it.value) }})"
}

private fun getCompileValue(value: Any?): String {
    return when (value) {
        is List<*> -> value.joinToString(prefix = "[", separator = ", ", postfix = "]") { getCompileValue(it) }
        is String -> "\"$value\""
        is KSAnnotation -> value.render().drop(1) // drop @ for nested annotations
        is KSValueArgument -> "${value.name!!.asString()} = ${getCompileValue(value.value)}"
        is KSClassDeclaration if (value.classKind == ClassKind.ENUM_ENTRY) -> value.toString()
        else -> error("Unsupported annotation value $value")
    }
}

fun KSType.toNestedTypeString(): String {
    val parent = ((declaration.parentDeclaration as? KSClassDeclaration)?.simpleName?.asString()?.plus(".")) ?: ""
    return "$parent${toString()}"
}
