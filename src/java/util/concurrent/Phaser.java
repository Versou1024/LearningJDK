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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * A reusable synchronization barrier, similar in functionality to
 * {@link java.util.concurrent.CyclicBarrier CyclicBarrier} and
 * {@link java.util.concurrent.CountDownLatch CountDownLatch}
 * but supporting more flexible usage.
 *
 * <p><b>Registration.</b> Unlike the case for other barriers, the
 * number of parties <em>registered</em> to synchronize on a phaser
 * may vary over time.  Tasks may be registered at any time (using
 * methods {@link #register}, {@link #bulkRegister}, or forms of
 * constructors establishing initial numbers of parties), and
 * optionally deregistered upon any arrival (using {@link
 * #arriveAndDeregister}).  As is the case with most basic
 * synchronization constructs, registration and deregistration affect
 * only internal counts; they do not establish any further internal
 * bookkeeping, so tasks cannot query whether they are registered.
 * (However, you can introduce such bookkeeping by subclassing this
 * class.)
 *
 * <p><b>Synchronization.</b> Like a {@code CyclicBarrier}, a {@code
 * Phaser} may be repeatedly awaited.  Method {@link
 * #arriveAndAwaitAdvance} has effect analogous to {@link
 * java.util.concurrent.CyclicBarrier#await CyclicBarrier.await}. Each
 * generation of a phaser has an associated phase number. The phase
 * number starts at zero, and advances when all parties arrive at the
 * phaser, wrapping around to zero after reaching {@code
 * Integer.MAX_VALUE}. The use of phase numbers enables independent
 * control of actions upon arrival at a phaser and upon awaiting
 * others, via two kinds of methods that may be invoked by any
 * registered party:
 *
 * <ul>
 *
 *   <li> <b>Arrival.</b> Methods {@link #arrive} and
 *       {@link #arriveAndDeregister} record arrival.  These methods
 *       do not block, but return an associated <em>arrival phase
 *       number</em>; that is, the phase number of the phaser to which
 *       the arrival applied. When the final party for a given phase
 *       arrives, an optional action is performed and the phase
 *       advances.  These actions are performed by the party
 *       triggering a phase advance, and are arranged by overriding
 *       method {@link #onAdvance(int, int)}, which also controls
 *       termination. Overriding this method is similar to, but more
 *       flexible than, providing a barrier action to a {@code
 *       CyclicBarrier}.
 *
 *   <li> <b>Waiting.</b> Method {@link #awaitAdvance} requires an
 *       argument indicating an arrival phase number, and returns when
 *       the phaser advances to (or is already at) a different phase.
 *       Unlike similar constructions using {@code CyclicBarrier},
 *       method {@code awaitAdvance} continues to wait even if the
 *       waiting thread is interrupted. Interruptible and timeout
 *       versions are also available, but exceptions encountered while
 *       tasks wait interruptibly or with timeout do not change the
 *       state of the phaser. If necessary, you can perform any
 *       associated recovery within handlers of those exceptions,
 *       often after invoking {@code forceTermination}.  Phasers may
 *       also be used by tasks executing in a {@link ForkJoinPool},
 *       which will ensure sufficient parallelism to execute tasks
 *       when others are blocked waiting for a phase to advance.
 *
 * </ul>
 *
 * <p><b>Termination.</b> A phaser may enter a <em>termination</em>
 * state, that may be checked using method {@link #isTerminated}. Upon
 * termination, all synchronization methods immediately return without
 * waiting for advance, as indicated by a negative return value.
 * Similarly, attempts to register upon termination have no effect.
 * Termination is triggered when an invocation of {@code onAdvance}
 * returns {@code true}. The default implementation returns {@code
 * true} if a deregistration has caused the number of registered
 * parties to become zero.  As illustrated below, when phasers control
 * actions with a fixed number of iterations, it is often convenient
 * to override this method to cause termination when the current phase
 * number reaches a threshold. Method {@link #forceTermination} is
 * also available to abruptly release waiting threads and allow them
 * to terminate.
 *
 * <p><b>Tiering.</b> Phasers may be <em>tiered</em> (i.e.,
 * constructed in tree structures) to reduce contention. Phasers with
 * large numbers of parties that would otherwise experience heavy
 * synchronization contention costs may instead be set up so that
 * groups of sub-phasers share a common parent.  This may greatly
 * increase throughput even though it incurs greater per-operation
 * overhead.
 *
 * <p>In a tree of tiered phasers, registration and deregistration of
 * child phasers with their parent are managed automatically.
 * Whenever the number of registered parties of a child phaser becomes
 * non-zero (as established in the {@link #Phaser(Phaser,int)}
 * constructor, {@link #register}, or {@link #bulkRegister}), the
 * child phaser is registered with its parent.  Whenever the number of
 * registered parties becomes zero as the result of an invocation of
 * {@link #arriveAndDeregister}, the child phaser is deregistered
 * from its parent.
 *
 * <p><b>Monitoring.</b> While synchronization methods may be invoked
 * only by registered parties, the current state of a phaser may be
 * monitored by any caller.  At any given moment there are {@link
 * #getRegisteredParties} parties in total, of which {@link
 * #getArrivedParties} have arrived at the current phase ({@link
 * #getPhase}).  When the remaining ({@link #getUnarrivedParties})
 * parties arrive, the phase advances.  The values returned by these
 * methods may reflect transient states and so are not in general
 * useful for synchronization control.  Method {@link #toString}
 * returns snapshots of these state queries in a form convenient for
 * informal monitoring.
 *
 * <p><b>Sample usages:</b>
 *
 * <p>A {@code Phaser} may be used instead of a {@code CountDownLatch}
 * to control a one-shot action serving a variable number of parties.
 * The typical idiom is for the method setting this up to first
 * register, then start the actions, then deregister, as in:
 *
 *  <pre> {@code
 * void runTasks(List<Runnable> tasks) {
 *   final Phaser phaser = new Phaser(1); // "1" to register self
 *   // create and start threads
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         phaser.arriveAndAwaitAdvance(); // await all creation
 *         task.run();
 *       }
 *     }.start();
 *   }
 *
 *   // allow threads to start and deregister self
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 * <p>One way to cause a set of threads to repeatedly perform actions
 * for a given number of iterations is to override {@code onAdvance}:
 *
 *  <pre> {@code
 * void startTasks(List<Runnable> tasks, final int iterations) {
 *   final Phaser phaser = new Phaser() {
 *     protected boolean onAdvance(int phase, int registeredParties) {
 *       return phase >= iterations || registeredParties == 0;
 *     }
 *   };
 *   phaser.register();
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         do {
 *           task.run();
 *           phaser.arriveAndAwaitAdvance();
 *         } while (!phaser.isTerminated());
 *       }
 *     }.start();
 *   }
 *   phaser.arriveAndDeregister(); // deregister self, don't wait
 * }}</pre>
 *
 * If the main task must later await termination, it
 * may re-register and then execute a similar loop:
 *  <pre> {@code
 *   // ...
 *   phaser.register();
 *   while (!phaser.isTerminated())
 *     phaser.arriveAndAwaitAdvance();}</pre>
 *
 * <p>Related constructions may be used to await particular phase numbers
 * in contexts where you are sure that the phase will never wrap around
 * {@code Integer.MAX_VALUE}. For example:
 *
 *  <pre> {@code
 * void awaitPhase(Phaser phaser, int phase) {
 *   int p = phaser.register(); // assumes caller not already registered
 *   while (p < phase) {
 *     if (phaser.isTerminated())
 *       // ... deal with unexpected termination
 *     else
 *       p = phaser.arriveAndAwaitAdvance();
 *   }
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 *
 * <p>To create a set of {@code n} tasks using a tree of phasers, you
 * could use code of the following form, assuming a Task class with a
 * constructor accepting a {@code Phaser} that it registers with upon
 * construction. After invocation of {@code build(new Task[n], 0, n,
 * new Phaser())}, these tasks could then be started, for example by
 * submitting to a pool:
 *
 *  <pre> {@code
 * void build(Task[] tasks, int lo, int hi, Phaser ph) {
 *   if (hi - lo > TASKS_PER_PHASER) {
 *     for (int i = lo; i < hi; i += TASKS_PER_PHASER) {
 *       int j = Math.min(i + TASKS_PER_PHASER, hi);
 *       build(tasks, i, j, new Phaser(ph));
 *     }
 *   } else {
 *     for (int i = lo; i < hi; ++i)
 *       tasks[i] = new Task(ph);
 *       // assumes new Task(ph) performs ph.register()
 *   }
 * }}</pre>
 *
 * The best value of {@code TASKS_PER_PHASER} depends mainly on
 * expected synchronization rates. A value as low as four may
 * be appropriate for extremely small per-phase task bodies (thus
 * high rates), or up to hundreds for extremely large ones.
 *
 * <p><b>Implementation notes</b>: This implementation restricts the
 * maximum number of parties to 65535. Attempts to register additional
 * parties result in {@code IllegalStateException}. However, you can and
 * should create tiered phasers to accommodate arbitrarily large sets
 * of participants.
 *
 * @since 1.7
 * @author Doug Lea
 */
public class Phaser {
    /*
     * https://pdai.tech/md/java/thread/java-thread-x-juc-tool-phaser.html
     * Phaser的特点：
     * 1、阶段数phaser的概念，超过Integer.MAX_VALUE后又会从0开始
     * 2、同CountDownLatch、CyclicBarrier类似，只是负责线程间的同步，每个阶段都需要用户自己投入线程作为参与者，
     *      比如，主线程在判断phase=1时的parties是3，就需要用户启动三个线程完成阶段1的三个任务，执行完后调用arrive()/arriveAndAwaitAdvance()/arriveAndDeregister()来完成阶段1，
     *                    假设一个线程调用arrive()到达后就离开，一个线程调用arriveAndAwaitAdvance()到达后阻塞起来，一个线程调用arriveAndDeregister()到达后离开前还将参与者数量减1
     *                    在这之后主线程调用awaitAdvance()阻塞等待已经到phase阶段1执行结束。
     *           主线程判断phase=2时，又需要两个线程完成阶段2的任务，用户可以选择之前的线程也可以选择新的线程帮助完成
     */

    /**
     * Primary state representation, holding four bit-fields:
     * <pre/>
     * unarrived  -- the number of parties yet to hit barrier (bits  0-15)
     * parties    -- the number of parties to wait            (bits 16-31)
     * phase      -- the generation of the barrier            (bits 32-62)
     * terminated -- set if barrier is terminated             (bit  63 / sign)
     *
     */
    private volatile long state; // 一共64位，分为4部分有效数据

    private static final int  MAX_PARTIES     = 0xffff;            //
    private static final int  MAX_PHASE       = Integer.MAX_VALUE; //
    private static final int  PARTIES_SHIFT   = 16;
    private static final int  PHASE_SHIFT     = 32;
    private static final int  UNARRIVED_MASK  = 0xffff;      // 计算未到达的线程的掩码 0-15
    private static final long PARTIES_MASK    = 0xffff0000L; // 计算parties的掩码 23-62
    private static final long COUNTS_MASK     = 0xffffffffL; // 计算数量的压那么
    private static final long TERMINATION_BIT = 1L << 63; // 最高位的掩码 -- 是否被终止

    // some special values
    private static final int  ONE_ARRIVAL     = 1; // 一次一个参与者完成
    private static final int  ONE_PARTY       = 1 << PARTIES_SHIFT; // 增加减少参与者的使用
    private static final int  ONE_DEREGISTER  = ONE_ARRIVAL|ONE_PARTY; // 减少参与者的使用
    private static final int  EMPTY           = 1; // 没有参与者使用

    // The following unpacking methods are usually manually inlined

    private static int unarrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
    }

    private static int partiesOf(long s) {
        return (int)s >>> PARTIES_SHIFT;
    }

    private static int phaseOf(long s) {
        return (int)(s >>> PHASE_SHIFT);
    }

    private static int arrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 : (counts >>> PARTIES_SHIFT) - (counts & UNARRIVED_MASK);
    }

    /**
     * The parent of this phaser, or null if none
     */
    private final Phaser parent;

    /**
     * The root of phaser tree. Equals this if not in a tree.
     */
    private final Phaser root; // phaser树，若果没有树，那么this通常等于root

    /**
     * Heads of Treiber stacks for waiting threads. To eliminate
     * contention when releasing some threads while adding others, we
     * use two of them, alternating across even and odd phases.
     * Subphasers share queues with root to speed up releases.
     */
    private final AtomicReference<QNode> evenQ; // 等待线程的Treiber堆栈头。为了在释放一些线程同时添加其他线程时消除争用，我们使用其中两个线程，
    private final AtomicReference<QNode> oddQ; // 在偶数和奇数阶段交替使用。子阶段与root共享队列以加快发布速度。

    private AtomicReference<QNode> queueFor(int phase) {
        // 偶数阶段使用evenQ，奇数阶段是oddQ
        return ((phase & 1) == 0) ? evenQ : oddQ;
    }

    /**
     * Returns message string for bounds exceptions on arrival.
     */
    private String badArrive(long s) {
        return "Attempted arrival of unregistered party for " +
            stateToString(s);
    }

    /**
     * Returns message string for bounds exceptions on registration.
     */
    private String badRegister(long s) {
        return "Attempt to register more than " +
            MAX_PARTIES + " parties for " + stateToString(s);
    }

    /**
     * Main implementation for methods arrive and arriveAndDeregister.
     * Manually tuned to speed up and minimize race windows for the
     * common case of just decrementing unarrived field.
     *
     * @param adjust value to subtract from state;
     *               ONE_ARRIVAL for arrive,
     *               ONE_DEREGISTER for arriveAndDeregister
     */
    private int doArrive(int adjust) {
        /*
         * 该方法主要被arrive()和arriveAndDeregister()调用
         * 主要作用：
         * 1、根据adjust更新未到达的参与者数量，下一次参与者的数量
         * 2、本线程为最后一个参与者会分三种情况执行后，返回阶段数
         *      a.只有一个phaser，需要调用onAdvance()[phaser是否可以运行下一个阶段]，可以继续运行则为下阶段更新state，释放当前phaser的阻塞的线程
         *          前置知识：phaser允许作为另一个phaser的一个参与者，
         *              若phaser完成某一个阶段，就会告知父phaser更新未到达数量；
         *              若phaser完成某一个阶段，参与者数量为0，就会告知父phaser更新未到达数量，以及下一次的参与者数量；
         *      b.该phaser有父节点phaser，但下一次参与者数量为0，就需要告知父phaser，做ONE_DEREGISTER操作
         *      c.该phaser有父节点phaser，但下一次参与者数量不为0，就需要告知父phaser，做ONE_ARRIVE操作
         * 3、本线程非最后一个参与者，执行后直接返回阶段数
         */
        final Phaser root = this.root;
        // 死循环
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0)
                return phase;
            int counts = (int)s;
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            // 更新阶段数
            // adjust 是 ONE_ARRIVAL 会减去未到达数
            // adjust 是 ONE_DEREGISTER 会减去未到达数以及注册数
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s-=adjust)) {
                // 更新前未到达数量为1，暗示此次更新后数量为0
                if (unarrived == 1) {
                    long n = s & PARTIES_MASK;  // base of next state
                    int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                    // root就是当前phaser，调用模板方法onAdvance，并更新state，唤醒当前phaser中的所有元素
                    if (root == this) {
                        if (onAdvance(phase, nextUnarrived))
                            n |= TERMINATION_BIT;
                        else if (nextUnarrived == 0)
                            n |= EMPTY;
                        else
                            n |= nextUnarrived;
                        int nextPhase = (phase + 1) & MAX_PHASE;
                        n |= (long)nextPhase << PHASE_SHIFT;
                        UNSAFE.compareAndSwapLong(this, stateOffset, s, n);
                        releaseWaiters(phase);
                    }
                    // root非当前phaser，且下一次参与者数量为0，告知父phaser未到达以及下一次参与者数量减去1
                    else if (nextUnarrived == 0) { // propagate deregistration
                        phase = parent.doArrive(ONE_DEREGISTER);
                        UNSAFE.compareAndSwapLong(this, stateOffset,
                                                  s, s | EMPTY);
                    }
                    // root非当前phaser，且下一次参与者数量不为0，告知父phaser未到达参与者数量减去1
                    else
                        phase = parent.doArrive(ONE_ARRIVAL);
                }
                return phase;
            }
        }
    }

    /**
     * Implementation of register, bulkRegister
     *
     * @param registrations number to add to both parties and
     * unarrived fields. Must be greater than zero.
     */
    private int doRegister(int registrations) {
        // adjustment to state
        long adjust = ((long)registrations << PARTIES_SHIFT) | registrations; // adjust需要调整的值，包括0-16位的未达到参与者数量，16-32位的目标参与者数量
        final Phaser parent = this.parent;
        int phase;
        for (;;) {
            // 获取状态s、低32位的count、参与者数量parties、未到达参与者数量unarrived、
            long s = (parent == null) ? state : reconcileState();
            int counts = (int)s;
            int parties = counts >>> PARTIES_SHIFT;
            int unarrived = counts & UNARRIVED_MASK;
            // 检查是否溢出
            if (registrations > MAX_PARTIES - parties)
                throw new IllegalStateException(badRegister(s));
            phase = (int)(s >>> PHASE_SHIFT);
            // 当前阶段phase小于0，注册失败
            if (phase < 0)
                break;
            // 初始化Phaser时若形参parties为0，那么counts就会等于EMPTY，counts != EMPTY说明就不是第一个参与者
            if (counts != EMPTY) {                  // not 1st registration
                // 且parent为null，独立Phaser
                if (parent == null || reconcileState() == s) {
                    // unarrived等于0说明当前阶段正在执行onAdvance()方法，等待其执行完
                    if (unarrived == 0)             // wait out advance
                        root.internalAwaitAdvance(phase, null);
                    // 否则就修改state的值，增加adjust，如果成功就跳出循环
                    else if (UNSAFE.compareAndSwapLong(this, stateOffset,
                                                       s, s + adjust))
                        break;
                }
            }
            // 否则，作为第一个参与者，且parent为null，没有分层结构 -- 即独立phaser
            else if (parent == null) {              // 1st root registration
                // 计算注册结束后下一个阶段phase的state值
                long next = ((long)phase << PHASE_SHIFT) | adjust;
                if (UNSAFE.compareAndSwapLong(this, stateOffset, s, next))
                    break;
            }
            // 从Phaser(Phaser parent)中，可知parent非null，counts为Empty就是多层级Phaser结构】
            // 进入到这里就表示当前phaser需要先parnet进行注册
            else {
                // 多层级阶段的处理方式
                synchronized (this) {               // 1st sub registration
                    if (state == s) {               // recheck under lock
                        phase = parent.doRegister(1); // 当前phaser需要
                        if (phase < 0)
                            break;
                        // finish registration whenever parent registration
                        // succeeded, even when racing with termination,
                        // since these are part of the same "transaction".
                        while (!UNSAFE.compareAndSwapLong
                               (this, stateOffset, s,
                                ((long)phase << PHASE_SHIFT) | adjust)) { // 子phaser的阶段数是无用的，只有后32位的parites与unarrived才是有用，需要adjust来调整
                            s = state;
                            phase = (int)(root.state >>> PHASE_SHIFT);
                            // assert (int)s == EMPTY;
                        }
                        break;
                    }
                }
            }
        }
        return phase;
    }

    /**
     * Resolves lagged phase propagation from root if necessary.
     * Reconciliation normally occurs when root has advanced but
     * subphasers have not yet done so, in which case they must finish
     * their own advance by setting unarrived to parties (or if
     * parties is zero, resetting to unregistered EMPTY state).
     *
     * @return reconciled state
     */
    private long reconcileState() {
        /*
         * 上面说过，子Phaser的phase在没有被真正使用之前，允许滞后于它的root节点。非首次注册时，如果Phaser有父节点，则调用reconcileState()方法解决root节点的phase延迟传递问题
         * 方法主要作用：
         * 1、phaser链头即root非当前phaser时，
         */
        final Phaser root = this.root;
        long s = state;
        if (root != this) {
            int phase, p;
            // root的阶段数和当前phaser的阶段数不同
            // 并且，自旋CAS更新当前state，阶段数更新为root的阶段数，COUNTS更新为以下某种情况 [COUNTS就是高16位参与者数量，低16位未到达参与者数量]
            // 1、root的阶段数小于0即终结，COUNTS就是当前phaser的state的COUNTS
            // 2、root的阶段数不是0，
            // 3、

            // 因此可以发现子Phaser的phase在没有被真正使用之前，允许滞后于它的root节点。
            while ((phase = (int)(root.state >>> PHASE_SHIFT)) != (int)(s >>> PHASE_SHIFT)
                    &&
                   !UNSAFE.compareAndSwapLong(this, stateOffset, s, s = (((long)phase << PHASE_SHIFT) | ((phase < 0) ? (s & COUNTS_MASK) : (((p = (int)s >>> PARTIES_SHIFT) == 0) ? EMPTY : ((s & PARTIES_MASK) | p))))))
                s = state;
        }
        return s;
    }

    /**
     * 创建一个新的phaser移相器，不带有任何初始化注册的参与者，没有paret
     * 并且，初始化阶段phase等于0，任何线程使用这个phaser都需要进入该phaser中做注册
     */
    public Phaser() {
        this(null, 0);
    }

    /**
     * 创建一个新的phaser，其具有给定注册未到达参与者数量，没有parent
     * 并且初始化的phase阶段数为0
     *
     * @param parties the number of parties required to advance to the
     * next phase
     * @throws IllegalArgumentException if parties less than zero
     * or greater than the maximum number of parties supported
     */
    public Phaser(int parties) {
        this(null, parties);
    }

    /**
     * Equivalent to {@link #Phaser(Phaser, int) Phaser(parent, 0)}.
     *
     * @param parent the parent phaser
     */
    public Phaser(Phaser parent) {
        this(parent, 0);
    }

    /**
     * Creates a new phaser with the given parent and number of
     * registered unarrived parties.  When the given parent is non-null
     * and the given number of parties is greater than zero, this
     * child phaser is registered with its parent.
     *
     * @param parent the parent phaser
     * @param parties the number of parties required to advance to the
     * next phase
     * @throws IllegalArgumentException if parties less than zero
     * or greater than the maximum number of parties supported
     */
    public Phaser(Phaser parent, int parties) {
        // 检查parties是否超过2^16-1的长度
        if (parties >>> PARTIES_SHIFT != 0)
            throw new IllegalArgumentException("Illegal number of parties");
        int phase = 0;
        this.parent = parent;
        if (parent != null) {
            /*
             * 若当前需要生成的phaser需要作为parent phaser的一个参与者
             * 那么root就是parent.root，同时
             */
            final Phaser root = parent.root;
            this.root = root;
            this.evenQ = root.evenQ;
            this.oddQ = root.oddQ;
            if (parties != 0)
                phase = parent.doRegister(1);
        }
        else {
            this.root = this;
            this.evenQ = new AtomicReference<QNode>();
            this.oddQ = new AtomicReference<QNode>();
        }
        // 状态变量存储分为三段
        // this.state = empty 表示参与者为0
        // 否则再分为三段phaser、parties、unarrived
        this.state = (parties == 0) ? (long)EMPTY :
            ((long)phase << PHASE_SHIFT) |
            ((long)parties << PARTIES_SHIFT) |
            ((long)parties);
    }

    /**
     * Adds a new unarrived party to this phaser.  If an ongoing
     * invocation of {@link #onAdvance} is in progress, this method
     * may await its completion before returning.  If this phaser has
     * a parent, and this phaser previously had no registered parties,
     * this child phaser is also registered with its parent. If
     * this phaser is terminated, the attempt to register has
     * no effect, and a negative value is returned.
     *
     * @return the arrival phase number to which this registration
     * applied.  If this value is negative, then this phaser has
     * terminated, in which case registration has no effect.
     * @throws IllegalStateException if attempting to register more
     * than the maximum supported number of parties
     */
    public int register() {
        return doRegister(1);
    }

    /**
     * Adds the given number of new unarrived parties to this phaser.
     * If an ongoing invocation of {@link #onAdvance} is in progress,
     * this method may await its completion before returning.  If this
     * phaser has a parent, and the given number of parties is greater
     * than zero, and this phaser previously had no registered
     * parties, this child phaser is also registered with its parent.
     * If this phaser is terminated, the attempt to register has no
     * effect, and a negative value is returned.
     *
     * @param parties the number of additional parties required to
     * advance to the next phase
     * @return the arrival phase number to which this registration
     * applied.  If this value is negative, then this phaser has
     * terminated, in which case registration has no effect.
     * @throws IllegalStateException if attempting to register more
     * than the maximum supported number of parties
     * @throws IllegalArgumentException if {@code parties < 0}
     */
    public int bulkRegister(int parties) {
        // 批量注册的前提：1、要求参与者不能小于0；2、参与者等于0已结束，返回阶段数phase
        if (parties < 0)
            throw new IllegalArgumentException();
        if (parties == 0)
            return getPhase();
        return doRegister(parties);
    }

    /**
     * Arrives at this phaser, without waiting for others to arrive.
     *
     * <p>It is a usage error for an unregistered party to invoke this
     * method.  However, this error may result in an {@code
     * IllegalStateException} only upon some subsequent operation on
     * this phaser, if ever.
     *
     * @return the arrival phase number, or a negative value if terminated
     * @throws IllegalStateException if not terminated and the number
     * of unarrived parties would become negative
     */
    public int arrive() {
        return doArrive(ONE_ARRIVAL);
    }

    /**
     * Arrives at this phaser and deregisters from it without waiting
     * for others to arrive. Deregistration reduces the number of
     * parties required to advance in future phases.  If this phaser
     * has a parent, and deregistration causes this phaser to have
     * zero parties, this phaser is also deregistered from its parent.
     *
     * <p>It is a usage error for an unregistered party to invoke this
     * method.  However, this error may result in an {@code
     * IllegalStateException} only upon some subsequent operation on
     * this phaser, if ever.
     *
     * @return the arrival phase number, or a negative value if terminated
     * @throws IllegalStateException if not terminated and the number
     * of registered or unarrived parties would become negative
     */
    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }

    /**
     * Arrives at this phaser and awaits others. Equivalent in effect
     * to {@code awaitAdvance(arrive())}.  If you need to await with
     * interruption or timeout, you can arrange this with an analogous
     * construction using one of the other forms of the {@code
     * awaitAdvance} method.  If instead you need to deregister upon
     * arrival, use {@code awaitAdvance(arriveAndDeregister())}.
     *
     * <p>It is a usage error for an unregistered party to invoke this
     * method.  However, this error may result in an {@code
     * IllegalStateException} only upon some subsequent operation on
     * this phaser, if ever.
     *
     * @return the arrival phase number, or the (negative)
     * {@linkplain #getPhase() current phase} if terminated
     * @throws IllegalStateException if not terminated and the number
     * of unarrived parties would become negative
     */
    public int arriveAndAwaitAdvance() {
        /*
         * 主要工作：
         * 1、修改state值，将未到达数量减去一个，如果不是最后一个到达的参与者，则调用internalAwaitAdvance()方法自旋或进入队列等待
         * 2、如果是最后一个到达的参与者，需要更新phase+1、parties重置、未到达数量重置，然后调用releaseWaiters()唤醒其他线程
         * 3、返回下一个阶段值
         */
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState(); // root!=this就是当前phaser作为parent phaser的一个参与者
            int phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0) // ① 状态s被左移32位后，就获得phase阶段数，如果phase<0,说明最高位就是1，那么说明有人调用forceTermination，或者参与者数量为0，或者onAdvance返回false
                return phase;
            int counts = (int)s;
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            // 修改state的值，未到达数量减去一
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s,
                                          s -= ONE_ARRIVAL)) {
                // 如果不是最后一个到达的，则调用internalAwaitAdvance()方法自旋或进入队列等待
                if (unarrived > 1)
                    return root.internalAwaitAdvance(phase, null);
                // 到这里说明是最后一个到达的参与者
                if (root != this)
                    return parent.arriveAndAwaitAdvance();
                long n = s & PARTIES_MASK;  // parties数量，即下一个阶段需要到达的参与者数量
                int nextUnarrived = (int)n >>> PARTIES_SHIFT; // 下一个阶段的未到达刷您
                // 执行onAdvance()方法，返回true表示下一阶段参与者数量为0了，也就是结束了
                if (onAdvance(phase, nextUnarrived))
                    n |= TERMINATION_BIT; // 返回true，不是需要继续运行啦，将最高位设为1，在①就会返回
                else if (nextUnarrived == 0)
                    n |= EMPTY;
                else
                    n |= nextUnarrived;
                int nextPhase = (phase + 1) & MAX_PHASE; // 阶段数+1
                n |= (long)nextPhase << PHASE_SHIFT;
                // 到这里计算出的，n值就是下一个阶段的状态值state
                if (!UNSAFE.compareAndSwapLong(this, stateOffset, s, n))
                    return (int)(state >>> PHASE_SHIFT); // 判断高63位，是否已经中断
                // 唤醒其他参与者并进入到下一个阶段
                releaseWaiters(phase);
                return nextPhase; // 返回下一个阶段
            }
        }
    }

    /**
     * Awaits the phase of this phaser to advance from the given phase
     * value, returning immediately if the current phase is not equal
     * to the given phase value or this phaser is terminated.
     *
     * @param phase an arrival phase number, or negative value if
     * terminated; this argument is normally the value returned by a
     * previous call to {@code arrive} or {@code arriveAndDeregister}.
     * @return the next arrival phase number, or the argument if it is
     * negative, or the (negative) {@linkplain #getPhase() current phase}
     * if terminated
     */
    public int awaitAdvance(int phase) {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState(); // 获取纠正后的state
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase; // phaser已经被终止
        if (p == phase) // 假设插入的phase是第二阶段，然后当前阶段就是第二阶段，就需要阻塞起来
            return root.internalAwaitAdvance(phase, null);
        return p; // 直到进入第三阶段被唤醒后，进入该方法
    }

    /**
     * Awaits the phase of this phaser to advance from the given phase
     * value, throwing {@code InterruptedException} if interrupted
     * while waiting, or returning immediately if the current phase is
     * not equal to the given phase value or this phaser is
     * terminated.
     *
     * @param phase an arrival phase number, or negative value if
     * terminated; this argument is normally the value returned by a
     * previous call to {@code arrive} or {@code arriveAndDeregister}.
     * @return the next arrival phase number, or the argument if it is
     * negative, or the (negative) {@linkplain #getPhase() current phase}
     * if terminated
     * @throws InterruptedException if thread interrupted while waiting
     */
    public int awaitAdvanceInterruptibly(int phase)
        throws InterruptedException {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, false, 0L);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
        }
        return p;
    }

    /**
     * Awaits the phase of this phaser to advance from the given phase
     * value or the given timeout to elapse, throwing {@code
     * InterruptedException} if interrupted while waiting, or
     * returning immediately if the current phase is not equal to the
     * given phase value or this phaser is terminated.
     *
     * @param phase an arrival phase number, or negative value if
     * terminated; this argument is normally the value returned by a
     * previous call to {@code arrive} or {@code arriveAndDeregister}.
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return the next arrival phase number, or the argument if it is
     * negative, or the (negative) {@linkplain #getPhase() current phase}
     * if terminated
     * @throws InterruptedException if thread interrupted while waiting
     * @throws TimeoutException if timed out while waiting
     */
    public int awaitAdvanceInterruptibly(int phase,
                                         long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, true, nanos);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
            else if (p == phase)
                throw new TimeoutException();
        }
        return p;
    }

    /**
     * Forces this phaser to enter termination state.  Counts of
     * registered parties are unaffected.  If this phaser is a member
     * of a tiered set of phasers, then all of the phasers in the set
     * are terminated.  If this phaser is already terminated, this
     * method has no effect.  This method may be useful for
     * coordinating recovery after one or more tasks encounter
     * unexpected exceptions.
     */
    public void forceTermination() {
        /*
         * forceTermination方法也可以释放等待线程并且允许它们终止；
         * 作用：
         * 1、更新state为终止状态，方便其他线程发现这个状态并做出反应
         * 2、将两只队列中的waiter多唤醒
         */
        final Phaser root = this.root;
        long s;
        while ((s = root.state) >= 0) {
            if (UNSAFE.compareAndSwapLong(root, stateOffset,
                                          s, s | TERMINATION_BIT)) {
                // 将两只队列中的阻塞的线程都唤醒
                releaseWaiters(0); // Waiters on evenQ
                releaseWaiters(1); // Waiters on oddQ
                return;
            }
        }
    }

    /**
     * Returns the current phase number. The maximum phase number is
     * {@code Integer.MAX_VALUE}, after which it restarts at
     * zero. Upon termination, the phase number is negative,
     * in which case the prevailing phase prior to termination
     * may be obtained via {@code getPhase() + Integer.MIN_VALUE}.
     *
     * @return the phase number, or a negative value if terminated
     */
    public final int getPhase() {
        return (int)(root.state >>> PHASE_SHIFT); // 阶段数为state的高32位 -- 是root的phase
    }

    /**
     * Returns the number of parties registered at this phaser.
     *
     * @return the number of parties
     */
    public int getRegisteredParties() {
        return partiesOf(state); // 注册数为state的16-32位
    }

    /**
     * Returns the number of registered parties that have arrived at
     * the current phase of this phaser. If this phaser has terminated,
     * the returned value is meaningless and arbitrary.
     *
     * @return the number of arrived parties
     */
    public int getArrivedParties() {
        return arrivedOf(reconcileState());
    }

    /**
     * Returns the number of registered parties that have not yet
     * arrived at the current phase of this phaser. If this phaser has
     * terminated, the returned value is meaningless and arbitrary.
     *
     * @return the number of unarrived parties
     */
    public int getUnarrivedParties() {
        return unarrivedOf(reconcileState());
    }

    /**
     * Returns the parent of this phaser, or {@code null} if none.
     *
     * @return the parent of this phaser, or {@code null} if none
     */
    public Phaser getParent() {
        return parent;
    }

    /**
     * Returns the root ancestor of this phaser, which is the same as
     * this phaser if it has no parent.
     *
     * @return the root ancestor of this phaser
     */
    public Phaser getRoot() {
        return root;
    }

    /**
     * Returns {@code true} if this phaser has been terminated.
     *
     * @return {@code true} if this phaser has been terminated
     */
    public boolean isTerminated() {
        return root.state < 0L; // 最高位就表示是否终止的表示
    }

    /**
     * Overridable method to perform an action upon impending phase
     * advance, and to control termination. This method is invoked
     * upon arrival of the party advancing this phaser (when all other
     * waiting parties are dormant).  If this method returns {@code
     * true}, this phaser will be set to a final termination state
     * upon advance, and subsequent calls to {@link #isTerminated}
     * will return true. Any (unchecked) Exception or Error thrown by
     * an invocation of this method is propagated to the party
     * attempting to advance this phaser, in which case no advance
     * occurs.
     *
     * <p>The arguments to this method provide the state of the phaser
     * prevailing for the current transition.  The effects of invoking
     * arrival, registration, and waiting methods on this phaser from
     * within {@code onAdvance} are unspecified and should not be
     * relied on.
     *
     * <p>If this phaser is a member of a tiered set of phasers, then
     * {@code onAdvance} is invoked only for its root phaser on each
     * advance.
     *
     * <p>To support the most common use cases, the default
     * implementation of this method returns {@code true} when the
     * number of registered parties has become zero as the result of a
     * party invoking {@code arriveAndDeregister}.  You can disable
     * this behavior, thus enabling continuation upon future
     * registrations, by overriding this method to always return
     * {@code false}:
     *
     * <pre> {@code
     * Phaser phaser = new Phaser() {
     *   protected boolean onAdvance(int phase, int parties) { return false; }
     * }}</pre>
     *
     * @param phase the current phase number on entry to this method,
     * before this phaser is advanced
     * @param registeredParties the current number of registered parties
     * @return {@code true} if this phaser should terminate
     */
    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }

    /**
     * Returns a string identifying this phaser, as well as its
     * state.  The state, in brackets, includes the String {@code
     * "phase = "} followed by the phase number, {@code "parties = "}
     * followed by the number of registered parties, and {@code
     * "arrived = "} followed by the number of arrived parties.
     *
     * @return a string identifying this phaser, as well as its state
     */
    public String toString() {
        return stateToString(reconcileState());
    }

    /**
     * Implementation of toString and string-based error messages
     */
    private String stateToString(long s) {
        return super.toString() +
            "[phase = " + phaseOf(s) +
            " parties = " + partiesOf(s) +
            " arrived = " + arrivedOf(s) + "]";
    }

    // Waiting mechanics

    /**
     * Removes and signals threads from queue for phase.
     */
    private void releaseWaiters(int phase) {
        /*
         * 主要工作：
         * 1、计算当前阶段所在所在队列
         * 2、遍历整个队列，将队列中的所有node的线程唤醒
         */
        QNode q;   // first element of queue
        Thread t;  // its thread
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        // 队列头不为空，且仍停留在当前阶段，CAS自旋尝试更新head为head.next
        // 然后清理队头的线程，并尝试唤醒对方
        while ((q = head.get()) != null &&
               q.phase != (int)(root.state >>> PHASE_SHIFT)) {
            if (head.compareAndSet(q, q.next) &&
                (t = q.thread) != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }

    /**
     * Variant of releaseWaiters that additionally tries to remove any
     * nodes no longer waiting for advance due to timeout or
     * interrupt. Currently, nodes are removed only if they are at
     * head of queue, which suffices to reduce memory footprint in
     * most usages.
     *
     * @return current phase on exit
     */
    private int abortWait(int phase) {
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        for (;;) {
            Thread t;
            QNode q = head.get();
            int p = (int)(root.state >>> PHASE_SHIFT);
            if (q == null || ((t = q.thread) != null && q.phase == p))
                return p;
            if (head.compareAndSet(q, q.next) && t != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }

    /** The number of CPUs, for spin control */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The number of times to spin before blocking while waiting for
     * advance, per arrival while waiting. On multiprocessors, fully
     * blocking and waking up a large number of threads all at once is
     * usually a very slow process, so we use rechargeable spins to
     * avoid it when threads regularly arrive: When a thread in
     * internalAwaitAdvance notices another arrival before blocking,
     * and there appear to be enough CPUs available, it spins
     * SPINS_PER_ARRIVAL more times before blocking. The value trades
     * off good-citizenship vs big unnecessary slowdowns.
     */
    static final int SPINS_PER_ARRIVAL = (NCPU < 2) ? 1 : 1 << 8;

    /**
     * Possibly blocks and waits for phase to advance unless aborted.
     * Call only on root phaser.
     *
     * @param phase current phase
     * @param node if non-null, the wait node to track interrupt and timeout;
     * if null, denotes noninterruptible wait
     * @return current phase
     */
    private int internalAwaitAdvance(int phase, QNode node) {
        // 主要工作：
        // 1、如果仍在当前阶段，则会不断自旋，自旋过程中，会检查unarrived，如果有变化，就延长自旋次数以期待能够不使用耗时的阻塞
        // 2、自旋次数用完后，尝试创建node，若当前阶段没有变化则将node头插入到队列中，之后进入阻塞状态
        // 3、线程被唤醒后，检查阶段数是否已经变化，将node中的线程清空，然后调用releaseWaiters(phaser)唤醒phaser阶段中的线程
        // 额外：线程阻塞期间，若线程数量已经到达当前参与者的parties数量，就会唤醒一个node，并然该node唤醒其他node

        // 保证队列为空
        releaseWaiters(phase-1);    // 确保上一个阶段的队列已经被清空，否则继续做清空操作
        boolean queued = false;           // true when node is enqueued
        int lastUnarrived = 0;            // to increase spins upon change
        // 自旋次数
        int spins = SPINS_PER_ARRIVAL;
        long s;
        int p;
        // 检查当前阶段是否变化，如果变化了说明进入下一个阶段了，这时候就没有必要自旋了
        while ((p = (int)((s = state) >>> PHASE_SHIFT)) == phase) {
            // 仍在当前阶段，进入循环体
            // 如果node为空，注册的时候传入的为空
            if (node == null) {           // spinning in noninterruptible mode
                // 未完成的参与者数量
                int unarrived = (int)s & UNARRIVED_MASK;
                // unarrived有变化，增加自旋次数 -- 意义：有参与者已到达，距离所有参与者到达更近啦，可以增加自旋，避免使用耗时的阻塞
                if (unarrived != lastUnarrived &&
                    (lastUnarrived = unarrived) < NCPU)
                    spins += SPINS_PER_ARRIVAL;
                boolean interrupted = Thread.interrupted();
                // 自旋次数spins小于0，表示自旋结束，但仍在当前阶段，则新建一个节点
                if (interrupted || --spins < 0) { // need node to record intr
                    node = new QNode(this, phase, false, false, 0L); // 永久阻塞的QNode
                    node.wasInterrupted = interrupted;
                }
            }
            // 自旋中发现节点已经被释放，结束自旋
            else if (node.isReleasable()) // done or aborted
                break;
            // 创建完节点后，还未进入队列即queued为false -- 自旋CAS入队操作
            else if (!queued) {           // push onto queue
                AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ; // 判断该入那个一个队列，和阶段数相关
                QNode q = node.next = head.get(); // 头插法
                // 情况：
                // 1、队列没有头节点，且阶段数仍为当前阶段，协助初始化
                // 2、头结点的阶段数和当前阶段数相同，且当前阶段数无变化，设置队头
                if ((q == null || q.phase == phase) &&
                    (int)(state >>> PHASE_SHIFT) == phase) // avoid stale enq
                    queued = head.compareAndSet(q, node); // 入队成功，queued标识设为true
            }
            else {
                try {
                    // 当前线程进入阻塞状态，跟调用LockSupport.park()一样，等待被唤醒
                    ForkJoinPool.managedBlock(node);
                } catch (InterruptedException ie) {
                    node.wasInterrupted = true;
                }
            }
        }
        // 节点所在线程被唤醒后的操作
        if (node != null) {
            // 置空节点中的线程
            if (node.thread != null)
                node.thread = null;       // avoid need for unpark()
            if (node.wasInterrupted && !node.interruptible)
                Thread.currentThread().interrupt();
            if (p == phase && (p = (int)(state >>> PHASE_SHIFT)) == phase)
                return abortWait(phase); // possibly clean up on abort
        }
        // 唤醒当前阶段阻塞着的线程
        releaseWaiters(phase);
        return p;
    }

    /**
     * Wait nodes for Treiber stack representing wait queue
     */
    static final class QNode implements ForkJoinPool.ManagedBlocker {
        final Phaser phaser;
        final int phase;
        final boolean interruptible;
        final boolean timed;
        boolean wasInterrupted;
        long nanos;
        final long deadline;
        volatile Thread thread; // nulled to cancel wait
        QNode next;

        QNode(Phaser phaser, int phase, boolean interruptible,
              boolean timed, long nanos) {
            this.phaser = phaser;
            this.phase = phase;
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.timed = timed;
            this.deadline = timed ? System.nanoTime() + nanos : 0L;
            thread = Thread.currentThread();
        }

        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (phaser.getPhase() != phase) {
                thread = null;
                return true;
            }
            if (Thread.interrupted())
                wasInterrupted = true;
            if (wasInterrupted && interruptible) {
                thread = null;
                return true;
            }
            if (timed) {
                if (nanos > 0L) {
                    nanos = deadline - System.nanoTime();
                }
                if (nanos <= 0L) {
                    thread = null;
                    return true;
                }
            }
            return false;
        }

        public boolean block() {
            if (isReleasable())
                return true;
            else if (!timed)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = Phaser.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
