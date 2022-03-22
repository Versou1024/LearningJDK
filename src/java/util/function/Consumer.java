/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package java.util.function;

import java.util.Objects;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);

    /**
     * 返回一个组合的Consumer ，它依次执行此操作和after操作。 如果执行任一操作引发异常，则将其转发给组合操作的调用者。 如果执行此操作抛出异常，则不会执行after操作。
     * 参数：
     * after – 在此操作之后执行的操作
     * 返回：
     * 一个组合的Consumer ，它依次执行此操作和after操作
     * 抛出：
     * NullPointerException – 如果after为空
     *
     * after不能为空；
     * 从最后的lambda表达式可知：先执行this.accept(t)、然后执行after.accept(t)
     * 返回结果是一个 组合的consumer，该组合的consumer里面执行的是this和after的accept
     */
    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
