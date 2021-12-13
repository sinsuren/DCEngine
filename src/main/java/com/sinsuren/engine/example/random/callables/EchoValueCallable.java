package com.sinsuren.engine.example.random.callables;

import java.util.Map;
import java.util.concurrent.Callable;

public class EchoValueCallable implements Callable {

    private final Map<String, Object> values;

    public EchoValueCallable(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public Object call() throws Exception {
        return values.get("value");
    }
}
