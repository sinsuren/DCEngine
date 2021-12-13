package com.sinsuren.engine.example.employee.callables;

import java.util.concurrent.Callable;

public class EmployeeDepartmentService implements Callable {

    private final Object employeeId;

    public EmployeeDepartmentService(Object employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public Object call() throws Exception {
        return "MyDepartment";
    }
}
