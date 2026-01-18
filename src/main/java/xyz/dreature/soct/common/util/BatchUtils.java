package xyz.dreature.soct.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class BatchUtils {
    // 重复执行（无返回值）
    public static void repeat(int count, Runnable action) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            action.run();
        }
    }

    // 重复生成（有返回值）
    public static <R> List<R> generate(int count, Supplier<R> supplier) {
        if (count <= 0) return new ArrayList<>();
        List<R> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.add(supplier.get());
        }
        return results;
    }

    // 重复生成（有返回值）
    public static <R> List<R> flatGenerate(int count, Supplier<List<R>> supplier) {
        if (count <= 0) return new ArrayList<>();
        List<R> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.addAll(supplier.get());
        }
        return results;
    }

    // 逐项处理（无返回值）
    public static <T> void processEach(Iterable<T> iterable, Consumer<T> processor) {
        if (iterable == null) return;

        for (T item : iterable) {
            processor.accept(item);
        }
    }

    // 逐项聚合（返回整数）
    public static <T> int reduceEachToInt(Iterable<T> iterable, ToIntFunction<T> mapper) {
        if (iterable == null) return 0;

        int result = 0;
        for (T item : iterable) {
            result += mapper.applyAsInt(item);
        }
        return result;
    }

    // 逐项映射（对象列表）
    public static <T, R> List<R> mapEach(Iterable<T> iterable, Function<T, R> mapper) {
        if (iterable == null) return new ArrayList<>();

        List<R> result = new ArrayList<>();
        for (T item : iterable) {
            result.add(mapper.apply(item));
        }
        return result;
    }

    // 逐项映射（扁平化列表）
    public static <T, R> List<R> flatMapEach(Iterable<T> iterable, Function<T, ? extends Iterable<? extends R>> mapper) {
        if (iterable == null) return new ArrayList<>();

        List<R> result = new ArrayList<>();
        for (T item : iterable) {
            Iterable<? extends R> elements = mapper.apply(item);
            if (elements != null) {
                for (R element : elements) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    // 分批处理（无返回值）
    public static <T> void processBatch(List<T> list, int batchSize, Consumer<List<T>> processor) {
        if (list == null) return;

        while (!list.isEmpty()) {
            List<T> subList = list.subList(0, Math.min(batchSize, list.size()));
            processor.accept(subList);
            list.subList(0, subList.size()).clear();
        }
    }

    // 分批聚合（返回整数）
    public static <T> int reduceBatchToInt(List<T> list, int batchSize, ToIntFunction<List<T>> mapper) {
        if (list == null) return 0;

        int result = 0;
        while (!list.isEmpty()) {
            List<T> subList = list.subList(0, Math.min(batchSize, list.size()));
            int subResult = mapper.applyAsInt(subList);
            result += subResult;
            list.subList(0, subList.size()).clear();
        }
        return result;
    }

    // 分批映射（对象列表）
    public static <T, R> List<R> mapBatch(List<T> list, int batchSize, Function<List<T>, R> mapper) {
        if (list == null) return new ArrayList<>();

        List<R> result = new ArrayList<>();
        while (!list.isEmpty()) {
            List<T> subList = list.subList(0, Math.min(batchSize, list.size()));
            R subResult = mapper.apply(subList);
            result.add(subResult);
            list.subList(0, subList.size()).clear();
        }
        return result;
    }

    // 分批映射（扁平化列表）
    public static <T, R> List<R> flatMapBatch(List<T> list, int batchSize, Function<List<T>, List<R>> mapper) {
        if (list == null) return new ArrayList<>();

        List<R> result = new ArrayList<>();
        while (!list.isEmpty()) {
            List<T> subList = list.subList(0, Math.min(batchSize, list.size()));
            List<R> subResult = mapper.apply(subList);
            if (subResult != null) {
                result.addAll(subResult);
            }
            list.subList(0, subList.size()).clear();
        }
        return result;
    }
}
