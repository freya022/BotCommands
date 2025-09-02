package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.default.DefaultInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct.DirectInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.modality.BlockingInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.modality.SuspendingInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_Continuation
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_IllegalSuspendCallException
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_MethodArguments
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.ACC_FINAL
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

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

            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withInterfaceSymbols(CD_MethodAccessor)

            // TODO replace with class data of hidden class
            addFields(classBuilder)

            addConstructor(classBuilder)

            classBuilder.withMethodBody("hasInstance", MethodTypeDesc.of(CD_boolean), ACC_PUBLIC or ACC_FINAL) { codeBuilder ->
                if (isStatic || executable is Constructor<*>) {
                    codeBuilder.iconst_0() // false, does not have instance parameter
                } else {
                    codeBuilder.iconst_1() // true, has instance parameter
                }
                codeBuilder.ireturn()
            }

            classBuilder.withMethodBody("callSuspend", MethodTypeDesc.of(CD_Object, CD_MethodArguments, CD_Continuation), ACC_PUBLIC or ACC_FINAL) { codeBuilder ->
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

            classBuilder.withMethodBody("call", MethodTypeDesc.of(CD_Object, CD_MethodArguments), ACC_PUBLIC or ACC_FINAL) { codeBuilder ->
                if (function.isSuspend) {
                    // throw new IllegalSuspendCallException()
                    codeBuilder.new_(CD_IllegalSuspendCallException)
                    codeBuilder.dup() // so we can throw it
                    codeBuilder.invokespecial(CD_IllegalSuspendCallException, INIT_NAME, MethodTypeDesc.of(CD_void))
                    codeBuilder.athrow()
                } else {
                    with(BlockingInvokerGenerator) {
                        val invokerGenerator = when {
                            function.parameters.any { it.isOptional } -> DefaultInvokerGenerator
                            else -> DirectInvokerGenerator
                        }
                        generate(invokerGenerator, codeBuilder)
                    }
                }
            }

            classBuilder.withMethodBody("createBlankArguments", MethodTypeDesc.of(CD_MethodArguments), ACC_PUBLIC or ACC_FINAL) { codeBuilder ->
                // return new MethodArguments([parameterCount])
                codeBuilder.new_(CD_MethodArguments)
                codeBuilder.dup()
                codeBuilder.loadConstant(function.parameters.count { it.kind != KParameter.Kind.INSTANCE })
                codeBuilder.invokespecial(CD_MethodArguments, INIT_NAME, MethodTypeDesc.of(CD_void, CD_int))
                codeBuilder.areturn()
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
