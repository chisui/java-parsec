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
public enum AnyByte implements Parser<Tuple0, Byte> {
    INSTANCE;

    @Override
    public Either<Tuple0, Byte> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        Input.Chunk read = in.read(1);
        if (read.size() != 1) {
            return Left(Tuple());
        } else {
            return Right(read.volatileBytes()[read.start()]);
        }
    }
}
