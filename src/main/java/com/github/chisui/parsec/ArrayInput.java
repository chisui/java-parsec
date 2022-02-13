package com.github.chisui.parsec;

import lombok.RequiredArgsConstructor;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor(staticName = "of")
public class ArrayInput implements Input, Input.Chunk {

    private final byte[] bytes;
    private int start;
    private int end;

    static ArrayInput of(String str) {
        return of(str.getBytes(UTF_8));
    }

    @Override
    public byte[] volatileBytes() {
        return bytes;
    }

    @Override
    public int start() {
        return start;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public boolean isTail() {
        return end == bytes.length;
    }

    @Override
    public Marker mark() {
        int mark = end;
        return () -> {
            end = mark;
            start = mark;
        };
    }

    @Override
    public Chunk read(int size) {
        start = end;
        end = Math.min(end + size, bytes.length);
        return this;
    }

    @Override
    public void close() {
    }
}
