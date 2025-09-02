package dev.freya02.botcommands.method.accessors.internal.codegen.invoker

import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_MethodArguments
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.unboxOrCastTo
import java.lang.classfile.CodeBuilder
import java.lang.constant.ConstantDescs.CD_Object
import java.lang.constant.ConstantDescs.CD_int
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.KClass

internal abstract class AbstractInvokerGenerator : InvokerGenerator {

    protected fun CodeBuilder.loadUnboxedOptional(
        type: Class<*>,
        kotlinErasure: KClass<*>,
        argsSlot: Int,
        index: Int,
        maskSlot: Int,
        valueParameterIndex: Int,
    ) {
        val realValueLabel = newLabel()
        val endLabel = newLabel()

        // <stack> <- (<type>) args.get(parameter)
        loadArg(argsSlot, index)
        dup() // So we can cast and keep in stack

        // If the value is MethodArguments#NO_VALUE, jump to loading the default
        getstatic(CD_MethodArguments, "NO_VALUE", CD_Object)
        if_acmpeq(realValueLabel) // NOTE: Remember to have the same amount of stack data in and out of the branch
        run {
            // Key exists, unbox or cast
            // The value may be null, but null can always be cast to any object type
            unboxOrCastTo(type, kotlinErasure)
            goto_(endLabel)
        }

        labelBinding(realValueLabel)
        run {
            pop() // We don't need the NO_VALUE
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

        labelBinding(endLabel)
    }

    protected fun CodeBuilder.loadArg(argsSlot: Int, index: Int) {
        aload(argsSlot)
        loadConstant(index)
        invokevirtual(CD_MethodArguments, "get", MethodTypeDesc.of(CD_Object, CD_int))
    }
}
