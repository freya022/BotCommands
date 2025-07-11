package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.*
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.*
import dev.freya02.botcommands.typesafe.messages.internal.utils.simpleNestedBinaryName
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.mapToArray
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import java.lang.classfile.ClassFile
import java.lang.classfile.TypeKind
import java.lang.classfile.attribute.SourceFileAttribute
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

internal object MessageSourceGenerator {

    internal fun create(sourceType: KClass<out IMessageSource>): MethodHandle {
        if (!sourceType.java.isInterface) {
            throw IllegalMessageSourceClassTypeException("${sourceType.jvmName} must be an interface!")
        }

        val abstractMethods = sourceType.memberFunctions.filter { it.isAbstract }
        val toImplement = abstractMethods.filter { it.hasAnnotation<LocalizedContent>() }

        (abstractMethods - toImplement).also { unimplementedMethods ->
            if (unimplementedMethods.isNotEmpty()) {
                throw AbstractMessageSourceMethodException("Abstract methods in ${sourceType.jvmName} can only be implemented if annotated with @${LocalizedContent::class.java.simpleName}:\n${unimplementedMethods.joinAsList()}")
            }
        }

        val classFile = ClassFile.of()
        val thisClass = ClassDesc.of("${MessageSourceGenerator::class.java.packageName}.${sourceType.simpleNestedBinaryName}Impl")
        val sourceBytes = classFile.build(thisClass) { classBuilder ->
            classBuilder.with(SourceFileAttribute.of(thisClass.displayName()))
            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withInterfaceSymbols(ClassDesc.of(sourceType.jvmName))

            classBuilder.withField("localizationContext", CD_LocalizationContext, ClassFile.ACC_PRIVATE or ClassFile.ACC_FINAL)

            classBuilder.withMethodBody(
                INIT_NAME,
                MethodTypeDesc.of(CD_void, CD_LocalizationContext),
                ClassFile.ACC_PUBLIC
            ) { codeBuilder ->
                val lineNumber = LineNumber(codeBuilder)

                val thisSlot = codeBuilder.receiverSlot()
                val localizationContextSlot = codeBuilder.parameterSlot(0)

                // this.super()
                lineNumber.setAndIncrement()
                codeBuilder.aload(thisSlot)
                codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

                // this.localizationContext = localizationContext
                lineNumber.setAndIncrement()
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(localizationContextSlot)
                codeBuilder.putfield(thisClass, "localizationContext", CD_LocalizationContext)

                // Required
                codeBuilder.return_()
            }

            toImplement.forEach { method ->
                if (method.returnType.jvmErasure != String::class) {
                    throw IllegalMessageSourceReturnTypeException("Method must return a String: ${method.getSignature(qualifiedClass = true, source = false)}")
                }

                val annotation = method.findAnnotation<LocalizedContent>()
                    ?: error("Method was to be implemented but annotation is absent")
                val templateParameters = method.valueParameters

                templateParameters.forEach { parameter ->
                    if (parameter.isOptional) {
                        throw UnsupportedOptionalParameterException("Optional parameters is not supported! $parameter")
                    }
                }

                // TODO make sure a fallback message exists for the given "templateKey"

                classBuilder.withMethodBody(
                    method.name,
                    MethodTypeDesc.of(method.returnType.jvmErasure.toClassDesc(), *method.valueParameters.mapToArray { it.type.jvmErasure.toClassDesc() }),
                    ClassFile.ACC_PUBLIC or ClassFile.ACC_FINAL,
                ) { codeBuilder ->
                    val lineNumber = LineNumber(codeBuilder)

                    val thisSlot = codeBuilder.receiverSlot()
                    val localizationArgsSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                    val localizationEntrySlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                    // var localizationArgs = new Localization.Entry[templateParameters.size];
                    lineNumber.setAndIncrement()
                    codeBuilder.loadConstant(templateParameters.size)
                    codeBuilder.anewarray(CD_Localization_Entry)
                    codeBuilder.astore(localizationArgsSlot)

                    templateParameters.withIndex().forEachIndexed { arrayIndex, (parameterIndex, parameter) ->
                        val templateVarName = parameter.name?.convertToCamelCase()
                            ?: error("Parameter names are absent from $method ; see https://bc.freya02.dev/3.X/using-botcommands/parameter-names/")

                        // localizationEntry = new Localization.Entry(paramName, value)
                        lineNumber.setAndIncrement()
                        codeBuilder.new_(CD_Localization_Entry)
                        codeBuilder.dup() // As <init> doesn't return itself
                        codeBuilder.ldc(templateVarName)
                        codeBuilder.loadLocal(TypeKind.from(parameter.type.jvmErasure.java), codeBuilder.parameterSlot(parameterIndex))
                        codeBuilder.boxIfNecessary(parameter.type.jvmErasure)
                        codeBuilder.invokespecial(
                            CD_Localization_Entry,
                            INIT_NAME, MethodTypeDesc.of(CD_void, CD_String, CD_Object))
                        codeBuilder.astore(localizationEntrySlot)

                        // localizationArgs[i] = localizationEntry
                        lineNumber.setAndIncrement()
                        codeBuilder.aload(localizationArgsSlot)
                        codeBuilder.loadConstant(arrayIndex)
                        codeBuilder.aload(localizationEntrySlot)
                        codeBuilder.aastore()
                    }

                    // return this.localizationContext.localize("<templateKey>", localizationArgs)
                    lineNumber.setAndIncrement()
                    codeBuilder.aload(thisSlot)
                    codeBuilder.getfield(thisClass, "localizationContext", CD_LocalizationContext)
                    codeBuilder.ldc(annotation.templateKey)
                    codeBuilder.aload(localizationArgsSlot)
                    codeBuilder.invokeinterface(CD_LocalizationContext, "localize", MethodTypeDesc.of(CD_String, CD_String, CD_Localization_Entry.arrayType()))
                    codeBuilder.areturn()
                }
            }
        }

        val lookup = MethodHandles.lookup()
        return lookup.defineClass(sourceBytes)
            .getConstructor(LocalizationContext::class.java)
            .let(lookup::unreflectConstructor)
    }

    private fun String.convertToCamelCase(): String {
        val builder = StringBuilder(this.length * 2)
        for (char in this) {
            if (char.isUpperCase()) {
                builder.append('_').append(char.lowercaseChar())
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : IMessageSource> instantiate(handle: MethodHandle, localizationContext: LocalizationContext): T {
        return handle.invoke(localizationContext) as T
    }
}
