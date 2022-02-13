package com.github.chisui.parsec;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.nio.charset.Charset;
import java.util.stream.Collector;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Bytes {

    public static Collector<Byte, ?, byte[]> bytes() {
        return collectingAndThen(toList(), it -> {
            byte[] bytes = new byte[it.size()];
            int i = 0;
            for (Byte b : it) {
                bytes[i++] = b;
            }
            return bytes;
        });
    }

    public static Collector<Byte, ?, String> asString(@NonNull Charset charset) {
        return collectingAndThen(bytes(), b -> new String(b, charset));
    }
}
