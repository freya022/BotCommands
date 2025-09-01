package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.default.DefaultInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct.DirectInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.modality.BlockingInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.modality.SuspendingInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_Continuation
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.ACC_FINAL
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_Map
import java.lang.constant.ConstantDescs.CD_Object
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction

internal abstract class AbstractClassFileMethodAccessorGenerator<R>(
    internal val instance: Any?,
    internal val function: KFunction<R>,
    internal val lookup: MethodHandles.Lookup,
) {

    internal val executable: Executable = function.javaExecutable
    internal val instanceClass: Class<*> = executable.declaringClass
    internal val isInterface: Boolean get() = instanceClass.isInterface
    internal val instanceDesc: ClassDesc = instanceClass.describeConstable().get()
    internal val isStatic: Boolean = Modifier.isStatic(executable.modifiers)

    internal val thisClass: ClassDesc = ClassDesc.of("${lookup.lookupClass().packageName}.ClassFileMethodAccessor")

    // The class must be unique per function, which is why we don't cache the class
    // Also "duplicate" definitions are allowed for hidden classes
    internal fun generate(): MethodAccessor<R> {
        val bytes = ClassFile.of().build(thisClass) { classBuilder ->
            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withInterfaceSymbols(CD_MethodAccessor)

            // TODO replace with class data of hidden class
            addFields(classBuilder)

            addConstructor(classBuilder)

            classBuilder.withMethodBody("callSuspend", MethodTypeDesc.of(CD_Object, CD_Map, CD_Continuation), ACC_PUBLIC or ACC_FINAL) { codeBuilder ->
                val modalityGenerator = when {
                    function.isSuspend -> SuspendingInvokerGenerator
                    else -> BlockingInvokerGenerator
                }
                with(modalityGenerator) {
                    val invokerGenerator = when {
                        function.parameters.any { it.isOptional } -> DefaultInvokerGenerator
                        else -> DirectInvokerGenerator
                    }
                    generate(invokerGenerator, codeBuilder)
                }
            }
        }

        val clazz = lookup
            .defineHiddenClass(bytes, true)
            .lookupClass()

        @Suppress("UNCHECKED_CAST")
        return createInstance(clazz) as MethodAccessor<R>
    }

    protected abstract fun addFields(classBuilder: ClassBuilder)

    protected abstract fun addConstructor(classBuilder: ClassBuilder)

    protected abstract fun createInstance(clazz: Class<*>): MethodAccessor<*>
}
