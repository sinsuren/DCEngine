package com.sinsuren.engine.composer.utils;

import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.expression.Expression;
import com.sinsuren.engine.expression.exception.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposerEvaluator {

    public static Object evaluate(Object context, Map<String, Object> values) throws ComposerEvaluationException {
        if (context instanceof Expression) {
            try {
                return ((Expression) context).calculate(values);
            } catch (ExpressionEvaluationException e) {
                throw new ComposerEvaluationException("Unable to evaluate composer. ", e);
            }
        }

        if (context instanceof Map) {
            Map mapContext = (Map) context;

            Map<Object, Object> newMapContext = new HashMap<>();

            for (Object key : mapContext.keySet()) {
                newMapContext.put(evaluate(key, values), evaluate(mapContext.get(key), values));
            }

            context = newMapContext;
        } else if (context instanceof List) {
            List listContext = (List) context;

            List<Object> newListContext = new ArrayList<>();

            for (Object value : listContext) {
                newListContext.add(evaluate(value, values));
            }

            context = newListContext;
        }

        return context;
    }
}
