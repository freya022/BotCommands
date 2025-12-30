package dev.freya02.botcommands.method.accessors.internal;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MethodAccessorContinuation extends ContinuationImpl {
    private final MethodAccessor<?> methodAccessor;

    @Nullable
    public Object result;
    public int label;

    public MethodAccessorContinuation(@Nullable Continuation<Object> completion, MethodAccessor<?> methodAccessor) {
        super(completion);
        this.methodAccessor = methodAccessor;
    }

    @SuppressWarnings("unused") // dynamic call
    public boolean isResumeLabel() {
        return (label & Integer.MIN_VALUE) != 0;
    }

    @Nullable
    @Override
    @SuppressWarnings("DataFlowIssue") // The next continuation label does not need any data, it will only return the result
    protected Object invokeSuspend(Object result) {
        this.result = result;
        this.label |= Integer.MIN_VALUE;
        return methodAccessor.callSuspend(null, this);
    }
}
