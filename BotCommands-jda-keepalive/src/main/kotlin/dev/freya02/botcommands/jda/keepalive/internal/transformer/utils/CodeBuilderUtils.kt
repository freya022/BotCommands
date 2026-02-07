package dev.freya02.botcommands.jda.keepalive.internal.transformer.utils

import java.lang.classfile.ClassFileBuilder
import java.lang.classfile.ClassFileElement
import java.lang.classfile.CodeBuilder
import java.lang.constant.*
import java.lang.constant.ConstantDescs.CD_String
import java.lang.invoke.*
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

internal inline fun <reified T : Any> classDesc(): ClassDesc = ClassDesc.of(T::class.java.name)

internal fun <E : ClassFileElement> ClassFileBuilder<E, *>.retain(element: E) {
    with(element)
}

internal fun CodeBuilder.ldc(string: String) {
    ldc(string as java.lang.String)
}

internal val lambdaMetafactoryDesc = MethodHandleDesc.ofMethod(
    DirectMethodHandleDesc.Kind.STATIC,
    classDesc<LambdaMetafactory>(),
    "metafactory",
    MethodTypeDesc.of(
        classDesc<CallSite>(),
        classDesc<MethodHandles.Lookup>(),
        CD_String,
        classDesc<MethodType>(),
        classDesc<MethodType>(),
        classDesc<MethodHandle>(),
        classDesc<MethodType>()
    )
)

internal fun createLambda(
    interfaceMethod: KFunction<*>,
    targetType: ClassDesc,
    targetMethod: String,
    targetMethodReturnType: ClassDesc,
    targetMethodArguments: List<ClassDesc>,
    capturedTypes: List<ClassDesc>,
    isStatic: Boolean,
): DynamicCallSiteDesc {
    val effectiveCapturedTypes = when {
        isStatic -> capturedTypes
        else -> listOf(targetType) + capturedTypes
    }

    fun Class<*>.toClassDesc(): ClassDesc =
        describeConstable().orElseThrow { IllegalArgumentException("$name cannot be transformed to a ClassDesc") }

    val interfaceJavaMethod = interfaceMethod.javaMethod!!
    val targetInterface = interfaceJavaMethod.declaringClass.toClassDesc()
    val methodReturnType = interfaceJavaMethod.returnType.toClassDesc()
    val methodArguments = interfaceJavaMethod.parameterTypes.map { it.toClassDesc() }

    return DynamicCallSiteDesc.of(
        lambdaMetafactoryDesc,
        // The following parameters are from [[LambdaMetafactory#metafactory]]
        // This is the 2nd argument of LambdaMetafactory#metafactory, "interfaceMethodName",
        // the method name in Runnable is "run"
        interfaceMethod.name,
        // This is the 3rd argument of LambdaMetafactory#metafactory, "factoryType",
        // the return type is the implemented interface,
        // while the parameters are the captured variables
        MethodTypeDesc.of(targetInterface, effectiveCapturedTypes),
        // Bootstrap arguments (see `javap -c -v <class file>` from a working .java sample)
        // This is the 4th argument of LambdaMetafactory#metafactory, "interfaceMethodType",
        // which is the signature of the implemented method, in this case, void Runnable.run()
        MethodTypeDesc.of(methodReturnType, methodArguments),
        // This is the 5th argument of LambdaMetafactory#metafactory, "implementation",
        // this is the method to be called when invoking the lambda,
        // with the captured variables and parameters
        MethodHandleDesc.ofMethod(
            if (isStatic) DirectMethodHandleDesc.Kind.STATIC else DirectMethodHandleDesc.Kind.VIRTUAL,
            targetType,
            targetMethod,
            MethodTypeDesc.of(targetMethodReturnType, capturedTypes + targetMethodArguments)
        ),
        // This is the 6th argument of LambdaMetafactory#metafactory, "dynamicMethodType",
        // this is "the signature and return type to be enforced dynamically at invocation type"
        // This is usually the same as "interfaceMethodType"
        MethodTypeDesc.of(methodReturnType, methodArguments),
    )
}
