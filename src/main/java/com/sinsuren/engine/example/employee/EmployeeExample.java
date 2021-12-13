package com.sinsuren.engine.example.employee;

import com.sinsuren.engine.composer.Composer;
import com.sinsuren.engine.composer.DefaultComposer;
import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.composer.exception.ComposerInstantiationException;
import com.sinsuren.engine.dispatcher.DefaultDispatcher;
import com.sinsuren.engine.dispatcher.Dispatcher;
import com.sinsuren.engine.dispatcher.exception.DispatchFailedException;
import com.sinsuren.engine.example.employee.callables.*;
import com.sinsuren.engine.expression.DefaultExpression;
import com.sinsuren.engine.expression.Expression;
import com.sinsuren.engine.expression.exception.ExpressionParseException;
import com.sinsuren.engine.task.DefaultMultiTask;
import com.sinsuren.engine.task.DefaultTask;
import com.sinsuren.engine.task.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmployeeExample {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws ComposerInstantiationException, ExpressionParseException, DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> initialParams = getInitialParams();
        Map<String, Task> tasks = getTasks();
        Composer response = getResponseComposer();

        // Passing Optional Parameter (ExecutorService) so we can use the same for MultiTask
        Dispatcher dispatcher = new DefaultDispatcher(executor);
        Object output = dispatcher.execute(initialParams, tasks, response);

        System.out.println(output);
        dispatcher.shutdown();
    }

    private static Map<String, Object> getInitialParams() {
        Map<String, Object> initialParams = new HashMap<>();
        initialParams.put("employeeName", "John Doe");
        return initialParams;
    }

    private static Map<String, Task> getTasks() throws ComposerInstantiationException, ExpressionParseException {
        Map<String, Task> tasks = new HashMap<>();

        Expression expression1 = new DefaultExpression("$employeeName");
        Composer composer1 = new DefaultComposer(expression1, true);
        Task employeeIDTask = new DefaultTask(EmployeeIdentificationService.class, composer1);

        // Short notation for creating expression on the fly
        Composer composer2 = new DefaultComposer("{{$employeeName}}");
        Task joiningDateTask = new DefaultTask(EmployeeInfoService.class, composer2);

        // Short notation for creating composer on the fly
        Task departmentTask = new DefaultTask(EmployeeDepartmentService.class, "{{$employeeID}}");

        Task salaryTask = new DefaultTask(EmployeeSalaryService.class, "{{$employeeID}}");

        Task locationTask = new DefaultTask(EmployeeLocationService.class, "{{$employeeName}}");

        // MultiTask for executing callable once for each value of the provided looping composer
        // Composer can use $__key and $__value while iterating
        // We also need to provide a ExecutorService to MultiTask
        Task latLngTask = new DefaultMultiTask(executor, LatLngService.class, "{{$location}}", "{{$__value}}");

        tasks.put("joiningDate", joiningDateTask);
        tasks.put("salary", salaryTask);
        tasks.put("department", departmentTask);
        tasks.put("employeeID", employeeIDTask);
        tasks.put("location", locationTask);
        tasks.put("latlng", latLngTask);

        return tasks;
    }

    private static Composer getResponseComposer() throws ComposerInstantiationException {
        Map<String, Object> responseContext = new HashMap<>();
        responseContext.put("employeeName", "{{$employeeName}}");
        responseContext.put("employeeID", "{{$employeeID}}");
        responseContext.put("department", "{{$department}}");

        // Optional data - will not fail on null value
        responseContext.put("salary", "{{#$salary}}");

        // Using expressions to extract part of data
        responseContext.put("city", "{{$location.city}}");

        // Using provided data access functions
        responseContext.put("address", "{{$(join, $(values, $location))}}");

        responseContext.put("latlng", "{{$latlng}}");

        // This recursively iterates over the responseContext and parses any expression that it finds.
        return new DefaultComposer(responseContext);
    }
}
