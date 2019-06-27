package instrument;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import org.graalvm.options.OptionDescriptors;

import java.util.Objects;

@TruffleInstrument.Registration(id = TestEventListenerInstrument.ID, name = "Test Listener", services = TestEventListenerInstrument.class)
public class TestEventListenerInstrument extends TruffleInstrument{

    public TestEventListenerInstrument() {

    }

    static final String ID = "testListener";

    @Override
    protected void onCreate(TruffleInstrument.Env env) {

        System.out.println("onCreate");
        env.registerService(this);
        env.getInstrumenter().attachExecutionEventListener(
                SourceSectionFilter.newBuilder()
                        .tagIs(StandardTags.RootTag.class, StandardTags.CallTag.class, StandardTags.StatementTag.class).build(),
                new ExecutionEventListener() {

                    @Override
                    public void onEnter(EventContext context, VirtualFrame frame) {
                        System.out.println("onEnter");
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


    protected OptionDescriptors getOptionDescriptors() {
        return new TestEventListenerCLIOptionDescriptors();
    }
}

