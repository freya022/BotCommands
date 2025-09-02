package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import kotlin.reflect.KFunction

internal class ClassFileStaticMethodAccessorGenerator<R>(
    instance: Any?,
    function: KFunction<R>,
    lookup: MethodHandles.Lookup,
) : AbstractClassFileMethodAccessorGenerator<R>(instance, function, lookup) {

    override fun addFields(classBuilder: ClassBuilder) {

    }

    override fun addConstructor(classBuilder: ClassBuilder) {
        classBuilder.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void), ACC_PUBLIC) { codeBuilder ->
            val thisSlot = codeBuilder.receiverSlot()

            // this.super()
            codeBuilder.aload(thisSlot)
            codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

            codeBuilder.return_()
        }
    }

    override fun createInstance(clazz: Class<*>): MethodAccessor<*> {
        return clazz
            .getDeclaredConstructor()
            .newInstance() as MethodAccessor<*>
    }
}
