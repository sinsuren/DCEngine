package com.sinsuren.engine.example.employee.callables;

import java.util.concurrent.Callable;

public class EmployeeSalaryService implements Callable {

    private final Object employeeId;

    public EmployeeSalaryService(Object employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
