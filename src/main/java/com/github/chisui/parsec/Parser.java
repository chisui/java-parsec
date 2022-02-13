package com.github.chisui.parsec;

import com.github.chisui.parsec.base.*;
import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

import static com.github.chisui.parsec.base.Id.id;
import static com.github.chisui.parsec.base.NamedFunction.named;
import static io.vavr.API.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public interface Parser<E, R> {

    default Either<E, R> parse(Input in) throws IOException {
        return parse(in, any -> {});
    }

    Either<E, R> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException;

    default <S> Parser<E, S> map(@NonNull Function<? super R, ? extends S> f) {
        return bimap(id(), f);
    }

    default <F> Parser<F, R> mapErr(@NonNull Function<? super E, ? extends F> f) {
        return bimap(f, id());
    }

    default <F, S> Parser<F, S> bimap(
            @NonNull Function<? super E, ? extends F> f,
            @NonNull Function<? super R, ? extends S> g) {
        return new Mapped<>(this, (Function<R, S>) g, (Function<E, F>) f);
    }

    default <F> Parser<F, R> flatMapErr(
            @NonNull Function<? super E, ? extends Parser<? extends F, ? extends R>> f) {
        return biFlatMap(f, named("success", Parser::success));
    }

    default <S> Parser<E, S> flatMap(
            @NonNull Function<? super R, ? extends Parser<? extends E, ? extends S>> f) {
        return biFlatMap(named("error", Parser::error), f);
    }

    default <F, S> Parser<F, S> biFlatMap(
            @NonNull Function<? super E, ? extends Parser<? extends F, ? extends S>> f,
            @NonNull Function<? super R, ? extends Parser<? extends F, ? extends S>> g) {
        return new FlatMapped<>(this,
                (Function<E, Parser<F, S>>) f,
                (Function<R, Parser<F, S>>) g);
    }

    default <B, C> Parser<E, C> then(
            @NonNull Parser<? extends E, ? extends B> p,
            @NonNull BiFunction<? super R, ? super B, ? extends C> f) {
        return flatMap(named("then(" + p + ", " + f + ")", r -> p.map(b -> f.apply(r, b))));
    }

    default <B> Parser<E, B> then(@NonNull Parser<? extends E, ? extends B> p) {
        return then(p, (a, b) -> b);
    }

    default <B> Parser<E, R> followedBy(@NonNull Parser<? extends E, ? extends B> p) {
        return then(p, (a, b) -> a);
    }

    default Parser<Either<E, R>, R> filter(@NonNull Function<? super R, Boolean> p) {
        return mapErr(named("left", Either::<E, R>left))
                .flatMap(named("filterBy(" + p + ")", c -> p.apply(c) ? success(c) : error(Right(c))));
    }

    default Parser<R, E> negate() {
        return new Not<>(this);
    }

    static <E, F> Parser<Tuple0, F> ignoreErrorDetails(@NonNull Parser<? extends E, ? extends F> p) {
        return narrow(p.mapErr(named("any -> Tuple()", any -> Tuple())));
    }

    static <T> Parser<T, Tuple0> empty() {
        return success(Tuple());
    }

    @SuppressWarnings({
            "unchecked", // valid downcast since Parser is covariant in both arguments
    })
    static <A, B> Parser<A, B> narrow(Parser<? extends A, ? extends B> p) {
        return (Parser<A, B>) p;
    }

    static Parser<Tuple0, Byte> anyByte() {
        return AnyByte.INSTANCE;
    }

    static <E, R> Parser<E, R> pure(@NonNull R value) {
        return success(value);
    }

    static <E, R> Parser<E, R> success(@NonNull R value) {
        return new Const<>(Right(value));
    }

    static <E, R> Parser<E, R> error(@NonNull E error) {
        return new Const<>(Left(error));
    }

    static Parser<Tuple0, Tuple0> eof() {
        return EOF.INSTANCE;
    }

    static Parser<byte[], Character> character(@NonNull Charset charset) {
        return AnyCharacter.of(charset);
    }

    static Parser<Either<byte[], Character>, Character> matches(Function<? super Character, Boolean> p) {
        return character(UTF_8).filter(p);
    }

    static <A, B> Parser<A, B> not(Parser<? extends B, ? extends A> p) {
        return narrow(p.negate());
    }

    static Parser<Integer, String> expect(@NonNull String expected) {
        return expect(expected.getBytes(UTF_8))
                .map(bytes -> new String(bytes, UTF_8));
    }

    static Parser<Integer, byte[]> expect(@NonNull byte[] expected) {
        return new Expect(expected);
    }

    static <E, L, R> Parser<E, Either<L, R>> tryParse(@NonNull Parser<? extends L, ? extends R> p) {
        return new TryParse<>(narrow(p));
    }

    static <X, E, R> Parser<X, List<R>> zeroOrMore(@NonNull Parser<? extends E, ? extends R> p) {
        return zeroOrMore(p, List.collector());
    }

    static <X, E, A, R, S> Parser<X, S> zeroOrMore(
            @NonNull Parser<? extends E, ? extends R> p,
            @NonNull Collector<? super R, A, S> col) {
        return new ZeroOrMore<>(narrow(p), (Collector<R, A, S>) col);
    }

    static <E, R> Parser<E, List<R>> oneOrMore(@NonNull Parser<? extends E, ? extends R> p) {
        return oneOrMore(p, List.collector());
    }

    static <E, A, R, S> Parser<E, S> oneOrMore(
            @NonNull Parser<? extends E, ? extends R> p,
            @NonNull Collector<? super R, A, S> col) {
        return new OneOrMore<>(narrow(p), (Collector<R, A, S>) col);
    }

    static <E, A> Parser<E, A> or(Parser<? extends E, ? extends A>... px) {
        return FirstMatch.of(px);
    }
}
