package com.bruin.type;

/**
 * @description:
 * @author: xiongwenwen   2019/12/31 15:30
 */
public class Generic<T> {
    private T key;

    public <T> T genericMethod(Class<T> tClass) throws IllegalAccessException, InstantiationException {
        T instance = tClass.newInstance();

        return instance;
    }

    public Generic(T key){
        this.key = key;
    }

    public T getKey(){
        return key;
    }
}
