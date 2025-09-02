package dev.freya02.botcommands.method.accessors.internal

interface MethodAccessor<R> {

    /**
     * `true` if this method requires an instance parameter
     *
     * This is mostly used to offset the argument indexes in [MethodArguments],
     * as the instance is already inserted by the accessor
     */
    fun hasInstance(): Boolean

    suspend fun callSuspend(args: MethodArguments): R

    fun call(args: MethodArguments): R

    fun createBlankArguments(): MethodArguments
}
