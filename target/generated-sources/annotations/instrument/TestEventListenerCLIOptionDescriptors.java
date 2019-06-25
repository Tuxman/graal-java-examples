// CheckStyle: start generated
package instrument;

import com.oracle.truffle.api.dsl.GeneratedBy;
import instrument.TestEventListenerCLI;
import java.util.Arrays;
import java.util.Iterator;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionStability;

@GeneratedBy(TestEventListenerCLI.class)
final class TestEventListenerCLIOptionDescriptors implements OptionDescriptors {

    @Override
    public OptionDescriptor get(String optionName) {
        switch (optionName) {
            case "testListener" :
                return OptionDescriptor.newBuilder(TestEventListenerCLI.ENABLED, "testListener").deprecated(false).help("Enable the Test Listener (default: false).").category(OptionCategory.USER).stability(OptionStability.STABLE).build();
            case "testListener.TraceRoots" :
                return OptionDescriptor.newBuilder(TestEventListenerCLI.TRACE_ROOTS, "testListener.TraceRoots").deprecated(false).help("Capture roots when tracing (default:true).").category(OptionCategory.USER).stability(OptionStability.STABLE).build();
        }
        return null;
    }

    @Override
    public Iterator<OptionDescriptor> iterator() {
        return Arrays.asList(
            OptionDescriptor.newBuilder(TestEventListenerCLI.ENABLED, "testListener").deprecated(false).help("Enable the Test Listener (default: false).").category(OptionCategory.USER).stability(OptionStability.STABLE).build(),
            OptionDescriptor.newBuilder(TestEventListenerCLI.TRACE_ROOTS, "testListener.TraceRoots").deprecated(false).help("Capture roots when tracing (default:true).").category(OptionCategory.USER).stability(OptionStability.STABLE).build())
        .iterator();
    }

}
