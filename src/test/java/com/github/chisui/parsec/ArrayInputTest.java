package com.github.chisui.parsec;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayInputTest {

    @Test
    void testReadSize() {
        ArrayInput in = ArrayInput.of("asdf");
        Input.Chunk read = in.read(2);

        assertThat(read.size()).isEqualTo(2);
        assertThat(read.copy()).asString().isEqualTo("as");
    }

    @Test
    void testReadMovesPointer() {
        ArrayInput in = ArrayInput.of("asdfg");

        assertThat(in.read(2).copy()).asString().isEqualTo("as");
        assertThat(in.read(2).copy()).asString().isEqualTo("df");
        assertThat(in.read(2).copy()).asString().isEqualTo("g");
    }

    @Test
    void testMarkerRewind() throws IOException {
        ArrayInput in = ArrayInput.of("asdf");
        in.read(1);

        Input.Marker mark = in.mark();

        in.read(2);

        mark.rewind();

        assertThat(in.read(2).copy()).asString().isEqualTo("sd");
    }
}