package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_KFunction
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_MethodAccessor
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile.*
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import kotlin.reflect.KFunction

internal class ClassFileStaticMethodAccessorGenerator<R>(
    instance: Any?,
    function: KFunction<R>,
    lookup: MethodHandles.Lookup,
) : AbstractClassFileMethodAccessorGenerator<R>(instance, function, lookup) {

    override fun addFields(classBuilder: ClassBuilder) {
        classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
        classBuilder.withInterfaceSymbols(CD_MethodAccessor)

        classBuilder.withField("function", CD_KFunction, ACC_PRIVATE or ACC_FINAL)
    }

    override fun addConstructor(classBuilder: ClassBuilder) {
        classBuilder.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, CD_KFunction), ACC_PUBLIC) { codeBuilder ->
            val thisSlot = codeBuilder.receiverSlot()
            val functionSlot = codeBuilder.parameterSlot(if (isStatic) 0 else 1)

            // this.super()
            codeBuilder.aload(thisSlot)
            codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

            // this.function = function;
            codeBuilder.aload(thisSlot)
            codeBuilder.aload(functionSlot)
            codeBuilder.putfield(thisClass, "function", CD_KFunction)

            codeBuilder.return_()
        }
    }

    override fun createInstance(clazz: Class<*>): MethodAccessor<*> {
        return clazz
            .getDeclaredConstructor(KFunction::class.java)
            .newInstance(function) as MethodAccessor<*>
    }
}
