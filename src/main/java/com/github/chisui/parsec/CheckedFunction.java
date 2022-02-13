package com.github.chisui.parsec;

public interface CheckedFunction<E extends Throwable, A, R> {
    R apply(A arg) throws E;
}
