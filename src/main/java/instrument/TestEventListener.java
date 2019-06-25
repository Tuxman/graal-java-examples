package instrument;

import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Env;
import org.graalvm.polyglot.Engine;

import java.io.Closeable;

public class TestEventListener implements Closeable {

    private static final SourceSectionFilter DEFAULT_FILTER = SourceSectionFilter.newBuilder().tagIs(StandardTags.RootTag.class).build();

    TestEventListener(Env env) {
        this.env = env;
    }

    private final TruffleInstrument.Env env;

    private SourceSectionFilter filter = null;

    private boolean closed = false;

    public static TestEventListener find(Engine engine) {
        return TestEventListenerInstrument.getListener(engine);
    }

    public synchronized void setFilter(SourceSectionFilter filter) {
        verifyConfigAllowed();
        this.filter = filter;
    }



    private synchronized void verifyConfigAllowed() {
        assert Thread.holdsLock(this);
        if (closed) {
            throw new IllegalStateException("CPUTracer is already closed.");
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
    }
}
