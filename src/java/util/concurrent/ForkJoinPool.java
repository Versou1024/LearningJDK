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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.security.Permissions;

/**
 * An {@link ExecutorService} for running {@link ForkJoinTask}s.
 * A {@code ForkJoinPool} provides the entry point for submissions
 * from non-{@code ForkJoinTask} clients, as well as management and
 * monitoring operations.
 *
 * <p>A {@code ForkJoinPool} differs from other kinds of {@link
 * ExecutorService} mainly by virtue of employing
 * <em>work-stealing</em>: all threads in the pool attempt to find and
 * execute tasks submitted to the pool and/or created by other active
 * tasks (eventually blocking waiting for work if none exist). This
 * enables efficient processing when most tasks spawn other subtasks
 * (as do most {@code ForkJoinTask}s), as well as when many small
 * tasks are submitted to the pool from external clients.  Especially
 * when setting <em>asyncMode</em> to true in constructors, {@code
 * ForkJoinPool}s may also be appropriate for use with event-style
 * tasks that are never joined.
 *
 * <p>A static {@link #commonPool()} is available and appropriate for
 * most applications. The common pool is used by any ForkJoinTask that
 * is not explicitly submitted to a specified pool. Using the common
 * pool normally reduces resource usage (its threads are slowly
 * reclaimed during periods of non-use, and reinstated upon subsequent
 * use).
 *
 * <p>For applications that require separate or custom pools, a {@code
 * ForkJoinPool} may be constructed with a given target parallelism
 * level; by default, equal to the number of available processors.
 * The pool attempts to maintain enough active (or available) threads
 * by dynamically adding, suspending, or resuming internal worker
 * threads, even if some tasks are stalled waiting to join others.
 * However, no such adjustments are guaranteed in the face of blocked
 * I/O or other unmanaged synchronization. The nested {@link
 * ManagedBlocker} interface enables extension of the kinds of
 * synchronization accommodated.
 *
 * <p>In addition to execution and lifecycle control methods, this
 * class provides status check methods (for example
 * {@link #getStealCount}) that are intended to aid in developing,
 * tuning, and monitoring fork/join applications. Also, method
 * {@link #toString} returns indications of pool state in a
 * convenient form for informal monitoring.
 *
 * <p>As is the case with other ExecutorServices, there are three
 * main task execution methods summarized in the following table.
 * These are designed to be used primarily by clients not already
 * engaged in fork/join computations in the current pool.  The main
 * forms of these methods accept instances of {@code ForkJoinTask},
 * but overloaded forms also allow mixed execution of plain {@code
 * Runnable}- or {@code Callable}- based activities as well.  However,
 * tasks that are already executing in a pool should normally instead
 * use the within-computation forms listed in the table unless using
 * async event-style tasks that are not usually joined, in which case
 * there is little difference among choice of methods.
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>Summary of task execution methods</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER> <b>Call from non-fork/join clients</b></td>
 *    <td ALIGN=CENTER> <b>Call from within fork/join computations</b></td>
 *  </tr>
 *  <tr>
 *    <td> <b>Arrange async execution</b></td>
 *    <td> {@link #execute(ForkJoinTask)}</td>
 *    <td> {@link ForkJoinTask#fork}</td>
 *  </tr>
 *  <tr>
 *    <td> <b>Await and obtain result</b></td>
 *    <td> {@link #invoke(ForkJoinTask)}</td>
 *    <td> {@link ForkJoinTask#invoke}</td>
 *  </tr>
 *  <tr>
 *    <td> <b>Arrange exec and obtain Future</b></td>
 *    <td> {@link #submit(ForkJoinTask)}</td>
 *    <td> {@link ForkJoinTask#fork} (ForkJoinTasks <em>are</em> Futures)</td>
 *  </tr>
 * </table>
 *
 * <p>The common pool is by default constructed with default
 * parameters, but these may be controlled by setting three
 * {@linkplain System#getProperty system properties}:
 * <ul>
 * <li>{@code java.util.concurrent.ForkJoinPool.common.parallelism}
 * - the parallelism level, a non-negative integer
 * <li>{@code java.util.concurrent.ForkJoinPool.common.threadFactory}
 * - the class name of a {@link ForkJoinWorkerThreadFactory}
 * <li>{@code java.util.concurrent.ForkJoinPool.common.exceptionHandler}
 * - the class name of a {@link UncaughtExceptionHandler}
 * </ul>
 * If a {@link SecurityManager} is present and no factory is
 * specified, then the default pool uses a factory supplying
 * threads that have no {@link Permissions} enabled.
 * The system class loader is used to load these classes.
 * Upon any error in establishing these settings, default parameters
 * are used. It is possible to disable or limit the use of threads in
 * the common pool by setting the parallelism property to zero, and/or
 * using a factory that may return {@code null}. However doing so may
 * cause unjoined tasks to never be executed.
 *
 * <p><b>Implementation notes</b>: This implementation restricts the
 * maximum number of running threads to 32767. Attempts to create
 * pools with greater than the maximum number result in
 * {@code IllegalArgumentException}.
 *
 * <p>This implementation rejects submitted tasks (that is, by throwing
 * {@link RejectedExecutionException}) only when the pool is shut down
 * or internal resources have been exhausted.
 *
 * @since 1.7
 * @author Doug Lea
 */
@sun.misc.Contended
public class ForkJoinPool extends AbstractExecutorService {

    /**
     * 宏观注意点：
     * 1. workQueues的偶数位置（外部提交的任务）是不配线程的。它们是shared的，就是总是被偷去执行的。
     * 2. 外部提交的任务不是随机找位置的，是确定的。每个线程维护一个数（probe），只要这个数不变，每次提交任务都到同一个任务队列；
     *      如果那个队列太忙了，就会提交失败，这时候就把数变一下，重新提交，直到成功，将来的任务也都会提交到这个新地方。
     * 3. 子任务fork()出来的新任务永远放在执行它的worker所拥有的队列里。如果它不被偷，就是原来的队列；如果被偷了，就是偷窃者的队列。
     * 4. 新的任务队列及其对应的worker线程的产生是被动的方式：
     *      每当有新任务来的时候，都会调用signalWork()方法。这个方法检查当前的线程是否够用，如果不够用，就释放一个闲置线程（idle worker)；
     *      如果没有闲置线程，就创建一个新线程。新线程创建后，配给它一个新建的任务队列，然后在workQueues里找一个合适的位置（奇数位置）把任务队列放进去，
     *      这个位置不是随机的，而是算出来的：从上一次新建队列的位置向后移动一个固定的偏移量。
     * 5. 补偿线程的意思是：一个线程执行join()的时候知道自己注定要去block了，因此唤醒或者新建一个线程补偿自己block之后的算力损失。
     */

    // Static utilities

    /**
     * If there is a security manager, makes sure caller has
     * permission to modify threads.
     */
    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(modifyThreadPermission);
    }

    // Nested classes

    /**
     * Factory for creating new {@link ForkJoinWorkerThread}s.
     * A {@code ForkJoinWorkerThreadFactory} must be defined and used
     * for {@code ForkJoinWorkerThread} subclasses that extend base
     * functionality or initialize threads with different contexts.
     */
    public static interface ForkJoinWorkerThreadFactory { // 内部线程工厂接口，用于创建工作线程ForkJoinWorkerThread
        /**
         * Returns a new worker thread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @return the new worker thread
         * @throws NullPointerException if the pool is null
         */
        public ForkJoinWorkerThread newThread(ForkJoinPool pool);
    }

    /**
     * ForkJoinWorkerThreadFactory 的默认实现类
     */
    static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool); // 将ForkJoinPool提交过去，注意这种方式是没有workQueue的
        }
    }

    /**
     * 实现ForkJoinTask，作为内部占位类，用于替换队列中 join 的任务。
     */
    static final class EmptyTask extends ForkJoinTask<Void> {
        private static final long serialVersionUID = -7721805057305804111L;
        EmptyTask() { status = ForkJoinTask.NORMAL; } // 强制完成
        public final Void getRawResult() { return null; }
        public final void setRawResult(Void x) {}
        public final boolean exec() { return true; }
    }

    // Constants shared across ForkJoinPool and WorkQueue

    // Bounds
    static final int SMASK        = 0xffff;        // 低位掩码，也是最大索引位
    static final int MAX_CAP      = 0x7fff;        // 工作线程最大容量
    static final int EVENMASK     = 0xfffe;        // 偶数低位掩码
    static final int SQMASK       = 0x007e;        // 0111 1110，不难看出workQueues数组最多64个槽位 -- 这里是用于获取偶数的槽位

    // Masks and units for WorkQueue.scanState and ctl sp subfield
    static final int SCANNING     = 1;             // 标记是否正在运行任务 -- 标记为繁忙状态 -- scanState 最后一位为1表示不繁忙[可能阻塞，可能在scan窃取任务]，为0表示繁忙已经在执行task
    static final int INACTIVE     = 1 << 31;       // 失活状态，必须是负数
    static final int SS_SEQ       = 1 << 16;       // 版本戳，防止ABA问题

    // Mode bits for ForkJoinPool.config and WorkQueue.config
    static final int MODE_MASK    = 0xffff << 16;  // 模式掩码
    static final int LIFO_QUEUE   = 0; // LIFO队列
    static final int FIFO_QUEUE   = 1 << 16;       // FIFO队列
    static final int SHARED_QUEUE = 1 << 31;       //  共享模式队列，负数

    /**
     * ForkJoinPool 的核心数据结构，本质上是work-stealing 模式的双端任务队列，内部存放 ForkJoinTask 对象任务，
     * 加上Contended防止伪共享
     */
    @sun.misc.Contended
    static final class WorkQueue {

        /**
         * Capacity of work-stealing queue array upon initialization.
         * Must be a power of two; at least 4, but should be larger to
         * reduce or eliminate cacheline sharing among queues.
         * Currently, it is much larger, as a partial workaround for
         * the fact that JVMs often place arrays in locations that
         * share GC bookkeeping (especially cardmarks) such that
         * per-write accesses encounter serious memory contention.
         */
        static final int INITIAL_QUEUE_CAPACITY = 1 << 13;

        /**
         * Maximum size for queue arrays. Must be a power of two less
         * than or equal to 1 << (31 - width of array entry) to ensure
         * lack of wraparound of index calculations, but defined to a
         * value a bit less than this to help users trap runaway
         * programs before saturating systems.
         */
        static final int MAXIMUM_QUEUE_CAPACITY = 1 << 26; // 64M
        /**
         * scanState int类型32bits，各bit位含义如下
         *     共：4个部分
         *     第31位表示线程状态（1非激活 - 负数），
         *     第30～16位表示版本计数；
         *     第0位表示worker线程是否在运行任务(1-scanning，0-busy)，
         *          这里有个小技巧，在创建worker线程的WorkQueue时scanState的第15～0位初始化为ForkJoinPool.workQueues的下标（worker线程的WorkQueue的下标是奇数），
         *          当worker线程运行任务时第0位设置0（busy），任务运行结束第0位又设置1（恢复为奇数），
         *     第15～0又可以表示在ForkJoinPool.workQueues数组的下标索引；
         */

        volatile int scanState;
        int stackPred;             // 当worker线程从激活变为非激活时设置值，且值为ForkJoinPool的ctl的低32位(实际就是当前非激活线程链的top非激活线程），这样就形成了一个非激活线程链 -- 注意
        int nsteals;               // 偷取的任务数
        int hint;                  // 记录偷取者索引
        int config;                // 线程池索引以及模式
        volatile int qlock;        // 1: 锁定, < 0: 终止，0：解锁
        volatile int base;         // 任务队列的队尾，工作窃取就是窃取base指向的任务； -- 初始为4096，因为默认数组大小为8192，4096就是数组中间
        int top;                   // 下一次push的时索引槽位 -- 初始为4096，因为默认数组大小为8192，
        ForkJoinTask<?>[] array;   // 元素数组 - 任务数组 - 最初没有分配
        final ForkJoinPool pool;   // 所在的pool
        final ForkJoinWorkerThread owner; // 所属的worker线程，如果在ForkJoinPool.workQueues数组中下标是奇数，则不为空。
        volatile Thread parker;    // 在执行park阻塞期间的监视器，等于owner，否则就是null -- 即parker不等于null，就等于owner，表示正在阻塞
        volatile ForkJoinTask<?> currentJoin;  // task being joined in awaitJoin
        volatile ForkJoinTask<?> currentSteal; // 当前窃取执行的任务

        WorkQueue(ForkJoinPool pool, ForkJoinWorkerThread owner) {
            this.pool = pool;
            this.owner = owner;
            // Place indices in the center of array (that is not yet allocated)
            base = top = INITIAL_QUEUE_CAPACITY >>> 1;
        }

        /**
         * Returns an exportable index (used by ForkJoinWorkerThread).
         */
        final int getPoolIndex() {
            return (config & 0xffff) >>> 1; // ignore odd/even tag bit
        }

        /**
         * Returns the approximate number of tasks in the queue.
         */
        final int queueSize() {
            // 任务数量
            int n = base - top;       // non-owner callers must read base first
            return (n >= 0) ? 0 : -n; // ignore transient negative
        }

        /**
         * Provides a more accurate estimate of whether this queue has
         * any tasks than does queueSize, by checking whether a
         * near-empty queue has at least one unclaimed task.
         */
        final boolean isEmpty() {
            ForkJoinTask<?>[] a; int n, m, s;
            return ((n = base - (s = top)) >= 0 ||
                    (n == -1 &&           // possibly one task
                     ((a = array) == null || (m = a.length - 1) < 0 ||
                      U.getObject
                      (a, (long)((m & (s - 1)) << ASHIFT) + ABASE) == null)));
        }

        /**
         * Pushes a task. Call only by owner in unshared queues.  (The
         * shared-queue version is embedded in method externalPush.)
         *
         * @param task the task. Caller must ensure non-null.
         * @throws RejectedExecutionException if array cannot be resized
         */
        final void push(ForkJoinTask<?> task) {
            /**
             * 1、首先把任务放入等待队列并更新top位；
             * 2、如果当前 WorkQueue 为新建的等待队列（top-base<=1），则调用signalWork方法为当前 WorkQueue 新建或唤醒一个工作线程；
             * 3、如果 WorkQueue 中的任务数组容量过小，则调用growArray()方法对其进行两倍扩容
             */
            ForkJoinTask<?>[] a; ForkJoinPool p;
            int b = base, s = top, n;
            if ((a = array) != null) {    // ignore if queue removed
                int m = a.length - 1;     // fenced write for task visibility
                U.putOrderedObject(a, ((m & s) << ASHIFT) + ABASE, task); // 将task任务入work线程自己的workQueue中的top位置
                U.putOrderedInt(this, QTOP, s + 1); // 更新自身的top
                if ((n = s - b) <= 1) { // 首次提交，创建或唤醒一个工作线程 --
                    if ((p = pool) != null)
                        p.signalWork(p.workQueues, this);
                }
                else if (n >= m)
                    growArray(); // 扩容
            }
        }

        /**
         * Initializes or doubles the capacity of array. Call either
         * by owner or with lock held -- it is OK for base, but not
         * top, to move while resizings are in progress.
         */
        final ForkJoinTask<?>[] growArray() {
            /*
             * 作用：
             * 1、array为null，初始化一个INITIAL_QUEUE_CAPACITY(该值默认为8192)大小的任务队列，就返回
             * 2、array非空，在原来的array数组容量上扩容一倍，然后进行数组元素迁移
             */
            ForkJoinTask<?>[] oldA = array;//获取内部任务列表
            int size = oldA != null ? oldA.length << 1 : INITIAL_QUEUE_CAPACITY; // 新数组大小：之前的一倍，或者为默认的 8192 的大小
            if (size > MAXIMUM_QUEUE_CAPACITY)
                throw new RejectedExecutionException("Queue capacity exceeded");
            int oldMask, t, b;
            //新建一个两倍容量的任务数组
            ForkJoinTask<?>[] a = array = new ForkJoinTask<?>[size];
            // 老array非null有其中有数组，进行迁移
            if (oldA != null && (oldMask = oldA.length - 1) >= 0 && (t = top) - (b = base) > 0) {
                int mask = size - 1;
                //从老数组中拿出数据，放到新的数组中
                do { // emulate poll from old array, push to new array
                    ForkJoinTask<?> x;
                    int oldj = ((b & oldMask) << ASHIFT) + ABASE;
                    int j    = ((b &    mask) << ASHIFT) + ABASE;
                    x = (ForkJoinTask<?>)U.getObjectVolatile(oldA, oldj);
                    if (x != null && U.compareAndSwapObject(oldA, oldj, x, null))
                        U.putObjectVolatile(a, j, x);
                } while (++b != t);
            }
            return a;
        }

        /**
         * Takes next task, if one exists, in LIFO order.  Call only
         * by owner in unshared queues.
         */
        final ForkJoinTask<?> pop() {
            ForkJoinTask<?>[] a; ForkJoinTask<?> t; int m;
            if ((a = array) != null && (m = a.length - 1) >= 0) {
                for (int s; (s = top - 1) - base >= 0;) {
                    long j = ((m & s) << ASHIFT) + ABASE;
                    if ((t = (ForkJoinTask<?>)U.getObject(a, j)) == null)
                        break;
                    if (U.compareAndSwapObject(a, j, t, null)) {
                        U.putOrderedInt(this, QTOP, s);
                        return t;
                    }
                }
            }
            return null;
        }

        /**
         * 如果b是队列和任务的base，则按FIFO顺序接受任务，可以毫无争议地提出索赔。
         */
        final ForkJoinTask<?> pollAt(int b) {
            ForkJoinTask<?> t; ForkJoinTask<?>[] a;
            if ((a = array) != null) {
                int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                // base任务存在，且base没有被其他人窃取走，弹出base任务
                if ((t = (ForkJoinTask<?>)U.getObjectVolatile(a, j)) != null && base == b && U.compareAndSwapObject(a, j, t, null)) {
                    base = b + 1;
                    return t;
                }
            }
            return null;
        }

        /**
         * Takes next task, if one exists, in FIFO order.
         */
        final ForkJoinTask<?> poll() {
            ForkJoinTask<?>[] a; int b; ForkJoinTask<?> t;
            while ((b = base) - top < 0 && (a = array) != null) {
                int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                t = (ForkJoinTask<?>)U.getObjectVolatile(a, j);
                if (base == b) {
                    if (t != null) {
                        if (U.compareAndSwapObject(a, j, t, null)) {
                            base = b + 1;
                            return t;
                        }
                    }
                    else if (b + 1 == top) // now empty
                        break;
                }
            }
            return null;
        }

        /**
         * Takes next task, if one exists, in order specified by mode.
         */
        final ForkJoinTask<?> nextLocalTask() {
            return (config & FIFO_QUEUE) == 0 ? pop() : poll();
        }

        /**
         * Returns next task, if one exists, in order specified by mode.
         */
        final ForkJoinTask<?> peek() {
            ForkJoinTask<?>[] a = array; int m;
            if (a == null || (m = a.length - 1) < 0)
                return null;
            int i = (config & FIFO_QUEUE) == 0 ? top - 1 : base;
            int j = ((i & m) << ASHIFT) + ABASE;
            return (ForkJoinTask<?>)U.getObjectVolatile(a, j);
        }

        /**
         * 仅当给定任务处于当前顶部时，才会弹出该任务。（共享版本只能通过FJP.tryExternalUnpush获得）
        */
        final boolean tryUnpush(ForkJoinTask<?> t) {
            ForkJoinTask<?>[] a; int s;
            // array非空，且有task在，将top的task置为null，然后更新top索引位置，返回true
            if ((a = array) != null && (s = top) != base && U.compareAndSwapObject(a, (((a.length - 1) & --s) << ASHIFT) + ABASE, t, null)) {
                U.putOrderedInt(this, QTOP, s);
                return true;
            }
            return false;
        }

        /**
         * Removes and cancels all known tasks, ignoring any exceptions.
         */
        final void cancelAll() {
            ForkJoinTask<?> t;
            if ((t = currentJoin) != null) {
                currentJoin = null;
                ForkJoinTask.cancelIgnoringExceptions(t);
            }
            if ((t = currentSteal) != null) {
                currentSteal = null;
                ForkJoinTask.cancelIgnoringExceptions(t);
            }
            while ((t = poll()) != null)
                ForkJoinTask.cancelIgnoringExceptions(t);
        }

        // Specialized execution methods

        /**
         * Polls and runs tasks until empty.
         */
        final void pollAndExecAll() {
            for (ForkJoinTask<?> t; (t = poll()) != null;)
                t.doExec();
        }

        /**
         * Removes and executes all local tasks. If LIFO, invokes
         * pollAndExecAll. Otherwise implements a specialized pop loop
         * to exec until empty.
         */
        final void execLocalTasks() {
            /*
             * 执行并移除所有本地任务，流程：
             * 1、假设为FIFO模式，若自己的workQueue的array中存在任务
             * 2、循环遍历本地任务，直到本地任务执行完毕
             */
            int b = base, m, s;
            ForkJoinTask<?>[] a = array;
            // array中存在任务；注意top是指向下一个push进的位置，减去1才是当前top元素所在位置
            if (b - (s = top - 1) <= 0 && a != null && (m = a.length - 1) >= 0) {
                if ((config & FIFO_QUEUE) == 0) { // config中前16位为并行度，后16位就是存储的模式：FIFO模式默认就是0
                    for (ForkJoinTask<?> t;;) { // 直到本地任务执行完
                        // 获取top上的任务t，并将设为null
                        if ((t = (ForkJoinTask<?>)U.getAndSetObject(a, ((m & s) << ASHIFT) + ABASE, null)) == null)
                            break; // top上任务为null，break
                        U.putOrderedInt(this, QTOP, s); // 更新top
                        t.doExec(); // 执行本地任务
                        if (base - (s = top - 1) > 0) // 直到本地任务执行完
                            break;
                    }
                }
                else
                    pollAndExecAll();//LIFO模式执行，取base任务
            }
        }

        /**
         * Executes the given task and any remaining local tasks.
         */
        final void runTask(ForkJoinTask<?> task) {
            /*
             * 流程：
             * 1、workQueue标记为繁忙状态；
             * 2、执行task的doExec()即运行任务，将当前窃取执行的任务，设置进currentSteal
             * 3、清空currentSteal；
             * 4、执行完窃取的任务后，就需要先把本地的任务执行完；
             * 5、本地任务执行完，更新偷取的任务数+1，注意需要判断溢出，若溢出调用transferStealCount()；
             * 6、将workQueue标记为非繁忙状态；
             */
            if (task != null) {
                // 标记为繁忙状态 -- SCANNING就是1，~SCANE就是1111 1111 .... 1110，因此scanState的最后一位位为0表示繁忙，默认为其在workQueue中索引位置，由于是奇数，因此非繁忙装填
                scanState &= ~SCANNING;
                // 当前窃取执行的任务，设置进currentSteal
                (currentSteal = task).doExec(); // 执行runTask的doExec() -- worker线程就可能会在这里进行执行take.fork()或者task.join()操作
                U.putOrderedObject(this, QCURRENTSTEAL, null); // 执行结束，释放当前窃取的任务currentSteal变量
                // 只有在执行完task中的代码后，才会尝试执行本地任务 -- 因此worker从scan窃取到任务后，并将任务执行完后，就会窃取自己的任务
                execLocalTasks();
                ForkJoinWorkerThread thread = owner;
                if (++nsteals < 0)      // collect on  overflow
                    transferStealCount(pool); // 由于nsteals溢出啦，需要进行处理
                scanState |= SCANNING; // 取消标记为繁忙状态
                if (thread != null)
                    thread.afterTopLevelExec(); ;//执行钩子函数；本地任务和窃取任务执行结束后运行钩子函数
            }
        }

        /**
         * Adds steal count to pool stealCounter if it exists, and resets.
         */
        final void transferStealCount(ForkJoinPool p) {
            AtomicLong sc;
            if (p != null && (sc = p.stealCounter) != null) {
                int s = nsteals;
                nsteals = 0;            // if negative,纠正溢出问题
                sc.getAndAdd((long)(s < 0 ? Integer.MAX_VALUE : s)); // nsteals++的动作已经在runTask中完成啦
            }
        }

        /**
         * If present, removes from queue and executes the given task,
         * or any other cancelled task. Used only by awaitJoin.
         *
         * @return true if queue empty and task not known to be done
         */
        final boolean tryRemoveAndExec(ForkJoinTask<?> task) {
            /**
             * 流程
             * 1、从top位开始自旋向下找到给定任务，如果找到把它从当前 Worker 的任务队列中移除并执行它。
             * 注意返回的参数：
             *      任务队列为空、任务队列中不存在该任务、任务未执行完毕返回true；
             *      期间任务执行完毕，或者任务被自己执行完毕，检查到task已经完成，才返回false
             */
            ForkJoinTask<?>[] a; int m, s, b, n;
            if ((a = array) != null && (m = a.length - 1) >= 0 && task != null) {
                while ((n = (s = top) - (b = base)) > 0) {
                    //从top往下自旋查找
                    for (ForkJoinTask<?> t;;) {      // traverse from s to b
                        long j = ((--s & m) << ASHIFT) + ABASE; // 计算任务索引
                        // 1、遍历的任务为null，到了队尾
                        if ((t = (ForkJoinTask<?>)U.getObject(a, j)) == null)
                            // 若array中没有任务，那么遍历到第一个s为null，那么s+1=top，即array中没有任务，也会返回true
                            // 若array中有任务，那么遍历到最后一个任务s为null，那么s+1不等于top，即array中有任务，会返回false
                            return s + 1 == top;
                        // 2、给定的任务就是索引上的任务
                        else if (t == task) {
                            boolean removed = false;
                            // ① 如果目前遍历到的s就是栈顶，即s+1是top值，就弹出任务
                            if (s + 1 == top) {      // pop 弹出任务
                                if (U.compareAndSwapObject(a, j, task, null)) {
                                    U.putOrderedInt(this, QTOP, s);
                                    removed = true; // 移除成功
                                }
                            }
                            // ② 执行到此：说明task任务不在array的栈顶
                            // base等于b，表示期间没有任何task被窃取走 -- 因为窃取是从base位置窃取的
                            else if (base == b)      // replace with proxy
                                removed = U.compareAndSwapObject(a, j, task, new EmptyTask()); // 非栈顶任务，不能直接移除，应该是占位任务；join任务已经被移除，替换为一个占位任务
                            // ③ 移除成功，就执行任务task
                            if (removed)
                                task.doExec(); // 执行任务
                            break;
                        }
                        // 3、当前遍历的任务非当前任务，在做一次task的任务状态检查，是否已经完成
                        else if (t.status < 0 && s + 1 == top) {
                            if (U.compareAndSwapObject(a, j, t, null))
                                U.putOrderedInt(this, QTOP, s);
                            break;                  // was cancelled
                        }
                        // 4、每次循环一次，n减去1，n初始为array的大小
                        if (--n == 0)
                            return false; // 遍历结束，仍未找到task
                    }

                    // 5、位于外部循环，检查任务task是否结束
                    if (task.status < 0)
                        return false;
                }
            }
            return true;
        }

        /**
         * Pops task if in the same CC computation as the given task,
         * in either shared or owned mode. Used only by helpComplete.
         */
        final CountedCompleter<?> popCC(CountedCompleter<?> task, int mode) {
            int s; ForkJoinTask<?>[] a; Object o;
            if (base - (s = top) < 0 && (a = array) != null) {
                long j = (((a.length - 1) & (s - 1)) << ASHIFT) + ABASE;
                if ((o = U.getObjectVolatile(a, j)) != null &&
                    (o instanceof CountedCompleter)) {
                    CountedCompleter<?> t = (CountedCompleter<?>)o;
                    for (CountedCompleter<?> r = t;;) {
                        if (r == task) {
                            if (mode < 0) { // must lock
                                if (U.compareAndSwapInt(this, QLOCK, 0, 1)) {
                                    if (top == s && array == a &&
                                        U.compareAndSwapObject(a, j, t, null)) {
                                        U.putOrderedInt(this, QTOP, s - 1);
                                        U.putOrderedInt(this, QLOCK, 0);
                                        return t;
                                    }
                                    U.compareAndSwapInt(this, QLOCK, 1, 0);
                                }
                            }
                            else if (U.compareAndSwapObject(a, j, t, null)) {
                                U.putOrderedInt(this, QTOP, s - 1);
                                return t;
                            }
                            break;
                        }
                        else if ((r = r.completer) == null) // try parent
                            break;
                    }
                }
            }
            return null;
        }

        /**
         * Steals and runs a task in the same CC computation as the
         * given task if one exists and can be taken without
         * contention. Otherwise returns a checksum/control value for
         * use by method helpComplete.
         *
         * @return 1 if successful, 2 if retryable (lost to another
         * stealer), -1 if non-empty but no matching task found, else
         * the base index, forced negative.
         */
        final int pollAndExecCC(CountedCompleter<?> task) {
            int b, h; ForkJoinTask<?>[] a; Object o;
            if ((b = base) - top >= 0 || (a = array) == null)
                h = b | Integer.MIN_VALUE;  // to sense movement on re-poll
            else {
                long j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                if ((o = U.getObjectVolatile(a, j)) == null)
                    h = 2;                  // retryable
                else if (!(o instanceof CountedCompleter))
                    h = -1;                 // unmatchable
                else {
                    CountedCompleter<?> t = (CountedCompleter<?>)o;
                    for (CountedCompleter<?> r = t;;) {
                        if (r == task) {
                            if (base == b &&
                                U.compareAndSwapObject(a, j, t, null)) {
                                base = b + 1;
                                t.doExec();
                                h = 1;      // success
                            }
                            else
                                h = 2;      // lost CAS
                            break;
                        }
                        else if ((r = r.completer) == null) {
                            h = -1;         // unmatched
                            break;
                        }
                    }
                }
            }
            return h;
        }

        /**
         * 如果owned且不知道被blcoked，则返回true。
         */
        final boolean isApparentlyUnblocked() {
            Thread wt; Thread.State s;
            // 返回true即运行的情况：
            // 1、scanState是激活状态
            // 2、workQueue的owner非null
            // 3、线程是非阻塞状态
            // 4、线程是非等待状态
            return (scanState >= 0 &&
                    (wt = owner) != null &&
                    (s = wt.getState()) != Thread.State.BLOCKED &&
                    s != Thread.State.WAITING &&
                    s != Thread.State.TIMED_WAITING);
        }

        // Unsafe mechanics. Note that some are (and must be) the same as in FJP
        private static final sun.misc.Unsafe U;
        private static final int  ABASE;
        private static final int  ASHIFT;
        private static final long QTOP;
        private static final long QLOCK;
        private static final long QCURRENTSTEAL;
        static {
            try {
                U = sun.misc.Unsafe.getUnsafe();
                Class<?> wk = WorkQueue.class;
                Class<?> ak = ForkJoinTask[].class;
                QTOP = U.objectFieldOffset(wk.getDeclaredField("top")); // 更新workQueue的top
                QLOCK = U.objectFieldOffset(wk.getDeclaredField("qlock")); // 更新workQueue的qlock
                QCURRENTSTEAL = U.objectFieldOffset(wk.getDeclaredField("currentSteal")); // 更新workQueue的currentSteal
                ABASE = U.arrayBaseOffset(ak);
                int scale = U.arrayIndexScale(ak);
                if ((scale & (scale - 1)) != 0)
                    throw new Error("data type scale not a power of two");
                ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    // static fields (initialized in static initializer below)

    /**
     * Creates a new ForkJoinWorkerThread. This factory is used unless
     * overridden in ForkJoinPool constructors.
     */
    public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory;

    /**
     * Permission required for callers of methods that may start or
     * kill threads.
     */
    private static final RuntimePermission modifyThreadPermission; //启动或杀死线程的方法调用者的权限

    /**
     * Common (static) pool. Non-null for public use unless a static
     * construction exception, but internal usages null-check on use
     * to paranoically avoid potential initialization circularities
     * as well as to simplify generated code.
     */
    static final ForkJoinPool common; // 公共静态pool

    /**
     * Common pool parallelism. To allow simpler use and management
     * when common pool threads are disabled, we allow the underlying
     * common.parallelism field to be zero, but in that case still report
     * parallelism as 1 to reflect resulting caller-runs mechanics.
     */
    static final int commonParallelism; // 并行度，对应内部的common池

    /**
     * Limit on spare thread construction in tryCompensate.
     */
    private static int commonMaxSpares; // 备用线程数，在tryCompenstate使用

    /**
     * Sequence number for creating workerNamePrefix.
     */
    private static int poolNumberSequence; // 创建wokerNamPrefix时的序号

    /**
     * Returns the next sequence number. We don't expect this to
     * ever contend, so use simple builtin sync.
     */
    private static final synchronized int nextPoolId() {
        return ++poolNumberSequence;
    }

    // static configuration constants

    /**
     * Initial timeout value (in nanoseconds) for the thread
     * triggering quiescence to park waiting for new work. On timeout,
     * the thread will instead try to shrink the number of
     * workers. The value should be large enough to avoid overly
     * aggressive shrinkage during most transient stalls (long GCs
     * etc).
     */
    private static final long IDLE_TIMEOUT = 2000L * 1000L * 1000L; // 2sec 线程组赛等待新的任务的超时时间

    /**
     * Tolerance for idle timeouts, to cope with timer undershoots
     */
    private static final long TIMEOUT_SLOP = 20L * 1000L * 1000L;  // 20ms 空闲超时时间，防止timer未命中

    /**
     * The initial value for commonMaxSpares during static
     * initialization. The value is far in excess of normal
     * requirements, but also far short of MAX_CAP and typical
     * OS thread limits, so allows JVMs to catch misuse/abuse
     * before running out of resources needed to do so.
     */
    private static final int DEFAULT_COMMON_MAX_SPARES = 256; // 备用线程数

    /**
     * Number of times to spin-wait before blocking. The spins (in
     * awaitRunStateLock and awaitWork) currently use randomized
     * spins. Currently set to zero to reduce CPU usage.
     *
     * If greater than zero the value of SPINS must be a power
     * of two, at least 4.  A value of 2048 causes spinning for a
     * small fraction of typical context-switch times.
     *
     * If/when MWAIT-like intrinsics becomes available, they
     * may allow quieter spinning.
     */
    private static final int SPINS  = 0; // 组赛前的自旋次数，用在awaitRUnStateLock和awaitWork中

    /**
     * Increment for seed generators. See class ThreadLocal for
     * explanation.
     */
    private static final int SEED_INCREMENT = 0x9e3779b9; // indexSeed的增量

    /*
     * Bits and masks for field ctl, packed with 4 16 bit subfields:
     * AC: Number of active running workers minus target parallelism
     * TC: Number of total workers minus target parallelism
     * SS: version count and status of top waiting thread
     * ID: poolIndex of top of Treiber stack of waiters
     *
     * When convenient, we can extract the lower 32 stack top bits
     * (including version bits) as sp=(int)ctl.  The offsets of counts
     * by the target parallelism and the positionings of fields makes
     * it possible to perform the most common checks via sign tests of
     * fields: When ac is negative, there are not enough active
     * workers, when tc is negative, there are not enough total
     * workers.  When sp is non-zero, there are waiting workers.  To
     * deal with possibly negative fields, we use casts in and out of
     * "short" and/or signed shifts to maintain signedness.
     *
     * Because it occupies uppermost bits, we can add one active count
     * using getAndAddLong of AC_UNIT, rather than CAS, when returning
     * from a blocked join.  Other updates entail multiple subfields
     * and masking, requiring CAS.
     */

    //  低位和高位掩码
    private static final long SP_MASK    = 0xffffffffL; // 32个0 - 32个1
    private static final long UC_MASK    = ~SP_MASK; // 32个1 - 32个0

    // AC 活跃线程数 相关参数
    private static final int  AC_SHIFT   = 48;
    private static final long AC_UNIT    = 0x0001L << AC_SHIFT; // 0000 0000 0000 0001 ....
    private static final long AC_MASK    = 0xffffL << AC_SHIFT; // 1111 1111 1111 1111 ....

    // TC 工作线程数 相关参数
    private static final int  TC_SHIFT   = 32;
    private static final long TC_UNIT    = 0x0001L << TC_SHIFT; // 0000 0000 0000 0000      0000 0000 0000 0001 ....
    private static final long TC_MASK    = 0xffffL << TC_SHIFT; // 0000 0000 0000 0000      1111 1111 1111 1111 ....
    private static final long ADD_WORKER = 0x0001L << (TC_SHIFT + 15); // 0000 0000 0000 0000     1000 0000 ....

    // runState 池状态
    private static final int  RSLOCK     = 1; // runState lock -- 用来锁住runState
    private static final int  RSIGNAL    = 1 << 1; // runState Signal -- 线程获取rs被阻塞前，设置的
    private static final int  STARTED    = 1 << 2; // 线程是否已经已经启动
    private static final int  STOP       = 1 << 29; // 停止状态 SOTP
    private static final int  TERMINATED = 1 << 30; // 终止状态 TERMINATED
    private static final int  SHUTDOWN   = 1 << 31; // 关闭状态 已为负数 SHUTDOWN

    /**
     * 说明： ForkJoinPool 的内部状态都是通过一个64位的 long 型 变量ctl来存储，它由四个16位的子域组成：
     *
     * AC：高16位(63~48)表示活跃的线程，值为活跃线程数减去parallelism（补码表示），初始值是0-parallelism,工作线程激活则加1，去激活则减1。当累积加了parallelism时第63bit位翻转为0，则不允许再激活工作线程。
     * TC：表示当前所有工作线程(包括未激活的)，值为所有工作线程数-parallelism（补码表示），创建线程则加1，终止线程则减1。当累积加了parallelism时第47位翻转位0，则不允许再创建线程；
     * SS：栈顶等待线程的版本计数和状态，中低16位
     * ID：栈顶 WorkQueue 在池中的索引（poolIndex），低16位
     * 假设 parallelism为12，1111 1111 1111 0100就是-12
     * AC：正在运行工作线程数减去目标并行度，高16位
     * TC：总工作线程数减去目标并行度，中高16位
     * SS：非激活线程链中top线程的版本计数和线程状态，与第15～0位合起来看
     * ID：表示非激活线程链中top线程的本地WorkQueue在ForkJoinPool.workQueues数组中下标索引，第31～0位合起来的值实际是非激活线程链中top线程的本地WorkQueue.scanState
     *      注意：第31～0位合起来的值实际是非激活线程链中top线程的本地WorkQueue.scanState
     * 那么ctl就是 1111 1111 1111 0100      1111 1111 111 1010      0000 0000 0000 0000     0000 0000 0000 0000
     * ctl更新就是 1111 1111 1111 0101      0000 0000 0000 0000     1000 0000 0000 0000     0000 0000 0000 0101
     * 表示：AC初始为-12，现在为-11，表示有一个激活的线程。TC初始化we为-12，现在为0，表示所有的线程都已经被创建。因此有11个空闲线程。
     * SS默认为全0，ID默认为全0，后续，id变为5表示第一个空闲的worker线程在workQueue中的5号索引位上
     */
    // 实例字段
    volatile long ctl;                   // 线程池主要控制参数
    volatile int runState;               // 运行状态锁 最低位1|0用来作为锁状态，
    final int config;                    // 并行度|模式 = 是创建ForkJoinPool的配置，int类型32bits，高16位表示pool的mode（FIFO或LIFO）,低16位表示parallelism（并行度，默认大小可用处理器数java.lang.Runtime#availableProcessors）；
    int indexSeed;                       // 用户生成工作线程索引 -- 防止index冲突的随机数种子
    volatile WorkQueue[] workQueues;     // 主对象注册信息，workQueue
    final ForkJoinWorkerThreadFactory factory; // 线程工厂
    final UncaughtExceptionHandler ueh;  // 每个工作线程的异常信息
    final String workerNamePrefix;       // 用于创建工作线程的名称
    volatile AtomicLong stealCounter;    // 工作窃取的计数器

    /**
     * 获取运行状态锁；返回当前（锁定）运行状态。
     */
    private int lockRunState() {
        int rs;
        // 分析
        // runState 最低位为1表示已锁住
        // 若runState 最低位为0，并且尝试CAS更新成功，最低位为1，表示锁住
        // 否则调用awaitRunStateLock()
        return ((((rs = runState) & RSLOCK) != 0 || !U.compareAndSwapInt(this, RUNSTATE, rs, rs |= RSLOCK))
                ? awaitRunStateLock() : rs);
    }

    /**
     * Spins and/or blocks until runstate lock is available.  See
     * above for explanation.
     */
    private int awaitRunStateLock() {
        // 经典的spin+lock，直到运行状态锁可用。
        Object lock;
        boolean wasInterrupted = false;
        for (int spins = SPINS, r = 0, rs, ns;;) {
            // 是否为解锁状态
            if (((rs = runState) & RSLOCK) == 0) {
                if (U.compareAndSwapInt(this, RUNSTATE, rs, ns = rs | RSLOCK)) {
                    if (wasInterrupted) {
                        try {
                            Thread.currentThread().interrupt();
                        } catch (SecurityException ignore) {
                        }
                    }
                    return ns;
                }
            }
            // 执行到此：随机自旋，下面可以看见spins--，取决于r值，因此是一个随机自旋次数的做法
            else if (r == 0)
                r = ThreadLocalRandom.nextSecondarySeed();
            // 自旋次数大于0，将spins做减法
            else if (spins > 0) {
                r ^= r << 6; r ^= r >>> 21; r ^= r << 7; // xorshift
                if (r >= 0)
                    --spins;
            }
            // 执行到此：随机自旋次数用完
            // 操作：检查rs是否为STARTED，或者，stealCounter等于空
            else if ((rs & STARTED) == 0 || (lock = stealCounter) == null)
                Thread.yield();   // initialization race
            // 更新rs将倒数第二位更新为1
            else if (U.compareAndSwapInt(this, RUNSTATE, rs, rs | RSIGNAL)) {
                // lcok就是stealCounter
                synchronized (lock) {
                    if ((runState & RSIGNAL) != 0) {
                        try {
                            lock.wait(); // ①  线程由自旋转为主食啊
                        } catch (InterruptedException ie) {
                            if (!(Thread.currentThread() instanceof ForkJoinWorkerThread))
                                wasInterrupted = true;
                        }
                    }
                    else
                        lock.notifyAll(); // 第二个获取rs阻塞的线程会将第一个线程唤醒
                }
            }
        }
    }

    /**
     * Unlocks and sets runState to newRunState.
     *
     * @param oldRunState a value returned from lockRunState
     * @param newRunState the next value (must have lock bit clear).
     */
    private void unlockRunState(int oldRunState, int newRunState) {
        if (!U.compareAndSwapInt(this, RUNSTATE, oldRunState, newRunState)) {
            Object lock = stealCounter;
            runState = newRunState;              // clears RSIGNAL bit
            if (lock != null)
                synchronized (lock) { lock.notifyAll(); } // 和awaitRunStateLock() ①做呼应
        }
    }

    // Creating, registering and deregistering workers

    /**
     * Tries to construct and start one worker. Assumes that total
     * count has already been incremented as a reservation.  Invokes
     * deregisterWorker on any failure.
     *
     * @return true if successful
     */
    private boolean createWorker() {
        /**
         * 说明：
         * 1、createWorker首先通过线程工厂创一个新的ForkJoinWorkerThread，
         * 2、然后启动这个工作线程（wt.start()）。如果期间发生异常，调用deregisterWorker处理线程创建失败的逻辑（deregisterWorker在后面再详细说明）。
         */
        ForkJoinWorkerThreadFactory fac = factory;
        Throwable ex = null;
        ForkJoinWorkerThread wt = null;
        try {
            // 注意几点：
            // 1、创建出来的worker的workQueue的array中是没有任何任务的
            // 2、worker需要去别的workQueue上偷取任务才可以
            if (fac != null && (wt = fac.newThread(this)) != null) {
                wt.start(); // 线程启动后，当为线程分配到CPU执行时间片之后会运行 ForkJoinWorkerThread 的run方法开启线程来执行任务。工作线程执行任务的流程我们在讲完内部任务提交之后会统一讲解。
                return true;
            }
        } catch (Throwable rex) {
            ex = rex;
        }
        deregisterWorker(wt, ex);//线程创建失败处理
        return false;
    }

    /**
     * Tries to add one worker, incrementing ctl counts before doing
     * so, relying on createWorker to back out on failure.
     *
     * @param c incoming ctl value, with total count negative and no
     * idle workers.  On CAS failure, c is refreshed and retried if
     * this holds (otherwise, a new worker is not needed).
     */
    private void tryAddWorker(long c) {
        /**
         * 尝试添加worker，主要任务检查状态、修改状态：前提 -- TC非0，允许创建worker
         * 1、修改ctl，若线程池非STOP状态，将AC与TC都加1，更新ctl
         * 2、释放rs的锁
         * 3、更新ctl成功，调用createWorker()创建worker -- 真实创建worker的地方
         */
        boolean add = false;
        do {
            // 添加 AC 和 TC 即活跃线程数和工作线程数，之前为-12，现在改为-11 1111 1111 1111 0101
            long nc = ((AC_MASK & (c + AC_UNIT)) | (TC_MASK & (c + TC_UNIT)));
            if (ctl == c) {
                int rs, stop;                 // check if terminating
                if ((stop = (rs = lockRunState()) & STOP) == 0) // 非STOP状态
                    add = U.compareAndSwapLong(this, CTL, c, nc); // 更新ctl
                unlockRunState(rs, rs & ~RSLOCK); // 释放锁
                if (stop != 0)
                    break;
                if (add) {
                    createWorker();//创建工作线程
                    break;
                }
            }
        // ctl & ADD_WORKER 就是取TC的最高位，因为TC为-10表示还可以创建10个worker，为全0时，即最高位为0就表示无法创建worker
        // 若这里等于0 -- 说明由于多线程竞争，有线程已经将最后一个worker线程给创建啦
        } while (((c = ctl) & ADD_WORKER) != 0L && (int)c == 0);
    }

    /**
     * Callback from ForkJoinWorkerThread constructor to establish and
     * record its WorkQueue.
     *
     * @param wt the worker thread
     * @return the worker's queue
     */
    final WorkQueue registerWorker(ForkJoinWorkerThread wt) {
        /*
         * 说明：registerWorker是 ForkJoinWorkerThread 构造器的回调函数，
         * 用于创建和记录工作线程的 WorkQueue。比较简单，就不多赘述了。
         * 注意在此为工作线程创建的 WorkQueue 是放在奇数索引的（代码行：i = ((s << 1) | 1) & m;）
         */
        UncaughtExceptionHandler handler;
        wt.setDaemon(true);                           // configure thread
        if ((handler = ueh) != null)
            wt.setUncaughtExceptionHandler(handler);
        WorkQueue w = new WorkQueue(this, wt); //构造新的WorkQueue
        int i = 0;                                    // assign a pool index
        int mode = config & MODE_MASK;
        int rs = lockRunState();
        try {
            WorkQueue[] ws; int n;                    // skip if no array
            if ((ws = workQueues) != null && (n = ws.length) > 0) {
                //生成新建WorkQueue的索引
                int s = indexSeed += SEED_INCREMENT;  // 这种计算在2的幂次方中不容易发生碰撞
                int m = n - 1;
                i = ((s << 1) | 1) & m;               // 为工作线程创建的 WorkQueue 是放在奇数索引
                if (ws[i] != null) {                  // 发生碰撞
                    int probes = 0;                   // step by approx half n
                    int step = (n <= 4) ? 2 : ((n >>> 1) & EVENMASK) + 2;
                    while (ws[i = (i + step) & m] != null) {
                        // 线性探测，若probes从0添加到大于等于n，说明需要扩容
                        if (++probes >= n) { //所有索引位都被占用，对workQueues进行扩容一倍
                            workQueues = ws = Arrays.copyOf(ws, n <<= 1);
                            m = n - 1;
                            probes = 0;
                        }
                    }
                }
                w.hint = s;                           // use as random seed
                w.config = i | mode;
                w.scanState = i;                      // publication fence
                ws[i] = w;
            }
        } finally {
            unlockRunState(rs, rs & ~RSLOCK);
        }
        wt.setName(workerNamePrefix.concat(Integer.toString(i >>> 1)));
        return w;
    }

    /**
     * Final callback from terminating worker, as well as upon failure
     * to construct or start a worker.  Removes record of worker from
     * array, and adjusts counts. If pool is shutting down, tries to
     * complete termination.
     *
     * @param wt the worker thread, or null if construction failed
     * @param ex the exception causing failure, or null if none
     */
    final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex) {
        WorkQueue w = null;
        //1.移除workQueue
        if (wt != null && (w = wt.workQueue) != null) {
            WorkQueue[] ws;                           // remove index from array
            int idx = w.config & SMASK; //计算workQueue索引
            int rs = lockRunState();
            if ((ws = workQueues) != null && ws.length > idx && ws[idx] == w)
                ws[idx] = null; //移除workQueue
            unlockRunState(rs, rs & ~RSLOCK);
        }
        long c;                                       // decrement counts
        //2.减少CTL数
        do {} while (!U.compareAndSwapLong
                     (this, CTL, c = ctl, ((AC_MASK & (c - AC_UNIT)) |
                                           (TC_MASK & (c - TC_UNIT)) |
                                           (SP_MASK & c))));
        //3.处理被移除workQueue内部相关参数
        if (w != null) {
            w.qlock = -1;                             // ensure set
            w.transferStealCount(this);
            w.cancelAll();                            // 取消剩余的任务
        }
        //4.如果线程未终止，替换被移除的workQueue并唤醒内部线程
        for (;;) {                                    // possibly replace
            WorkQueue[] ws; int m, sp;
            //尝试终止线程池
            if (tryTerminate(false, false) || w == null || w.array == null ||
                (runState & STOP) != 0 || (ws = workQueues) == null ||
                (m = ws.length - 1) < 0)              // already terminating
                break;
            //唤醒被替换的线程，依赖于下一步
            if ((sp = (int)(c = ctl)) != 0) {         // wake up replacement
                if (tryRelease(c, ws[sp & m], AC_UNIT))
                    break;
            }
            //创建工作线程替换
            else if (ex != null && (c & ADD_WORKER) != 0L) {
                tryAddWorker(c);                      // create replacement
                break;
            }
            else                                      // don't need replacement
                break;
        }
        //5.处理异常
        if (ex == null)                               // help clean on way out
            ForkJoinTask.helpExpungeStaleExceptions();
        else                                          // rethrow
            ForkJoinTask.rethrow(ex);
    }

    // Signalling

    /**
     * Tries to create or activate a worker if too few are active.
     *
     * @param ws the worker array to use to find signallees
     * @param q a WorkQueue --if non-null, don't retry if now empty
     */
    final void signalWork(WorkQueue[] ws, WorkQueue q) {
        /*
         * 说明：新建或唤醒一个工作线程，在externalPush、externalSubmit、workQueue.push、scan中调用。【优先唤醒，无空闲worker才创建】
         * 1、首先判断是否有worker可以被激活【唤醒或者创建】，即ctl小于0，即表示有多少个worker可以被激活【-10表示有个10个worker可以被激活】
         * 2、再次判断ctl后32位，是否有空闲的worker，
         * 3、若没有空闲的worker，我们才调用tryAddWorker()创建worker()
         * 3、若有空闲的worker，那么就检查各种状态、修改状态值，然后唤醒worker
         */
        long c; int sp, i; WorkQueue v; Thread p;
        // ctl小于0，说明worker可唤醒 -- 因为前16位是AC表示激活的线程数，当AC从-12变为0，就表示所有worker都在工作，无法进入以下循环
        while ((c = ctl) < 0L) {
            // 1、c的后32位存储的就是空闲线程链的top的scanState的值，如果为0表示没有空闲线程链，因此这里表示没有空闲线程
            if ((sp = (int)c) == 0) {                  // 注意这一步：ctl是long值，通过int做完强制转换后，就是ctl的后32位
                if ((c & ADD_WORKER) != 0L)
                    tryAddWorker(c); // 工作线程太少，添加新的工作线程，如果是添加worker成功，那么就会从AC以及TC都从-12变为-11，TC的-12表示总共只能创建12个线程，TC只能一直加到为0后就无变化
                break;
            }
            // 执行到这：说明有空闲的worker

            //连续检查状态 - ws、空闲worker的索引位置、空闲worker的workQueue
            if (ws == null)                            // 过程中pool已被终止，导致ws清空
                break;
            if (ws.length <= (i = sp & SMASK))         // sp存储就是空闲线程的ScanState值，其中后0-16位存储的就是该top空闲worker在ws中的索引位置
                break;
            if ((v = ws[i]) == null)                   // 终止
                break;

            // 执行到这：说明各种状态检查通过
            // 计算ctl，加上版本戳SS_SEQ避免ABA问题
            // 假设sp为 0000 0000 0000 0000   0000 0000 0000 0101,SS_SEQ为 1 0000 0000 0000 0000，INACTIVE为 1000 0000 ...
            // 结果vs为：0000 0000 0000 0001   0000 0000 0000 0101
            int vs = (sp + SS_SEQ) & ~INACTIVE;        // 下一个scanState值，将worker唤醒，将scanState的最高位设置为0，【1表示失活】
            int d = sp - v.scanState;                  // 在默认情况下的，版本号一致与索引位置一致下，d等于0
            // 更新活跃的线程数，比如AC之前为-10，现在激活一个变为-9，并且由于空闲worker链的top已经被更换，因此更换ctl中的后32位为当前top的后一个空闲worker即stackPred
            long nc = (UC_MASK & (c + AC_UNIT)) | (SP_MASK & v.stackPred);
            if (d == 0 && U.compareAndSwapLong(this, CTL, c, nc)) {
                v.scanState = vs;                      // 激活v
                if ((p = v.parker) != null)
                    U.unpark(p); ;//唤醒阻塞线程
                break;
            }
            if (q != null && q.base == q.top)          // no more work
                break;
        }
    }

    /**
     * 唤醒以及释放worker v，前提是v是空闲worker的链表头，则发出信号并释放workerv。
     * 仅当（显然）至少有一名空闲worker时，才会执行一次性信号工作。
     *
     * @param c incoming ctl value
     * @param v if non-null, a worker
     * @param inc the increment to active count (zero when compensating)
     * @return true if successful
     */
    private boolean tryRelease(long c, WorkQueue v, long inc) {
        int sp = (int)c, vs = (sp + SS_SEQ) & ~INACTIVE; Thread p;
        //ctl低32位等于scanState，说明可以唤醒parker线程
        if (v != null && v.scanState == sp) {          // v is at top of stack
            //计算活跃线程数（高32位）并更新为下一个栈顶的scanState（低32位）
            long nc = (UC_MASK & (c + inc)) | (SP_MASK & v.stackPred);
            if (U.compareAndSwapLong(this, CTL, c, nc)) {
                v.scanState = vs;
                if ((p = v.parker) != null)
                    U.unpark(p); //唤醒线程
                return true;
            }
        }
        return false;
    }

    // Scanning for tasks

    /**
     * Top-level runloop for workers, called by ForkJoinWorkerThread.run.
     */
    final void runWorker(WorkQueue w) {
        /*
         * runWorker是 ForkJoinWorkerThread 的主运行方法，用来依次执行当前工作线程中的任务。
         * 函数流程很简单：
         * 0、才创建的worker调用ForkJoinPoolThread.run()后就会进入该方法，此刻对应的array是null，因此需要去growArray()中创建任务队列
         * 1、调用scan方法获取任务[不一定是本队列的任务，可以从别的队列窃取的任务]，然后由该worker执行该任务t
         * 2、如果scan没有获取到任务，则调用awaitWork等待，直到工作线程/线程池终止或等待超时。
         */
        w.growArray();                   // worke线程第一次进入runWorker()时需要初始化array
        int seed = w.hint;               // initially holds randomization hint
        int r = (seed == 0) ? 1 : seed;  // 由于后续xor异或的移动，需要避免r等于0的情况
        /**
         *
         */
        for (ForkJoinTask<?> t;;) {
            if ((t = scan(w, r)) != null) // 扫描任务执行
                w.runTask(t); // 执行窃取任务和本地任务 -- 窃取任务中可能会执行fork或者join，调用fork主要是push到当前workQueue中，后续又可能被自己处理掉
            /*
             * 执行到此处：
             * scan()就返回null，同时做了以下处理：
             * 1、将workQueue会被设置为失活状态，同时成为最后一个等待者
             * 2、crl的后32位已经指向当前worker的scanState的值；
             * 3、当前workQueue的stackPred就是前一个等待者
             */
            else if (!awaitWork(w, r)) // 窃取不到任务，即整个workQueues中都没有任务可执行，需要进入阻塞，
                break; // awaitWork()返回false，表示需要终止这个worker，一路返回到run()中的deregisterWorker取消worker的注册
            r ^= r << 13;
            r ^= r >>> 17;
            r ^= r << 5; // xorshift
        }
    }

    /**
     * Scans for and tries to steal a top-level task. Scans start at a
     * random location, randomly moving on apparent contention,
     * otherwise continuing linearly until reaching two consecutive
     * empty passes over all queues with the same checksum (summing
     * each base index of each queue, that moves on each steal), at
     * which point the worker tries to inactivate and then re-scans,
     * attempting to re-activate (itself or some other worker) if
     * finding a task; otherwise returning null to await work.  Scans
     * otherwise touch as little memory as possible, to reduce
     * disruption on other scanning threads.
     *
     * @param w the worker (via its WorkQueue)
     * @param r a random seed
     * @return a task, or null if none found
     */
    private ForkJoinTask<?> scan(WorkQueue w, int r) {
        /**
         * 说明：扫描并尝试偷取一个任务。使用w.hint进行随机索引 WorkQueue，也就是说并不一定会执行当前 WorkQueue 中的任务，而是偷取别的Worker的任务来执行。
         * 函数的大概执行流程如下：
         *
         * 1、取随机位置的一个 WorkQueue；
         * 2、获取base位的 ForkJoinTask，成功取到后更新base位并返回任务；如果取到的 WorkQueue 中任务数大于1，则调用signalWork创建或唤醒其他工作线程；
         * 3、如果当前工作线程处于不活跃状态（INACTIVE），则调用tryRelease尝试唤醒ws的栈顶工作线程来执行。
         * 4、如果base位任务为空或发生偏移，则对索引位进行随机移位，然后重新扫描；
         * 5、如果扫描整个workQueues之后没有获取到任务，则设置当前工作线程为INACTIVE状态；然后重置checkSum，再次扫描一圈之后如果还没有任务则跳出循环返回null。
         *
         */
        // r 是一个随机的hash值，通过 r & m 就可以后去初始的随机的扫描起点
        WorkQueue[] ws; int m;
        if ((ws = workQueues) != null && (m = ws.length - 1) > 0 && w != null) {
            int ss = w.scanState;                     // initially non-negative
            // 初始扫描起点，自旋扫描 -- 一个个的向下扫描
            for (int origin = r & m, k = origin, oldSum = 0, checkSum = 0;;) {
                WorkQueue q; ForkJoinTask<?>[] a; ForkJoinTask<?> t;
                int b, n; long c;
                // 1、获取随机的不为null的workQueue -- 不一定是worker自己的workQueue
                if ((q = ws[k]) != null) {
                    // ① 非空array，workQueue中有任务可执行
                    if ((n = (b = q.base) - q.top) < 0 && (a = q.array) != null) {
                        long i = (((a.length - 1) & b) << ASHIFT) + ABASE; // 计算base位置的偏移量 -- 因此去获取任意一个workQueue的base位置的任务
                        // (1) 取出的base位置的任务不为null
                        if ((t = ((ForkJoinTask<?>) U.getObjectVolatile(a, i))) != null && q.base == b) {
                            // a. work自身的workQueue是激活状态，才可以继续执行
                            if (ss >= 0) {
                                if (U.compareAndSwapObject(a, i, t, null)) { // 随机workQueue的array中位置base上的任务更新为null
                                    q.base = b + 1;   // 更新base
                                    if (n < -1)       // 假设n=-3，表示这个worker中base和top相隔3个举例，这个worker取走一个，还剩两个，需要去唤醒别的worker或者创建worker来执行
                                        signalWork(ws, q);
                                    return t; // 返回窃取到的任务
                                }
                            }
                            // b. work自身的workQueue不是激活状态，且oldSum为0
                            else if (oldSum == 0 && w.scanState < 0)
                                tryRelease(c = ctl, ws[m & (int)c], AC_UNIT); // 如果当前工作线程处于不活跃状态（INACTIVE），则调用tryRelease尝试唤醒ws的栈顶工作线程来执行
                        }
                        // (2) base位置任务为空或base位置偏移，重新检查ss
                        if (ss < 0)                   // work自身的workQueue已经被其他线程给弄成非活跃状态
                            ss = w.scanState;         // 重新获取scanState
                        // 为什么总是下面这4步：这个是经历过验证能够最大程序做到hash，减少冲突的方法
                        r ^= r << 1;
                        r ^= r >>> 3;
                        r ^= r << 10;
                        origin = k = r & m;           // 移动origin和k，找其他的workQueue
                        oldSum = checkSum = 0;        // 恢复到遍历前的状态
                        continue;
                    }
                    // ② 随机到的workQueue中的array的队列任务为空，记录base位
                    checkSum += b;
                }
                // 2、更新索引k 继续向后查找
                if ((k = (k + 1) & m) == origin) {    // continue until stable
                    // ① 运行到这里说明已经扫描了全部的 workQueues，但并未扫描到任务

                    // ② worker是激活状态，并且整个array的校验和正确
                    if ((ss >= 0 || (ss == (ss = w.scanState))) && oldSum == (oldSum = checkSum)) {
                        // （1）多线程原因 -- workerQueue已经为非活跃的
                        if (ss < 0 || w.qlock < 0)
                            break; // 已经被灭活或终止,跳出循环

                        /*
                         * 接下来对当前的WorkQueue进行灭活操作
                         * 1、更新runstate，设置为INACTIVE状态；
                         * 2、更新ctl的活跃线程数-1；
                         * 3、更新ctl的记录的最后一个等待者即当前worker失活后，其scanState需要被记录在ctl的后32位
                         * 4、更新当前workQueue的stackPred，即为当前ctl的记录的前一个最后等待者 - 形成worker等待者链表
                         * 5、设置灭活成功后，在多次循环后，最终在 2-②-(1)中退出循环
                         */
                        int ns = ss | INACTIVE;       // 将scanState最高位设置为-1，表示失活 -- 处于未激活状态
                        // 计算ctl为INACTIVE状态并减少活跃线程数
                        long nc = ((SP_MASK & ns) | (UC_MASK & ((c = ctl) - AC_UNIT)));
                        w.stackPred = (int)c;         // 记录前一个栈顶的ctl
                        U.putInt(w, QSCANSTATE, ns); // 更新当前workQueue的状态为灭火的
                        //修改scanState为inactive状态
                        if (U.compareAndSwapLong(this, CTL, c, nc)) //更新当前的ctl为scanState为灭活状态
                            ss = ns;
                        else
                            w.scanState = ss;         // back out
                    }
                    checkSum = 0; // 重置checkSum，继续循环
                }
            }
        }
        return null; // 窃取失败
    }

    /**
     * Possibly blocks worker w waiting for a task to steal, or
     * returns false if the worker should terminate.  If inactivating
     * w has caused the pool to become quiescent, checks for pool
     * termination, and, so long as this is not the only worker, waits
     * for up to a given duration.  On timeout, if ctl has not
     * changed, terminates the worker, which will in turn wake up
     * another worker to possibly repeat this process.
     *
     * @param w the calling worker
     * @param r a random seed (for spins)
     * @return false if the worker should terminate
     */
    private boolean awaitWork(WorkQueue w, int r) {
        /*
         * 说明：回到runWorker方法，如果scan方法未扫描到任务，会调用awaitWork等待获取任务。
         * 函数的具体执行流程大家看源码，这里简单说一下：
         * 0、在等待获取任务期间，如果当前workQueue为激活状态，不需要阻塞
         * 1、在等待获取任务期间，如果工作线程或线程池已经终止则直接返回false。
         * 2、期间不断随机自旋，潘墩workQueue是否被激活，同时若下一个空闲者已经被唤醒，重置自旋次数 -- 表示有希望下一个就是自己
         * 3、自旋次数用完，且当前workQueue已被终止，返回false
         * 4、当前workQueue没有被终止，判断线程池中的活跃线程数量
         *      若活跃线程数为0，就调用tryTerminate()，然后返回false
         *      若线程池就是STOP状态，直接返回false
         *      若活跃线程数为0，调用tryTerminate()失败，且自己是最后一个等待者
         *          更新ctl，若发现创建的线程数已经超过并行度+2，直接返回false
         *          否则，计算空闲超时时间
         * 5、准备阻塞工作
         */
        if (w == null || w.qlock < 0)                 // 线程池正在被终止，w已被设为null
            return false;
        // pred表示在空闲worker栈中，当前workQueue的下一个空闲worker，其ctl后32位记录的即使空闲栈顶的【一般来说，此刻ctl的记录的top就是当前work】
        for (int pred = w.stackPred, spins = SPINS, ss;;) {
            // 1、workQueue正在扫描，跳出循环  - scanState大于0，表示最高位为1，即激活状态 - 不需要阻塞
            if ((ss = w.scanState) >= 0)
                break; // ++++++++++++ 退出循环还可以执行的地方
            // 2、随机自旋
            else if (spins > 0) {
                r ^= r << 6;
                r ^= r >>> 21;
                r ^= r << 7;
                if (r >= 0 && --spins == 0) {         // 随机自旋次数
                    WorkQueue v; WorkQueue[] ws; int s, j; AtomicLong sc;
                    if (pred != 0 && (ws = workQueues) != null &&
                        (j = pred & SMASK) < ws.length && // 前继者空闲worker的scanState存在，scanState的后16位就是索引，因此索引正常
                        (v = ws[j]) != null &&        // see if pred parking
                        (v.parker == null || v.scanState >= 0))
                        spins = SPINS;                // 更新自旋次数 -- 若前继者空闲者已经被唤醒
                }
            }
            // 3、自旋次数用完，且当前workQueue已经终止
            else if (w.qlock < 0)                     // qlock小于0，当前workQueue已经终止，返回false recheck after spins
                return false;
            // 4、当前workQueue没有被终止，判断线程是否被中断，并清除中断状态
            else if (!Thread.interrupted()) {
                long c, prevctl, parkTime, deadline;
                int ac = (int)((c = ctl) >> AC_SHIFT) + (config & SMASK); // 活跃线程数,假设ctl高16位 1111 1111 1111 0100 加上 并行度
                // ① 无active线程，尝试终止；然后返回false或者线程池是终止状态
                if ((ac <= 0 && tryTerminate(false, false)) ||   //
                    (runState & STOP) != 0)
                    return false;
                // ② 无active线程，尝试终止失败；且当前worker是最后一个等待者，ctl的后32位就是存储的空闲者链最后一个值
                if (ac <= 0 && ss == (int)c) {
                    // 计算活跃线程数（高32位）并更新为下一个栈顶的scanState（低32位）
                    prevctl = (UC_MASK & (c + AC_UNIT)) | (SP_MASK & pred); // 操作：前继者的pred的scanState，或上，UC_MASK用于获取高32位，并且见c中AC+1
                    int t = (short)(c >>> TC_SHIFT);  // 获取c的高32位，然后用short截取到其中的低16位，即TC值
                    if (t > 2 && U.compareAndSwapLong(this, CTL, c, prevctl)) // tc原本是-12，现在变为2，表示创建的总线程过量
                        return false;                 // else use timed wait
                    // 计算空闲超时时间
                    parkTime = IDLE_TIMEOUT * ((t >= 0) ? 1 : 1 - t);
                    deadline = System.nanoTime() + parkTime - TIMEOUT_SLOP;
                }
                // ③ 线程池中有活跃的线程数，或者线程池中没有活跃的线程数，但自己不是最后一个等待者
                else
                    prevctl = parkTime = deadline = 0L;

                // 设置+阻塞操作
                Thread wt = Thread.currentThread();
                U.putObject(wt, PARKBLOCKER, this);   // emulate LockSupport
                w.parker = wt; // 设置parker，准备阻塞
                if (w.scanState < 0 && ctl == c)      // 阻塞前再次检查，仍然是否未激活状态
                    U.park(false, parkTime); //阻塞指定的时间

                // 唤醒的后续操作
                U.putOrderedObject(w, QPARKER, null);
                U.putObject(wt, PARKBLOCKER, null);
                if (w.scanState >= 0) //正在扫描，说明获取到任务，跳出循环 ++++++++++++ 退出循环还可以执行的地方
                    break;

                // 仍未唤醒后未等到任务，且超时，则更新ctl，返回false，终止当前worker
                if (parkTime != 0L && ctl == c &&
                    deadline - System.nanoTime() <= 0L &&
                    U.compareAndSwapLong(this, CTL, c, prevctl))
                    return false;                     // shrink pool
            }
        }
        return true;
    }

    // Joining tasks

    /**
     * Tries to steal and run tasks within the target's computation.
     * Uses a variant of the top-level algorithm, restricted to tasks
     * with the given task as ancestor: It prefers taking and running
     * eligible tasks popped from the worker's own queue (via
     * popCC). Otherwise it scans others, randomly moving on
     * contention or execution, deciding to give up based on a
     * checksum (via return codes frob pollAndExecCC). The maxTasks
     * argument supports external usages; internal calls use zero,
     * allowing unbounded steps (external calls trap non-positive
     * values).
     *
     * @param w caller
     * @param maxTasks if non-zero, the maximum number of other tasks to run
     * @return task status on exit
     */
    final int helpComplete(WorkQueue w, CountedCompleter<?> task, int maxTasks) {
        WorkQueue[] ws; int s = 0, m;
        if ((ws = workQueues) != null && (m = ws.length - 1) >= 0 && task != null && w != null) {
            int mode = w.config;                 // 并发度 | 模式
            int r = w.hint ^ w.top;              // arbitrary seed for origin
            int origin = r & m;                  // 队伍第一个遍历的索引位置
            int h = 1;                           // 1:运行, >1:以包含, <0:hash值
            for (int k = origin, oldSum = 0, checkSum = 0;;) {
                CountedCompleter<?> p; WorkQueue q;
                // 1、task已完成直接break
                if ((s = task.status) < 0)
                    break;
                // 2、
                if (h == 1 && (p = w.popCC(task, mode)) != null) {
                    p.doExec();                  // run local task
                    if (maxTasks != 0 && --maxTasks == 0)
                        break;
                    origin = k;                  // reset
                    oldSum = checkSum = 0;
                }
                else {                           // poll other queues
                    if ((q = ws[k]) == null)
                        h = 0;
                    else if ((h = q.pollAndExecCC(task)) < 0)
                        checkSum += h;
                    if (h > 0) {
                        if (h == 1 && maxTasks != 0 && --maxTasks == 0)
                            break;
                        r ^= r << 13; r ^= r >>> 17; r ^= r << 5; // xorshift
                        origin = k = r & m;      // move and restart
                        oldSum = checkSum = 0;
                    }
                    else if ((k = (k + 1) & m) == origin) {
                        if (oldSum == (oldSum = checkSum))
                            break;
                        checkSum = 0;
                    }
                }
            }
        }
        return s;
    }

    /**
     * Tries to locate and execute tasks for a stealer of the given
     * task, or in turn one of its stealers, Traces currentSteal ->
     * currentJoin links looking for a thread working on a descendant
     * of the given task and with a non-empty queue to steal back and
     * execute tasks from. The first call to this method upon a
     * waiting join will often entail scanning/search, (which is OK
     * because the joiner has nothing better to do), but this method
     * leaves hints in workers to speed up subsequent calls.
     *
     * @param w caller
     * @param task the task to join
     */
    private void helpStealer(WorkQueue w, ForkJoinTask<?> task) {
        /**
         * 如果队列为空或任务执行失败，说明任务可能被偷，调用此方法来帮助偷取者执行任务。基本思想是：偷取者帮助我执行任务，我去帮助偷取者执行它的任务。
         * 函数执行流程如下：
         *
         * 1、循环定位偷取者，由于Worker是在奇数索引位，所以每次会跳两个索引位。定位到偷取者之后，更新调用者 WorkQueue 的 hint 为偷取者的索引，方便下次定位；
         * 2、定位到偷取者后，开始帮助偷取者执行任务。从偷取者的base索引开始，每次偷取一个任务执行。
         * 3、在帮助偷取者执行任务后，如果调用者发现本身已经有任务（w.top != top），则依次弹出自己的任务(LIFO顺序)并执行（也就是说自己偷自己的任务执行）。
         */
        WorkQueue[] ws = workQueues;
        int oldSum = 0, checkSum, m;
        if (ws != null && (m = ws.length - 1) >= 0 && w != null && task != null) {
            do {                                       // restart point
                checkSum = 0;                          // for stability check
                ForkJoinTask<?> subtask;
                WorkQueue j = w, v;                    // v is subtask stealer
                // task没有被执行完，就死循环在这个循环体里，除非：
                descent: for (subtask = task; subtask.status >= 0; ) {
                    // 1. 找到给定WorkQueue的偷取者v，偷猎者作为一个worker，一定在奇数索引位
                    for (int h = j.hint | 1, k = 0, i; ; k += 2) { // 跳两个索引，因为Worker在奇数索引位
                        if (k > m)                     // 超过ws的长度，则没有发现偷猎者
                            break descent;
                        if ((v = ws[i = (h + k) & m]) != null) { // 从ws计算
                            if (v.currentSteal == subtask) { // 定位到偷取者
                                j.hint = i; // 记录偷取者的索引位置
                                break;
                            }
                            checkSum += v.base;
                        }
                    }
                    // 2. 帮助偷取者v执行任务,j是当前的我的workQueue，subtask就是原本是我的任务被偷取走那个
                    for (;;) {                         // help v or descend
                        ForkJoinTask<?>[] a; int b;   //偷取者内部的任务
                        checkSum += (b = v.base);
                        ForkJoinTask<?> next = v.currentJoin; //获取偷取者的join任务--即偷猎者也在等待执行join任务
                        // ① 数据发生变动 -- 我的task已经被执行结束，或者，我当前的join任务并不是这个task啦，或者对方的偷猎者的任务已经不是我的任务啦
                        if (subtask.status < 0 || j.currentJoin != subtask || v.currentSteal != subtask) // stale
                            break descent; // 数据已经变旧，需要跳出descent循环重来
                        // ② 偷取者内部任务为空，同时偷取者的join任务也为null，直接返回
                        // 偷猎者内部任务为空，但是偷取者的join任务不为null，那么将j设为偷猎者，subtask设为偷猎者的join任务，然后重新遍历一次
                        if (b - v.top >= 0 || (a = v.array) == null) {
                            if ((subtask = next) == null)  //偷取者的join任务为null，跳出descent循环
                                break descent;
                            j = v;
                            break;  //偷取者内部任务为空，可能任务也被偷走了；跳出本次循环，查找偷取者的偷取者
                        }
                        // ③ 偷猎则的array中存在任务
                        int i = (((a.length - 1) & b) << ASHIFT) + ABASE; //获取base偏移地址
                        ForkJoinTask<?> t = ((ForkJoinTask<?>) U.getObjectVolatile(a, i)); //获取偷取者的base任务
                        if (v.base == b) { // 若偷猎者array的任务被人窃取后就会导致base变化 -- 没有变化，才允许进入
                            if (t == null)             // base任务为null，数据变旧，重新来
                                break descent;
                            if (U.compareAndSwapObject(a, i, t, null)) { //弹出任务
                                v.base = b + 1;  //更新偷取者的base位
                                ForkJoinTask<?> ps = w.currentSteal;// 获取调用者之前可能偷来的任务
                                int top = w.top;
                                // 首先更新给定workQueue的currentSteal为偷取者的base任务，然后执行该任务
                                // 然后通过检查top来判断给定workQueue是否有自己的任务，如果有，
                                // 则依次弹出任务(LIFO)->更新currentSteal->执行该任务（注意这里是自己偷自己的任务执行）
                                do {
                                    U.putOrderedObject(w, QCURRENTSTEAL, t); // 更新调用者偷取的任务为t
                                    t.doExec();        // 执行偷取的任务t，然后后面会执行自己队列任务
                                } while (task.status >= 0 && // join task没有执行完毕
                                         w.top != top && // worker的队列中已有自己的任务，依次弹出执行
                                         (t = w.pop()) != null); // 弹出worker自己的任务来执行
                                U.putOrderedObject(w, QCURRENTSTEAL, ps); // 还原给定调用者之前的currentSteal
                                if (w.base != w.top) // work自己的workQueue有自己的任务了，帮助窃取任务结束，返回
                                    return;            // 无法再继续帮助
                            }
                        }
                    }
                }
            } while (task.status >= 0 && oldSum != (oldSum = checkSum));
        }
    }

    /**
     * Tries to decrement active count (sometimes implicitly) and
     * possibly release or create a compensating worker in preparation
     * for blocking. Returns false (retryable by caller), on
     * contention, detected staleness, instability, or termination.
     *
     * @param w caller
     */
    private boolean tryCompensate(WorkQueue w) {
        /*
         * 执行补偿操作：尝试缩减活动线程量，可能释放或创建一个补偿线程来准备阻塞
         * 具体的执行看源码及注释，这里我们简单总结一下需要和不需要补偿的几种情况：
         * 需要补偿：
         *      调用者队列不为空，并且有空闲工作线程，这种情况会唤醒空闲线程（调用tryRelease方法）
         *      池尚未停止，活跃线程数不足，这时会新建一个工作线程（调用createWorker方法）
         * 不需要补偿：
         *      调用者已终止或池处于不稳定状态
         *      总线程数大于并行度 && 活动线程数大于1 && 调用者任务队列为空
         */
        boolean canBlock;
        WorkQueue[] ws; long c; int m, pc, sp;
        // 1、调用者已经被终止、workQueues已经为空、并行度上不支持 --- 不需要补偿，不需要阻塞
        if (w == null || w.qlock < 0 ||           // 调用者已经被终止
            (ws = workQueues) == null || (m = ws.length - 1) <= 0 || // workQueues已经为空
            (pc = config & SMASK) == 0)           //并行度上不支持
            canBlock = false;
        // 2、ctl后32位不等于0，表示有空闲线程，就唤醒空闲的worker --- 需要补偿，需要阻塞
        else if ((sp = (int)(c = ctl)) != 0)
            canBlock = tryRelease(c, ws[sp & m], 0L); //唤醒等待的工作线程
        // 3、没有空闲线程
        else {
            int ac = (int)(c >> AC_SHIFT) + pc; // 当前活跃跃线程数
            int tc = (short)(c >> TC_SHIFT) + pc; // 当前创建的总线程数， -1 + 12(并行度) = 11 表示共创建11个线程
            int nbusy = 0;
            // ① 计算当前正在繁忙的任务数量
            for (int i = 0; i <= m; ++i) {        // two passes of odd indices
                WorkQueue v;
                if ((v = ws[((i << 1) | 1) & m]) != null) { //取奇数索引位
                    if ((v.scanState & SCANNING) != 0) // 没有正在运行任务，就跳出
                        break;
                    ++nbusy; // 正在运行任务，nbusy++
                }
            }
            // ② ctl期间已经改变，或者繁忙的线程不足其创建的总线程数的2倍 --- 需要补偿，不需要阻塞
            if (nbusy != (tc << 1) || ctl != c)
                canBlock = false;                 // unstable or stale
            // ③ 创建总线程数大于并行度 && 活动线程数大于1 && 调用者任务队列为空 --- 不需要补偿
            else if (tc >= pc && ac > 1 && w.isEmpty()) {
                long nc = ((AC_MASK & (c - AC_UNIT)) | (~AC_MASK & c));       // uncompensated
                canBlock = U.compareAndSwapLong(this, CTL, c, nc); //更新活跃线程数
            }
            // ④ 创建的超出最大线程数 -- 直接抛出异常
            else if (tc >= MAX_CAP || (this == common && tc >= pc + commonMaxSpares))
                throw new RejectedExecutionException("Thread limit exceeded replacing blocked worker");
            // ⑤ 说明还允许创建线程worker --- 需要补偿，
            else {                                // similar to tryAddWorker
                boolean add = false; int rs;      // CAS within lock
                long nc = ((AC_MASK & c) | (TC_MASK & (c + TC_UNIT))); // 计算新的创建的总线程数
                if (((rs = lockRunState()) & STOP) == 0)
                    add = U.compareAndSwapLong(this, CTL, c, nc); // 线程池未关闭，更新总线程数
                unlockRunState(rs, rs & ~RSLOCK);
                // 运行到这里说明活跃工作线程数不足，需要创建一个新的工作线程来补偿  --- 需要补偿
                canBlock = add && createWorker();
            }
        }
        return canBlock;
    }

    /**
     * Helps and/or blocks until the given task is done or timeout.
     *
     * @param w caller
     * @param task the task
     * @param deadline for timed waits, if nonzero
     * @return task status on exit
     */
    final int awaitJoin(WorkQueue w, ForkJoinTask<?> task, long deadline) {
        /*
        * 如果当前join任务不在Worker等待队列的top位，或者任务执行失败，调用此方法来帮助执行或阻塞当前 join 的任务。函数执行流程如下：
        * 由于每次调用awaitJoin都会优先执行当前join的任务，所以首先会更新currentJoin为当前join任务；
        * 进入自旋：
        * 1、首先检查任务是否已经完成（通过task.status < 0判断），如果给定任务执行完毕|取消|异常 则跳出循环返回执行状态s；
        * 2、如果是 CountedCompleter 任务类型，调用helpComplete方法来完成join操作（后面笔者会开新篇来专门讲解CountedCompleter，本篇暂时不做详细解析）；
        * 3、非 CountedCompleter 任务类型调用WorkQueue.tryRemoveAndExec尝试在队列中找到这个join任务并执行这个任务；
        * 4、如果给定 WorkQueue 的等待队列为空或任务执行失败，说明任务可能被偷，调用helpStealer帮助偷取者执行任务（也就是说，偷取者帮我执行任务，我去帮偷取者执行它的任务）；
        * 5、再次判断任务是否执行完毕（task.status < 0），如果任务执行失败，计算一个等待时间准备进行补偿操作；
        * 6、调用tryCompensate方法为给定 WorkQueue 尝试执行补偿操作。在执行补偿期间，如果发现 资源争用|池处于unstable状态|当前Worker已终止，则调用ForkJoinTask.internalWait()方法等待指定的时间，任务唤醒之后继续自旋
        */
        int s = 0;
        if (task != null && w != null) {
            ForkJoinTask<?> prevJoin = w.currentJoin;  // 获取Worker上一次的join任务
            U.putOrderedObject(w, QCURRENTJOIN, task); // 把currentJoin替换为当前需要join的任务 -- 表名当前worker正在等待task去join
            CountedCompleter<?> cc = (task instanceof CountedCompleter) ? (CountedCompleter<?>)task : null;
            for (;;) {
                // 1、join任务，已经完成|取消|异常 跳出循环，表示当前任务可以getRawResult()
                if ((s = task.status) < 0)
                    break;
                // 2、task属于CountedCompleter
                if (cc != null) //CountedCompleter任务由helpComplete来完成join
                    helpComplete(w, cc, 0);
                // 3、当前worker中存在任务，调用w.tryRemoveAndExec(task)尝试执行这个join任务 --
                // 假设：执行顺序是t1.fork t2.fork t1.join t2.join, 那么t1将不是array的top值，那么上述的tryUnpush就会失败，
                // 从而进入awaitJoin中再次尝试在自己的队列中找到这个需要join的任务
                // tryRemoveAndExec返回false，表示t1这个任务就在自己的队列中，由自己来执行，或者任务task已经被执行完 -- 此时不需要去协助，因为task执行结束啦，需要立即返回
                // tryRemoveAndExec返回true，表示t1这个任务确实已经不在自己的队列中，找到偷猎者，他帮我我帮他执行任务 -- 此时task任务没结束，反正都是闲着，去帮对方执行任务
                else if (w.base == w.top || w.tryRemoveAndExec(task))   //尝试执行
                    helpStealer(w, task); //队列为空或执行失败，任务可能被偷，帮助偷取者执行该任务
                // 执行到此处：task可能已经被执行、也可能是偷猎者队列中没有任务且currentJoin任务也不存在、

                // 4、检查是否已经完成|取消|异常的情况，跳出循环
                if ((s = task.status) < 0)
                    break;

                // 5、任务仍然没有结束，就需要进入等待啦
                long ms, ns;
                if (deadline == 0L) // ① deadline为0，不需要超时等待，ms设为0
                    ms = 0L;
                else if ((ns = deadline - System.nanoTime()) <= 0L)  // ② 已经超时啦，直接跳出循环
                    break;
                else if ((ms = TimeUnit.NANOSECONDS.toMillis(ns)) <= 0L) // ③ 没有超时，ms设为1
                    ms = 1L;
                if (tryCompensate(w)) { // 执行补偿操作
                    task.internalWait(ms); // 补偿执行成功，任务等待指定时间，ms=1超时等待，ms=0无限等待
                    U.getAndAddLong(this, CTL, AC_UNIT); // 唤醒后，添加一个活跃线程数
                }
            }
            U.putOrderedObject(w, QCURRENTJOIN, prevJoin); // 循环结束，替换为原来的join任务
        }
        return s;
    }

    // Specialized scanning

    /**
     * Returns a (probably) non-empty steal queue, if one is found
     * during a scan, else null.  This method must be retried by
     * caller if, by the time it tries to use the queue, it is empty.
     */
    private WorkQueue findNonEmptyStealQueue() {
        WorkQueue[] ws; int m;  // one-shot version of scan loop
        int r = ThreadLocalRandom.nextSecondarySeed();
        if ((ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            for (int origin = r & m, k = origin, oldSum = 0, checkSum = 0;;) {
                WorkQueue q; int b;
                if ((q = ws[k]) != null) {
                    if ((b = q.base) - q.top < 0)
                        return q;
                    checkSum += b;
                }
                if ((k = (k + 1) & m) == origin) {
                    if (oldSum == (oldSum = checkSum))
                        break;
                    checkSum = 0;
                }
            }
        }
        return null;
    }

    /**
     * Runs tasks until {@code isQuiescent()}. We piggyback on
     * active count ctl maintenance, but rather than blocking
     * when tasks cannot be found, we rescan until all others cannot
     * find tasks either.
     */
    final void helpQuiescePool(WorkQueue w) {
        ForkJoinTask<?> ps = w.currentSteal; // save context
        for (boolean active = true;;) {
            long c; WorkQueue q; ForkJoinTask<?> t; int b;
            w.execLocalTasks();     // run locals before each scan
            if ((q = findNonEmptyStealQueue()) != null) {
                if (!active) {      // re-establish active count
                    active = true;
                    U.getAndAddLong(this, CTL, AC_UNIT);
                }
                if ((b = q.base) - q.top < 0 && (t = q.pollAt(b)) != null) {
                    U.putOrderedObject(w, QCURRENTSTEAL, t);
                    t.doExec();
                    if (++w.nsteals < 0)
                        w.transferStealCount(this);
                }
            }
            else if (active) {      // decrement active count without queuing
                long nc = (AC_MASK & ((c = ctl) - AC_UNIT)) | (~AC_MASK & c);
                if ((int)(nc >> AC_SHIFT) + (config & SMASK) <= 0)
                    break;          // bypass decrement-then-increment
                if (U.compareAndSwapLong(this, CTL, c, nc))
                    active = false;
            }
            else if ((int)((c = ctl) >> AC_SHIFT) + (config & SMASK) <= 0 &&
                     U.compareAndSwapLong(this, CTL, c, c + AC_UNIT))
                break;
        }
        U.putOrderedObject(w, QCURRENTSTEAL, ps);
    }

    /**
     * Gets and removes a local or stolen task for the given worker.
     *
     * @return a task, if available
     */
    final ForkJoinTask<?> nextTaskFor(WorkQueue w) {
        for (ForkJoinTask<?> t;;) {
            WorkQueue q; int b;
            if ((t = w.nextLocalTask()) != null)
                return t;
            if ((q = findNonEmptyStealQueue()) == null)
                return null;
            if ((b = q.base) - q.top < 0 && (t = q.pollAt(b)) != null)
                return t;
        }
    }

    /**
     * Returns a cheap heuristic guide for task partitioning when
     * programmers, frameworks, tools, or languages have little or no
     * idea about task granularity.  In essence, by offering this
     * method, we ask users only about tradeoffs in overhead vs
     * expected throughput and its variance, rather than how finely to
     * partition tasks.
     *
     * In a steady state strict (tree-structured) computation, each
     * thread makes available for stealing enough tasks for other
     * threads to remain active. Inductively, if all threads play by
     * the same rules, each thread should make available only a
     * constant number of tasks.
     *
     * The minimum useful constant is just 1. But using a value of 1
     * would require immediate replenishment upon each steal to
     * maintain enough tasks, which is infeasible.  Further,
     * partitionings/granularities of offered tasks should minimize
     * steal rates, which in general means that threads nearer the top
     * of computation tree should generate more than those nearer the
     * bottom. In perfect steady state, each thread is at
     * approximately the same level of computation tree. However,
     * producing extra tasks amortizes the uncertainty of progress and
     * diffusion assumptions.
     *
     * So, users will want to use values larger (but not much larger)
     * than 1 to both smooth over transient shortages and hedge
     * against uneven progress; as traded off against the cost of
     * extra task overhead. We leave the user to pick a threshold
     * value to compare with the results of this call to guide
     * decisions, but recommend values such as 3.
     *
     * When all threads are active, it is on average OK to estimate
     * surplus strictly locally. In steady-state, if one thread is
     * maintaining say 2 surplus tasks, then so are others. So we can
     * just use estimated queue length.  However, this strategy alone
     * leads to serious mis-estimates in some non-steady-state
     * conditions (ramp-up, ramp-down, other stalls). We can detect
     * many of these by further considering the number of "idle"
     * threads, that are known to have zero queued tasks, so
     * compensate by a factor of (#idle/#active) threads.
     */
    static int getSurplusQueuedTaskCount() {
        Thread t; ForkJoinWorkerThread wt; ForkJoinPool pool; WorkQueue q;
        if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)) {
            int p = (pool = (wt = (ForkJoinWorkerThread)t).pool).
                config & SMASK;
            int n = (q = wt.workQueue).top - q.base;
            int a = (int)(pool.ctl >> AC_SHIFT) + p;
            return n - (a > (p >>>= 1) ? 0 :
                        a > (p >>>= 1) ? 1 :
                        a > (p >>>= 1) ? 2 :
                        a > (p >>>= 1) ? 4 :
                        8);
        }
        return 0;
    }

    //  Termination

    /**
     * Possibly initiates and/or completes termination.
     *
     * @param now if true, unconditionally terminate, else only
     * if no work and no active workers
     * @param enable if true, enable shutdown when next possible
     * @return true if now terminating or terminated
     */
    private boolean tryTerminate(boolean now, boolean enable) {
        int rs;
        // 1、common线程池不允许关闭，返回false
        if (this == common)
            return false;
        // 2、线程池非SHUTDOWN装填，不需要tryTerminate，返回false
        if ((rs = runState) >= 0) {
            if (!enable)
                return false;
            rs = lockRunState();                  // enter SHUTDOWN phase
            unlockRunState(rs, (rs & ~RSLOCK) | SHUTDOWN);
        }
        // 3、线程池已经关闭
        if ((rs & STOP) == 0) {
            // ① 非立即关闭
            if (!now) {                           // check quiescence
                for (long oldSum = 0L;;) {        // 重复，直到线程池稳定
                    WorkQueue[] ws; WorkQueue w; int m, b; long c;
                    long checkSum = ctl;
                    // a.仍然有活跃的线程，不允许关闭，返回false
                    if ((int)(checkSum >> AC_SHIFT) + (config & SMASK) > 0)
                        return false;
                    // b.ws不有效，跳出循环
                    if ((ws = workQueues) == null || (m = ws.length - 1) <= 0)
                        break;                    // check queues
                    // c.遍历ws，若有workQueue有任务，或者仍在激活状态执行中，或者有窃取的任务在执行，调用tryRealse，返回false
                    for (int i = 0; i <= m; ++i) {
                        if ((w = ws[i]) != null) {
                            if ((b = w.base) != w.top || w.scanState >= 0 || w.currentSteal != null) {
                                tryRelease(c = ctl, ws[m & (int)c], AC_UNIT);
                                return false;     // 安排到下一次的复查
                            }
                            // d.执行到这：array中不存在任务可执行，获取base值
                            checkSum += b;
                            if ((i & 1) == 0)
                                w.qlock = -1;     // 偶数位置上workQueue没有任务，设为-1，表示终止
                        }
                    }
                    if (oldSum == (oldSum = checkSum))
                        break;
                }
            }
            if ((runState & STOP) == 0) {
                rs = lockRunState();              // 确保在STOP阶段
                unlockRunState(rs, (rs & ~RSLOCK) | STOP);
            }
        }

        // 执行到这说明：需要终止这个线程池

        int pass = 0;                             // pass=3才帮助终止
        for (long oldSum = 0L;;) {                // 或者直到完成，或直到稳定
            WorkQueue[] ws; WorkQueue w; ForkJoinWorkerThread wt; int m;
            long checkSum = ctl;
            // 1、创建线程数已经小于0，或ws非有效值，且非TERMINATED装填，更新为TERMINATED状态，就跳出循环
            if ((short)(checkSum >>> TC_SHIFT) + (config & SMASK) <= 0 || (ws = workQueues) == null || (m = ws.length - 1) <= 0) {
                if ((runState & TERMINATED) == 0) {
                    rs = lockRunState();          // done
                    unlockRunState(rs, (rs & ~RSLOCK) | TERMINATED);
                    synchronized (this) { notifyAll(); } // 为了唤醒在 awaitTermination 中的线程 -- 因为这里进入终止状态啦
                }
                break;
            }
            // 2、遍历ws，将ws禁止，
            for (int i = 0; i <= m; ++i) {
                if ((w = ws[i]) != null) {
                    checkSum += w.base;           // 添加校验和 -- 线程池非静态的，有workQueue有任务值
                    w.qlock = -1;                 // 尝试禁止 qlock设为-1
                    if (pass > 0) {               // 第一次pass是进不来的，必须执行到4，做过tryRelease() -- 唤醒worker后，才可以帮助清空任务
                        w.cancelAll();            // 取消队列中的所有任务
                        if (pass > 1 && (wt = w.owner) != null) {
                            if (!wt.isInterrupted()) { // 非中断状态
                                try {
                                    wt.interrupt(); // 尝试中断worker
                                } catch (Throwable ignore) {
                                }
                            }
                            if (w.scanState < 0)
                                U.unpark(wt);     // 灭活状态，worker处于阻塞，需要唤醒worker -- 让其执行中发现pool已经TERMINATED了
                        }
                    }
                }
            }
            // 2、若是不稳定的【线程池不稳定即有活跃的线程】，将oldSum=checkSum，pass=0
            if (checkSum != oldSum) {
                oldSum = checkSum;
                pass = 0;
            }
            // 3、循环超过3尺，不需要就行帮助 -- break
            else if (pass > 3 && pass > m)        // can't further help
                break;
            // 4、pass自增 -- 唤醒所有空闲的worker
            else if (++pass > 1) {                // try to dequeue
                long c; int j = 0, sp;            // bound attempts
                /*
                 * 1、将所有的空闲worker唤醒的操作；
                 * 2、sp就是空闲worker链表头，在tryRelease()中满足链表头的需求，因此会被唤醒
                 * 3、进入下一次循环，再次获取新的链表头
                 */
                while (j++ <= m && (sp = (int)(c = ctl)) != 0) // 直到ctl后32位为0，不存在任何空闲worker的链表头
                    tryRelease(c, ws[sp & m], AC_UNIT); // 唤醒空心啊的worker，前提是第二个参数确实是空闲work链表的头部，才会唤醒他帮助清空队列中的任务
            }
        }
        return true;
    }

    // External operations

    /**
     * Full version of externalPush, handling uncommon cases, as well
     * as performing secondary initialization upon the first
     * submission of the first task to the pool.  It also detects
     * first submission by an external thread and creates a new shared
     * queue if the one at index if empty or contended.
     *
     * @param task the task. Caller must ensure non-null.
     */
    private void externalSubmit(ForkJoinTask<?> task) {
        /*
         * 工作：
         * 1、如果池为终止状态(runState<0)，调用tryTerminate来终止线程池，并抛出任务拒绝异常；
         * 2、如果尚未初始化，就为 FJP 执行初始化操作：初始化stealCounter、创建workerQueues，然后继续自旋；
         * 3、初始化完成后，执行在externalPush中相同的操作：获取 workQueue，放入指定任务。任务提交成功后调用signalWork方法创建或激活线程；
         * 4、如果在步骤3中获取到的 workQueue 为null，会在这一步中创建一个 workQueue，创建成功继续自旋执行第三步操作；
         * 5、如果非上述情况，或者有线程争用资源导致获取锁失败，就重新获取线程探针值继续自旋。
         */
        int r;                                    // initialize caller's probe
        // 初始化调用线程的探针值，用于workQueue计算索引
        if ((r = ThreadLocalRandom.getProbe()) == 0) {
            ThreadLocalRandom.localInit();
            r = ThreadLocalRandom.getProbe();
        }
        for (;;) {
            WorkQueue[] ws; WorkQueue q; int rs, m, k;
            boolean move = false;
            //池已关闭
            if ((rs = runState) < 0) {
                tryTerminate(false, false);     // 协助关闭
                throw new RejectedExecutionException();
            }
            // ①操作：线程池为NEW状态，或者workQueues为空，初始化workQueues、stealCount、runState
            else if ((rs & STARTED) == 0 ||     // 线程池是初始化状态 -- 才new出来的ForkJoinPool的runState就是0
                     ((ws = workQueues) == null || (m = ws.length - 1) < 0)) {
                int ns = 0;
                rs = lockRunState();//锁定runState
                try {
                    // rs仍未初始化，尝试初始化
                    if ((rs & STARTED) == 0) {
                        // 初始化stealCounter - 偷取任务数量
                        U.compareAndSwapObject(this, STEALCOUNTER, null, new AtomicLong());
                        // 创建workQueues，容量为2的幂次方 -- 若config即配置的并行度为12，那么n就是32
                        int p = config & SMASK; // ensure at least 2 slots
                        int n = (p > 1) ? p - 1 : 1;
                        n |= n >>> 1;
                        n |= n >>> 2;
                        n |= n >>> 4;
                        n |= n >>> 8;
                        n |= n >>> 16;
                        n = (n + 1) << 1;
                        workQueues = new WorkQueue[n];
                        ns = STARTED;
                    }
                } finally {
                    unlockRunState(rs, (rs & ~RSLOCK) | ns); // 解锁，更新runState为STARTED状态
                }
            }
            // 执行到此：线程池状态至少是STARED的，且workerQueues非空 -- 可以线程在①中创建workQueues循环进入此处
            // 操作：若对应的workQueue已经存在，则同步加锁，获取workQueue，判断容量是否充足否则扩容，将任务放入其中，更新QTOP，创建或激活一个工作线程过来运行任务
            else if ((q = ws[k = r & m & SQMASK]) != null) { //获取随机偶数槽位的workQueue -- SQMASK 为 0111 1110 故一定为偶数
                if (q.qlock == 0 && U.compareAndSwapInt(q, QLOCK, 0, 1)) { //锁定 workQueue
                    ForkJoinTask<?>[] a = q.array; //当前workQueue的全部任务
                    int s = q.top;
                    boolean submitted = false; // initial submission or resizing
                    try {                      // locked version of push
                        if ((a != null && a.length > s + 1 - q.base) || // a.length > s + 1 - q.base 判断workQueue容量够不够
                            (a = q.growArray()) != null) { //扩容
                            int j = (((a.length - 1) & s) << ASHIFT) + ABASE;
                            U.putOrderedObject(a, j, task);//放入给定任务
                            U.putOrderedInt(q, QTOP, s + 1);//修改push slot
                            submitted = true;
                        }
                    } finally {
                        U.compareAndSwapInt(q, QLOCK, 1, 0);
                    }
                    if (submitted) { //任务提交成功，创建或激活工作线程
                        signalWork(ws, q); //创建或激活一个工作线程来运行任务
                        return;
                    }
                }
                move = true;                    // move on failure 操作失败，重新获取探针值
            }
            // 执行到此：说明workQueues虽然存在，但对应的workQueue为null
            // 操作：若rs没有被锁定，创建workQueue
            else if (((rs = runState) & RSLOCK) == 0) { // create new queue
                q = new WorkQueue(this, null);
                q.hint = r; // 记录偷取者的索引，r是当前线程的探测值
                q.config = k | SHARED_QUEUE; // k是当前workQueues的索引位置，
                q.scanState = INACTIVE; // worker的状态，非活跃的
                rs = lockRunState();           // publish index
                if (rs > 0 &&  (ws = workQueues) != null &&
                    k < ws.length && ws[k] == null)
                    ws[k] = q;                // 创建的索引k位值的workQueue
                unlockRunState(rs, rs & ~RSLOCK);
            }
            else
                move = true;                   // move if busy
            if (move)
                r = ThreadLocalRandom.advanceProbe(r); //重新获取线程探针值
        }
    }

    /**
     * Tries to add the given task to a submission queue at
     * submitter's current queue. Only the (vastly) most common path
     * is directly handled in this method, while screening for need
     * for externalSubmit.
     *
     * @param task the task. Caller must ensure non-null.
     */
    final void externalPush(ForkJoinTask<?> task) {
        /**
         * 添加给定任务到submission队列中
         * externalPush的执行流程很简单：
         * 1、首先找到一个随机偶数槽位的 workQueue，然后把任务放入这个 workQueue 的任务数组中，并更新top位。
         * 2、如果队列的剩余任务数小于1，则尝试创建或激活一个工作线程来运行任务（防止在externalSubmit初始化时发生异常导致工作线程创建失败）。
         * 3、如果workerQueues没有初始化、对应的workQueue为空、对应的任务列表array为空、对应的array容量不够，以上情况都需要则交给更强大的externalSubmit()
         */
        WorkQueue[] ws; WorkQueue q; int m;
        int r = ThreadLocalRandom.getProbe();;//探针值，用于计算WorkQueue槽位索引
        int rs = runState;
        // workQueues工作队列集合非空，q = ws[m & r & SQMASK] 获取当前task所在的随机偶数槽位的workQueue
        if ((ws = workQueues) != null && (m = (ws.length - 1)) >= 0 &&
            (q = ws[m & r & SQMASK]) != null && r != 0 && rs > 0 && // 获取随机偶数槽位的workQueue -- rs>0非SHUTFOWN状态
            U.compareAndSwapInt(q, QLOCK, 0, 1)) { //锁定workQueue
            ForkJoinTask<?>[] a; int am, n, s;
            // 数组非空，且数组容量足够，将task插入到队列中
            if ((a = q.array) != null && (am = a.length - 1) > (n = (s = q.top) - q.base)) {
                int j = ((am & s) << ASHIFT) + ABASE;
                U.putOrderedObject(a, j, task); // 任务入队列
                U.putOrderedInt(q, QTOP, s + 1); // 更新top的值
                U.putIntVolatile(q, QLOCK, 0); // 解锁
                if (n <= 1)
                    signalWork(ws, q); // 任务数小于等于1，尝试唤醒或者创建一个工作线程
                return;
            }
            U.compareAndSwapInt(q, QLOCK, 1, 0); // 解除锁定
        }
        // workQueues没有初始化，则需要workQueues等等
        // 或者对应的workQueue没有初始化，则需要初始化对应的workQueue
        // 或者对应的workQueue的array没有初始化，或者array已满，需要做扩容，就需要比externPush更强大的externalSubmit来完成
        /*
         * 首先说明一下externalPush和externalSubmit两个方法的联系：它们的作用都是把任务放到队列中等待执行。
         * 不同的是，externalSubmit可以说是完整版的externalPush，在任务首次提交时，需要初始化workQueues及其他相关属性，
         * 这个初始化操作就是externalSubmit来完成的；而后再向池中提交的任务都是通过简化版的externalSubmit-externalPush来完成。
         */
        externalSubmit(task);
    }

    /**
     * Returns common pool queue for an external thread.
     */
    static WorkQueue commonSubmitterQueue() {
        ForkJoinPool p = common;
        int r = ThreadLocalRandom.getProbe();
        WorkQueue[] ws; int m;
        return (p != null && (ws = p.workQueues) != null &&
                (m = ws.length - 1) >= 0) ?
            ws[m & r & SQMASK] : null;
    }

    /**
     * 为外部提交者提供 tryUnpush 功能（给定任务在top位时弹出任务）
     */
    final boolean tryExternalUnpush(ForkJoinTask<?> task) {
        /*
         * 作用：
         * 1、当前线程所在的workQueue中array的top就是task任务，那么弹出这个任务，返回true
         * 2、非以上情况，就直接返回false
         */
        WorkQueue[] ws; WorkQueue w; ForkJoinTask<?>[] a; int m, s;
        int r = ThreadLocalRandom.getProbe();
        if ((ws = workQueues) != null && (m = ws.length - 1) >= 0 &&
            (w = ws[m & r & SQMASK]) != null && // 当前线程t所在的偶数位置的workQueue
            (a = w.array) != null && (s = w.top) != w.base) {
            long j = (((a.length - 1) & (s - 1)) << ASHIFT) + ABASE;  // 取top位任务
            if (U.compareAndSwapInt(w, QLOCK, 0, 1)) { // 加锁
                if (w.top == s && w.array == a &&
                    U.getObject(a, j) == task && // top上的任务是否就是task
                    U.compareAndSwapObject(a, j, task, null)) { // 若top上的任务就是task，那么符合条件，弹出
                    U.putOrderedInt(w, QTOP, s - 1);  // 更新top
                    U.putOrderedInt(w, QLOCK, 0); // 解锁，返回true
                    return true;
                }
                U.compareAndSwapInt(w, QLOCK, 1, 0);  // 当前任务不在top位，解锁返回false
            }
        }
        return false;
    }

    /**
     * 为外部提交者调用 helpComplete()
     */
    final int externalHelpComplete(CountedCompleter<?> task, int maxTasks) {
        WorkQueue[] ws; int n;
        int r = ThreadLocalRandom.getProbe();
        // ws为null，或者不存在，返回0
        //否则，调用helpComplete()
        return ((ws = workQueues) == null || (n = ws.length) == 0) ? 0 :
            helpComplete(ws[(n - 1) & r & SQMASK], task, maxTasks);
    }

    // Exported methods

    // Constructors

    /**
     * Creates a {@code ForkJoinPool} with parallelism equal to {@link
     * java.lang.Runtime#availableProcessors}, using the {@linkplain
     * #defaultForkJoinWorkerThreadFactory default thread factory},
     * no UncaughtExceptionHandler, and non-async LIFO processing mode.
     *
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public ForkJoinPool() {
        this(Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()),
             defaultForkJoinWorkerThreadFactory, null, false);
    }

    /**
     * Creates a {@code ForkJoinPool} with the indicated parallelism
     * level, the {@linkplain
     * #defaultForkJoinWorkerThreadFactory default thread factory},
     * no UncaughtExceptionHandler, and non-async LIFO processing mode.
     *
     * @param parallelism the parallelism level
     * @throws IllegalArgumentException if parallelism less than or
     *         equal to zero, or greater than implementation limit
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
    }

    /**
     * Creates a {@code ForkJoinPool} with the given parameters.
     *
     * @param parallelism the parallelism level. For default value,
     * use {@link java.lang.Runtime#availableProcessors}.
     * @param factory the factory for creating new threads. For default value,
     * use {@link #defaultForkJoinWorkerThreadFactory}.
     * @param handler the handler for internal worker threads that
     * terminate due to unrecoverable errors encountered while executing
     * tasks. For default value, use {@code null}.
     * @param asyncMode if true,
     * establishes local first-in-first-out scheduling mode for forked
     * tasks that are never joined. This mode may be more appropriate
     * than default locally stack-based mode in applications in which
     * worker threads only process event-style asynchronous tasks.
     * For default value, use {@code false}.
     * @throws IllegalArgumentException if parallelism less than or
     *         equal to zero, or greater than implementation limit
     * @throws NullPointerException if the factory is null
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public ForkJoinPool(int parallelism,
                        ForkJoinWorkerThreadFactory factory,
                        UncaughtExceptionHandler handler,
                        boolean asyncMode) {
        // 以下默认是指：new ForkJoinPool()
        this(checkParallelism(parallelism), // parallelism：并行度，默认为CPU数 -- 检查非0
             checkFactory(factory), // factory：工作线程工厂 -- 检查非null
             handler, // handler：处理工作线程运行任务时的异常情况类，默认为null；
             asyncMode ? FIFO_QUEUE : LIFO_QUEUE,
                // asyncMode：是否为异步模式，默认为 false。如果为true，表示子任务的执行遵循 FIFO 顺序并且任务不能被合并（join），这种模式适用于工作线程只运行事件类型的异步任务。
             "ForkJoinPool-" + nextPoolId() + "-worker-");
        checkPermission();
    }

    private static int checkParallelism(int parallelism) {
        if (parallelism <= 0 || parallelism > MAX_CAP)
            throw new IllegalArgumentException();
        return parallelism;
    }

    private static ForkJoinWorkerThreadFactory checkFactory(ForkJoinWorkerThreadFactory factory) {
        if (factory == null)
            throw new NullPointerException();
        return factory;
    }

    /**
     * Creates a {@code ForkJoinPool} with the given parameters, without
     * any security checks or parameter validation.  Invoked directly by
     * makeCommonPool.
     */
    private ForkJoinPool(int parallelism,
                         ForkJoinWorkerThreadFactory factory,
                         UncaughtExceptionHandler handler,
                         int mode,
                         String workerNamePrefix) {
        this.workerNamePrefix = workerNamePrefix;
        this.factory = factory;
        this.ueh = handler;
        this.config = (parallelism & SMASK) | mode; // mode为false，一般此刻config就是parallelism并行度=CPU核数；假设并行度为12
        long np = (long)(-parallelism); // offset ctl counts
        this.ctl = ((np << AC_SHIFT) & AC_MASK) | ((np << TC_SHIFT) & TC_MASK); // CTL的初始值

    }

    /**
     * 返回公共池实例common。
     * 这个common池是静态初始化代码块创建的；其运行状态不受尝试关机或立即关机的影响。
     * 然而，该池和任何正在进行的处理都会在程序调用System.exit()运行时自动终止。
     * 任何依赖异步任务处理在程序终止完成离开前，都应该调用 commonPool().awaitQuiescence。直到线程池为
     *
     * @return the common pool instance
     * @since 1.8
     */
    public static ForkJoinPool commonPool() {
        // assert common != null : "static init error";
        return common;
    }

    // Execution methods

    /**
     * Performs the given task, returning its result upon completion.
     * If the computation encounters an unchecked Exception or Error,
     * it is rethrown as the outcome of this invocation.  Rethrown
     * exceptions behave in the same way as regular exceptions, but,
     * when possible, contain stack traces (as displayed for example
     * using {@code ex.printStackTrace()}) of both the current thread
     * as well as the thread actually encountering the exception;
     * minimally only the latter.
     *
     * @param task the task
     * @param <T> the type of the task's result
     * @return the task's result
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public <T> T invoke(ForkJoinTask<T> task) {
        // invoke会等待任务计算完毕并返回计算结果，因此低啊用task.join()进行阻塞等待
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
        return task.join();
    }

    /**
     * Arranges for (asynchronous) execution of the given task.
     *
     * @param task the task
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public void execute(ForkJoinTask<?> task) {
        //向 ForkJoinPool 提交任务有三种方式：
        // invoke()会等待任务计算完毕并返回计算结果；
        // execute()是直接向池提交一个任务来异步执行，无返回结果；
        // submit()也是异步执行，但是会返回提交的任务，在适当的时候可通过task.get()获取执行结果。
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
    }

    // AbstractExecutorService methods

    /**
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public void execute(Runnable task) {
        // execute()是直接向池提交一个任务来异步执行，无返回结果；
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) // avoid re-wrap
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.RunnableExecuteAction(task);
        externalPush(job);
    }

    /**
     * Submits a ForkJoinTask for execution.
     *
     * @param task the task to submit
     * @param <T> the type of the task's result
     * @return the task
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        // submit()也是异步执行，但是会返回提交的任务，在适当的时候可通过task.get()获取执行结果。
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
        return task;
    }

    /**
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        // submit()也是异步执行，但是会返回提交的任务，在适当的时候可通过task.get()获取执行结果。
        ForkJoinTask<T> job = new ForkJoinTask.AdaptedCallable<T>(task);
        externalPush(job);
        return job;
    }

    /**
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        // submit()也是异步执行，但是会返回提交的任务，在适当的时候可通过task.get()获取执行结果。
        ForkJoinTask<T> job = new ForkJoinTask.AdaptedRunnable<T>(task, result);
        externalPush(job);
        return job;
    }

    /**
     * @throws NullPointerException if the task is null
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     */
    public ForkJoinTask<?> submit(Runnable task) {
        // submit()也是异步执行，但是会返回提交的任务，在适当的时候可通过task.get()获取执行结果。
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) // avoid re-wrap
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.AdaptedRunnableAction(task);
        externalPush(job);
        return job;
    }

    /**
     * @throws NullPointerException       {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        // In previous versions of this class, this method constructed
        // a task to run ForkJoinTask.invokeAll, but now external
        // invocation of multiple tasks is at least as efficient.
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());

        boolean done = false;
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask<T> f = new ForkJoinTask.AdaptedCallable<T>(t);
                futures.add(f);
                externalPush(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++)
                ((ForkJoinTask<?>)futures.get(i)).quietlyJoin();
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(false);
        }
    }

    /**
     * Returns the factory used for constructing new workers.
     *
     * @return the factory used for constructing new workers
     */
    public ForkJoinWorkerThreadFactory getFactory() {
        return factory;
    }

    /**
     * 返回由于执行任务时遇到不可恢复的错误而终止的内部工作线程worker的处理程序的异常
     *
     * @return the handler, or {@code null} if none
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return ueh;
    }

    /**
     * 获取线程池的并行度
     *
     * @return the targeted parallelism level of this pool
     */
    public int getParallelism() {
        int par;
        return ((par = config & SMASK) > 0) ? par : 1; // 并行度的算法 config & SMASK 高16位
    }

    /**
     * 获取common线程池的并行度
     *
     * @return the targeted parallelism level of the common pool
     * @since 1.8
     */
    public static int getCommonPoolParallelism() {
        return commonParallelism;
    }

    /**
     * 返回已启动但尚未终止的工作线程数。
     * 此方法返回的结果可能与getParallelism不同，
     * getParallelism创建线程是为了在其他线程被协同阻止时保持并行性。
     *
     * @return the number of worker threads
     */
    public int getPoolSize() {
        return (config & SMASK) + (short)(ctl >>> TC_SHIFT);
    }

    /**
     * Returns {@code true} if this pool uses local first-in-first-out
     * scheduling mode for forked tasks that are never joined.
     *
     * @return {@code true} if this pool uses async mode
     */
    public boolean getAsyncMode() {
        return (config & FIFO_QUEUE) != 0; // FIFO【异步模式】 还是 LIFO 【同步模式】
    }

    /**
     * Returns an estimate of the number of worker threads that are
     * not blocked waiting to join tasks or for other managed
     * synchronization. This method may overestimate the
     * number of running threads.
     *
     * @return the number of worker threads
     */
    public int getRunningThreadCount() {
        // 当前正在运行的线程Thread
        int rc = 0;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) { // 奇数位置上worker
                if ((w = ws[i]) != null && w.isApparentlyUnblocked())
                    ++rc;
            }
        }
        return rc;
    }

    /**
     * Returns an estimate of the number of threads that are currently
     * stealing or executing tasks. This method may overestimate the
     * number of active threads.
     *
     * @return the number of active threads
     */
    public int getActiveThreadCount() {
        int r = (config & SMASK) + (int)(ctl >> AC_SHIFT);
        return (r <= 0) ? 0 : r; // 获取当前活跃的线程数
    }

    /**
     * 如果所有工作线程当前都处于空闲状态，则返回true。空闲工作线程是指无法获得要执行的任务的工作线程，因为没有可从其他线程窃取的任务，
     * 也没有挂起的对池的提交。这种方法是保守的；它可能不会在所有线程空闲时立即返回true，但如果线程保持非活动状态，它最终将变为true。
     *
     * @return 所有线程都是空闲，才认为线程池就是静止
     */
    public boolean isQuiescent() {
        // (config & SMASK) config的高16位就是并行度，因此获取并行度，比如12
        // (int)(ctl >> AC_SHIFT) 获取最高16位，假设为-10 1111 1111 1111 0100
        // 12 - 10 = 2 表示有2个激活中的线程
        return (config & SMASK) + (int)(ctl >> AC_SHIFT) <= 0;
    }

    /**
     * 返回一个线程的工作队列中被另一个线程窃取的任务总数的估计值。
     * 报告的值低估了池不静止时的实际盗窃总数。
     * 该值对于监视和tuning fork/join程序可能很有用：
     * 一般来说，偷取计数应该足够高，以使线程保持忙碌，但足够低，以避免线程间的开销和争用。
     *
     * @return 窃取的任务数量
     */
    public long getStealCount() {
        AtomicLong sc = stealCounter;
        long count = (sc == null) ? 0L : sc.get();
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.nsteals; // 累加每一个奇数位的worker上完成的窃取任务数量
            }
        }
        return count;
    }

    /**
     * 获取内部分裂出的已提交到未执行的子任务数量
     * 该值只是一个近似值，通过在池中的所有线程上迭代获得。此方法可能有助于调整任务粒度。
     */
    public long getQueuedTaskCount() {
        // 获取内部分裂出的已提交到未执行的子任务数量
        long count = 0;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.queueSize();
            }
        }
        return count;
    }

    /**
     * 获取外部提交未执行的的任务数量
     * 该值只是一个近似值，通过在池中的所有线程上迭代获得。此方法可能有助于调整任务粒度。
     */
    public int getQueuedSubmissionCount() {
        // 获取外部提交未执行的的任务数量
        int count = 0;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) { // 偶数位置
                if ((w = ws[i]) != null)
                    count += w.queueSize();
            }
        }
        return count;
    }

    /**
     * 是否还未执行提交的外部任务
     */
    public boolean hasQueuedSubmissions() {
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && !w.isEmpty())
                    return true;
            }
        }
        return false;
    }

    /**
     * Removes and returns the next unexecuted submission if one is
     * available.  This method may be useful in extensions to this
     * class that re-assign work in systems with multiple pools.
     *
     * @return the next submission, or {@code null} if none
     */
    protected ForkJoinTask<?> pollSubmission() {
        WorkQueue[] ws; WorkQueue w; ForkJoinTask<?> t;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && (t = w.poll()) != null)
                    return t;
            }
        }
        return null;
    }

    /**
     * Removes all available unexecuted submitted and forked tasks
     * from scheduling queues and adds them to the given collection,
     * without altering their execution status. These may include
     * artificially generated or wrapped tasks. This method is
     * designed to be invoked only when the pool is known to be
     * quiescent. Invocations at other times may not remove all
     * tasks. A failure encountered while attempting to add elements
     * to collection {@code c} may result in elements being in
     * neither, either or both collections when the associated
     * exception is thrown.  The behavior of this operation is
     * undefined if the specified collection is modified while the
     * operation is in progress.
     *
     * @param c the collection to transfer elements into
     * @return the number of elements transferred
     */
    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int count = 0;
        WorkQueue[] ws; WorkQueue w; ForkJoinTask<?> t;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; ++i) {
                if ((w = ws[i]) != null) {
                    while ((t = w.poll()) != null) {
                        c.add(t);
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns a string identifying this pool, as well as its state,
     * including indications of run state, parallelism level, and
     * worker and task counts.
     *
     * @return a string identifying this pool, as well as its state
     */
    public String toString() {
        // Use a single pass through workQueues to collect counts
        long qt = 0L, qs = 0L; int rc = 0;
        AtomicLong sc = stealCounter;
        long st = (sc == null) ? 0L : sc.get();
        long c = ctl;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; ++i) {
                if ((w = ws[i]) != null) {
                    int size = w.queueSize();
                    if ((i & 1) == 0)
                        qs += size;
                    else {
                        qt += size;
                        st += w.nsteals;
                        if (w.isApparentlyUnblocked())
                            ++rc;
                    }
                }
            }
        }
        int pc = (config & SMASK);
        int tc = pc + (short)(c >>> TC_SHIFT);
        int ac = pc + (int)(c >> AC_SHIFT);
        if (ac < 0) // ignore transient negative
            ac = 0;
        int rs = runState;
        String level = ((rs & TERMINATED) != 0 ? "Terminated" :
                        (rs & STOP)       != 0 ? "Terminating" :
                        (rs & SHUTDOWN)   != 0 ? "Shutting down" :
                        "Running");
        return super.toString() +
            "[" + level +
            ", parallelism = " + pc +
            ", size = " + tc +
            ", active = " + ac +
            ", running = " + rc +
            ", steals = " + st +
            ", tasks = " + qt +
            ", submissions = " + qs +
            "]";
    }

    /**
     * Possibly initiates an orderly shutdown in which previously
     * submitted tasks are executed, but no new tasks will be
     * accepted. Invocation has no effect on execution state if this
     * is the {@link #commonPool()}, and no additional effect if
     * already shut down.  Tasks that are in the process of being
     * submitted concurrently during the course of this method may or
     * may not be rejected.
     *
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public void shutdown() {
        checkPermission();
        tryTerminate(false, true);
    }

    /**
     * Possibly attempts to cancel and/or stop all tasks, and reject
     * all subsequently submitted tasks.  Invocation has no effect on
     * execution state if this is the {@link #commonPool()}, and no
     * additional effect if already shut down. Otherwise, tasks that
     * are in the process of being submitted or executed concurrently
     * during the course of this method may or may not be
     * rejected. This method cancels both existing and unexecuted
     * tasks, in order to permit termination in the presence of task
     * dependencies. So the method always returns an empty list
     * (unlike the case for some other Executors).
     *
     * @return an empty list
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public List<Runnable> shutdownNow() {
        checkPermission();
        tryTerminate(true, true);
        return Collections.emptyList();
    }

    /**
     * Returns {@code true} if all tasks have completed following shut down.
     *
     * @return {@code true} if all tasks have completed following shut down
     */
    public boolean isTerminated() {
        return (runState & TERMINATED) != 0;
    }

    /**
     * Returns {@code true} if the process of termination has
     * commenced but not yet completed.  This method may be useful for
     * debugging. A return of {@code true} reported a sufficient
     * period after shutdown may indicate that submitted tasks have
     * ignored or suppressed interruption, or are waiting for I/O,
     * causing this executor not to properly terminate. (See the
     * advisory notes for class {@link ForkJoinTask} stating that
     * tasks should not normally entail blocking operations.  But if
     * they do, they must abort them on interrupt.)
     *
     * @return {@code true} if terminating but not yet terminated
     */
    public boolean isTerminating() {
        int rs = runState;
        return (rs & STOP) != 0 && (rs & TERMINATED) == 0;
    }

    /**
     * Returns {@code true} if this pool has been shut down.
     *
     * @return {@code true} if this pool has been shut down
     */
    public boolean isShutdown() {
        return (runState & SHUTDOWN) != 0; // ForkJoinPool池状态检查 -- 非0即为SHUTDOWN
    }

    /**
     *
     * 调用该方法会阻塞，直到以下哪一个先发生为止：
     * <pre>
     *      超时；
     *      或者所有任务都已经完成在发出一个SHUTDOWN请求后；
     *      或者线程被中断；
     * </pre>
     * 由于commonPool在程序关闭之前都不会终止，因此该方法当应用于公共池时，否则对common池使用时该方法相当于等待静止，但总是返回false
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return {@code true} if this executor terminated and
     *         {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        // 1、线程已被中断
        if (Thread.interrupted())
            throw new InterruptedException();
        // 2、关闭的线程池为内次common线程池
        if (this == common) {
            awaitQuiescence(timeout, unit); // 安静的等待
            return false; // 无法关闭common池
        }
        /*
         * 3、关闭的线程池非common线程池
         *      a.线程池已经被关闭，直接返回true
         *      b.已经超时，返回false -- 关闭失败
         */
        long nanos = unit.toNanos(timeout);
        if (isTerminated())
            return true;
        if (nanos <= 0L)
            return false;
        long deadline = System.nanoTime() + nanos;
        synchronized (this) { // 以pool为对象，进行锁定
            for (;;) {
                if (isTerminated())
                    return true;
                if (nanos <= 0L)
                    return false;
                long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                wait(millis > 0L ? millis : 1L);
                nanos = deadline - System.nanoTime();
            }
        }
    }

    /**
     * 如果由在此池中运行的ForkJoinTask调用，则实际上相当于ForkJoinTask.helpQuiescePool。
     * 否则，进入等待 或者 尝试协助执行任务，直到该池静止或指示的超时时间过去。
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return 如果静止，则为真；如果超时已过，则为false。
     */
    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        long nanos = unit.toNanos(timeout);
        ForkJoinWorkerThread wt;
        Thread thread = Thread.currentThread();
        // 1、线程属于ForkJoinWorkerThread，且pool就是当前线程池，调用helpQuiescePool()
        if ((thread instanceof ForkJoinWorkerThread) && (wt = (ForkJoinWorkerThread)thread).pool == this) {
            helpQuiescePool(wt.workQueue);
            return true;
        }
        long startTime = System.nanoTime();
        WorkQueue[] ws;
        int r = 0, m;
        boolean found = true;
        // 2、当前pool非静止即有非空闲线程在运行【说明可能有任务，因此我可以去协助】，并且ws已经存在，那么当前线程反正都是阻塞，还不如去帮助完成任务
        while (!isQuiescent() && (ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            if (!found) {
                if ((System.nanoTime() - startTime) > nanos)
                    return false; // 直到超时
                Thread.yield(); // ④ 在下面的for循环体中没有找到可执行的arry，则自旋让步，然后再去 3 找任务
            }
            found = false;
            // 3、 (m + 1) << 2 就是 ws的长度乘以4，初始话的ws就是32，最长为64，假设为32那么得到j就是128 -- 表示循环4次数组长度的
            for (int j = (m + 1) << 2; j >= 0; --j) {
                ForkJoinTask<?> t; WorkQueue q; int b, k;
                // 计算索引位置k、对应的workQueue存在且有元素
                if ((k = r++ & m) <= m && k >= 0 && (q = ws[k]) != null && (b = q.base) - q.top < 0) {
                    found = true; // ③ 已经找到可执行的workQueue
                    if ((t = q.pollAt(b)) != null) // ① 将队头的base任务弹出，窃取给我来执行
                        t.doExec(); // ② 执行任务
                    break; // 结束等待，只执行一个任务
                }
            }
        }
        return true;
    }

    /**
     * Waits and/or attempts to assist performing tasks indefinitely
     * until the {@link #commonPool()} {@link #isQuiescent}.
     */
    static void quiesceCommonPool() {
        common.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    /**
     为 ForkJoinPool 中的任务提供扩展管理并行数的接口，一般用在可能会阻塞的任务（如在 Phaser 中用于等待 phase 到下一个generation）。
     *
     * <p>For example, here is a ManagedBlocker based on a
     * ReentrantLock:
     *  <pre> {@code
     * class ManagedLocker implements ManagedBlocker {
     *   final ReentrantLock lock;
     *   boolean hasLock = false;
     *   ManagedLocker(ReentrantLock lock) { this.lock = lock; }
     *   public boolean block() {
     *     if (!hasLock)
     *       lock.lock();
     *     return true;
     *   }
     *   public boolean isReleasable() {
     *     return hasLock || (hasLock = lock.tryLock());
     *   }
     * }}</pre>
     *
     * <p>Here is a class that possibly blocks waiting for an
     * item on a given queue:
     *  <pre> {@code
     * class QueueTaker<E> implements ManagedBlocker {
     *   final BlockingQueue<E> queue;
     *   volatile E item = null;
     *   QueueTaker(BlockingQueue<E> q) { this.queue = q; }
     *   public boolean block() throws InterruptedException {
     *     if (item == null)
     *       item = queue.take();
     *     return true;
     *   }
     *   public boolean isReleasable() {
     *     return item != null || (item = queue.poll()) != null;
     *   }
     *   public E getItem() { // call after pool.managedBlock completes
     *     return item;
     *   }
     * }}</pre>
     */
    public static interface ManagedBlocker {
        /**
         * Possibly blocks the current thread, for example waiting for
         * a lock or condition.
         *
         * @return {@code true} if no additional blocking is necessary
         * (i.e., if isReleasable would return true)
         * @throws InterruptedException if interrupted while waiting
         * (the method is not required to do so, but is allowed to)
         */
        boolean block() throws InterruptedException;

        /**
         * Returns {@code true} if blocking is unnecessary.
         * @return {@code true} if blocking is unnecessary
         */
        boolean isReleasable();
    }

    /**
     * Runs the given possibly blocking task.  When {@linkplain
     * ForkJoinTask#inForkJoinPool() running in a ForkJoinPool}, this
     * method possibly arranges for a spare thread to be activated if
     * necessary to ensure sufficient parallelism while the current
     * thread is blocked in {@link ManagedBlocker#block blocker.block()}.
     *
     * <p>This method repeatedly calls {@code blocker.isReleasable()} and
     * {@code blocker.block()} until either method returns {@code true}.
     * Every call to {@code blocker.block()} is preceded by a call to
     * {@code blocker.isReleasable()} that returned {@code false}.
     *
     * <p>If not running in a ForkJoinPool, this method is
     * behaviorally equivalent to
     *  <pre> {@code
     * while (!blocker.isReleasable())
     *   if (blocker.block())
     *     break;}</pre>
     *
     * If running in a ForkJoinPool, the pool may first be expanded to
     * ensure sufficient parallelism available during the call to
     * {@code blocker.block()}.
     *
     * @param blocker the blocker task
     * @throws InterruptedException if {@code blocker.block()} did so
     */
    public static void managedBlock(ManagedBlocker blocker)
        throws InterruptedException {
        ForkJoinPool p;
        ForkJoinWorkerThread wt;
        Thread t = Thread.currentThread();
        if ((t instanceof ForkJoinWorkerThread) &&
            (p = (wt = (ForkJoinWorkerThread)t).pool) != null) {
            WorkQueue w = wt.workQueue;
            while (!blocker.isReleasable()) {
                if (p.tryCompensate(w)) {
                    try {
                        do {} while (!blocker.isReleasable() &&
                                     !blocker.block());
                    } finally {
                        U.getAndAddLong(p, CTL, AC_UNIT);
                    }
                    break;
                }
            }
        }
        else {
            do {} while (!blocker.isReleasable() &&
                         !blocker.block());
        }
    }

    // AbstractExecutorService overrides.  These rely on undocumented
    // fact that ForkJoinTask.adapt returns ForkJoinTasks that also
    // implement RunnableFuture.

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ForkJoinTask.AdaptedRunnable<T>(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ForkJoinTask.AdaptedCallable<T>(callable);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final int  ABASE;
    private static final int  ASHIFT;
    private static final long CTL;
    private static final long RUNSTATE;
    private static final long STEALCOUNTER;
    private static final long PARKBLOCKER;
    private static final long QTOP;
    private static final long QLOCK;
    private static final long QSCANSTATE;
    private static final long QPARKER;
    private static final long QCURRENTSTEAL;
    private static final long QCURRENTJOIN;

    static {
        // 静态初始化
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ForkJoinPool.class;
            CTL = U.objectFieldOffset(k.getDeclaredField("ctl"));
            RUNSTATE = U.objectFieldOffset(k.getDeclaredField("runState"));
            STEALCOUNTER = U.objectFieldOffset(k.getDeclaredField("stealCounter"));
            Class<?> tk = Thread.class;
            PARKBLOCKER = U.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
            Class<?> wk = WorkQueue.class;
            QTOP = U.objectFieldOffset(wk.getDeclaredField("top"));
            QLOCK = U.objectFieldOffset(wk.getDeclaredField("qlock"));
            QSCANSTATE = U.objectFieldOffset(wk.getDeclaredField("scanState"));
            QPARKER = U.objectFieldOffset(wk.getDeclaredField("parker"));
            QCURRENTSTEAL = U.objectFieldOffset(wk.getDeclaredField("currentSteal"));
            QCURRENTJOIN = U.objectFieldOffset(wk.getDeclaredField("currentJoin"));
            Class<?> ak = ForkJoinTask[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }

        commonMaxSpares = DEFAULT_COMMON_MAX_SPARES;
        defaultForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory(); // 默认线程工厂
        modifyThreadPermission = new RuntimePermission("modifyThread");

        common = java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction<ForkJoinPool>() {
                public ForkJoinPool run() { return makeCommonPool(); }}); // 创建common线程池
        int par = common.config & SMASK; // report 1 even if threads disabled
        commonParallelism = par > 0 ? par : 1; // common池并行度
    }

    /**
     * Creates and returns the common pool, respecting user settings
     * specified via system properties.
     */
    private static ForkJoinPool makeCommonPool() {
        int parallelism = -1;
        ForkJoinWorkerThreadFactory factory = null;
        UncaughtExceptionHandler handler = null;
        try {  // ignore exceptions in accessing/parsing properties
            String pp = System.getProperty
                ("java.util.concurrent.ForkJoinPool.common.parallelism");//并行度
            String fp = System.getProperty
                ("java.util.concurrent.ForkJoinPool.common.threadFactory");//线程工厂
            String hp = System.getProperty
                ("java.util.concurrent.ForkJoinPool.common.exceptionHandler");//异常处理类
            if (pp != null)
                parallelism = Integer.parseInt(pp);
            if (fp != null)
                factory = ((ForkJoinWorkerThreadFactory)ClassLoader.
                           getSystemClassLoader().loadClass(fp).newInstance());
            if (hp != null)
                handler = ((UncaughtExceptionHandler)ClassLoader.
                           getSystemClassLoader().loadClass(hp).newInstance());
        } catch (Exception ignore) {
        }
        if (factory == null) {
            if (System.getSecurityManager() == null)
                factory = defaultForkJoinWorkerThreadFactory;
            else // use security-managed default
                factory = new InnocuousForkJoinWorkerThreadFactory();
        }
        if (parallelism < 0 && // default 1 less than #cores
            (parallelism = Runtime.getRuntime().availableProcessors() - 1) <= 0)
            parallelism = 1; //默认并行度为1
        if (parallelism > MAX_CAP)
            parallelism = MAX_CAP;
        return new ForkJoinPool(parallelism, factory, handler, LIFO_QUEUE, "ForkJoinPool.commonPool-worker-");
    }

    /**
     * 实现了 ForkJoinWorkerThreadFactory，无许可线程工厂，当系统变量中有系统安全管理相关属性时，默认使用这个工厂创建工作线程。
     */
    static final class InnocuousForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

        /**
         * An ACC to restrict permissions for the factory itself.
         * The constructed workers have no permissions set.
         */
        private static final AccessControlContext innocuousAcc;
        static {
            Permissions innocuousPerms = new Permissions();
            innocuousPerms.add(modifyThreadPermission);
            innocuousPerms.add(new RuntimePermission("enableContextClassLoaderOverride"));
            innocuousPerms.add(new RuntimePermission("modifyThreadGroup"));
            innocuousAcc = new AccessControlContext(new ProtectionDomain[] {new ProtectionDomain(null, innocuousPerms)});
        }

        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return (ForkJoinWorkerThread.InnocuousForkJoinWorkerThread)
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<ForkJoinWorkerThread>() {
                    public ForkJoinWorkerThread run() {
                        return new ForkJoinWorkerThread.
                            InnocuousForkJoinWorkerThread(pool);
                    }}, innocuousAcc);
        }
    }

}
