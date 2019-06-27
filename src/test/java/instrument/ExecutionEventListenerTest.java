package instrument;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ExecutionEventListenerTest {

    @Test
    void listen() {

        String script = "function add(a,b) { return a + b }";

        Context context = Context.newBuilder("js")
                .option("testListener", "true")
                .build();

        context.getEngine().getInstruments().get("testListener").lookup(Object.class);

        String instruments = context.getEngine().getInstruments().keySet().toString();

        System.out.println("name: " + context.getEngine().getInstruments().get("testListener").getName());

        System.out.println(instruments);

        context.eval("js", script);

        int result = context.eval("js", "add(2,2)").asInt();

        Assertions.assertEquals(42, result);
    }
}
