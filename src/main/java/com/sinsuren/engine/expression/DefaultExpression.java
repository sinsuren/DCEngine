package com.sinsuren.engine.expression;

import com.sinsuren.engine.expression.exception.ExpressionEvaluationException;
import com.sinsuren.engine.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sinsuren.engine.expression.utils.ExpressionEvaluator.evaluate;
import static com.sinsuren.engine.expression.utils.ExpressionParser.parse;

public class DefaultExpression implements Expression {

    private final String expression;
    private final List operands;
    private final List<String> dependencies;
    private final boolean isOptional;

    public DefaultExpression(String expression) throws ExpressionParseException {
        expression = expression.replaceAll("\\s", "");

        this.isOptional = expression.startsWith("#");
        this.expression = expression.substring(isOptional ? 1 : 0);

        this.operands = parse(this.expression);
        this.dependencies = findDependencies(operands);

    }

    @Override
    public Object calculate(Map<String, Object> values) throws ExpressionEvaluationException {
        Object value = evaluate(operands, values);

        if (!isOptional && value == null) {
            throw new ExpressionEvaluationException("Null value found for non-optional expression - " + expression);
        }
        return value;
    }


    private List<String> findDependencies(List operands) {

        List<String> dependencies = new ArrayList<>();

        for (Object value : operands) {

            if (value instanceof List) {
                dependencies.addAll(findDependencies((List) value));
            } else if (value instanceof String) {
                String stringValue = (String) value;

                if (stringValue.startsWith("$") && !stringValue.startsWith("$__")) {
                    dependencies.add(stringValue.substring(1));
                }
            }
        }

        return dependencies;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }
}
