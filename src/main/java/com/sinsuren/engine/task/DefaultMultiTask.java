package com.sinsuren.engine.task;

import com.sinsuren.engine.composer.Composer;
import com.sinsuren.engine.composer.DefaultComposer;
import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.composer.exception.ComposerInstantiationException;
import com.sinsuren.engine.task.entities.WrapperCallable;
import com.sinsuren.engine.task.exception.BadCallableException;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class DefaultMultiTask extends DefaultTask {
    protected final Composer loopComposer;
    protected final ExecutorService executor;

    public DefaultMultiTask(ExecutorService executor, Class<? extends Callable> callableClass, Object loopOverContext, Object context) throws ComposerInstantiationException {
        this(executor, callableClass, context, loopOverContext, false);
    }

    public DefaultMultiTask(ExecutorService executor, Class<? extends Callable> callableClass, Object context, Object loopOverContext, boolean isAlreadyParsed) throws ComposerInstantiationException {
        this(executor, callableClass, new DefaultComposer(context, isAlreadyParsed), new DefaultComposer(loopOverContext, isAlreadyParsed));
    }

    public DefaultMultiTask(ExecutorService executor, Class<? extends Callable> callableClass, Composer composer, Composer loopComposer) {
        super(callableClass, composer);
        this.executor = executor;
        this.loopComposer = loopComposer;
    }


    @Override
    public Callable<Object> getCallable(Map<String, Object> values) throws BadCallableException {
        if (loopComposer == null) {
            return super.getCallable(values);
        } else {
            try {
                return new WrapperCallable(executor, callableClass, loopComposer, composer, values);
            } catch (NoSuchMethodException | ComposerEvaluationException e) {
                throw new BadCallableException("Unable to execute callable", e);
            }
        }
    }


    @Override
    public List<String> getDependencies() {
        Set<String> dependencies = new HashSet<>(super.getDependencies());
        if (loopComposer != null) {
            dependencies.addAll(loopComposer.getDependencies());
        }

        return new ArrayList<>(dependencies);
    }

}
