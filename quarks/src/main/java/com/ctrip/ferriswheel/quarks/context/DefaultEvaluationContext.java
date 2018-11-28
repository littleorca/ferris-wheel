package com.ctrip.ferriswheel.quarks.context;

import com.ctrip.ferriswheel.quarks.EvaluationContext;

import java.util.HashMap;
import java.util.Map;

public class DefaultEvaluationContext<T> implements EvaluationContext<T> {

    private Map<String, T> _map = new HashMap<>();

    @Override
    public boolean contains(String token) {
        return _map.containsKey(token);
    }

    @Override
    public T get(String token) {
        return _map.get(token);
    }

    @Override
    public void put(String token, T value) {
        _map.put(token, value);
    }

}
