package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

@Value
@SuppressWarnings({
        "unchecked", // it's a success
})
public class Const<E, R> implements Parser<E, R> {
    @NonNull Either<E, R> value;

    @Override
    public Either<E, R> parse(@NonNull Input in, Consumer<? super Parser<?, ?>> trace) {
        trace.accept(this);
        return value;
    }

    @Override
    public <S> Parser<E, S> map(@NonNull Function<? super R, ? extends S> f) {
        if (value.isLeft()) {
            return (Parser<E, S>) this;
        } else {
            return new Const<>(value.map(f));
        }
    }

    @Override
    public <F> Parser<F, R> mapErr(@NonNull Function<? super E, ? extends F> f) {
        if (value.isRight()) {
            return (Parser<F, R>) this;
        } else {
            return new Const<>(value.mapLeft(f));
        }
    }

    @Override
    public <F, S> Parser<F, S> bimap(@NonNull Function<? super E, ? extends F> f, @NonNull Function<? super R, ? extends S> g) {
        return new Const<>(value.bimap(f, g));
    }

    @Override
    public <F> Parser<F, R> flatMapErr(@NonNull Function<? super E, ? extends Parser<? extends F, ? extends R>> f) {
        if (value.isRight()) {
            return (Parser<F, R>) this;
        } else {
            return (Parser<F, R>) f.apply(value.getLeft());
        }
    }

    @Override
    public <S> Parser<E, S> flatMap(@NonNull Function<? super R, ? extends Parser<? extends E, ? extends S>> f) {
        if (value.isLeft()) {
            return (Parser<E, S>) this;
        } else {
            return (Parser<E, S>) f.apply(value.get());
        }
    }

    @Override
    public <F, S> Parser<F, S> biFlatMap(
            @NonNull Function<? super E, ? extends Parser<? extends F, ? extends S>> f,
            @NonNull Function<? super R, ? extends Parser<? extends F, ? extends S>> g) {
        return (Parser<F, S>) value.fold(f, g);
    }

    @Override
    public <B, C> Parser<E, C> then(@NonNull Parser<? extends E, ? extends B> p, @NonNull BiFunction<? super R, ? super B, ? extends C> f) {
        if (value.isLeft()) {
            return (Parser<E, C>) this;
        } else {
            R r = value.get();
            return (Parser<E, C>) p.map(b -> f.apply(r, b));
        }
    }

    @Override
    public <B> Parser<E, B> then(@NonNull Parser<? extends E, ? extends B> p) {
        if (value.isLeft()) {
            return (Parser<E, B>) this;
        } else {
            return (Parser<E, B>) p;
        }
    }

    @Override
    public Parser<Either<E, R>, R> filter(@NonNull Function<? super R, Boolean> p) {
        return new Const<>(value.fold(
                e -> Left(Left(e)),
                r -> p.apply(r)
                        ? Right(r)
                        : Left(Right(r))));
    }

    @Override
    public Parser<R, E> negate() {
        return new Const<>(value.fold(Either::right, Either::left));
    }
}
