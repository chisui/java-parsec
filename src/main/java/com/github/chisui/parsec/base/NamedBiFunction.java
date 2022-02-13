package com.github.chisui.parsec.base;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.function.BiFunction;

@Value
@RequiredArgsConstructor(staticName = "named2")
public class NamedBiFunction<A, B, C> implements BiFunction<A, B, C> {
    @NonNull String name;
    @NonNull BiFunction<A, B, C> f;

    @Override
    public C apply(A a, B b) {
        return f.apply(a, b);
    }

    public String toString() {
        return name;
    }
}
