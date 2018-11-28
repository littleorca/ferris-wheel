package com.ctrip.ferriswheel.quarks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * @author sqwen
 *
 */
public class FunctionRegistry implements FunctionHolderInternal {

    private Map<String, Function<?>> _func_map;
    
    private Map<String, Function<?>> _kernel_map;

    public FunctionRegistry() {
        _func_map = new HashMap<>();
        _kernel_map = new HashMap<>();
    }
    
    /**
     * 
     * @param name
     * @return
     */
    @Override
    public Function<?> getFunction(String name) {
        Function<?> fun = _kernel_map.get(name);
        if(fun == null) {
            fun = _func_map.get(name);
        }
        return fun;
    }
    
    /**
     * register function
     * 
     * @param fun
     */
    @Override
    public void registerFunction(Function<?> fun) {
        this.registerFunction(fun.getName(), fun);
    }

    @Override
    public void registerFunction(String name, Function<?> fun) {
        //check name
        for(int i=0, len = name.length(); i<len; i++) {
            int ch = name.charAt(i);
            if(i == 0 && (ch >= 48 && ch <= 57)) {
                throw new IllegalArgumentException("Function '" + name + "' name is illegal which is start with number.");
            } else if(ch == 95 || (ch >= 65 && ch <= 89) || (ch >= 97 && ch <= 122) || (ch >= 48 && ch <= 57)) {
                continue;
            } else {
                throw new IllegalArgumentException("Function '" + name + "' is illegal at col " + i + " with char '" + (char)ch + "'");
            }
        }
        if(_kernel_map.containsKey(name)) {
            throw new IllegalArgumentException("Function '" + name + "'already exists in kernel with function class " + fun.getClass());
        }
        
        if(_func_map.containsKey(name)) {
            throw new IllegalArgumentException("Function '" + name + "'already exists with function class " + fun.getClass());
        }
        _func_map.put(name, fun);
    }
    
    
    @Override
    public Function<?> unregisterFunction(String name) {
        return _func_map.remove(name);
    }
    
    @Override
    public Function<?> unregisterFunction(Function<?> fun) {
        return unregisterFunction(fun.getName());
    }

    @Override
    public void registerKernelFunction(Function<?> fun) {
        String name = fun.getName();
        if(_kernel_map.containsKey(name)) {
            throw new IllegalArgumentException("Function '" + name + "'already exists in kernel with function class " + fun.getClass());
        }
        _kernel_map.put(name, fun);
    }

    @Override
    public Set<String> getKernelFunctionNames() {
        return _kernel_map.keySet();
    }

    @Override
    public Set<String> getFunctionNames() {
        return _func_map.keySet();
    }
}
