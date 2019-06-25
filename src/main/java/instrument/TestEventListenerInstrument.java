package instrument;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.tools.profiler.impl.ProfilerToolFactory;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Instrument;

import java.util.Objects;

@TruffleInstrument.Registration(id = TestEventListenerInstrument.ID, name = "Test Listener", services = TestEventListenerInstrument.class)
public class TestEventListenerInstrument extends TruffleInstrument{

    public TestEventListenerInstrument() {

    }

    public static final String ID = "testListener";
    private boolean enabled;
    private TestEventListener listener;
    private static ProfilerToolFactory<TestEventListener> factory;



    public static void setFactory(ProfilerToolFactory<TestEventListener> factory) {
        if (factory == null || !factory.getClass().getName().startsWith("com.oracle.truffle.tools.profiler")) {
            throw new IllegalArgumentException("Wrong factory: " + factory);
        }
        TestEventListenerInstrument.factory = factory;
    }

    static {
        // Be sure that the factory is initialized:
        try {
            Class.forName(TestEventListener.class.getName(), true, TestEventListener.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // Can not happen
            throw new AssertionError();
        }
    }


    public static TestEventListener getListener(Engine engine) {
        Instrument instrument = engine.getInstruments().get(ID);
        if (instrument == null) {
            throw new IllegalStateException("Test Listener is not installed.");
        }
        return instrument.lookup(TestEventListener.class);
    }

    @Override
    protected void onCreate(TruffleInstrument.Env env) {


        /*listener = factory.create(env);
        env.registerService(listener);*/

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

    private static void getSourceSectionFilter(Env env) {
        final boolean roots = env.getOptions().get(TestEventListenerCLI.TRACE_ROOTS);
    }
}

