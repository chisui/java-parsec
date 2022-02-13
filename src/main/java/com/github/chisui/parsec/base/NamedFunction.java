package com.github.chisui.parsec.base;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Delegate;

import java.util.function.Function;

@Value
@AllArgsConstructor(staticName = "named")
public class NamedFunction<A, B> implements Function<A, B> {
    @NonNull String name;
    @NonNull @Delegate Function<A, B> f;

    public String toString() {
        return name;
    }
}
