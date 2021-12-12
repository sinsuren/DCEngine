package com.sinsuren.engine.expression;

import com.sinsuren.engine.expression.exception.ExpressionEvaluationException;

import java.util.List;
import java.util.Map;

public interface Expression {
    Object calculate(Map<String, Object> values) throws ExpressionEvaluationException;

    List<String> getDependencies();

}
