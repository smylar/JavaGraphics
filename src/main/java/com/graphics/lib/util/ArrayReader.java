package com.graphics.lib.util;

import com.google.common.base.Splitter;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;
import io.reactivex.Observable;

public class ArrayReader {
    
    private ArrayReader() {}

    public static <T> Observable<List<T>> getLineAsArray(String resource, Function<String, T> converter, String splitter) {
        Splitter split = Splitter.on(splitter);
        return ResourceLineReader.getLineObserver(resource)
                                 .map(line -> split.splitToList(line).stream()
                                                   .map(item -> converter.apply(item.trim()))
                                                   .collect(Collectors.toList())
                                  );
    }
    
    public static Observable<List<Double>> getLineAsDoubleArray(String resource, String splitter) {
        return getLineAsArray(resource, Double::parseDouble, splitter);
    }
    
    public static Observable<List<Integer>> getLineAsIntArray(String resource, String splitter) {
        return getLineAsArray(resource, Integer::parseInt, splitter);
    }
}
