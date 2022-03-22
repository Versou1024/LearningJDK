/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * TransferQueue特点：
 * <pre>
 *     1、生产者可以阻塞等待直到消费者接受元素；
 *     2、队列空时，消费者线程若进入阻塞等待，会创建一个值为null的节点，生产者通过调用transfer()，发现队头有null值的node，会直接把值给消费者线程
 *     3、有界队列
 * </pre>
 * 场景：在消息传递应用程序中，TransferQueue可能很有用。在消息传递应用程序中，生产者有时（使用方法transfer）等待消费者调用take或poll来接收元素，
 * 而有时（通过方法put）将元素排队，而不等待消费者来接收。
 * <p>
 *     tryTransfer的还有非阻塞和超时版本也可用。TransferQueue还可以通过hasWaitingConsumer查询是否有线程等待条目，这与peek操作相反。
 * </p>
 *
 * @since 1.7
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public interface TransferQueue<E> extends BlockingQueue<E> {
    /**
     * Transfers the element to a waiting consumer immediately, if possible.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * otherwise returning {@code false} without enqueuing the element.
     *
     * @param e the element to transfer
     * @return {@code true} if the element was transferred, else
     *         {@code false}
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    boolean tryTransfer(E e);

    /**
     * Transfers the element to a consumer, waiting if necessary to do so.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else waits until the element is received by a consumer.
     *
     * @param e the element to transfer
     * @throws InterruptedException if interrupted while waiting,
     *         in which case the element is not left enqueued
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    void transfer(E e) throws InterruptedException;

    /**
     * Transfers the element to a consumer if it is possible to do so
     * before the timeout elapses.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else waits until the element is received by a consumer,
     * returning {@code false} if the specified wait time elapses
     * before the element can be transferred.
     *
     * @param e the element to transfer
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return {@code true} if successful, or {@code false} if
     *         the specified waiting time elapses before completion,
     *         in which case the element is not left enqueued
     * @throws InterruptedException if interrupted while waiting,
     *         in which case the element is not left enqueued
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    boolean tryTransfer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * Returns {@code true} if there is at least one consumer waiting
     * to receive an element via {@link #take} or
     * timed {@link #poll(long,TimeUnit) poll}.
     * The return value represents a momentary state of affairs.
     *
     * @return {@code true} if there is at least one waiting consumer
     */
    boolean hasWaitingConsumer();

    /**
     * Returns an estimate of the number of consumers waiting to
     * receive elements via {@link #take} or timed
     * {@link #poll(long,TimeUnit) poll}.  The return value is an
     * approximation of a momentary state of affairs, that may be
     * inaccurate if consumers have completed or given up waiting.
     * The value may be useful for monitoring and heuristics, but
     * not for synchronization control.  Implementations of this
     * method are likely to be noticeably slower than those for
     * {@link #hasWaitingConsumer}.
     *
     * @return the number of consumers waiting to receive elements
     */
    int getWaitingConsumerCount();
}
