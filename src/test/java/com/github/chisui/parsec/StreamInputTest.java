package com.github.chisui.parsec;

import com.github.chisui.parsec.base.NamedBiFunction;
import com.github.chisui.parsec.base.NamedFunction;
import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.collection.Traversable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

import static com.github.chisui.parsec.Parser.*;
import static com.github.chisui.parsec.base.NamedBiFunction.named2;
import static com.github.chisui.parsec.base.NamedFunction.named;
import static io.vavr.API.List;
import static io.vavr.API.Right;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class StreamInputTest {

    @Test
    void testReadIntoInitialWindow() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 15);

        assertThat(si.read(8).copy()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
        assertThat(si.read(8).copy()).containsExactly(8, 9, 10, 11, 12, 13, 14, 15);
    }

    @Test
    void testReadRequestExceedsChunkSize() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 15);

        assertThat(si.read(20).copy())
                .hasSize(16)
                .containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    }

    @Test
    void testReadIntoNextWindow() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 15);

        assertThat(si.read(20).copy())
                .hasSize(16)
                .containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        assertThat(si.read(4).copy())
                .hasSize(4)
                .containsExactly(16, 17, 18, 19);
    }

    @Test
    void testReadAllWindows() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Input.Chunk chunk = si.read(16);
        while (!chunk.isTail()) {
            out.write(chunk.copy());
            chunk = si.read(16);
        }

        assertThat(out.toByteArray()).containsExactly(byteArray());
    }

    @Test
    void testMarkStartAndRewindInInitialWindow() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 2);

        Input.Marker mark = si.mark();
        assertThat(si.read(8).copy()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
        mark.rewind();
        assertThat(si.read(8).copy()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
    }


    @Test
    void testMarkStartAndRewindInSecondWindow() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 2);

        Input.Marker mark = si.mark();
        assertThat(si.read(16).copy())
                .containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        assertThat(si.read(4).copy())
                .containsExactly(16, 17, 18, 19);

        mark.rewind();
        assertThat(si.read(8).copy()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    void testMarkOffsetAndRewindInInitialWindow() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 2);

        assertThat(si.read(8).copy()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
        Input.Marker mark = si.mark();
        assertThat(si.read(8).copy()).containsExactly(8, 9, 10, 11, 12, 13, 14, 15);

        mark.rewind();
        assertThat(si.read(8).copy()).containsExactly(8, 9, 10, 11, 12, 13, 14, 15);
    }

    @Test
    void testMarkOffsetAndRewindInSecondWindow() throws Exception {
        StreamInput si = StreamInput.of(bytes(), 4, 16, 2);

        assertThat(si.read(8).copy()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
        Input.Marker mark = si.mark();
        assertThat(si.read(8).copy()).containsExactly(8, 9, 10, 11, 12, 13, 14, 15);
        assertThat(si.read(4).copy())
                .containsExactly(16, 17, 18, 19);

        mark.rewind();
        assertThat(si.read(8).copy()).containsExactly(8, 9, 10, 11, 12, 13, 14, 15);
    }



    @Test
    void testParseWithLookahead() throws IOException {

        ByteArrayInputStream in = new ByteArrayInputStream("Please parse this".getBytes(UTF_8));
        StreamInput si = StreamInput.of(in);

        Parser<Tuple0, Character> space = matchChar("== ' '", c1 -> ' ' == c1);

        Parser<Tuple0, Character> notSpace = matchChar("!= ' '", c -> ' ' != c);

        Parser<Tuple0, String> word = oneOrMore(notSpace)
                .map(mkString());

        Parser<Tuple0, List<String>> words = zeroOrMore(space.then(word));

        Parser<Tuple0, List<String>> sentence = word.then(words, prepend());

        assertThat(sentence.parse(si))
                .isEqualTo(Right(List("Please", "parse", "this")));
    }

    private <T extends Traversable<?>> NamedFunction<T, String> mkString() {
        return named("mkString", Traversable::mkString);
    }

    private <T> NamedBiFunction<T, List<T>, List<T>> prepend() {
        return named2("prepend", (s, s2) -> s2.prepend(s));
    }

    private Parser<Tuple0, Character> matchChar(String name, Function<Character, Boolean> pred) {
        return ignoreErrorDetails(matches(named(name, pred)));
    }

    private ByteArrayInputStream bytes() {
        return new ByteArrayInputStream(byteArray());
    }

    private byte[] byteArray() {
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }
}