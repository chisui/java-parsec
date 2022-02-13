package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.util.function.Consumer;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

@Value
public class Expect implements Parser<Integer, byte[]>  {
    @NonNull byte[] expected;

    @Override
    public Either<Integer, byte[]> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        int pos = 0;
        Input.Chunk read;
        do {
            read = in.read(expected.length - pos);
            byte[] bytes = read.volatileBytes();
            int size = read.size();
            for (int i = 0; i < size; i++) {
                if (expected[pos + i] != bytes[read.start() + i]) {
                    return Left(i);
                }
            }
            pos += size;
        } while (pos < expected.length);
        return Right(expected);
    }
}
