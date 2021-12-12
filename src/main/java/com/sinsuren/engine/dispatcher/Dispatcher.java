package com.sinsuren.engine.dispatcher;

import com.sinsuren.engine.composer.Composer;
import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.dispatcher.exception.DispatchFailedException;
import com.sinsuren.engine.task.Task;

import java.util.Map;

public interface Dispatcher {

    Object execute(Map<String, Object> params, Map<String, Task> tasks, Object context) throws DispatchFailedException, ComposerEvaluationException;

    Object execute(Map<String, Object> params, Map<String, Task> tasks, Object context, boolean isAlreadyParsed) throws DispatchFailedException, ComposerEvaluationException;

    Object execute(Map<String, Object> params, Map<String, Task> tasks, Composer composer) throws DispatchFailedException, ComposerEvaluationException;

    void shutdown();
}
