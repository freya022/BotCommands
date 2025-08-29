package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.codegen.utils.*
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import io.github.freya022.botcommands.method.accessors.internal.MethodAccessor
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.*
import java.lang.classfile.TypeKind
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal object ClassFileMethodAccessorGenerator {

    internal fun generate(
        instance: Any,
        function: KFunction<*>,
        lookup: MethodHandles.Lookup,
    ): MethodAccessor {
        // TODO support constructors? unsure if it will be beneficial for services, they run once, see what's the diff in stack traces
        val executable = function.javaExecutable
        require(executable is Method) { "Constructors are not supported yet" }

        val hasOptionals = function.parameters.any { it.isOptional }
        require(!hasOptionals) { "Optionals are not supported yet" }

        val isSuspend = function.isSuspend
        require(!isSuspend) { "Suspending functions are not supported yet" }

        function.parameters.forEach { parameter ->
            require(parameter.kind == KParameter.Kind.INSTANCE || parameter.kind == KParameter.Kind.VALUE) {
                "Unsupported parameter kind: $parameter"
            }
        }

        val instanceDesc = instance.javaClass.describeConstable().get()
        val methodTypeDesc = run {
            val returnTypeDesc = executable.returnType.describeConstable().get()
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            MethodTypeDesc.of(returnTypeDesc, parameterDescs)
        }

        // The class must be unique per function, which is why we don't cache the class
        // Also "duplicate" definitions are allowed for hidden classes
        val thisClass = ClassDesc.of("${lookup.lookupClass().packageName}.ClassFileMethodAccessor")
        val bytes = ClassFile.of().build(thisClass) { classBuilder ->
            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withInterfaceSymbols(CD_MethodAccessor)

            // TODO replace with class data of hidden class
            classBuilder.withField("instance", instanceDesc, ACC_PRIVATE or ACC_FINAL)
            classBuilder.withField("function", CD_KFunction, ACC_PRIVATE or ACC_FINAL)

            classBuilder.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, instanceDesc, CD_KFunction), ACC_PUBLIC) { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()
                val instanceSlot = codeBuilder.parameterSlot(0)
                val functionSlot = codeBuilder.parameterSlot(1)

                // this.super()
                codeBuilder.aload(thisSlot)
                codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

                // this.instance = instance;
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(instanceSlot)
                codeBuilder.putfield(thisClass, "instance", instanceDesc)

                // this.function = function;
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(functionSlot)
                codeBuilder.putfield(thisClass, "function", CD_KFunction)

                codeBuilder.return_()
            }

            classBuilder.withMethodBody("call", MethodTypeDesc.of(CD_Object, CD_Map, CD_Continuation), ACC_PUBLIC or ACC_FINAL) { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()
                val argsSlot = codeBuilder.parameterSlot(0)
                val continuationSlot = codeBuilder.parameterSlot(1)

                val parameterSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // this.instance.[methodName]([params])
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(thisClass, "instance", instanceDesc)
                function.parameters.forEachIndexed { index, parameter ->
                    if (parameter.kind != KParameter.Kind.VALUE) return@forEachIndexed

                    // var parameter = function.getParameters().get([index])
                    codeBuilder.aload(thisSlot)
                    codeBuilder.getfield(thisClass, "function", CD_KFunction)
                    codeBuilder.invokeinterface(CD_KCallable, "getParameters", MethodTypeDesc.of(CD_List))
                    codeBuilder.loadConstant(index)
                    codeBuilder.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int))
                    codeBuilder.checkcast(CD_KParameter)
                    codeBuilder.astore(parameterSlot)

                    // <parameter> = args.get(parameter)
                    codeBuilder.aload(argsSlot)
                    codeBuilder.aload(parameterSlot)
                    codeBuilder.invokeinterface(CD_Map, "get", MethodTypeDesc.of(CD_Object, CD_Object))
                    codeBuilder.castTo(target = parameter.type.jvmErasure.java)
                }
                if (Modifier.isStatic(executable.modifiers)) {
                    codeBuilder.invokestatic(instanceDesc, executable.name, methodTypeDesc)
                } else {
                    codeBuilder.invokevirtual(instanceDesc, executable.name, methodTypeDesc)
                }
                // Discard invoked method return value
                if (methodTypeDesc.returnType() != CD_void) codeBuilder.pop()

                codeBuilder.aload(continuationSlot)
                codeBuilder.areturn()
            }
        }

        val clazz = lookup
            .defineHiddenClass(bytes, true)
            .lookupClass()
        return clazz
            .getDeclaredConstructor(instance.javaClass, KFunction::class.java)
            .newInstance(instance, function) as MethodAccessor
    }
}
