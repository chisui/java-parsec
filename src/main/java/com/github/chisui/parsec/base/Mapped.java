package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

@Value
public class Mapped<E, F, R, S> implements Parser<F, S> {
    @NonNull Parser<E, R> p;
    @NonNull Function<R, S> f;
    @NonNull Function<E, F> g;

    @Override
    public Either<F, S> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        return p.parse(in, trace).bimap(g, f);
    }

    @Override
    public <T> Parser<F, T> map(@NonNull Function<? super S, ? extends T> h) {
        return new Mapped<>(p, f.andThen(h), g);
    }

    @Override
    public <G> Parser<G, S> mapErr(@NonNull Function<? super F, ? extends G> h) {
        return new Mapped<>(p, f, g.andThen(h));
    }

    public String toString() {
        return p + ".bimap(" + f + ", " + g + ")";
    }
}
