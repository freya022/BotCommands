package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile.*
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import kotlin.reflect.KFunction

internal class ClassFileMemberMethodAccessorGenerator<R>(
    instance: Any?,
    function: KFunction<R>,
    lookup: MethodHandles.Lookup,
) : AbstractClassFileMethodAccessorGenerator<R>(instance, function, lookup) {

    override fun addFields(classBuilder: ClassBuilder) {
        classBuilder.withField("instance", instanceDesc, ACC_PRIVATE or ACC_FINAL)
    }

    override fun addConstructor(classBuilder: ClassBuilder) {
        classBuilder.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, instanceDesc), ACC_PUBLIC) { codeBuilder ->
            val thisSlot = codeBuilder.receiverSlot()

            // this.super()
            codeBuilder.aload(thisSlot)
            codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

            // this.instance = instance;
            val instanceSlot = codeBuilder.parameterSlot(0)
            codeBuilder.aload(thisSlot)
            codeBuilder.aload(instanceSlot)
            codeBuilder.putfield(thisClass, "instance", instanceDesc)

            codeBuilder.return_()
        }
    }

    override fun createInstance(clazz: Class<*>): MethodAccessor<*> {
        return clazz
            .getDeclaredConstructor(instanceClass)
            .newInstance(instance) as MethodAccessor<*>
    }
}
