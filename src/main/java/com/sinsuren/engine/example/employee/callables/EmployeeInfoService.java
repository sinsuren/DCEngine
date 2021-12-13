package com.sinsuren.engine.example.employee.callables;

import java.util.concurrent.Callable;

public class EmployeeInfoService implements Callable {

    private final Object employeeName;

    public EmployeeInfoService(Object employeeName) {
        this.employeeName = employeeName;
    }

    @Override
    public Object call() throws Exception {
        return "1/1/1970";
    }
}
