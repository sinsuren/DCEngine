
package com.sinsuren.engine.example.employee.callables;

import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

public class EmployeeLocationService implements Callable {

    private final Object employeeId;

    public EmployeeLocationService(Object employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public Object call() throws Exception {
        return new LinkedHashMap<String, String>() {{
            put("city", "Bangalore");
            put("state", "Karnataka");
            put("country", "India");
        }};
    }
}
