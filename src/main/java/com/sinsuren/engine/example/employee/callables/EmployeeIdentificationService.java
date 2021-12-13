
package com.sinsuren.engine.example.employee.callables;

import java.util.concurrent.Callable;

public class EmployeeIdentificationService implements Callable {

    private final Object employeeName;

    public EmployeeIdentificationService(Object employeeName) {
        this.employeeName = employeeName;
    }

    @Override
    public Object call() throws Exception {
        return 1;
    }
}
