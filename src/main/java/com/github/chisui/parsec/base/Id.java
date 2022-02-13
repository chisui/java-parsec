package com.github.chisui.parsec.base;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@Value
@NoArgsConstructor(access = PRIVATE)
public class Id<A> implements Function<A, A> {

    private static final Function<?, ?> id = new Id<>();

    @SuppressWarnings({
            "unchecked", // Todd Howard approves
    })
    public static <A> Function<A, A> id() {
        return (Function<A, A>) id;
    }

    @Override
    public A apply(A a) {
        return a;
    }

    @Override
    @SuppressWarnings({
            "unchecked", // it just works
    })
    public <V> Function<V, A> compose(
            @NonNull Function<? super V, ? extends A> f) {
        return (Function<V, A>) f;
    }

    @Override
    @SuppressWarnings({
            "unchecked", // it just works
    })
    public <V> Function<A, V> andThen(
            @NonNull Function<? super A, ? extends V> f) {
        return (Function<A, V>) f;
    }

    public String toString() {
        return "id";
    }

    @Override
    protected Id<A> clone() {
        return this;
    }
}
