package dev.freya02.botcommands.method.accessors.internal.codegen.modality

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.InvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.*
import java.lang.classfile.CodeBuilder
import java.lang.classfile.TypeKind
import java.lang.classfile.instruction.SwitchCase
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.Method

internal object SuspendingInvokerGenerator : ModalityAwareInvokerGenerator {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        invokerGenerator: InvokerGenerator,
        codeBuilder: CodeBuilder,
    ) {
        require(executable is Method)

        val continuationSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
        val callReturnValueSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

        assignOrCreateContinuation(codeBuilder, continuationSlot)

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

        with(invokerGenerator) { generate(continuationSlot, codeBuilder) }
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

    private fun assignOrCreateContinuation(codeBuilder: CodeBuilder, continuationSlot: Int) {
        val thisSlot = codeBuilder.receiverSlot()
        val completionSlot = codeBuilder.parameterSlot(1)

        codeBuilder.block { blockCodeBuilder ->
            // if (completion instanceof MethodAccessorContinuation) { ... }
            blockCodeBuilder.aload(completionSlot)
            blockCodeBuilder.instanceOf(CD_MethodAccessorContinuation)
            blockCodeBuilder.ifThen { instanceOfCodeBuilder ->
                // continuation = (MethodAccessorContinuation) completion;
                instanceOfCodeBuilder.aload(completionSlot)
                instanceOfCodeBuilder.checkcast(CD_MethodAccessorContinuation)
                instanceOfCodeBuilder.astore(continuationSlot)

                // if (continuation.isResumeLabel()) { ... }
                instanceOfCodeBuilder.aload(continuationSlot)
                instanceOfCodeBuilder.invokevirtual(
                    CD_MethodAccessorContinuation,
                    "isResumeLabel",
                    MethodTypeDesc.of(CD_boolean)
                )
                instanceOfCodeBuilder.ifThen { isResumeCodeBuilder ->
                    // continuation.label = continuation.label - Integer.MIN_VALUE
                    isResumeCodeBuilder.aload(continuationSlot)
                    isResumeCodeBuilder.dup() // So we can reassign it
                    isResumeCodeBuilder.getfield(CD_MethodAccessorContinuation, "label", CD_int)
                    isResumeCodeBuilder.loadConstant(Integer.MIN_VALUE)
                    isResumeCodeBuilder.isub()
                    isResumeCodeBuilder.putfield(CD_MethodAccessorContinuation, "label", CD_int)

                    // break <block>
                    isResumeCodeBuilder.goto_(blockCodeBuilder.breakLabel())
                }
            }

            // If we're here, the continuation either isn't ours, or it is (what I assume) a resumed one
            // continuation = new MethodAccessorContinuation(completion, this);
            codeBuilder.new_(CD_MethodAccessorContinuation)
            codeBuilder.dup() // To assign after <init>
            codeBuilder.aload(completionSlot)
            codeBuilder.aload(thisSlot)
            codeBuilder.invokespecial(CD_MethodAccessorContinuation, INIT_NAME, MethodTypeDesc.of(CD_void, CD_Continuation, CD_MethodAccessor))
            codeBuilder.astore(continuationSlot)
        }
    }
}
