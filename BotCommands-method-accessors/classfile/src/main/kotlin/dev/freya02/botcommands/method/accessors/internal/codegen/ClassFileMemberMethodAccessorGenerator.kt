package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.utils.isInnerClass
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile.*
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor

internal class ClassFileMemberMethodAccessorGenerator<R>(
    instance: Any?,
    function: KFunction<R>,
    lookup: MethodHandles.Lookup,
) : AbstractClassFileMethodAccessorGenerator<R>(instance, function, lookup) {

    /** Only present if function is a constructor */
    internal val effectiveInstanceClass: Class<*>
    internal val effectiveInstanceDesc: ClassDesc

    internal val isInnerClassConstructor: Boolean
        get() = instanceClass != effectiveInstanceClass

    init {
        val constructor = function.javaConstructor
        effectiveInstanceClass = when (constructor) {
            is Constructor<*> if constructor.declaringClass.isInnerClass -> constructor.declaringClass.declaringClass
            else -> instanceClass
        }
        effectiveInstanceDesc = effectiveInstanceClass.describeConstable().get()
    }

    override fun addFields(classBuilder: ClassBuilder) {
        classBuilder.withField("instance", effectiveInstanceDesc, ACC_PRIVATE or ACC_FINAL)
    }

    override fun addConstructor(classBuilder: ClassBuilder) {
        classBuilder.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, effectiveInstanceDesc), ACC_PUBLIC) { codeBuilder ->
            val thisSlot = codeBuilder.receiverSlot()

            // this.super()
            codeBuilder.aload(thisSlot)
            codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

            // this.instance = instance;
            val instanceSlot = codeBuilder.parameterSlot(0)
            codeBuilder.aload(thisSlot)
            codeBuilder.aload(instanceSlot)
            codeBuilder.putfield(thisClass, "instance", effectiveInstanceDesc)

            codeBuilder.return_()
        }
    }

    override fun createInstance(clazz: Class<*>): MethodAccessor<*> {
        return clazz
            .getDeclaredConstructor(effectiveInstanceClass)
            .newInstance(instance) as MethodAccessor<*>
    }
}
