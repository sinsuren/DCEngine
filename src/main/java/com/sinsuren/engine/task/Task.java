package com.sinsuren.engine.task;

import com.sinsuren.engine.task.exception.BadCallableException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface Task {

    Callable<Object> getCallable(Map<String, Object> values) throws BadCallableException;

    List<String> getDependencies();
}
