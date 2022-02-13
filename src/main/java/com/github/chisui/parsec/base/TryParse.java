package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.function.Consumer;

import static io.vavr.API.Right;

@Value
public class TryParse<E, L, R> implements Parser<E, Either<L, R>> {
    @NonNull Parser<L, R> p;

    @Override
    public Either<E, Either<L, R>> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        return in.withMarker(marker -> {
            Either<L, R> res = p.parse(in, trace);
            if (res.isLeft()) {
                marker.rewind();
            }
            return Right(res);
        });
    }
}
