package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Collector;

import static io.vavr.API.Right;

@Value
public class ZeroOrMore<X, E, A, R, S> implements Parser<X, S> {
    @NonNull Parser<E, R> p;
    @NonNull Collector<R, A, S> col;

    @Override
    public Either<X, S> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        A out = col.supplier().get();
        while (true) {
            try (Input.Marker m = in.mark()) {
                Either<E, R> res = p.parse(in, trace);
                if (res.isLeft()) {
                    m.rewind();
                    break;
                } else {
                    col.accumulator().accept(out, res.get());
                }
            }
        }
        return Right(col.finisher().apply(out));
    }

    public String toString() {
        return "zeroOrMore(" + p + ")";
    }
}
