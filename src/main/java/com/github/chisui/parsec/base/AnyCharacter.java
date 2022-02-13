package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.function.Consumer;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

@Value
public class AnyCharacter implements Parser<byte[], java.lang.Character> {
    @NonNull Charset charset;

    public static Parser<byte[], java.lang.Character> of(Charset charset) {
        return new AnyCharacter(charset);
    }

    @Override
    public Either<byte[], java.lang.Character> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        CharsetDecoder decoder = charset.newDecoder();
        ByteBuffer buf = ByteBuffer.allocate(8);
        CharBuffer out = CharBuffer.allocate(8);
        CoderResult res;
        int pos = 0;
        do {
            buf.position(pos++);
            buf.limit(buf.capacity());
            Input.Chunk read = in.read(1);
            if (read.size() < 1) {
                return Left(buf.array());
            }
            buf.put(read.volatileBytes()[read.start()]);
            buf.position(0);
            buf.limit(pos);
            res = decoder.decode(buf, out, true);
        } while (out.position() < 1);
        if (res.isError()) {
            return Left(buf.array());
        } else {
            out.position(0);
            return Right(out.charAt(0));
        }
    }

    public String toString() {
        return "anyChar(" + charset + ")";
    }
}
