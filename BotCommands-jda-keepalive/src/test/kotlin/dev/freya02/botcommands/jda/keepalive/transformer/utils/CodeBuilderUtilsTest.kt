package dev.freya02.botcommands.jda.keepalive.transformer.utils

import dev.freya02.botcommands.jda.keepalive.internal.transformer.*
import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.InternalModuleNames
import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.createLambda
import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.lambdaMetafactoryDesc
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.constant.*
import java.util.function.Supplier
import kotlin.test.assertEquals

object CodeBuilderUtilsTest {

    @MethodSource("Test createLambda")
    @ParameterizedTest
    fun `Test createLambda`(expected: DynamicCallSiteDesc, actual: DynamicCallSiteDesc) {
        assertEquals(expected, actual)
    }

    @JvmStatic
    fun `Test createLambda`(): List<Arguments> = listOf(
        Arguments.of(
            DynamicCallSiteDesc.of(
                lambdaMetafactoryDesc,
                "get",
                MethodTypeDesc.of(CD_Supplier, CD_JDABuilder),
                MethodTypeDesc.of(ConstantDescs.CD_Object),
                MethodHandleDesc.ofMethod(
                    DirectMethodHandleDesc.Kind.VIRTUAL,
                    CD_JDABuilder,
                    "doBuild",
                    MethodTypeDesc.of(CD_JDA)
                ),
                MethodTypeDesc.of(ConstantDescs.CD_Object),
            ),
            createLambda(
                interfaceMethod = Supplier<*>::get,
                targetType = CD_JDABuilder,
                targetMethod = "doBuild",
                targetMethodReturnType = CD_JDA,
                targetMethodArguments = listOf(),
                capturedTypes = listOf(),
                isStatic = false
            )
        ),

        Arguments.of(
            DynamicCallSiteDesc.of(
                lambdaMetafactoryDesc,
                "run",
                MethodTypeDesc.of(CD_Runnable, CD_JDAService, CD_BReadyEvent, CD_IEventManager),
                MethodTypeDesc.of(ConstantDescs.CD_void),
                MethodHandleDesc.ofMethod(
                    DirectMethodHandleDesc.Kind.VIRTUAL,
                    CD_JDAService,
                    $$"lambda$onReadyEvent$$${InternalModuleNames.CORE}$withBuilderSession",
                    MethodTypeDesc.of(ConstantDescs.CD_void, CD_BReadyEvent, CD_IEventManager)
                ),
                MethodTypeDesc.of(ConstantDescs.CD_void),
            ),
            createLambda(
                interfaceMethod = Runnable::run,
                targetType = CD_JDAService,
                targetMethod = $$"lambda$onReadyEvent$$${InternalModuleNames.CORE}$withBuilderSession",
                targetMethodReturnType = ConstantDescs.CD_void,
                targetMethodArguments = listOf(),
                capturedTypes = listOf(CD_BReadyEvent, CD_IEventManager),
                isStatic = false
            )
        ),

        Arguments.of(
            DynamicCallSiteDesc.of(
                lambdaMetafactoryDesc,
                "run",
                MethodTypeDesc.of(CD_Runnable, CD_JDAImpl),
                MethodTypeDesc.of(ConstantDescs.CD_void),
                MethodHandleDesc.ofMethod(
                    DirectMethodHandleDesc.Kind.VIRTUAL,
                    CD_JDAImpl,
                    "doShutdown",
                    MethodTypeDesc.of(ConstantDescs.CD_void)
                ),
                MethodTypeDesc.of(ConstantDescs.CD_void),
            ),
            createLambda(
                interfaceMethod = Runnable::run,
                targetType = CD_JDAImpl,
                targetMethod = "doShutdown",
                targetMethodReturnType = ConstantDescs.CD_void,
                targetMethodArguments = listOf(),
                capturedTypes = listOf(),
                isStatic = false
            )
        ),

        Arguments.of(
            DynamicCallSiteDesc.of(
                lambdaMetafactoryDesc,
                "run",
                MethodTypeDesc.of(CD_Runnable, CD_BContextImpl, CD_Function0),
                MethodTypeDesc.of(ConstantDescs.CD_void),
                MethodHandleDesc.ofMethod(
                    DirectMethodHandleDesc.Kind.VIRTUAL,
                    CD_BContextImpl,
                    "doScheduleShutdownSignal",
                    MethodTypeDesc.of(ConstantDescs.CD_void, CD_Function0)
                ),
                MethodTypeDesc.of(ConstantDescs.CD_void),
            ),
            createLambda(
                interfaceMethod = Runnable::run,
                targetType = CD_BContextImpl,
                targetMethod = "doScheduleShutdownSignal",
                targetMethodReturnType = ConstantDescs.CD_void,
                targetMethodArguments = listOf(),
                capturedTypes = listOf(CD_Function0),
                isStatic = false
            )
        ),
    )
}
