package taste;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Date: 9/8/14
 * Time: 10:53 PM
 * Copyright: Denis Baranov 2012-2014
 */
public class Main {

    public static void main(String[] args) {
        Main instance = new Main();

        instance.exerciseMinK();
    }

    public void exerciseMinK() {
        final int maxCount = 18, elementCount = 1000000; // 100000000 requires 5GB heap

        long timeNanos = System.nanoTime();
        minK1(new Random(0).ints(elementCount), maxCount)
            .forEach(this::printDebug);
        System.out.println("Sequential stream: " + (System.nanoTime() - timeNanos) / 1.0e9 + "s");
        printed = 1;

        timeNanos = System.nanoTime();
        minK1(new Random(0).ints(elementCount).parallel(), maxCount)
            .forEach(this::printDebug);
        System.out.println("Parallel stream: " + (System.nanoTime() - timeNanos) / 1.0e9 + "s");
        printed = 1;

        timeNanos = System.nanoTime();
        List<Integer> input = new Random(0).ints(elementCount)
            .collect(LinkedList<Integer>::new, LinkedList::add, LinkedList::addAll);
        System.out.println("Filling the array: " + (System.nanoTime() - timeNanos) / 1.0e9 + "s");

        timeNanos = System.nanoTime();
        minK1(input.parallelStream().mapToInt((e) -> e), maxCount)
            .forEach(this::printDebug);
        System.out.println("Parallel array: " + (System.nanoTime() - timeNanos) / 1.0e9 + "s");
        printed = 1;
    }

    /**
     * Find K lowest elements in unordered stream.
     * @return ordered stream of results
     * @param input unordered stream to work with
     * @param subCount number of
     */
    public IntStream minK1(IntStream input, final int subCount) {
        return input.collect(
            () -> new FixedSizePQ<>(subCount, Comparator.<Integer>naturalOrder().reversed()),
            FixedSizePQ::add,
            FixedSizePQ::addAll).stream().mapToInt(i -> i);
    }

    private class FixedSizePQ<T> extends PriorityQueue<T> {
        final int limit;
        int overflows = 0;

        public FixedSizePQ(int limit, Comparator<T> compr) {
            super(compr);
            this.limit = limit;
            System.out.println("Created buffer");
        }

        @Override
        public boolean add(T t) {
            super.add(t);
            if (size() > limit) {
                super.remove();
                overflows++;
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            super.addAll(c);
            while (size() > limit) {
                super.remove();
                overflows++;
            }

            return true;
        }

        @Override
        public Stream<T> stream() {
            System.out.println("Buffer overflown " + overflows + " times");
            return super.stream();
        }
    }

    private int printed = 1;

    private void printDebug(int e) {
        System.out.println((printed++) + ": " + e);
    }
}
