package com.sinsuren.engine.composer;

import com.sinsuren.engine.composer.exception.ComposerEvaluationException;

import java.util.List;
import java.util.Map;

public interface Composer {
    Object compose(Map<String, Object> values) throws ComposerEvaluationException;

    List<String> getDependencies();
}
