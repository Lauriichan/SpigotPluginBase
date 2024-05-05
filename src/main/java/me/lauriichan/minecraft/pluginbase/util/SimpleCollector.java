package me.lauriichan.minecraft.pluginbase.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public class SimpleCollector<I, A, R> implements Collector<I, A, R> {

    public static final Set<Collector.Characteristics> CH_ID = Collections
        .unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

    private static final Function passthrough = i -> i;
    private static final Function casting = createCasting();


    private static <A> Function<?, A> createCasting() {
        return i -> (A) i;
    }

    public static <I, R> Function<I, R> castingIdentity() {
        return casting;
    }

    public static <T> Function<T, T> passthrough() {
        return passthrough;
    }
    
    public static <A> BinaryOperator<A> toBinary(BiConsumer<A, A> consumer) {
        return (a1, a2) -> {
            consumer.accept(a1, a2);
            return a1;
        };
    }

    private final Supplier<A> supplier;
    private final BiConsumer<A, I> accumulator;
    private final BinaryOperator<A> combiner;
    private final Function<A, R> finisher;
    private final Set<Characteristics> characteristics;

    public SimpleCollector(Supplier<A> supplier, BiConsumer<A, I> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher,
        Set<Characteristics> characteristics) {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = characteristics;
    }

    public SimpleCollector(Supplier<A> supplier, BiConsumer<A, I> accumulator, BinaryOperator<A> combiner,
        Set<Characteristics> characteristics) {
        this(supplier, accumulator, combiner, castingIdentity(), characteristics);
    }

    public SimpleCollector(Supplier<A> supplier, BiConsumer<A, I> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher) {
        this(supplier, accumulator, combiner, finisher, CH_ID);
    }

    public SimpleCollector(Supplier<A> supplier, BiConsumer<A, I> accumulator, BinaryOperator<A> combiner) {
        this(supplier, accumulator, combiner, castingIdentity(), CH_ID);
    }

    @Override
    public BiConsumer<A, I> accumulator() {
        return accumulator;
    }

    @Override
    public Supplier<A> supplier() {
        return supplier;
    }

    @Override
    public BinaryOperator<A> combiner() {
        return combiner;
    }

    @Override
    public Function<A, R> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }
}
