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
import java.util.concurrent.locks.LockSupport;

/**
 * A cancellable asynchronous computation.  This class provides a base
 * implementation of {@link Future}, with methods to start and cancel
 * a computation, query to see if the computation is complete, and
 * retrieve the result of the computation.  The result can only be
 * retrieved when the computation has completed; the {@code get}
 * methods will block if the computation has not yet completed.  Once
 * the computation has completed, the computation cannot be restarted
 * or cancelled (unless the computation is invoked using
 * {@link #runAndReset}).
 *
 * <p>A {@code FutureTask} can be used to wrap a {@link Callable} or
 * {@link Runnable} object.  Because {@code FutureTask} implements
 * {@code Runnable}, a {@code FutureTask} can be submitted to an
 * {@link Executor} for execution.
 *
 * <p>In addition to serving as a standalone class, this class provides
 * {@code protected} functionality that may be useful when creating
 * customized task classes.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> The result type returned by this FutureTask's {@code get} methods
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /**
     * FutureTask 实现 RunnableFuture 接口的同时，还组合 Callable
     */

    /**
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     *
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     *
     * <p>此任务的运行状态，最初是NEW的。运行状态仅在方法set、setException和cancel中转换为terminal状态。
     * 在完成过程中，状态可能会呈现完成COMPLETING（设置结果时）或中断INTERRUPTING（仅在中断运行程序以满足取消（true）条件时）的瞬时值。
     * 从这些中间状态到最终状态的转换使用更便宜的有序/延迟写入，因为值是唯一的，不能进一步修改。
     * 可能的状态转换：
     * <pre>
     *     NEW -> COMPLETING -> NORMAL
     *     NEW -> COMPLETING -> EXCEPTIONAL
     *     NEW -> CANCELLED
     *     NEW -> INTERRUPTING -> INTERRUPTED
     * </pre>
     */
    private volatile int state; // state 是Volatile的
    private static final int NEW          = 0; // 任务创建后，Callable.call() 未启动、启动中、执行结束 都是NEW状态
    private static final int COMPLETING   = 1; // 任务中的Callable.call()执行结束后，set()\setException()设置结果前，
    private static final int NORMAL       = 2; // Callable.call()的结果为正常结果，并且被赋值给outcom后，就是NORMAL状态
    private static final int EXCEPTIONAL  = 3; // Callable.call()的结果为异常结果，并且异常被赋值给outcom后，就是EXCEPTIONAL状态
    private static final int CANCELLED    = 4; // 调用 cancelled 状态，状态为NEW，且参数为true后就是CANCELLED状态
    private static final int INTERRUPTING = 5; // 调用 cancelled 状态，状态为NEW，且参数为false后就是INTERRUPTING状态，后续执行后更换为INTERRUPTED
    private static final int INTERRUPTED  = 6; //  调用 cancelled 状态，状态不为NEW，状态为INTERRUPTED

    /** The underlying callable; nulled out after running */
    private Callable<V> callable;
    /** The result to return or exception to throw from get() */
    private Object outcome; //  outcome 可能是正常返回的值，也可能是抛出的异常。 非volatile，通过state来读/写保护
    /** The thread running the callable; CASed during run() */
    private volatile Thread runner; // 执行 callable 的线程
    /** Treiber stack of waiting threads */
    private volatile WaitNode waiters;

    /**
     * 返回已完成任务的结果或引发异常。
     *
     * @param s completed state value
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        // 1 NORMAl表示任务正常结束，返回值x
        if (s == NORMAL)
            return (V)x;
        // 2 状态大于等于CANCELLED抛出CancellationException
        if (s >= CANCELLED)
            throw new CancellationException();
        // 3 状态为 NEW、COMPLETING、EXCEPTIONAL，认为无法立即获取到x，抛出ExecutionException异常
        throw new ExecutionException((Throwable)x);
    }

    /**
     * 创建一个FutureTask，它将在运行时执行给定的可调用任务
     *
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }

    /**
     * 创建一个FutureTask，它将在运行时执行给定的Runnable，并安排get在成功完成时返回给定的结果。
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion. If
     * you don't need a particular result, consider using
     * constructions of the form:
     * {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result); // 使用Executors帮忙封装为一个Callable方法
        this.state = NEW;       // ensure visibility of callable
    }

    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    public boolean isDone() {
        // 正常执行结束、执行方法抛出异常、执行中被中断、执行中被取消 -- 都认为是完成的
        return state != NEW;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        /**
         * 返回的情况：
         * 1、状态非NEW的就一定可以被取消成功，返回true；
         * 2、状态为NEW的就一定可以取消成功，返回true；
         *   mayInterruptIfRunning 为false，直接设置为CANCELLED就进入finishCompletion()；
         *   mayInterruptIfRunning 为true，先设置为直接设置为INTERRUPTING，然后调用t.interrupt()中强制中断，然后更新为INTERRUPTED状态，就进入finishCompletion()；
         *
         * mayInterruptIfRunning：表示FutureTask的中包装的callable.call()在执行中是否会被中断 -- 前提是callable.call()支持处理中断
         */
        // 1、任务为NEW状态，那么callable.call()方法可能未开始执行、正在执行、执行结束都有可能
        // 这里可以将状态标记为为 INTERRUPTING 或者 CANCELLED 状态
        // ① callable未开始执行，在FutureTask.run()的第一个if即 if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset,null, Thread.currentThread())) 执行失败 -- 表示取消任务成功
        // ②③ callable执行中或者执行结束，在FutureTask.run()的setException()或者set()中的第一个if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING))  执行失败 -- 表示取消任务成功
        if (!(state == NEW && UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false; // 返回的false表示取消失败
        try {
            // 2、任务是NEW状态，并且将任务状态设置为 INTERRUPTING 状态也成功啦，就需要开始尝试强行中断
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null) // 在FutureTask.run()中已经执行到finally块，则runner就会被清空，因此不能t.interrupt()
                        t.interrupt();
                } finally {
                    // 最终的状态更新为 INTERRUPTED
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING) //当前状态为完成中COMPLETING或者未开始NEW，阻塞等待执行
            s = awaitDone(false, 0L); //将调用awaitDone()同步等待阻塞
        return report(s);
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        // 小于 COMPLETING 状态，将调用awaitDone()等待阻塞
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * 当此任务转换为状态isDone（正常或通过取消）时调用的受保护方法。
     * 默认实现什么都不做。子类可以重写此方法来调用完成回调。
     * 请注意，您可以在该方法的实现中查询状态，以确定该任务是否已被取消
     */
    protected void done() { }

    /**
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon successful completion of the computation.
     *
     * @param v the value
     */
    protected void set(V v) {
        // 二阶段
        // 1、状态从 NWE 更新为 COMPLETING
        // 2、状态从 COMPLETING 更新为 NORMAL
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            finishCompletion();
        }
    }

    /**
     * Causes this future to report an {@link ExecutionException}
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */
    protected void setException(Throwable t) {
        // 二阶段
        // 1、状态从 NWE 更新为 COMPLETING
        // 2、状态从 COMPLETING 更新为 EXCEPTIONAL
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
            // 任务执行结束后，结果或者异常赋值给了outcome，若有线程在这之前调用get()，就会阻塞等待，因此线程获取到结果后，finishCompletion()来唤醒他们
            finishCompletion();
        }
    }

    public void run() {
        // ① 只有NEW状态的任务，才会被调用其callable的方法
        // ② 其余状态的任务，只能立即返回
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    // ③ 实际执行任务，即c.call()时，任务仍然是NEW状态的
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    // 任务执行中抛出任何异常
                    setException(ex);
                }
                // 任务执行成功
                if (ran)
                    set(result);
            }
        } finally {
            // 在解决状态之前，runner必须为非null，以防止对run（）的并发调用
            runner = null;
            // 程序运行期间，如果执行了Cancell()方法，那么s >= INTERRUPTING，转而执行 handlePossibleCancellationInterrupt(s)
            int s = state;
            // 线程被cancel(true)要求立即中断
            // 因为true表示线程被强制执行中断，由于这里已经执行了runner=null，所以cancel()方法中无法调用t.interrupt()
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return {@code true} if successfully run and reset
     */
    protected boolean runAndReset() {
        /*
         * 该方法的主要任务：
         * 1、任务想要被运行，前提是NEW状态，否则返回false
         * 2、执行任务，并将状态设置为NEW，清空runner，允许再次执行
         */
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s); // 帮助等待做状态切换
        }
        return ran && s == NEW;
    }

    /**
     * 确保来自可能Cancel（true）的任何中断仅在运行或设置时传递给任务。
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // 我们的interrupt()的调用有可能需要等待，直到我们在在有机会打断线程后。所以让我们耐心自旋等待。
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // 等待中断 -- 等待到状态被设置为INTERRUPTED

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber
     * stack.  See other classes such as Phaser and SynchronousQueue
     * for more detailed explanation.
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     *移除所有等待的线程并发出信号，调用done（），并为callable设置空值。
     */
    private void finishCompletion() {
        // 作用：任务执行结束后，set()结果或者setException()异常赋值给了outcome，
        // 多线程由于调用get()会阻塞等待，因此线程获取到结果后，使用finishCompletion()来唤醒他们
        for (WaitNode q; (q = waiters) != null;) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t); // 唤醒线程
                    }
                    // 继续迭代遍历
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }

        done();

        callable = null;        // 减少足迹
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     */
    private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            // state已经大于COMPLETING说明已完成，需要返回状态
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            }
            // COMPLETING 表示赋值前的一步，马上就要在set()或者setException(),因此只需要调用yield()就可以获取到结果
            else if (s == COMPLETING)
                Thread.yield(); //当前线程让出CPU等待下一次获取到时间片后再次运算
            // NEW状态下，线程第一次进入，q没有初始化，这里尝试出初始化node
            else if (q == null)
                q = new WaitNode();
            // NEW状态下，线程第二次进入，q已经初始化，可以尝试入等待队列，
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                     q.next = waiters, q);
            // NEW状态下，线程第三次进入，已经尝试入等待队列后，判断是否需要超时等待
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) { //若这段期间已经超时
                    removeWaiter(q);
                    return state; // 超时下也需要继续返回状态
                }
                LockSupport.parkNanos(this, nanos); //挂起当前线程
            }
            // NEW状态下，线程第三次进入，已经尝试入等待队列后，判断是否需要永久等待
            else
                LockSupport.park(this); //挂起线程
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage.  Internal nodes are simply unspliced
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.
     */
    private void removeWaiter(WaitNode node) {
        // 从等待队列链表中移除某一个node
        if (node != null) {
            node.thread = null; // 将node的线程设置为null，表示当前node已经被逻辑删除，需哟啊后续
            retry:
            for (;;) {          // 尝试将链表中所有逻辑删除的节点都给移除
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
