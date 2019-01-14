package com.yevhenii.service.utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FunctionalUtils {

    public static <T> Optional<List<T>> traverse(List<Optional<T>> list) {
        if (list.contains(Optional.<T>empty())) {
            return Optional.empty();
        } else {
            return Optional.of(
                    list.stream()
                            .map(Optional::get)
                            .collect(Collectors.toList())
            );
        }
    }
}
