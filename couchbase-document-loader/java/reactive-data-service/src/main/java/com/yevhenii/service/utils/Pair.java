package com.yevhenii.service.utils;

import java.util.Objects;
import java.util.Optional;

public class Pair<L, R> {

    private final L left;
    private final R right;


    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public <S> Pair<L, S> updatedRight(S newRight) {
        return Pair.of(left, newRight);
    }

    public <S> Pair<S, R> updatedLeft(S newLeft) {
        return Pair.of(newLeft, right);
    }

    public static <L, R> Optional<Pair<L, R>> traverse(Pair<L, Optional<R>> pair) {
        return pair.right.map(r -> Pair.of(pair.left, r));
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getLeft(), pair.getLeft()) &&
                Objects.equals(getRight(), pair.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }

    @Override
    public String toString() {
        return "Pair(" +
                left + ", " +
                right +
                ')';
    }
}
