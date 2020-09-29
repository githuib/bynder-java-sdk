package com.bynder.sdk.util;

public class Indexed<T> {

    private final int index;
    private final T value;

    public Indexed(T value, int index) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public T getValue() {
        return value;
    }

}