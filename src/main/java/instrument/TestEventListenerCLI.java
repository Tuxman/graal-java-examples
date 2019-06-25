package instrument;

import com.oracle.truffle.api.Option;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionStability;

@Option.Group(TestEventListenerInstrument.ID)
class TestEventListenerCLI extends ProfilerCLI {

    @Option(name = "", help = "Enable the Test Listener (default: false).", category = OptionCategory.USER, stability = OptionStability.STABLE) //
    static final OptionKey<Boolean> ENABLED = new OptionKey<>(false);

    @Option(name = "TraceRoots", help = "Capture roots when tracing (default:true).", category = OptionCategory.USER, stability = OptionStability.STABLE) //
    static final OptionKey<Boolean> TRACE_ROOTS = new OptionKey<>(true);
}
