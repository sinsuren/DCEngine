package com.sinsuren.engine.task;

import com.sinsuren.engine.composer.Composer;
import com.sinsuren.engine.composer.DefaultComposer;
import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.composer.exception.ComposerInstantiationException;
import com.sinsuren.engine.task.exception.BadCallableException;
import com.sinsuren.engine.task.helper.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class DefaultTask implements Task {
    protected final Class<? extends Callable> callableClass;
    protected final Composer composer;

    public DefaultTask(Class<? extends Callable> callableClass, Composer composer) {
        this.callableClass = callableClass;
        this.composer = composer;
    }

    public DefaultTask(Class<? extends Callable> callableClass, Object context) throws ComposerInstantiationException {
        this(callableClass, context, false);
    }

    public DefaultTask(Class<? extends Callable> callableClass, Object context, boolean isAlreadyParsed) throws ComposerInstantiationException {
        this.callableClass = callableClass;
        this.composer = new DefaultComposer(context, isAlreadyParsed);
    }


    @Override
    public Callable<Object> getCallable(Map<String, Object> values) throws BadCallableException {
        try {
            Constructor<? extends Callable> constructor = ReflectionHelper.getFirstSingleArgConstructor(callableClass);
            return (Callable<Object>) constructor.newInstance(composer.compose(values));
        } catch (InvocationTargetException | InstantiationException | NoSuchMethodException
                | IllegalAccessException | ComposerEvaluationException e) {
            throw new BadCallableException("Unable to execute callable", e);
        }
    }

    @Override
    public List<String> getDependencies() {
        return composer.getDependencies();
    }
}
