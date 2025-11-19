package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.supporting.json.ObjectMapperManager;

import java.lang.reflect.Array;
import java.util.List;
import java.util.stream.IntStream;


public class ArrayUtils {

    private ArrayUtils(){}

    public static <T> T[] asArray(List<T> list) {
        if (list.isEmpty()) {
            return (T[]) Array.newInstance(
                    ObjectMapperManager.getInstance().defaultMapper().getTypeFactory().constructType(Object.class).getClass(),
                    list.size()
            );
        }
        T[] array = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
        if (0 == array.length) {
            return array;
        }
        IntStream.range(0, list.size()).forEach(idx -> array[idx] = list.get(idx));
        return array;
    }

}
