package instrument;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CPUSamplerTest {

    @Test
    void json() {

    String script = "function add(a,b) { return a + b }";

    Context context = Context.newBuilder("js")
            .option("cputracer", "true")
            .option("cputracer.TraceStatements", "true")
            .option("cputracer.Output", "JSON")
            .build();

    Value output = context.eval("js", script);

        int result = context.eval("js", "add(2,2)").asInt();

    }

    @Test
    void histogram() {

    String script = "function add(a,b) { return a + b }";

    Context context = Context.newBuilder("js")
            .option("cputracer", "true")
            .option("cputracer.TraceStatements", "true")
            .option("cputracer.Output", "HISTOGRAM")
            .build();

    Value output = context.eval("js", script);

        int result = context.eval("js", "add(2,2)").asInt();
    }
}
