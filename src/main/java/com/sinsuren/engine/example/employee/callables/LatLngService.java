package com.sinsuren.engine.example.employee.callables;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class LatLngService implements Callable {

    private final Object location;

    public LatLngService(Object location) {
        this.location = location;
    }

    @Override
    public Object call() throws Exception {
        return Arrays.asList(-90 + Math.random() * 180, -180 + Math.random() * 360);
    }
}
