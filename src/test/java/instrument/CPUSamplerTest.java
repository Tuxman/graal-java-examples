package instrument;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;

class CPUSamplerTest {

    @Test
    void json() {

        String script = "function add(a,b) { return a + b }";

        Context context = Context.newBuilder("js")
            .option("cputracer", "true")
            .option("cputracer.Output", "JSON")
            .build();

        context.eval("js", script);

        context.eval("js", "add(2,2)");
    }

    @Test
    void histogram() {

        String script = "function add(a,b) { return a + b }";

        Context context = Context.newBuilder("js")
            .option("cputracer", "true")
            .option("cputracer.Output", "HISTOGRAM")
            .build();

        context.eval("js", script);

        context.eval("js", "add(2,2)");
    }
}
