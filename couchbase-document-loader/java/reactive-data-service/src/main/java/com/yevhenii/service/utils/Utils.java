package com.yevhenii.service.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

    public static int ceilDivision(int a, int b) {
        return (int) Math.ceil((double) a / b);
    }

    public static <T> List<List<T>> divideIntoParts(List<T> list, int parts) {
        int partSize = ceilDivision(list.size(), parts);

        return IntStream.range(0, parts).boxed()
                .map(i -> list.subList(i * partSize, Math.min((i + 1) * partSize, list.size())))
                .collect(Collectors.toList());
    }

    public static List<String> splitByLines(String str) {
        return Arrays.stream(normalizeAndSplit(str))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toList());
    }

    private static String[] normalizeAndSplit(String str) {
        return str.replace("}{", "}\n{")
                .split("\n");
    }
}
