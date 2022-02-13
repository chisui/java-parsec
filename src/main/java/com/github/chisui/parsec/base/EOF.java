package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.Tuple0;
import io.vavr.control.Either;
import lombok.ToString;

import java.io.IOException;
import java.util.function.Consumer;

import static io.vavr.API.*;

@ToString
public enum EOF implements Parser<Tuple0, Tuple0> {
    INSTANCE;

    @Override
    public Either<Tuple0, Tuple0> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        return in.read(0).isTail()
                ? Right(Tuple())
                : Left(Tuple());
    }
}
