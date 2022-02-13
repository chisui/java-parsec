package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.function.Consumer;

@Value
public class Not<E, R> implements Parser<R, E> {
    @NonNull Parser<E, R> p;

    @Override
    public Either<R, E> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        return p.parse(in, trace)
                .fold(Either::right, Either::left);
    }

    @Override
    public Parser<E, R> negate() {
        return p;
    }

    public String toString() {
        return "not(" + p + ")";
    }
}
