package com.sinsuren.engine.composer;

import com.sinsuren.engine.composer.exception.ComposerEvaluationException;
import com.sinsuren.engine.composer.exception.ComposerInstantiationException;
import com.sinsuren.engine.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sinsuren.engine.composer.utils.ComposerEvaluator.evaluate;
import static com.sinsuren.engine.composer.utils.ComposerParser.parse;

public class DefaultComposer implements Composer {

    private final Object context;
    private final List<String> dependencies;

    public DefaultComposer(Object context) throws ComposerInstantiationException {
        this(context, false);
    }


    public DefaultComposer(Object context, boolean isAlreadyParsed) throws ComposerInstantiationException {
        this.context = isAlreadyParsed ? context : parse(context);
        this.dependencies = findDependencies(this.context);
    }

    @Override
    public Object compose(Map<String, Object> values) throws ComposerEvaluationException {
        return evaluate(context, values);
    }

    @Override
    public List<String> getDependencies() {
        return this.dependencies;
    }


    private List<String> findDependencies(Object context) {
        List<String> dependencies = new ArrayList<>();

        if (context instanceof Expression) {
            dependencies.addAll(((Expression) context).getDependencies());
        } else if (context instanceof Map) {
            Map mapContext = (Map) context;

            for (Object value : mapContext.values()) {
                dependencies.addAll(findDependencies(value));
            }
        } else if (context instanceof List) {
            List listContext = (List) context;

            for (Object value : listContext) {
                dependencies.addAll(findDependencies(value));
            }
        }

        return dependencies;
    }

}
