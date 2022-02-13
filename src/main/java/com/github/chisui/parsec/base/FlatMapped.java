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
public class FlatMapped<E, F, R, S> implements Parser<F, S> {
    @NonNull Parser<E, R> p;
    @NonNull Function<E, Parser<F, S>> f;
    @NonNull Function<R, Parser<F, S>> g;

    @Override
    public Either<F, S> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        return p.parse(in, trace).fold(f, g).parse(in, trace);
    }

    public String toString() {
        return p + ".biFlatMap(" + f + ", " + g + ")";
    }
}
