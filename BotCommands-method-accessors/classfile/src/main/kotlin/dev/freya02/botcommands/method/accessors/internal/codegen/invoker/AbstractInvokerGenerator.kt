package dev.freya02.botcommands.method.accessors.internal.codegen.invoker

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_KCallable
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_KFunction
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_KParameter
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.unboxOrCastTo
import java.lang.classfile.CodeBuilder
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc

internal abstract class AbstractInvokerGenerator : InvokerGenerator {

    protected fun AbstractClassFileMethodAccessorGenerator<*>.loadParameter(
        codeBuilder: CodeBuilder,
        thisSlot: Int,
        index: Int,
        parameterSlot: Int
    ) {
        // var parameter = function.getParameters().get([index])
        codeBuilder.aload(thisSlot)
        codeBuilder.getfield(thisClass, "function", CD_KFunction)
        codeBuilder.invokeinterface(CD_KCallable, "getParameters", MethodTypeDesc.of(CD_List))
        codeBuilder.loadConstant(index)
        codeBuilder.invokeinterface(CD_List, "get", MethodTypeDesc.of(CD_Object, CD_int))
        codeBuilder.checkcast(CD_KParameter)
        codeBuilder.astore(parameterSlot)
    }

    protected fun CodeBuilder.loadUnboxedOptional(
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
                loadArg(argsSlot, parameterSlot, type)
            },
            {
                // Key does not exist, load default
                when (type) {
                    Boolean::class.javaPrimitiveType, Byte::class.javaPrimitiveType, Char::class.javaPrimitiveType, Short::class.javaPrimitiveType, Int::class.javaPrimitiveType ->
                        iconst_0()

                    Long::class.javaPrimitiveType -> lconst_0()
                    Float::class.javaPrimitiveType -> fconst_0()
                    Double::class.javaPrimitiveType -> dconst_0()
                    else -> aconst_null()
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

    protected fun CodeBuilder.loadArg(argsSlot: Int, parameterSlot: Int, type: Class<*>) {
        aload(argsSlot)
        aload(parameterSlot)
        invokeinterface(CD_Map, "get", MethodTypeDesc.of(CD_Object, CD_Object))
        // The value may be null, but null can always be cast to any object type
        unboxOrCastTo(type)
    }
}
