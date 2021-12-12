package com.sinsuren.engine.task.helper;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

public class ReflectionHelper {

    public static Constructor<? extends Callable> getFirstSingleArgConstructor(Class<? extends Callable> callableClass) throws NoSuchMethodException {
        Constructor<?>[] declaredConstructors = callableClass.getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1) {
                return constructor;
            }
        }

        throw new NoSuchMethodException("Unable to find a single argument constructor.");
    }
}
