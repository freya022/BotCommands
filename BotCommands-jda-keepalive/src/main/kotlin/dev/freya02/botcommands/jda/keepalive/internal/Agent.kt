package dev.freya02.botcommands.jda.keepalive.internal

import com.sun.tools.attach.VirtualMachine
import dev.freya02.botcommands.jda.keepalive.internal.exceptions.AlreadyLoadedClassesException
import dev.freya02.botcommands.jda.keepalive.internal.exceptions.AttachSelfDeniedException
import dev.freya02.botcommands.jda.keepalive.internal.exceptions.IllegalAgentContainerException
import dev.freya02.botcommands.jda.keepalive.internal.transformer.CD_JDABuilder
import dev.freya02.botcommands.jda.keepalive.internal.transformer.CD_JDAImpl
import dev.freya02.botcommands.jda.keepalive.internal.transformer.JDABuilderTransformer
import dev.freya02.botcommands.jda.keepalive.internal.transformer.JDAImplTransformer
import io.github.freya022.botcommands.api.core.utils.joinAsList
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.toPath

internal object Agent {

    internal val transformers = mapOf(
        CD_JDABuilder to JDABuilderTransformer,
        CD_JDAImpl to JDAImplTransformer,
//        CD_BContextImpl to BContextImplTransformer,
    )

    private val lock = ReentrantLock()
    internal var isLoaded = false
        private set

    @JvmStatic
    fun premain(agentArgs: String?, inst: Instrumentation) {
        lock.withLock {
            isLoaded = true
        }

        transformers.values.forEach(inst::addTransformer)
    }

    @JvmStatic
    fun agentmain(agentArgs: String?, inst: Instrumentation) {
        lock.withLock {
            if (isLoaded) return
            isLoaded = true
        }

        checkNoLoadedClassesAreToBeTransformed(inst.allLoadedClasses)

        transformers.values.forEach(inst::addTransformer)
    }

    internal fun checkNoLoadedClassesAreToBeTransformed(allLoadedClasses: Array<Class<*>>) {
        val transformedClasses = transformers.keys.mapTo(hashSetOf()) { it.packageName() + "." + it.displayName() }
        val earlyLoadedClasses = allLoadedClasses.filter { it.name in transformedClasses }
        if (earlyLoadedClasses.isNotEmpty()) {
            // TODO wiki link (of the whole loading mechanism probably)
            throw AlreadyLoadedClassesException(
                "Dynamically loaded agents must be loaded before the classes it transforms, although it is recommended to add the agent via the command line, offending classes:\n" +
                        earlyLoadedClasses.joinAsList { it.name }
            )
        }
    }

    internal fun load() {
        lock.withLock {
            if (isLoaded) return
        }

        // Check self-attaching agents are allowed
        if (System.getProperty("jdk.attach.allowAttachSelf") != "true") {
            // TODO wiki link (show how to do in IJ)
            throw AttachSelfDeniedException("Can only dynamically load an agent with the '-Djdk.attach.allowAttachSelf=true' VM argument")
        }

        // Get the agent JAR
        // In a user's dev environment, this should be the dependency's JAR
        // but in *our* test environment, we need a property to the JAR produced by Gradle,
        // as it still uses the directory in the classpath
        val agentSourcePath: Path = run {
            // Property to test instrumentation is applied
            System.getProperty("bc.jda.keepalive.agentPath")?.let { return@run Path(it) }

            javaClass.protectionDomain.codeSource.location.toURI().toPath()
        }
        if (agentSourcePath.extension != "jar") {
            // TODO add wiki link about including the dependency only in dev envs
            throw IllegalAgentContainerException(
                "Can only dynamically load an agent using a JAR, this agent should only be loaded in development, please see the wiki\n" +
                        "Agent source @ ${agentSourcePath.absolutePathString()}"
            )
        }

        // Load agent on ourselves
        val jvm = VirtualMachine.attach(ManagementFactory.getRuntimeMXBean().pid.toString())
        jvm.loadAgent(agentSourcePath.absolutePathString())
        jvm.detach()
    }
}
