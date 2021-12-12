package com.sinsuren.engine.task.entities;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sinsuren.engine.composer.Composer;
import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.task.exception.BadCallableException;
import com.sinsuren.engine.task.helper.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class WrapperCallable implements Callable<Object> {


    protected final ListeningExecutorService executorService;
    protected final Constructor<? extends Callable> constructor;
    protected final Object loopVar;
    protected final Composer composer;
    protected final Map<String, Object> values;

    public WrapperCallable(ExecutorService executor, Class<? extends Callable> callableClass, Composer loopComposer, Composer composer, Map<String, Object> values) throws NoSuchMethodException, ComposerEvaluationException {
        this(MoreExecutors.listeningDecorator(executor),
                ReflectionHelper.getFirstSingleArgConstructor(callableClass),
                loopComposer.compose(values),
                composer,
                values);
    }

    public WrapperCallable(ListeningExecutorService executorService, Constructor<? extends Callable> constructor, Object loopVar, Composer composer, Map<String, Object> values) {
        this.executorService = executorService;
        this.constructor = constructor;
        this.loopVar = loopVar;
        this.composer = composer;
        this.values = values;
    }


    @Override
    public Object call() throws Exception {
        try {
            if (loopVar instanceof Map) {
                return getResponsesForMap();
            } else if (loopVar instanceof List) {
                return getResponsesForList();
            } else if (loopVar.getClass().isArray()) {
                return getResponsesForArray();
            }
        } catch (Throwable e) {
            throw new BadCallableException("Unable to execute callable ", e);
        }

        throw new BadCallableException("Loop variable is ont iterable");
    }


    protected Object getResponsesForMap() throws Exception {
        Map iterable = (Map) loopVar;

        Map<Object, ListenableFuture<Object>> futureMap = new HashMap<>();

        for (Object o : iterable.keySet()) {
            ListenableFuture<Object> future = getFuture(o, iterable.get(o));
            futureMap.put(o, future);
        }

        ListenableFuture<List<Object>> compositeFuture = Futures.allAsList(futureMap.values());

        compositeFuture.get();

        Map<Object, Object> responseMap = new HashMap<>();

        for (Object i : iterable.keySet()) {
            responseMap.put(i, futureMap.get(i).get());
        }

        return responseMap;
    }

    protected Object getResponsesForList() throws Exception {
        List list = (List) loopVar;

        Map<Integer, ListenableFuture<Object>> futureMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            ListenableFuture<Object> future = getFuture(i, list.get(i));
            futureMap.put(i, future);
        }

        ListenableFuture<List<Object>> compositeFuture = Futures.allAsList(futureMap.values());
        compositeFuture.get();

        List<Object> responsesList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            responsesList.add(futureMap.get(i).get());
        }

        return responsesList;
    }


    protected Object getResponsesForArray() throws Exception {
        Object[] arr = (Object[]) loopVar;

        Map<Integer, ListenableFuture<Object>> futureMap = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            ListenableFuture<Object> future = getFuture(i, arr[i]);
            futureMap.put(i, future);
        }

        ListenableFuture<List<Object>> compositeFuture = Futures.allAsList(futureMap.values());
        compositeFuture.get();

        Object[] responsesArray = new Object[arr.length];
        for (int i = 0; i < arr.length; i++) {
            responsesArray[i] = futureMap.get(i).get();
        }

        return responsesArray;
    }


    protected ListenableFuture<Object> getFuture(Object key, Object value) throws Exception {
        Map<String, Object> request = new HashMap<>(values);

        request.put("__key", key);
        request.put("__value", value);
        Callable<Object> callable = (Callable<Object>) constructor.newInstance(composer.compose(request));

        return executorService.submit(callable);
    }
}
