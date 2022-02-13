package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collector;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

@Value
public class OneOrMore<E, A, R, S> implements Parser<E, S> {
    @NonNull Parser<E, R> p;
    @NonNull Collector<R, A, S> col;

    @Override
    public Either<E, S> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        A out = col.supplier().get();
        Either<E, R> res = p.parse(in, trace);
        BiConsumer<A, R> accum = col.accumulator();
        if (res.isLeft()) {
            return Left(res.getLeft());
        } else {
            accum.accept(out, res.get());
        }
        while (true) {
            try (Input.Marker m = in.mark()) {
                res = p.parse(in, trace);
                if (res.isLeft()) {
                    m.rewind();
                    break;
                } else {
                    accum.accept(out, res.get());
                }
            }
        }
        return Right(col.finisher().apply(out));
    }

    public String toString() {
        return "oneOrMore(" + p + ")";
    }
}
