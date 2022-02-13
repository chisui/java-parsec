package com.github.chisui.parsec.base;

import com.github.chisui.parsec.Input;
import com.github.chisui.parsec.Parser;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class FirstMatch<E, A> implements Parser<E, A> {
    @NonNull List<Parser<E, A>> px;

    @SuppressWarnings({
            "unchecked", "rawtypes", // list of parsers
    })
    public static <E, A> Parser<E, A>of(Parser<? extends E, ? extends A>... px) {
        if (px.length < 1) {
            throw new IllegalArgumentException("need at least one parser");
        }
        return new FirstMatch<>((List) List.of(px));
    }

    @Override
    public Either<E, A> parse(Input in, Consumer<? super Parser<?, ?>> trace) throws IOException {
        trace.accept(this);
        Iterator<Parser<E, A>> iterator = px.iterator();
        Either<E, A> res;
        do {
            res = in.withMarker(m -> {
                Either<E, A> r = iterator.next().parse(in, trace);
                if (r.isLeft()) {
                    m.rewind();
                }
                return r;
            });
            if (res.isRight()) {
                break;
            }
        } while (iterator.hasNext());
        return res;
    }
}
