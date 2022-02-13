package com.github.chisui.parsec.base;

import lombok.NonNull;
import lombok.Value;

import java.util.function.Function;

@Value
public class NamedFunction<A, B> implements Function<A, B> {
    @NonNull String name;
    @NonNull Function<A, B> f;

    @SuppressWarnings({
            "unchecked", "rawtypes", // narrow Function
    })
    public static <A, B> NamedFunction<A, B> named(
            @NonNull String name,
            @NonNull Function<? super A, ? extends B> f) {
        return new NamedFunction<>(name, (Function) f);
    }

    private NamedFunction(@NonNull String name, @NonNull Function<A, B> f) {
        this.name = name;
        this.f = f;
    }

    public String toString() {
        return name;
    }

    @Override
    public B apply(A a) {
        return f.apply(a);
    }
}
