package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.codegen.utils.*
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import io.github.freya022.botcommands.method.accessors.internal.MethodAccessor
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.*
import java.lang.classfile.CodeBuilder
import java.lang.classfile.TypeKind
import java.lang.classfile.instruction.SwitchCase
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

        function.parameters.forEach { parameter ->
            require(parameter.kind == KParameter.Kind.INSTANCE || parameter.kind == KParameter.Kind.VALUE) {
                "Unsupported parameter kind: $parameter"
            }
        }

        val instanceDesc = instance.javaClass.describeConstable().get()

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
                if (function.isSuspend) {
                    writeSuspendingCallerInstructions(function, thisClass, instanceDesc, executable, codeBuilder)
                } else {
                    writeBlockingCallerInstructions(function, thisClass, instanceDesc, executable, codeBuilder)
                }
            }
        }

        val clazz = lookup
            .defineHiddenClass(bytes, true)
            .lookupClass()
        return clazz
            .getDeclaredConstructor(instance.javaClass, KFunction::class.java)
            .newInstance(instance, function) as MethodAccessor
    }

    private fun writeBlockingCallerInstructions(function: KFunction<*>, thisClass: ClassDesc, instanceDesc: ClassDesc, executable: Method, codeBuilder: CodeBuilder) {
        if (function.parameters.any { it.isOptional }) {
            writeDefaultInvokeInstructions(thisClass, instanceDesc, function, executable, continuationSlot = null, codeBuilder)
        } else {
            writeInvokeInstructions(thisClass, instanceDesc, function, executable, continuationSlot = null, codeBuilder)
        }

        // Return value as Object, or return Unit as the implemented method must return something
        if (executable.returnType != Void.TYPE) {
            codeBuilder.boxIfPrimitive(type = executable.returnType)
        } else {
            codeBuilder.getstatic(CD_Unit, "INSTANCE", CD_Unit)
        }
        codeBuilder.areturn()
    }

    private fun writeSuspendingCallerInstructions(function: KFunction<*>, thisClass: ClassDesc, instanceDesc: ClassDesc, executable: Method, codeBuilder: CodeBuilder) {
        val continuationSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
        val callReturnValueSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

        codeBuilder.assignOrCreateContinuation(continuationSlot)

        val firstRunLabel = codeBuilder.newLabel()
        val firstResumeLabel = codeBuilder.newLabel()
        val defaultResumeLabel = codeBuilder.newLabel()
        /** Skips right to the return statement */
        val returnResultLabel = codeBuilder.newLabel()

        // var callReturnValue = continuation.result;
        codeBuilder.aload(continuationSlot)
        codeBuilder.getfield(CD_MethodAccessorContinuation, "result", CD_Object)
        codeBuilder.astore(callReturnValueSlot)

        // switch (continuation.label) { ... }
        codeBuilder.aload(continuationSlot)
        codeBuilder.getfield(CD_MethodAccessorContinuation, "label", CD_int)
        codeBuilder.tableswitch(
            defaultResumeLabel,
            listOf(
                SwitchCase.of(0, firstRunLabel),
                SwitchCase.of(1, firstResumeLabel)
            )
        )


        codeBuilder.labelBinding(firstRunLabel)
        // continuation.label = 1
        codeBuilder.aload(continuationSlot)
        codeBuilder.iconst_1()
        codeBuilder.putfield(CD_MethodAccessorContinuation, "label", CD_int)

        if (function.parameters.any { it.isOptional }) {
            writeDefaultInvokeInstructions(thisClass, instanceDesc, function, executable, continuationSlot, codeBuilder)
        } else {
            writeInvokeInstructions(thisClass, instanceDesc, function, executable, continuationSlot, codeBuilder)
        }
        codeBuilder.astore(callReturnValueSlot)

        // if (callReturnValue == IntrinsicsKt.getCOROUTINE_SUSPENDED()) { ... }
        codeBuilder.aload(callReturnValueSlot)
        codeBuilder.invokestatic(CD_IntrinsicsKt, "getCOROUTINE_SUSPENDED", MethodTypeDesc.of(CD_Object))
        codeBuilder.if_acmpne(returnResultLabel) // If not COROUTINE_SUSPENDED, go return real value
        // At this point the result is equal to COROUTINE_SUSPENDED
        // DebugProbesKt.probeCoroutineSuspended(continuation)
        codeBuilder.aload(continuationSlot)
        codeBuilder.invokestatic(CD_DebugProbesKt, "probeCoroutineSuspended", MethodTypeDesc.of(CD_void, CD_Continuation))
        // return callReturnValue (always COROUTINE_SUSPENDED)
        codeBuilder.aload(callReturnValueSlot)
        codeBuilder.areturn()


        codeBuilder.labelBinding(firstResumeLabel)
        // After the first suspension point (i.e. the call to the user function), return result
        // ResultKt.throwOnFailure(callReturnValue)
        codeBuilder.aload(callReturnValueSlot)
        codeBuilder.invokestatic(CD_ResultKt, "throwOnFailure", MethodTypeDesc.of(CD_void, CD_Object))
        // return result
        codeBuilder.goto_(returnResultLabel)


        codeBuilder.labelBinding(defaultResumeLabel)
        // throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine")
        codeBuilder.new_(CD_IllegalStateException)
        codeBuilder.dup()
        codeBuilder.ldc("call to 'resume' before 'invoke' with coroutine" as java.lang.String)
        codeBuilder.invokespecial(CD_IllegalStateException, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String))
        codeBuilder.athrow()


        codeBuilder.labelBinding(returnResultLabel)
        // As per KCallable#callSuspendBy, Unit functions may not return Unit in some cases
        if (function.returnType.classifier == Unit::class && !function.returnType.isMarkedNullable) {
            // In those cases, force return Unit
            codeBuilder.getstatic(CD_Unit, "INSTANCE", CD_Unit)
        } else {
            codeBuilder.aload(callReturnValueSlot)
        }
        codeBuilder.areturn()
    }

    private fun CodeBuilder.assignOrCreateContinuation(continuationSlot: Int) {
        val thisSlot = receiverSlot()
        val completionSlot = parameterSlot(1)

        block { blockCodeBuilder ->
            // if (completion instanceof MethodAccessorContinuation) { ... }
            aload(completionSlot)
            instanceOf(CD_MethodAccessorContinuation)
            ifThen { instanceOfCodeBuilder ->
                // continuation = (MethodAccessorContinuation) completion;
                instanceOfCodeBuilder.aload(completionSlot)
                instanceOfCodeBuilder.checkcast(CD_MethodAccessorContinuation)
                instanceOfCodeBuilder.astore(continuationSlot)

                // if (continuation.isResumeLabel()) { ... }
                instanceOfCodeBuilder.aload(continuationSlot)
                instanceOfCodeBuilder.invokevirtual(CD_MethodAccessorContinuation, "isResumeLabel", MethodTypeDesc.of(CD_boolean))
                instanceOfCodeBuilder.ifThen { isResumeCodeBuilder ->
                    // continuation.label = continuation.label - Integer.MIN_VALUE
                    isResumeCodeBuilder.aload(continuationSlot)
                    isResumeCodeBuilder.dup() // So we can reassign it
                    isResumeCodeBuilder.getfield(CD_MethodAccessorContinuation, "label", CD_int)
                    isResumeCodeBuilder.loadConstant(Integer.MIN_VALUE)
                    isResumeCodeBuilder.isub()
                    isResumeCodeBuilder.putfield(CD_MethodAccessorContinuation, "label", CD_int)

                    // break <block>
                    instanceOfCodeBuilder.goto_(blockCodeBuilder.breakLabel())
                }
            }

            // If we're here, the continuation either isn't ours, or it is (what I assume) a resumed one
            // continuation = new MethodAccessorContinuation(completion, this);
            new_(CD_MethodAccessorContinuation)
            dup() // To assign after <init>
            aload(completionSlot)
            aload(thisSlot)
            invokespecial(CD_MethodAccessorContinuation, INIT_NAME, MethodTypeDesc.of(CD_void, CD_Continuation, CD_MethodAccessor))
            astore(continuationSlot)
        }
    }

    private fun writeInvokeInstructions(
        thisClass: ClassDesc,
        instanceDesc: ClassDesc,
        function: KFunction<*>,
        executable: Method,
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        val methodTypeDesc = run {
            val returnTypeDesc = executable.returnType.describeConstable().get()
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            MethodTypeDesc.of(returnTypeDesc, parameterDescs)
        }

        val thisSlot = codeBuilder.receiverSlot()
        val argsSlot = codeBuilder.parameterSlot(0)

        val parameterSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

        // this.instance.[methodName]([params])
        codeBuilder.aload(thisSlot)
        codeBuilder.getfield(thisClass, "instance", instanceDesc)
        function.parameters.forEachIndexed { index, parameter ->
            if (parameter.kind != KParameter.Kind.VALUE) return@forEachIndexed

            // var parameter = function.getParameters().get([index])
            codeBuilder.loadParameter(thisSlot, thisClass, index, parameterSlot)

            // <parameter> = args.get(parameter)
            codeBuilder.aload(argsSlot)
            codeBuilder.aload(parameterSlot)
            codeBuilder.invokeinterface(CD_Map, "get", MethodTypeDesc.of(CD_Object, CD_Object))
            codeBuilder.unboxOrCastTo(target = parameter.type.jvmErasure.java)
        }
        if (continuationSlot != null) codeBuilder.aload(continuationSlot)
        if (Modifier.isStatic(executable.modifiers)) {
            codeBuilder.invokestatic(instanceDesc, executable.name, methodTypeDesc)
        } else {
            codeBuilder.invokevirtual(instanceDesc, executable.name, methodTypeDesc)
        }
    }

    private fun writeDefaultInvokeInstructions(
        thisClass: ClassDesc,
        instanceDesc: ClassDesc,
        function: KFunction<*>,
        executable: Method,
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        val methodTypeDesc = run {
            val returnTypeDesc = executable.returnType.describeConstable().get()
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            MethodTypeDesc.of(
                returnTypeDesc,
                listOf(instanceDesc) + parameterDescs + listOf(CD_int, CD_Object)
            )
        }

        val thisSlot = codeBuilder.receiverSlot()
        val argsSlot = codeBuilder.parameterSlot(0)

        val parameterSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
        val maskSlot = codeBuilder.allocateLocal(TypeKind.INT)

        // maskSlot = 0
        codeBuilder.iconst_0()
        codeBuilder.istore(maskSlot)

        // InstanceClass.[methodName]$default(instance, [params], mask, null)
        codeBuilder.aload(thisSlot)
        codeBuilder.getfield(thisClass, "instance", instanceDesc)
        var valueParameterIndex = 0
        function.parameters.forEachIndexed { index, parameter ->
            if (parameter.kind != KParameter.Kind.VALUE) return@forEachIndexed

            val paramJavaType = parameter.type.jvmErasure.java

            // var parameter = function.getParameters().get([index])
            codeBuilder.loadParameter(thisSlot, thisClass, index, parameterSlot)

            if (parameter.isOptional) {
                codeBuilder.loadUnboxedOptional(paramJavaType, argsSlot, parameterSlot, maskSlot, valueParameterIndex)
            } else {
                // <parameter> = args.get(parameter)
                codeBuilder.aload(argsSlot)
                codeBuilder.aload(parameterSlot)
                codeBuilder.invokeinterface(CD_Map, "get", MethodTypeDesc.of(CD_Object, CD_Object))
                // Cast non-null value into primitive/ref
                codeBuilder.unboxOrCastTo(target = paramJavaType)
            }

            valueParameterIndex++
        }
        if (continuationSlot != null) codeBuilder.aload(continuationSlot)
        codeBuilder.iload(maskSlot)
        codeBuilder.aconst_null()
        codeBuilder.invokestatic(instanceDesc, $$"$${executable.name}$default", methodTypeDesc)
    }
}

private fun CodeBuilder.loadParameter(thisSlot: Int, thisClass: ClassDesc, index: Int, parameterSlot: Int) {
    // var parameter = function.getParameters().get([index])
    aload(thisSlot)
    getfield(thisClass, "function", CD_KFunction)
    invokeinterface(CD_KCallable, "getParameters", MethodTypeDesc.of(CD_List))
    loadConstant(index)
    invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int))
    checkcast(CD_KParameter)
    astore(parameterSlot)
}

private fun CodeBuilder.loadUnboxedOptional(
    type: Class<*>,
    argsSlot: Int,
    parameterSlot: Int,
    maskSlot: Int,
    valueParameterIndex: Int,
) {
    aload(argsSlot)
    aload(parameterSlot)
    invokeinterface(CD_Map, "containsKey", MethodTypeDesc.of(CD_boolean, CD_Object))

    // NOTE: Remember to have the same amount of stack data in and out of the branch
    ifThenElse(
        {
            // Key exists, unbox or cast
            // <stack> <- (<type>) args.get(parameter)
            aload(argsSlot)
            aload(parameterSlot)
            invokeinterface(CD_Map, "get", MethodTypeDesc.of(CD_Object, CD_Object))
            // The value may be null, but null can always be cast to any object type
            unboxOrCastTo(type)
        },
        {
            // Key does not exist, load default
            when (type) {
                Boolean::class.javaPrimitiveType, Byte::class.javaPrimitiveType, Char::class.javaPrimitiveType, Short::class.javaPrimitiveType, Int::class.javaPrimitiveType ->
                    iconst_0()

                Long::class.javaPrimitiveType -> lconst_0()
                Float::class.javaPrimitiveType -> fconst_0()
                Double::class.javaPrimitiveType -> dconst_0()
                else -> error("Unmatched $type")
            }

            // Also set our mask bit so the placeholder gets replaced by the default
            // mask = mask | [1 << (valueParameterIndex % Integer.SIZE)]
            iload(maskSlot)
            loadConstant(1 shl (valueParameterIndex % Integer.SIZE))
            ior()
            istore(maskSlot)
        }
    )
}
