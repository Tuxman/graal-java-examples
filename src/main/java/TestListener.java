import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.tools.profiler.CPUTracer;

import java.util.Objects;

@TruffleInstrument.Registration(id = "testListener", name = "Test Listener", services = TestListener.class)
public class TestListener extends TruffleInstrument{

    public TestListener() {

    }

    private boolean enabled;

    @Override
    protected void onCreate(TruffleInstrument.Env env) {
        System.out.println("onCreate");
        env.registerService(this);
        env.getInstrumenter().attachExecutionEventListener(
                SourceSectionFilter.newBuilder()
                        .tagIs(StandardTags.RootTag.class).build(),
                new ExecutionEventListener() {
                    @Override
                    public void onEnter(EventContext context, VirtualFrame frame) {
                        System.out.print("onEnter");
                    }

                    @Override
                    public void onReturnValue(EventContext context, VirtualFrame frame, Object result) {
                        System.out.println("onReturnValue");
                        if(!Objects.equals(result, 42)) {
                            CompilerDirectives.transferToInterpreter();
                            throw context.createUnwind(42);
                        }
                    }

                    @Override
                    public void onReturnExceptional(EventContext context, VirtualFrame frame, Throwable exception) {}

                    @Override
                    public Object onUnwind(EventContext context, VirtualFrame frame, Object info) {
                        return info;
                    }
                });
    }
}

