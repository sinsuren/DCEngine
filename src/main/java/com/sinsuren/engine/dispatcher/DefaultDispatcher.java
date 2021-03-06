package com.sinsuren.engine.dispatcher;

import com.sinsuren.engine.composer.Composer;
import com.sinsuren.engine.composer.DefaultComposer;
import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.composer.exception.ComposerInstantiationException;
import com.sinsuren.engine.dispatcher.exception.DispatchFailedException;
import com.sinsuren.engine.task.Task;
import com.sinsuren.engine.task.exception.BadCallableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DefaultDispatcher implements Dispatcher {
    private final ExecutorService executor;
    private final ExecutorCompletionService<Object> completionService;


    public DefaultDispatcher() {
        this(Executors.newCachedThreadPool());
    }

    public DefaultDispatcher(ExecutorService executor) {
        this.executor = executor;
        completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Object context) throws DispatchFailedException, ComposerEvaluationException {
        return execute(params, tasks, context, false);
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Object context, boolean isAlreadyParsed) throws DispatchFailedException, ComposerEvaluationException {
        try {
            DefaultComposer defaultComposer = new DefaultComposer(context, isAlreadyParsed);
            return execute(params, tasks, defaultComposer);
        } catch (ComposerInstantiationException e) {
            throw new DispatchFailedException("Unable to create composer.", e);
        }
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Composer composer) throws DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> responses = dispatchAndCollect(params, tasks);

        List<String> dependencies = composer.getDependencies();
        Map<String, Object> collectedDependencies = collectDependencies(responses, dependencies);
        return composer.compose(collectedDependencies);
    }

    private Map<String, Object> dispatchAndCollect(Map<String, Object> params, Map<String, Task> tasks) throws DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> responses = new HashMap<>();
        List<String> dispatched = new ArrayList<>();
        Map<Future<Object>, String> futures = new HashMap<>();


        responses.putAll(params);

        int remaining = tasks.size();

        while (remaining > 0) {

            for (String key : tasks.keySet()) {
                Task task = tasks.get(key);

                if (!responses.containsKey(key) && !dispatched.contains(key)) {
                    List<String> dependencies = task.getDependencies();

                    Map<String, Object> collectedDependencies = collectDependencies(responses, dependencies);

                    if (collectedDependencies.size() == dependencies.size()) {
                        Future<Object> future = dispatchTask(task, collectedDependencies);
                        dispatched.add(key);
                        futures.put(future, key);
                    }
                }
            }


            if (dispatched.isEmpty()) {
                throw new DispatchFailedException("No possible resolutions of dependencies found");
            }


            try {
                Future future = completionService.take();

                String key = futures.get(future);
                responses.put(key, future.get());
                dispatched.remove(key);
                remaining--;
            } catch (InterruptedException | ExecutionException e) {
                throw new DispatchFailedException("Unable to fetch all required data", e);
            }
        }
        return responses;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }


    private Map<String, Object> collectDependencies(Map<String, Object> responses, List<String> dependencies) {
        Map<String, Object> collectedDependencies = new HashMap<>();
        for (String dependency : dependencies) {
            if (responses.containsKey(dependency)) {
                collectedDependencies.put(dependency, responses.get(dependency));
            }
        }

        return collectedDependencies;
    }

    private Future<Object> dispatchTask(Task task, Map<String, Object> responses) throws DispatchFailedException {
        try {
            Callable<Object> callable = task.getCallable(responses);
            return completionService.submit(callable);
        } catch (BadCallableException e) {
            throw new DispatchFailedException("Failed to dispatch task", e);
        }
    }
}
