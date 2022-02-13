package com.github.chisui.parsec;

import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.chisui.parsec.Bytes.asString;
import static com.github.chisui.parsec.Parser.*;
import static io.vavr.API.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void testEmpty() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Object, Tuple0> res = empty().parse(in);

        assertThat(res).isEqualTo(Right(Tuple()));
        assertThat(in.read(1).copy()).asString().isEqualTo("a");
    }


    @Test
    void testSuccess() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Object, String> res = success("a").parse(in);

        assertThat(res).isEqualTo(Right("a"));
        assertThat(in.read(1).copy()).asString().isEqualTo("a");
    }

    @Test
    void testError() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<String, Object> res = error("a").parse(in);

        assertThat(res).isEqualTo(Left("a"));
        assertThat(in.read(1).copy()).asString().isEqualTo("a");
    }

    @Test
    void testEof() throws IOException {
        Input in = ArrayInput.of("");

        Either<Tuple0, Tuple0> res = eof().parse(in);

        assertThat(res).isEqualTo(Right(Tuple()));
    }

    @Test
    void testEofMismatch() throws IOException {
        Input in = ArrayInput.of("a");

        Either<Tuple0, Tuple0> res = eof().parse(in);

        assertThat(res).isEqualTo(Left(Tuple()));
    }

    @Test
    void testAnyHasNext() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Tuple0, Byte> res = anyByte().parse(in);

        assertThat(res).isEqualTo(Right((byte) 'a'));
        assertThat(in.read(1).copy()).asString().isEqualTo("s");
    }

    @Test
    void testAnyEmpty() throws IOException {
        Input in = ArrayInput.of("");

        Either<Tuple0, Byte> res = anyByte().parse(in);

        assertThat(res).isEqualTo(Left(Tuple()));
    }

    @Test
    void testExpectMatch() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Integer, String> res = expect("as").parse(in);

        assertThat(res).isEqualTo(Right("as"));
    }

    @Test
    void testExpectMismatch() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Integer, String> res = expect("ab").parse(in);

        assertThat(res).isEqualTo(Left(1));
    }

    @Test
    void testTryParse() throws IOException {
        Input in = ArrayInput.of("asdf");
        in.read(1);

        Parser<Tuple0, Void> p = anyByte().then(anyByte()).then(error(Tuple()));
        Either<Object, Either<Tuple0, Void>> res = tryParse(p).parse(in);

        assertThat(res).isEqualTo(Right(Left(Tuple())));

        assertThat(in.read(2).copy()).asString().isEqualTo("sd");
    }

    @Test
    void testZeroOrMore() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Object, String> res = zeroOrMore(anyByte(), asString(UTF_8)).parse(in);

        assertThat(res).isEqualTo(Right("asdf"));
    }

    @Test
    void testOneOrMore() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<Tuple0, String> res = oneOrMore(anyByte(), asString(UTF_8)).parse(in);

        assertThat(res).isEqualTo(Right("asdf"));
    }

    @Test
    void testOneOrMoreMisMatch() throws IOException {
        Input in = ArrayInput.of("asdf");

        Either<String, List<Object>> res = oneOrMore(error("err")).parse(in);

        assertThat(res).isEqualTo(Left("err"));
    }

    @Test
    void testGrowByteBuffer() throws IOException {
        ArrayInput in = ArrayInput.of("ä-");

        byte[] bytes = "ä".getBytes(UTF_8);

        Either<byte[], Character> res = character(UTF_8).parse(in);

        assertThat(res).isEqualTo(Right('ä'));
        assertThat(in.read(10).copy()).asString().isEqualTo("-");
    }

    @Test
    void testOrMatchFirst() throws IOException {
        ArrayInput in = ArrayInput.of("aaa");

        Parser<Integer, String> p = or(expect("a"), expect("b"));

        Either<Integer, String> res = p.parse(in);

        assertThat(res).isEqualTo(Right("a"));
    }


    @Test
    void testOrMatchSecond() throws IOException {
        ArrayInput in = ArrayInput.of("bbb");

        Parser<Integer, String> p = or(expect("a"), expect("b"));

        Either<Integer, String> res = p.parse(in);

        assertThat(res).isEqualTo(Right("b"));
    }

    @Test
    void testOrMatchNone() throws IOException {
        ArrayInput in = ArrayInput.of("ccc");

        Parser<Integer, String> p = or(expect("a"), expect("b"));

        Either<Integer, String> res = p.parse(in);

        assertThat(res).isEqualTo(Left(0));
    }
}