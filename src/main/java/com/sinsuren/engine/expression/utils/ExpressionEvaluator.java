package com.sinsuren.engine.expression.utils;

import com.google.common.base.Joiner;
import com.sinsuren.engine.expression.exception.ExpressionEvaluationException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

    public static Object evaluate(List operands, Map<String, Object> values) throws ExpressionEvaluationException {
        Object response = null;

        Object firstOperand = operands.get(0);

        if (firstOperand instanceof String) {
            if (((String) firstOperand).endsWith("()")) {
                List arguments = operands.size() > 1 ? operands.subList(1, operands.size()) : new ArrayList();
                return evaluateFunction((String) firstOperand, arguments, values);
            }
        }

        for (Object operand : operands) {
            Object calculatedOperand = operand;

            if (operand instanceof List) {
                calculatedOperand = evaluate((List) operand, values);
            }

            response = calculateOperandResponse(calculatedOperand, values, response);

            if (response == null) {
                break;
            }
        }
        return response;
    }

    private static Object calculateOperandResponse(Object operand,
                                                   Map<String, Object> values, Object response) throws ExpressionEvaluationException {

        if (response == null) {
            if (operand instanceof String) {
                String stringOperand = (String) operand;

                if (stringOperand.startsWith("$")) {
                    String variable = stringOperand.substring(1);
                    response = values.get(variable);
                } else {
                    response = operand;
                }
            } else {
                response = operand;
            }
        } else {
            if (response instanceof List) {
                if (operand instanceof Integer) {
                    response = ((List) response).get((Integer) operand);
                } else {
                    throw new ExpressionEvaluationException("Invalid operand - " + operand);
                }
            } else if (response.getClass().isArray()) {
                if (operand instanceof Integer) {
                    response = ((Object[]) response)[(Integer) operand];
                } else {
                    throw new ExpressionEvaluationException("Invalid operand - " + operand);
                }
            } else if (response instanceof Map) {
                Object possibleResponse = ((Map) response).get(operand);
                response = possibleResponse != null ? possibleResponse : ((Map) response).get(String.valueOf(operand));
            } else if (response instanceof String) {
                if (operand instanceof Integer) {
                    response = ((String) response).charAt((Integer) operand);
                } else {
                    throw new ExpressionEvaluationException("Invalid operand - " + operand);
                }
            } else if (response instanceof Integer || response instanceof Double) {
                throw new ExpressionEvaluationException("Invalid operand - " + operand);
            } else if (!(operand instanceof String)) {
                throw new ExpressionEvaluationException("Invalid operand - " + operand);
            } else {
                response = getFieldValue((String) operand, response);
            }
        }

        return response;
    }

    private static Object getFieldValue(String operand, Object response) throws ExpressionEvaluationException {

        Class<?> klass = response.getClass();

        while (klass != null && klass != Object.class) {
            Field[] declaredFields = klass.getDeclaredFields();

            for (Field field : declaredFields) {
                if (field.getName().equals(operand)) {
                    try {
                        Field declaredField = klass.getDeclaredField(operand);

                        declaredField.setAccessible(true);
                        return declaredField.get(response);
                    } catch (IllegalAccessException | NoSuchFieldException ignored) {
                        //ignored
                    }
                }
            }

            klass = klass.getSuperclass();
        }

        throw new ExpressionEvaluationException("No field named '" + operand + "' exists for " + response.getClass().getName());
    }


    private static Object evaluateFunction(String fn, List arguments, Map<String, Object> values) throws ExpressionEvaluationException {
        try {
            String function = fn.substring(0, fn.length() - 2);
            List computedArgs = new ArrayList();

            for (Object arg : arguments) {
                computedArgs.add(evaluate((List) arg, values));
            }

            return findFunction(function, computedArgs).invoke(null, computedArgs.toArray());
        } catch (Throwable e) {
            throw new ExpressionEvaluationException("Unable to evaluate function - " + fn, e);
        }
    }

    private static Method findFunction(String name, List args) throws ExpressionEvaluationException {

        Method[] methods = Functions.class.getDeclaredMethods();

        for (Method method : methods) {
            boolean isVarArgs = method.isVarArgs();

            if (method.getName().equals(name) &&
                    (method.getParameterCount() == args.size() || (isVarArgs && method.getParameterCount() < args.size()))) {
                boolean match = true;

                Class[] types = method.getParameterTypes();

                for (int i = 0; i < args.size(); i++) {
                    int typeIndex = i < types.length ? i : types.length - 1;

                    if (!types[typeIndex].isInstance(args.get(i))) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return method;
                }
            }
        }

        String[] typeNames = new String[args.size()];
        for (int i = 0; i < args.size(); i++) {
            Class type = args.get(i).getClass();

            typeNames[i] = type.getSimpleName();

            if (typeNames[i].equals("")) {
                typeNames[i] = type.getSuperclass().getSimpleName();
            }
        }

        throw new ExpressionEvaluationException("No such function found ::  " + name + "(" + Joiner.on(", ").join(typeNames) + ")");

    }
}
