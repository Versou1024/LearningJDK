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

package java.util.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.LockSupport;

/**
 * A capability-based lock with three modes for controlling read/write
 * access.  The state of a StampedLock consists of a version and mode.
 * Lock acquisition methods return a stamp that represents and
 * controls access with respect to a lock state; "try" versions of
 * these methods may instead return the special value zero to
 * represent failure to acquire access. Lock release and conversion
 * methods require stamps as arguments, and fail if they do not match
 * the state of the lock. The three modes are:
 *
 * <ul>
 *
 *  <li><b>Writing.</b> Method {@link #writeLock} possibly blocks
 *   waiting for exclusive access, returning a stamp that can be used
 *   in method {@link #unlockWrite} to release the lock. Untimed and
 *   timed versions of {@code tryWriteLock} are also provided. When
 *   the lock is held in write mode, no read locks may be obtained,
 *   and all optimistic read validations will fail.  </li>
 *
 *  <li><b>Reading.</b> Method {@link #readLock} possibly blocks
 *   waiting for non-exclusive access, returning a stamp that can be
 *   used in method {@link #unlockRead} to release the lock. Untimed
 *   and timed versions of {@code tryReadLock} are also provided. </li>
 *
 *  <li><b>Optimistic Reading.</b> Method {@link #tryOptimisticRead}
 *   returns a non-zero stamp only if the lock is not currently held
 *   in write mode. Method {@link #validate} returns true if the lock
 *   has not been acquired in write mode since obtaining a given
 *   stamp.  This mode can be thought of as an extremely weak version
 *   of a read-lock, that can be broken by a writer at any time.  The
 *   use of optimistic mode for short read-only code segments often
 *   reduces contention and improves throughput.  However, its use is
 *   inherently fragile.  Optimistic read sections should only read
 *   fields and hold them in local variables for later use after
 *   validation. Fields read while in optimistic mode may be wildly
 *   inconsistent, so usage applies only when you are familiar enough
 *   with data representations to check consistency and/or repeatedly
 *   invoke method {@code validate()}.  For example, such steps are
 *   typically required when first reading an object or array
 *   reference, and then accessing one of its fields, elements or
 *   methods. </li>
 *
 * </ul>
 *
 * <p>This class also supports methods that conditionally provide
 * conversions across the three modes. For example, method {@link
 * #tryConvertToWriteLock} attempts to "upgrade" a mode, returning
 * a valid write stamp if (1) already in writing mode (2) in reading
 * mode and there are no other readers or (3) in optimistic mode and
 * the lock is available. The forms of these methods are designed to
 * help reduce some of the code bloat that otherwise occurs in
 * retry-based designs.
 *
 * <p>StampedLocks are designed for use as internal utilities in the
 * development of thread-safe components. Their use relies on
 * knowledge of the internal properties of the data, objects, and
 * methods they are protecting.  They are not reentrant, so locked
 * bodies should not call other unknown methods that may try to
 * re-acquire locks (although you may pass a stamp to other methods
 * that can use or convert it).  The use of read lock modes relies on
 * the associated code sections being side-effect-free.  Unvalidated
 * optimistic read sections cannot call methods that are not known to
 * tolerate potential inconsistencies.  Stamps use finite
 * representations, and are not cryptographically secure (i.e., a
 * valid stamp may be guessable). Stamp values may recycle after (no
 * sooner than) one year of continuous operation. A stamp held without
 * use or validation for longer than this period may fail to validate
 * correctly.  StampedLocks are serializable, but always deserialize
 * into initial unlocked state, so they are not useful for remote
 * locking.
 *
 * <p>The scheduling policy of StampedLock does not consistently
 * prefer readers over writers or vice versa.  All "try" methods are
 * best-effort and do not necessarily conform to any scheduling or
 * fairness policy. A zero return from any "try" method for acquiring
 * or converting locks does not carry any information about the state
 * of the lock; a subsequent invocation may succeed.
 *
 * <p>Because it supports coordinated usage across multiple lock
 * modes, this class does not directly implement the {@link Lock} or
 * {@link ReadWriteLock} interfaces. However, a StampedLock may be
 * viewed {@link #asReadLock()}, {@link #asWriteLock()}, or {@link
 * #asReadWriteLock()} in applications requiring only the associated
 * set of functionality.
 *
 * <p><b>Sample Usage.</b> The following illustrates some usage idioms
 * in a class that maintains simple two-dimensional points. The sample
 * code illustrates some try/catch conventions even though they are
 * not strictly needed here because no exceptions can occur in their
 * bodies.<br>
 *
 *  <pre>{@code
 * class Point {
 *   private double x, y;
 *   private final StampedLock sl = new StampedLock();
 *
 *   void move(double deltaX, double deltaY) { // an exclusively locked method
 *     long stamp = sl.writeLock();
 *     try {
 *       x += deltaX;
 *       y += deltaY;
 *     } finally {
 *       sl.unlockWrite(stamp);
 *     }
 *   }
 *
 *   double distanceFromOrigin() { // A read-only method
 *     long stamp = sl.tryOptimisticRead();
 *     double currentX = x, currentY = y;
 *     if (!sl.validate(stamp)) {
 *        stamp = sl.readLock();
 *        try {
 *          currentX = x;
 *          currentY = y;
 *        } finally {
 *           sl.unlockRead(stamp);
 *        }
 *     }
 *     return Math.sqrt(currentX * currentX + currentY * currentY);
 *   }
 *
 *   void moveIfAtOrigin(double newX, double newY) { // upgrade
 *     // Could instead start with optimistic, not read mode
 *     long stamp = sl.readLock();
 *     try {
 *       while (x == 0.0 && y == 0.0) {
 *         long ws = sl.tryConvertToWriteLock(stamp);
 *         if (ws != 0L) {
 *           stamp = ws;
 *           x = newX;
 *           y = newY;
 *           break;
 *         }
 *         else {
 *           sl.unlockRead(stamp);
 *           stamp = sl.writeLock();
 *         }
 *       }
 *     } finally {
 *       sl.unlock(stamp);
 *     }
 *   }
 * }}</pre>
 *
 * @since 1.8
 * @author Doug Lea
 */
public class StampedLock implements java.io.Serializable {
    /*
     * 1、StampedLock 是不可重入的，所以在锁的内部不能调用其他尝试重复获取锁的方法。
     * 2、StampedLocks是可序列化的，但是反序列化后变为初始的非锁定状态，所以在远程锁定中是不安全的。
     * 3、StampedLock还支持在三种模式中提供有条件地转换
     * 4、StampedLock的state由一个版本和模式构成。包括写模式、悲观读模式、乐观读模式
     * 5、
     */

    private static final long serialVersionUID = -6001602636862214147L;

    /** Number of processors, for spin control */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /** 获取锁失败入队之前的最大自旋次数（实际运行时并不一定是这个数）*/
    private static final int SPINS = (NCPU > 1) ? 1 << 6 : 0;

    /**头节点获取锁的最大自旋次数 */
    private static final int HEAD_SPINS = (NCPU > 1) ? 1 << 10 : 0;

    /** 头节点再次阻塞前的最大自旋次数 */
    private static final int MAX_HEAD_SPINS = (NCPU > 1) ? 1 << 16 : 0;

    /** 等待自旋锁溢出的周期数 */
    private static final int OVERFLOW_YIELD_RATE = 7; // must be power 2 - 1

    /** The number of bits to use for reader count before overflowing */
    private static final int LG_READERS = 7;

    /*
    state & WBIT == 0L                      写锁可用
    state & ABITS < RFULL                   读锁可用
    state & ABITS == WBIT                   写锁已经被其他线程获取
    state & ABITS == RFULL                  读锁饱和，可尝试增加额外资源数
    (stamp & SBITS) == (state & SBITS)      验证stamp是否为当前已经获取的锁stamp
    (state & WBIT) != 0L                    当前线程已经持有写锁
    (state & RBITS) != 0L                   当前线程已经持有读锁
    s & RBITS                               读锁已经被获取的数量
     */

    // Values for lock state and stamp operations
    private static final long RUNIT = 1L;               //0000 0000 0001 读锁单位
    private static final long WBIT  = 1L << LG_READERS; //0000 1000 0000 写状态标识位
    private static final long RBITS = WBIT - 1L;        //0000 0111 1111 读状态掩码
    private static final long RFULL = RBITS - 1L;       //0000 0111 1110 读锁最大资源数
    private static final long ABITS = RBITS | WBIT;     //0000 1111 1111 用于获取整个锁状态
    private static final long SBITS = ~RBITS;           //1111 1000 0000

    // 锁定状态的初始值
    private static final long ORIGIN = WBIT << 1; // 0001 0000 0000

    // 取消获取方法的特殊值，以便调用方可以抛出中断异常
    private static final long INTERRUPTED = 1L;

    // 对于node的状态 等待/取消
    private static final int WAITING   = -1;
    private static final int CANCELLED =  1;

    // node的模式 读/写
    private static final int RMODE = 0; // 获取读锁
    private static final int WMODE = 1; // 获取写锁

    /** Wait nodes */
    static final class WNode {
        /*
         * 相较于AQS，可以看到 StampedLock 的等待队列多了一个cowait节点链，这个节点用来存放等待读的线程列表。
         * 也就是说，等待写的线程存放在链表的正常节点中，如果有读线程等待获取锁，就会把这个读线程放到cowait节点链上。
         */
        volatile WNode prev;
        volatile WNode next;
        volatile WNode cowait;    // list of linked readers
        volatile Thread thread;   // non-null while possibly parked
        volatile int status;      // 0, WAITING, or CANCELLED
        final int mode;           // RMODE or WMODE
        WNode(int m, WNode p) { mode = m; prev = p; }
    }

    /** Head of CLH queue */
    private transient volatile WNode whead; // CLH链表的头部
    /** Tail (last) of CLH queue */
    private transient volatile WNode wtail; // CLH链表的尾部

    // 视图
    transient ReadLockView readLockView;
    transient WriteLockView writeLockView;
    transient ReadWriteLockView readWriteLockView;

    /** Lock sequence/state */
    private transient volatile long state; // 读锁通过前7位来表示，每获取一个读锁，则加1。写锁通过除前7位后剩下的位来表示，每获取一次写锁，则加1000 0000
    /** 读锁溢出时用来存储多出的读锁哦 */
    private transient int readerOverflow;

    /**
     * Creates a new lock, initially in unlocked state.
     */
    public StampedLock() {
        state = ORIGIN; // 0001 0000 0000
    }

    /**
     * Exclusively acquires the lock, blocking if necessary
     * until available.
     *
     * @return a stamp that can be used to unlock or convert mode
     */
    public long writeLock() {
        long s, next;  // bypass acquireWrite in fully unlocked case only
        return ((((s = state) & ABITS) == 0L && // 没有读写锁，就允许下面的cas操作
                 U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ? // cas操作尝试获取写锁
                next : acquireWrite(false, 0L)); // 获取写锁成功后返回next，失败则进行后续处理，排队也在后续处理中
    }

    /**
     * Exclusively acquires the lock if it is immediately available.
     *
     * @return a stamp that can be used to unlock or convert mode,
     * or zero if the lock is not available
     */
    public long tryWriteLock() {
        long s, next;
        return ((((s = state) & ABITS) == 0L && // 没有读写锁，允许尝试获取读锁，失败后立即返回0，成功则返回next的status
                 U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
                next : 0L);
    }

    /**
     * Exclusively acquires the lock if it is available within the
     * given time and the current thread has not been interrupted.
     * Behavior under timeout and interruption matches that specified
     * for method {@link Lock#tryLock(long,TimeUnit)}.
     *
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return a stamp that can be used to unlock or convert mode,
     * or zero if the lock is not available
     * @throws InterruptedException if the current thread is interrupted
     * before acquiring the lock
     */
    public long tryWriteLock(long time, TimeUnit unit) throws InterruptedException {
        /*
         * 期间完成共走：
         * 1、尝试获取写锁，获取成功就直接返回stamp
         * 2、获取失败，检查是否超时，超时返回0，表示失败
         * 3、没有超时，调用acquireWrite()进入超时等待 -- 其中会随机自旋更新
         */
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long next, deadline;
            if ((next = tryWriteLock()) != 0L)
                return next;
            if (nanos <= 0L)
                return 0L;
            if ((deadline = System.nanoTime() + nanos) == 0L)
                deadline = 1L;
            if ((next = acquireWrite(true, deadline)) != INTERRUPTED) // 1就是表示被中断唤醒
                return next;
        }
        throw new InterruptedException();
    }

    /**
     * Exclusively acquires the lock, blocking if necessary
     * until available or the current thread is interrupted.
     * Behavior under interruption matches that specified
     * for method {@link Lock#lockInterruptibly()}.
     *
     * @return a stamp that can be used to unlock or convert mode
     * @throws InterruptedException if the current thread is interrupted
     * before acquiring the lock
     */
    public long writeLockInterruptibly() throws InterruptedException {
        long next;
        // 线程非中断，就尝试获取获取写锁
        if (!Thread.interrupted() &&
            (next = acquireWrite(true, 0L)) != INTERRUPTED)
            return next;
        throw new InterruptedException();
    }

    /**
     * Non-exclusively acquires the lock, blocking if necessary
     * until available.
     *
     * @return a stamp that can be used to unlock or convert mode
     */
    public long readLock() {
        long s = state, next;  // bypass acquireRead on common uncontended case
        return ((whead == wtail && (s & ABITS) < RFULL && // 写锁队列为空，并且状态中无写锁，同时读锁未溢出，尝试获取读锁
                 U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)) ? // cas尝试获取读锁+1
                next : acquireRead(false, 0L)); // 获取读锁成功，返回s + RUNIT，失败进入后续处理，类似acquireWrite
    }

    /**
     * Non-exclusively acquires the lock if it is immediately available.
     *
     * @return a stamp that can be used to unlock or convert mode,
     * or zero if the lock is not available
     */
    public long tryReadLock() {
        for (;;) {
            long s, m, next;
            // 有写锁，返回0
            if ((m = (s = state) & ABITS) == WBIT)
                return 0L;
            // 读锁未溢出
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                    return next;
            }
            // 读锁溢出，尝试修改读锁溢出数量
            else if ((next = tryIncReaderOverflow(s)) != 0L)
                return next;
        }
    }

    /**
     * Non-exclusively acquires the lock if it is available within the
     * given time and the current thread has not been interrupted.
     * Behavior under timeout and interruption matches that specified
     * for method {@link Lock#tryLock(long,TimeUnit)}.
     *
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return a stamp that can be used to unlock or convert mode,
     * or zero if the lock is not available
     * @throws InterruptedException if the current thread is interrupted
     * before acquiring the lock
     */
    public long tryReadLock(long time, TimeUnit unit) throws InterruptedException {
        // 带有超时的版本
        long s, m, next, deadline;
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            if ((m = (s = state) & ABITS) != WBIT) {
                if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                        return next;
                }
                else if ((next = tryIncReaderOverflow(s)) != 0L)
                    return next;
            }
            // 已超时
            if (nanos <= 0L)
                return 0L;
            // 如果System.nanoTime加上nanos等于0，将其deadline时间设置为1，
            // 因为System.nanoTime可能为负数
            if ((deadline = System.nanoTime() + nanos) == 0L)
                deadline = 1L;
            //如果调用acquireRead方法返回不是中断的标志位INTERRUPTED,直接返回，
            //next不等于0获取读锁成功，否则获取读锁失败
            if ((next = acquireRead(true, deadline)) != INTERRUPTED)
                return next;
        }
        throw new InterruptedException();
    }

    /**
     * Non-exclusively acquires the lock, blocking if necessary
     * until available or the current thread is interrupted.
     * Behavior under interruption matches that specified
     * for method {@link Lock#lockInterruptibly()}.
     *
     * @return a stamp that can be used to unlock or convert mode
     * @throws InterruptedException if the current thread is interrupted
     * before acquiring the lock
     */
    public long readLockInterruptibly() throws InterruptedException {
        long next;
        if (!Thread.interrupted() && (next = acquireRead(true, 0L)) != INTERRUPTED)
            return next;
        throw new InterruptedException();
    }

    /**
     * Returns a stamp that can later be validated, or zero
     * if exclusively locked.
     *
     * @return a stamp, or zero if exclusively locked
     */
    public long tryOptimisticRead() {
        long s;
        // 乐观读锁，只是简单的检查是否存在写锁，不存在写锁就获取state的 7-64位 作为stamp值返回
        return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
    }

    /**
     * Returns true if the lock has not been exclusively acquired
     * since issuance of the given stamp. Always returns false if the
     * stamp is zero. Always returns true if the stamp represents a
     * currently held lock. Invoking this method with a value not
     * obtained from {@link #tryOptimisticRead} or a locking method
     * for this lock has no defined effect or result.
     *
     * @param stamp a stamp
     * @return {@code true} if the lock has not been exclusively acquired
     * since issuance of the given stamp; else false
     */
    public boolean validate(long stamp) {
        /*
         * 用于乐观读锁判断之前获取的乐观读锁是否还有效？
         * 1、stamp与state的版本戳是否相同即可
         *
         * 原因：在于获取写锁成功时，总是会在高位留下记录，即使期间写锁被立即释放，通过检查版本戳stamp，就可以准确获知是否有线程成功获取到写锁
         */
        // 在校验逻辑之前，会通过Unsafe的loadFence方法加入一个load内存屏障，目的是避免
        // copy变量到工作内存中和StampedLock.validate中锁状态校验运算发生重排序导致
        // 锁状态校验不准确的问题
        U.loadFence(); // 读内存屏障，用于保证前后读操作不调换，保证将CPU缓存中的无效队列的任务给执行掉，从而加载最新的值state
        return (stamp & SBITS) == (state & SBITS);
    }

    /**
     * If the lock state matches the given stamp, releases the
     * exclusive lock.
     *
     * @param stamp a stamp returned by a write-lock operation
     * @throws IllegalMonitorStateException if the stamp does
     * not match the current state of this lock
     */
    public void unlockWrite(long stamp) {
        WNode h;
        // stamp值被修改，或者写锁已经被释放，抛出错误
        if (state != stamp || (stamp & WBIT) == 0L)
            throw new IllegalMonitorStateException();
        state = (stamp += WBIT) == 0L ? ORIGIN : stamp;  // 加0000 1000 0000来记录写锁的变化，同时改变写锁状态
        /**
         * 假设stamp为0001 1000 0000
         *  0001 1000 0000 + 0000 1000 0000 = 0010 0000 0000
         * 这一步操作是重点！！！写锁的释放并不是像ReentrantReadWriteLock一样+1然后-1，而是通过再次加0000 1000 0000来使高位每次都产生变化，
         * 为什么要这样做？直接减掉0000 1000 0000不就可以了吗？这就是为了后面乐观锁做铺垫，让每次写锁都留下痕迹。
         */
        if ((h = whead) != null && h.status != 0)
            release(h); // 唤醒后续的
    }

    /**
     * If the lock state matches the given stamp, releases the
     * non-exclusive lock.
     *
     * @param stamp a stamp returned by a read-lock operation
     * @throws IllegalMonitorStateException if the stamp does
     * not match the current state of this lock
     */
    public void unlockRead(long stamp) {
        /*
         * 悲观读锁的获取和ReentrantReadWriteLock类似，不同在StampedLock的读锁很容易溢出，最大只有127，
         * 超过后通过一个额外的变量readerOverflow来存储，这是为了给写锁留下更大的空间，因为写锁是在不停增加的。
         */
        long s, m; WNode h;
        for (;;) {
            // 传进来的stamp和当前stampedLock的state状态不一致，或者当前处于乐观读、
            // 无锁状态，或者传进来的参数是乐观读、无锁的stamp，又或者当前状态为写锁状态，
            // 抛出非法的锁状态异常
            if (((s = state) & SBITS) != (stamp & SBITS) || // stamp戳位失效，state的从第7位到最高位都表示版本戳
                (stamp & ABITS) == 0L || (m = s & ABITS) == 0L || m == WBIT)
                throw new IllegalMonitorStateException();
            // 2、stamp没有问题
            // m小于最大记录值（最大记录值127超过后放在readerOverflow变量中）
            if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) { //cas尝试释放读锁-1
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h); // 唤醒后继者
                    break;
                }
            }
            // 3、m大于等于RFULL，读锁溢出，调用tryDecReaderOverflow()
            else if (tryDecReaderOverflow(s) != 0L)
                break;
        }
    }

    /**
     * If the lock state matches the given stamp, releases the
     * corresponding mode of the lock.
     *
     * @param stamp a stamp returned by a lock operation
     * @throws IllegalMonitorStateException if the stamp does
     * not match the current state of this lock
     */
    public void unlock(long stamp) {
        long a = stamp & ABITS, m, s; WNode h; // a是传入的锁状态，m是state的锁状态，s是state，
        // 校验版本戳是否一致
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            // 如果当前处于无锁状态，或者乐观读状态，直接退出，抛出异常
            if ((m = s & ABITS) == 0L)
                break;
            // 如果当前StampedLock的状态为写模式
            else if (m == WBIT) {
                // 锁模式不一致，即传入进来的stamp不是写模式，直接退出，抛出异常
                if (a != m)
                    break;
                // 否则的话释放写锁， S+=WBIT 是为了加上写锁的印记
                state = (s += WBIT) == 0L ? ORIGIN : s;
                // 头结点不为空，并且头结点的状态不为0，唤醒后继者
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return;
            }
            // 如果传入进来的状态是无锁模式，或者是乐观读模式，直接退出，抛出异常
            else if (a == 0L || a >= WBIT)
                break;
            // 如果处于读锁模式，并且读锁没有溢出
            else if (m < RFULL) {
                // cas操作使StampedLock的state状态减1，释放一个读锁，失败时，重新循环
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    // 如果当前读锁只有一个，并且头结点不为空，并且头结点的状态不为0
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return;
                }
            }
            // 读锁溢出，tryDecReaderOverflow方法看下面介绍
            else if (tryDecReaderOverflow(s) != 0L)
                return;
        }
        throw new IllegalMonitorStateException();
    }

    /**
     * If the lock state matches the given stamp, performs one of
     * the following actions. If the stamp represents holding a write
     * lock, returns it.  Or, if a read lock, if the write lock is
     * available, releases the read lock and returns a write stamp.
     * Or, if an optimistic read, returns a write stamp only if
     * immediately available. This method returns zero in all other
     * cases.
     *
     * @param stamp a stamp
     * @return a valid write stamp, or zero on failure
     */
    public long tryConvertToWriteLock(long stamp) {
        /**
         * state匹配stamp时, 执行下列操作之一：stamp是传入的锁状态
         *   1、stamp持有写锁时，直接返回改写锁 -- 写锁转写锁
         *   2、stamp持有读锁时，只有在有且只有这个读锁时，才允许转写锁 -- 唯一一个读锁时允许转写锁
         *   3、stamp持有无锁或者乐观读时，允许转写锁 -- 无锁、乐观读锁转读写锁
         *   4、其他情况都返回0
         */
        long a = stamp & ABITS, m, s, next; // a是传入的锁状态；m是state计算的锁状态
        // 如果传入进来的stamp和当前StampedLock的状态相同
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            // 如果当前处于无锁状态，或者乐观读状态
            if ((m = s & ABITS) == 0L) {
                // 但传入进来的stamp不处于无锁或者乐观读状态，直接退出，升级失败
                if (a != 0L)
                    break;
                // 获取state使用cas进行写锁的获取，如果获取写锁成功直接退出 -- 从无锁或者乐观锁升级为写锁
                if (U.compareAndSwapLong(this, STATE, s, next = s + WBIT))
                    return next;
            }
            // 如果当前stampedLock处于写锁状态
            else if (m == WBIT) {
                // 传入进来的stamp不处于写锁状态，直接退出
                if (a != m)
                    break;
                // 否则，就是 -- 同为写锁，无须升级
                return stamp;
            }
            // 如果当前只有一个读锁，当前状态state使用cas进行减1加WBIT操作，
            // 将其读锁升级为写锁状态
            else if (m == RUNIT && a != 0L) {
                if (U.compareAndSwapLong(this, STATE, s,
                                         next = s - RUNIT + WBIT))
                    return next;
            }
            // 否则直接退出
            else
                break;
        }
        return 0L;
    }

    /**
     * If the lock state matches the given stamp, performs one of
     * the following actions. If the stamp represents holding a write
     * lock, releases it and obtains a read lock.  Or, if a read lock,
     * returns it. Or, if an optimistic read, acquires a read lock and
     * returns a read stamp only if immediately available. This method
     * returns zero in all other cases.
     *
     * @param stamp a stamp
     * @return a valid read stamp, or zero on failure
     */
    public long tryConvertToReadLock(long stamp) {
        /**
         * state匹配stamp时, 执行下列操作之一.
         *      1、stamp 表示持有写锁时，释放写锁然后持有读锁 -- 写锁降级为读锁
         * 		2、stamp 表示持有读锁时，直接返回该读锁 -- 读锁转读锁
         * 		3、stamp 表示乐观读锁和无锁时，只在即时可用的前提下直接获取一个读锁 -- 乐观锁、无锁升级为读锁
         *      4、其他情况都返回0，表示失败
         */
        long a = stamp & ABITS, m, s, next; WNode h;
        // 如果传入进来的stamp和当前StampedLock的版本戳相同 -- 才允许继续判断
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            // 如果当前StampedLock处于无锁状态或者乐观读状态
            if ((m = s & ABITS) == 0L) {
                // 如果传入进来的stamp不处于无锁或者乐观读状态，锁状态不同，不允许升级
                if (a != 0L)
                    break;
                // 如果当前StampedLock处于无锁或者乐观读状态，并且读锁数没有溢出，
                else if (m < RFULL) {
                    // state使用cas操作进行加1操作，如果操作成功直接退
                    if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                        return next;
                }
                // 如果读锁溢出，调用tryIncReaderOverflow方法的溢出操作，
                // 此方法可以看上面的介绍，如果返回非0，表明升级读锁成功，直接退出
                else if ((next = tryIncReaderOverflow(s)) != 0L)
                    return next;
            }
            // 如果当前StampedLock的state处于写锁状态，如果锁升级成功，直接返回，否则重新循环
            else if (m == WBIT) {
                // 传入进来的stamp不处于写锁状态，两者状态不一致，不允许升级操作
                if (a != m)
                    break;
                // 释放写锁加读锁
                state = next = s + (WBIT + RUNIT);
                if ((h = whead) != null && h.status != 0)
                    // 释放头结点的下一个有效节点，在上面release方法时有介绍
                    release(h);
                return next;
            }
            // 如果传进来的stamp是读锁状态，直接返回传进来的stamp
            else if (a != 0L && a < WBIT)
                return stamp;
            // 否则直接退出
            else
                break;
        }
        return 0L;
    }

    /**
     * If the lock state matches the given stamp then, if the stamp
     * represents holding a lock, releases it and returns an
     * observation stamp.  Or, if an optimistic read, returns it if
     * validated. This method returns zero in all other cases, and so
     * may be useful as a form of "tryUnlock".
     *
     * @param stamp a stamp
     * @return a valid optimistic read stamp, or zero on failure
     */
    public long tryConvertToOptimisticRead(long stamp) {
        long a = stamp & ABITS, m, s, next; WNode h;
        U.loadFence();
        for (;;) {
            if (((s = state) & SBITS) != (stamp & SBITS))
                break;
            if ((m = s & ABITS) == 0L) {
                if (a != 0L)
                    break;
                return s;
            }
            else if (m == WBIT) {
                if (a != m)
                    break;
                state = next = (s += WBIT) == 0L ? ORIGIN : s;
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return next;
            }
            else if (a == 0L || a >= WBIT)
                break;
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return next & SBITS;
                }
            }
            else if ((next = tryDecReaderOverflow(s)) != 0L)
                return next & SBITS;
        }
        return 0L;
    }

    /**
     * Releases the write lock if it is held, without requiring a
     * stamp value. This method may be useful for recovery after
     * errors.
     *
     * @return {@code true} if the lock was held, else false
     */
    public boolean tryUnlockWrite() {
        long s; WNode h;
        if (((s = state) & WBIT) != 0L) {
            state = (s += WBIT) == 0L ? ORIGIN : s;
            if ((h = whead) != null && h.status != 0)
                release(h);
            return true;
        }
        return false;
    }

    /**
     * Releases one hold of the read lock if it is held, without
     * requiring a stamp value. This method may be useful for recovery
     * after errors.
     *
     * @return {@code true} if the read lock was held, else false
     */
    public boolean tryUnlockRead() {
        // 特点：无需传入stamp进行释放读锁
        long s, m; WNode h;
        // 如果当前状态处于读锁状态，而不是乐观读状态，或者无锁状态，或者写锁状态
        while ((m = (s = state) & ABITS) != 0L && m < WBIT) {
            // 如果当前state处于读锁状态，并且读锁没有溢出
            if (m < RFULL) {
                // stampedLock状态state使用cas进行减1操作，如果成功，跳出循环，直接返回
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    // 如果操作成功，并且当前状态处于无锁状态，并且头结点不为空，
                    // 及头结点的状态不为0
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return true;
                }
            }
            // 如果当前处于读锁模式，并且读锁溢出，使用上面介绍的tryDecReaderOverflow方法，
            // 如果返回非0，直接退出，返回成功，否则重新进行循环
            else if (tryDecReaderOverflow(s) != 0L)
                return true;
        }
        return false;
    }

    // status monitoring methods

    /**
     * Returns combined state-held and overflow read count for given
     * state s.
     */
    private int getReadLockCount(long s) {
        long readers;
        if ((readers = s & RBITS) >= RFULL)
            readers = RFULL + readerOverflow;
        return (int) readers;
    }

    /**
     * Returns {@code true} if the lock is currently held exclusively.
     *
     * @return {@code true} if the lock is currently held exclusively
     */
    public boolean isWriteLocked() {
        return (state & WBIT) != 0L;
    }

    /**
     * Returns {@code true} if the lock is currently held non-exclusively.
     *
     * @return {@code true} if the lock is currently held non-exclusively
     */
    public boolean isReadLocked() {
        return (state & RBITS) != 0L;
    }

    /**
     * Queries the number of read locks held for this lock. This
     * method is designed for use in monitoring system state, not for
     * synchronization control.
     * @return the number of read locks held
     */
    public int getReadLockCount() {
        return getReadLockCount(state);
    }

    /**
     * Returns a string identifying this lock, as well as its lock
     * state.  The state, in brackets, includes the String {@code
     * "Unlocked"} or the String {@code "Write-locked"} or the String
     * {@code "Read-locks:"} followed by the current number of
     * read-locks held.
     *
     * @return a string identifying this lock, as well as its lock state
     */
    public String toString() {
        long s = state;
        return super.toString() +
            ((s & ABITS) == 0L ? "[Unlocked]" :
             (s & WBIT) != 0L ? "[Write-locked]" :
             "[Read-locks:" + getReadLockCount(s) + "]");
    }

    // views

    /**
     * Returns a plain {@link Lock} view of this StampedLock in which
     * the {@link Lock#lock} method is mapped to {@link #readLock},
     * and similarly for other methods. The returned Lock does not
     * support a {@link Condition}; method {@link
     * Lock#newCondition()} throws {@code
     * UnsupportedOperationException}.
     *
     * @return the lock
     */
    public Lock asReadLock() {
        ReadLockView v;
        return ((v = readLockView) != null ? v :
                (readLockView = new ReadLockView()));
    }

    /**
     * Returns a plain {@link Lock} view of this StampedLock in which
     * the {@link Lock#lock} method is mapped to {@link #writeLock},
     * and similarly for other methods. The returned Lock does not
     * support a {@link Condition}; method {@link
     * Lock#newCondition()} throws {@code
     * UnsupportedOperationException}.
     *
     * @return the lock
     */
    public Lock asWriteLock() {
        WriteLockView v;
        return ((v = writeLockView) != null ? v :
                (writeLockView = new WriteLockView()));
    }

    /**
     * Returns a {@link ReadWriteLock} view of this StampedLock in
     * which the {@link ReadWriteLock#readLock()} method is mapped to
     * {@link #asReadLock()}, and {@link ReadWriteLock#writeLock()} to
     * {@link #asWriteLock()}.
     *
     * @return the lock
     */
    public ReadWriteLock asReadWriteLock() {
        ReadWriteLockView v;
        return ((v = readWriteLockView) != null ? v :
                (readWriteLockView = new ReadWriteLockView()));
    }

    // view classes

    final class ReadLockView implements Lock {
        public void lock() { readLock(); }
        public void lockInterruptibly() throws InterruptedException {
            readLockInterruptibly();
        }
        public boolean tryLock() { return tryReadLock() != 0L; }
        public boolean tryLock(long time, TimeUnit unit)
            throws InterruptedException {
            return tryReadLock(time, unit) != 0L;
        }
        public void unlock() { unstampedUnlockRead(); }
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class WriteLockView implements Lock {
        public void lock() { writeLock(); }
        public void lockInterruptibly() throws InterruptedException {
            writeLockInterruptibly();
        }
        public boolean tryLock() { return tryWriteLock() != 0L; }
        public boolean tryLock(long time, TimeUnit unit)
            throws InterruptedException {
            return tryWriteLock(time, unit) != 0L;
        }
        public void unlock() { unstampedUnlockWrite(); }
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class ReadWriteLockView implements ReadWriteLock {
        public Lock readLock() { return asReadLock(); }
        public Lock writeLock() { return asWriteLock(); }
    }

    // Unlock methods without stamp argument checks for view classes.
    // Needed because view-class lock methods throw away stamps.

    final void unstampedUnlockWrite() {
        WNode h; long s;
        if (((s = state) & WBIT) == 0L)
            throw new IllegalMonitorStateException();
        state = (s += WBIT) == 0L ? ORIGIN : s;
        if ((h = whead) != null && h.status != 0)
            release(h);
    }

    final void unstampedUnlockRead() {
        for (;;) {
            long s, m; WNode h;
            if ((m = (s = state) & ABITS) == 0L || m >= WBIT)
                throw new IllegalMonitorStateException();
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    break;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                break;
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        state = ORIGIN; // reset to unlocked state
    }

    // internals

    /**
     * Tries to increment readerOverflow by first setting state
     * access bits value to RBITS, indicating hold of spinlock,
     * then updating, then releasing.
     *
     * @param s a reader overflow stamp: (s & ABITS) >= RFULL
     * @return new stamp on success, else zero
     */
    private long tryIncReaderOverflow(long s) {
        // 读锁溢出
        if ((s & ABITS) == RFULL) {
            // state 更新为 0000 0111 1111 ，不难看出这一串用来表示锁状态，用来确保单线程修改readerOverflow，无并发问题
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                // 对溢出的读锁数量做++操作
                ++readerOverflow;
                // 将state还原为 0000 0111 1110，即对应一个解锁操作
                state = s;
                return s;
            }
        }
        // 如果当前线程的随机数增加操作&上7等于0，将其线程进行让步操作
        else if ((LockSupport.nextSecondarySeed() & OVERFLOW_YIELD_RATE) == 0)
            Thread.yield();
        // 否则直接返回0失败
        return 0L;
    }

    /**
     * Tries to decrement readerOverflow.
     *
     * @param s a reader overflow stamp: (s & ABITS) >= RFULL
     * @return new stamp on success, else zero
     */
    private long tryDecReaderOverflow(long s) {
        /*
         * 读锁的数量就是取决于 state的后
         */
        // 如果当前StampedLock的state的读模式已满，s&ABITS为 126即 0000 0111 1110
        if ((s & ABITS) == RFULL) {
            // 先将其state设置为127，版本戳将被清空变为 0000 0111 1111
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                int r; long next;
                // 1、如果当前readerOverflow（记录溢出的读锁个数）大于0
                if ((r = readerOverflow) > 0) {
                    // 2、readerOverflow做减1操作，从溢出的读锁个数中减去1
                    readerOverflow = r - 1;
                    // 将其next设置为原来的state
                    next = s;
                }
                else
                    // 3、否则的话，表名目前没有溢出的读锁，直接将当前的state做减1操作
                    next = s - RUNIT;
                 // 将其state设置为next
                 state = next;
                 return next;
            }
        }
        // 如果当前线程随机数&上7要是等于0，线程让步
        else if ((LockSupport.nextSecondarySeed() &
                  OVERFLOW_YIELD_RATE) == 0)
            Thread.yield();
        return 0L;
    }

    /**
     * Wakes up the successor of h (normally whead). This is normally
     * just h.next, but may require traversal from wtail if next
     * pointers are lagging. This may fail to wake up an acquiring
     * thread when one or more have been cancelled, but the cancel
     * methods themselves provide extra safeguards to ensure liveness.
     */
    private void release(WNode h) { // todo
        // 头结点不为空
        if (h != null) {
            WNode q; Thread w;
            // 头结点的状态为等待状态，将其状态设置为0
            U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
            if ((q = h.next) == null || q.status == CANCELLED) {
                // 从尾节点开始，到头节点结束，寻找状态为等待或者0的有效节点
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            // 如果寻找到有效节点不为空，并且其对应的线程也不为空，唤醒其线程
            if (q != null && (w = q.thread) != null)
                U.unpark(w);
        }
    }

    /**
     * See above for explanation.
     *
     * @param interruptible true if should check interrupts and if so
     * return INTERRUPTED
     * @param deadline if nonzero, the System.nanoTime value to timeout
     * at (and return zero)
     * @return next state, or INTERRUPTED
     */
    private long acquireWrite(boolean interruptible, long deadline) {
        /*
         * 获取写锁失败后调用该方法：
         * 主要流程
         * 1、随机自旋，不断尝试去获取读锁
         * 2、自旋结束，未获取到读锁，创建node尾插入到等待链表中
         * 3、node尾插入到等待队列后，再次进入自旋
         * 4、若node的前继者是头结点，就随机自旋尝试获取写锁
         * 5、若node的前继者不是头结点，帮助释放head上cowait
         * 6、检查链表的稳定性，即node的前继者是否已经改变，若已改变更新前继者的快照
         * 7、更新前继者为Waiting状态，然后node本身就进入阻塞等待状态
         */
        WNode node = null, p;
        // 第一个自旋，准备入队
        for (int spins = -1;;) { // spin while enqueuing
            // m是锁状态，s是state，ns是获取锁后的state
            long m, s, ns;
            // 1、读写锁皆为空，尝试获取锁
            if ((m = (s = state) & ABITS) == 0L) {
                if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT))
                    return ns;
            }
            // 2、初始化自旋次数
            else if (spins < 0)
                spins = (m == WBIT && wtail == whead) ? SPINS : 0; // 有线程持有写锁，且等待队列为空，初始化为SPINS
            // 3、开始做随机自旋
            else if (spins > 0) {
                if (LockSupport.nextSecondarySeed() >= 0)
                    --spins;
            }
            // 4、初始化等待队列
            else if ((p = wtail) == null) {
                WNode hd = new WNode(WMODE, null); // 头结点是一个哨兵节点，为write模式
                if (U.compareAndSwapObject(this, WHEAD, null, hd)) // cas创建头结点
                    wtail = hd;
            }
            // 5、等待队列已存在，初始化node
            else if (node == null)
                node = new WNode(WMODE, p);
            // 6、node已初始化，检查node.prev是否为之前的等待链表的尾结点 -- 确保5-6自旋一次的期间无任何变化，如之前的未节点被删除啦
            else if (node.prev != p)
                node.prev = p;
            // 7、CAS将node尾插入到等待对内中
            else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                p.next = node; // 建立双向关系
                break; // 结束
            }
        }

        // 第二个自旋，节点依次获取锁
        for (int spins = -1;;) {
            WNode h, np, pp; int ps;
            // 1、前继者是头结点 -- 同AQS的同步队列，此刻第一个节点不含有争夺锁的含义，因此当node的前继者p为head节点时，就表示可以尝试获取锁
            if ((h = whead) == p) {
                // a.初始化spins
                if (spins < 0)
                    spins = HEAD_SPINS;
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;
                // b. 进入自旋
                // 期间：若没有读写锁，则尝试获取写锁
                for (int k = spins;;) { // spin at head
                    long s, ns;
                    // 若没有读写锁，则尝试获取写锁
                    if (((s = state) & ABITS) == 0L) {
                        if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT)) { // 更新锁状态
                            whead = node; // 更新头结点为当前节点node，返回stamp
                            node.prev = null;
                            return ns;
                        }
                    }
                    // 随机自旋
                    else if (LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                        break; // 结束自旋
                }
            }
            // 2、头结点不为null，且头结点非当前node的前继者 -- 既无资格自旋时，可以帮助做一些协助工作
            else if (h != null) {
                // 不立即阻塞起来，而是帮助头结点唤醒等待者
                WNode c; Thread w;
                // 当前node获取写锁失败，可能是有读锁,或者其他线程占用写锁的存在，因此可以帮助完成以下动作：
                // 若是其他线程占用写锁，那么对应的head其实就是写线程，
                // 但若是头结点有cowait链表，说明头结点是读线程，那么就表明当前锁是读锁；【注意：等待写的线程存放在链表的正常节点上】
                // 那么就帮助尝试依次唤醒头节点的cowait节点线程，即依次唤醒读者线程
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) && (w = c.thread) != null)
                        U.unpark(w); // 唤醒对方
                }
            }

            // 3、whead 期间没有改变
            if (whead == h) {
                // ① 检查队列稳定性，np为node的前继者若已不为p，表示队列不稳定 -- p可能被取消后移除啦
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // 重新构建
                }
                // ② 前继者的状态是0即初始化，更新为WAITING等待状态 -- 表示需要后继者可以通过前继者释放后来唤醒
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                // ③ 前继者若是取消状态，将node的prev更新前继者的前继节点，并做双向关联，继续自旋
                else if (ps == CANCELLED) {
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                // ④ 进入阻塞等待
                else {
                    long time; // 0 argument to park means no timeout
                    // 永久阻塞
                    if (deadline == 0L)
                        time = 0L;
                    // 超时后，取消node
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    // 前继者处于等待状态，前继者非头结点，或有读写锁存在
                    if (p.status < 0 && (p != h || (state & ABITS) != 0L) && whead == h && node.prev == p)
                        U.park(false, time);  // 将当前线程阻塞起来
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted()) // 判断是否为中断唤醒，若是中断唤醒，而且不支持中断，就需要取消node
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

    /**
     * See above for explanation.
     *
     * @param interruptible true if should check interrupts and if so
     * return INTERRUPTED
     * @param deadline if nonzero, the System.nanoTime value to timeout
     * at (and return zero)
     * @return next state, or INTERRUPTED
     */
    private long acquireRead(boolean interruptible, long deadline) {
        /*
         * 如果头结点和尾节点相等，先让其线程自旋一段时间，如果队列为空初始化队列，
         * 生成头结点和尾节点。如果自旋操作没有获取到锁，并且头结点和尾节点相等，或者当前
         * stampedLock的状态为写锁状态，将其当前节点加入队列中，如果加入当前队列失败，
         * 或者头结点和尾节点不相等，或者当前处于读锁状态，将其加入尾节点的cwait中，
         * 如果头结点的cwait节点不为空，并且线程也不为空，唤醒其cwait队列，阻塞当前节点
         **/
        WNode node = null, p;
        // 第一个自旋，入队
        for (int spins = -1;;) {
            WNode h; // h为头节点，p为尾结点，s状态，m是锁状态，ns是获取锁后state
            // 1、等待队列为空，即无写锁 【切记：等待写锁的队列是通过Node的next和prev连接的，而等待读锁的队列是通过node的cowait链接起来的】
            // 因此等待队列为空，即无写锁，那么就一定可以获取到读锁
            // 【表明当前state可能是读锁，那么直接CAS获取读锁】
            // 【也可能如果是写锁，但由于没有写锁Node的等待，那么我就一直尝试自旋，直到写锁被释放，我去获取到读锁】
            // 【也可能是写锁被释放，但我抢夺写锁失败，不得不重新循环等操作】
            if ((h = whead) == (p = wtail)) {
                for (long m, s, ns;;) {
                    // 2、有可用读锁资源，则CAS获取读锁
                    // m < RFULL，就表示可能是有写锁[1000 0000]啦，也可能是读锁满[0111 1111]啊，因此再次检查m是否小于WBIT，若是则说明是读锁数量溢出啦
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                        return ns;
                    // 3、有写锁的话，做随机自旋，
                    else if (m >= WBIT) {
                        if (spins > 0) {
                            if (LockSupport.nextSecondarySeed() >= 0)
                                --spins;
                        }

                        else {
                            if (spins == 0) {  // 自旋结束，检查链表状态是否改变，若改变打破当前循环，继续外层循环，以获取新的whead和wtail
                                WNode nh = whead, np = wtail;
                                if ((nh == h && np == p) || (h = nh) != (p = np))
                                    break;
                            }
                            spins = SPINS; // 继续自旋
                        }
                    }
                }
            }
            // 执行到这：说明上面尝试自旋获取锁失败

            // 2、有需要时初始化等待队列
            if (p == null) { // initialize queue
                WNode hd = new WNode(WMODE, null); // 初始化等待对内，其头结点是一个写锁模式的等待者
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    wtail = hd;
            }
            // 3、有需要时，初始化node，RMODE模式
            else if (node == null)
                node = new WNode(RMODE, p);
            // 4、等待队列存在，node已创建，且当前尾结点是写线程，建立双向关联关系
            else if (h == p || p.mode != RMODE) {
                // 到这里说明尾节点是写线程，将node的prev指向p
                if (node.prev != p)
                    node.prev = p;
                // 否则，cas更新等待队列的尾结点为node，并将p作为node的前继者
                else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                    p.next = node;
                    break; // 结束循环
                }
            }
            // 5、到这里说明前尾节点p是等待读的节点，CAS把当前节点(node节点)转移到p节点的cowait上链接上去
            else if (!U.compareAndSwapObject(p, WCOWAIT, node.cowait = p.cowait, node))
                node.cowait = null;
            else {
                // 6、当前节点进入等待队列成功后的逻辑(当前节点已被转移到尾节点的cowait上)
                for (;;) {
                    WNode pp, c; Thread w;
                    // 7、开始帮助协议，若头结点不为空，且是读线程的链表头，那么取出读线程链表的头部，唤醒对方
                    if ((h = whead) != null && (c = h.cowait) != null &&
                        U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null) // help release
                        U.unpark(w);
                    // 8、node的前继者的前继者若为头结点，或者前继者就是头结点 -- 否则没有资格自旋尝试获取读锁
                    if (h == (pp = p.prev) || h == p || pp == null) {
                        long m, s, ns;
                        // 9、在持有读锁的情况下，不断CA获取读锁，与判断读锁溢出
                        do {
                            if ((m = (s = state) & ABITS) < RFULL ?
                                U.compareAndSwapLong(this, STATE, s,
                                                     ns = s + RUNIT) :
                                (m < WBIT &&
                                 (ns = tryIncReaderOverflow(s)) != 0L))
                                return ns;
                        } while (m < WBIT);
                    }
                    // 10、若node没有资格获取读锁，就需要进入阻塞
                    if (whead == h && p.prev == pp) { // 保证CLH链没有变化
                        long time;
                        if (pp == null || h == p || p.status > 0) {
                            node = null; // throw away
                            break;
                        }
                        if (deadline == 0L)
                            time = 0L;
                        else if ((time = deadline - System.nanoTime()) <= 0L)
                            return cancelWaiter(node, p, false); // 超时取消node
                        Thread wt = Thread.currentThread();
                        U.putObject(wt, PARKBLOCKER, this);
                        node.thread = wt;
                        // 阻塞前：node确实没有资格获取读锁，就将node阻塞起来
                        if ((h != pp || (state & ABITS) == WBIT) &&
                            whead == h && p.prev == pp)
                            U.park(false, time); // node阻塞起来
                        node.thread = null;
                        U.putObject(wt, PARKBLOCKER, null);
                        if (interruptible && Thread.interrupted())
                            return cancelWaiter(node, p, true);
                    }
                }
            }
        }

        // node被唤醒后，开始尝试获取读锁
        for (int spins = -1;;) {
            WNode h, np, pp; int ps;
            // 1、有资格获取读锁，随机自旋尝试获取读锁
            if ((h = whead) == p) {
                if (spins < 0)
                    spins = HEAD_SPINS;
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;
                for (int k = spins;;) { // spin at head
                    long m, s, ns;
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L)) {
                        WNode c; Thread w;
                        whead = node;
                        node.prev = null;
                        while ((c = node.cowait) != null) {
                            if (U.compareAndSwapObject(node, WCOWAIT,
                                                       c, c.cowait) &&
                                (w = c.thread) != null)
                                U.unpark(w);
                        }
                        return ns;
                    }
                    else if (m >= WBIT &&
                             LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                        break;
                }
            }
            else if (h != null) {
                WNode c; Thread w;
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            if (whead == h) {
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // stale
                }
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                else if (ps == CANCELLED) {
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                    long time;
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    if (p.status < 0 &&
                        (p != h || (state & ABITS) == WBIT) &&
                        whead == h && node.prev == p)
                        U.park(false, time);
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

    /**
     * If node non-null, forces cancel status and unsplices it from
     * queue if possible and wakes up any cowaiters (of the node, or
     * group, as applicable), and in any case helps release current
     * first waiter if lock is free. (Calling with null arguments
     * serves as a conditional form of release, which is not currently
     * needed but may be needed under possible future cancellation
     * policies). This is a variant of cancellation methods in
     * AbstractQueuedSynchronizer (see its detailed explanation in AQS
     * internal documentation).
     *
     * @param node if nonnull, the waiter
     * @param group either node or the group node is cowaiting with
     * @param interrupted if already interrupted
     * @return INTERRUPTED if interrupted or Thread.interrupted, else zero
     */
    private long cancelWaiter(WNode node, WNode group, boolean interrupted) {
        /*
         * 如果节点线程被中断或者等待超时，需要取消节点的链接.
         * 大概的操作就是首先修改节点为取消状态，然后解除它在等待队列中的链接，并且唤醒节点上所有等待读的线程(也就是cowait节点)；
         * 最后如果锁可用，帮助唤醒头节点的后继节点的线程。
         *
         * 说明两个形参：
         * 如果node!=group，说明node节点是group节点上的一个cowait节点（如果不明白请见上面代码中对acquireRead方法中的U.compareAndSwapObject(p, WCOWAIT,node.cowait = p.cowait, node)这一行代码的注释），
         * 这种情况下首先修改node节点的状态(node.status = CANCELLED)，然后直接操作group节点，依次解除group节点上已经取消的cowait节点的链接。最后如果锁可用，帮助唤醒头节点的后继节点的线程。
         *
         * 如果node==group，说明在node节点之前的节点为写线程节点，这时需要进行以下操作：
         * a) 依次唤醒node节点上的未取消的cowait节点线程
         * b) 解除node节点和一段节点（node节点到“距离node最近的一个有效节点”）的链接
         * c) 最后如果锁可用，帮助唤醒头节点的后继节点的线程。
         */
        if (node != null && group != null) {
            Thread w;
            // 1、修改节点状态为取消状态
            node.status = CANCELLED;
            // 2、依次解除已经取消的cowait节点的链接，遍历group中cowait链表中的取消的读线程
            for (WNode p = group, q; (q = p.cowait) != null;) {
                if (q.status == CANCELLED) {
                    U.compareAndSwapObject(p, WCOWAIT, q, q.cowait);
                    p = group; // restart
                }
                else
                    p = q;
            }
            if (group == node) {

                // 3、依次唤醒节点上的未取消的cowait节点线程
                for (WNode r = group.cowait; r != null; r = r.cowait) {
                    if ((w = r.thread) != null)
                        U.unpark(w);       // wake up uncancelled co-waiters
                }

                for (WNode pred = node.prev; pred != null; ) { // unsplice
                    WNode succ, pp;        // find valid successor
                    // 4、后继节点为空或者已经取消，则去查找一个有效的后继节点
                    while ((succ = node.next) == null || succ.status == CANCELLED) {
                        WNode q = null;
                        // 5、从后向前查找一个有效的后继者
                        for (WNode t = wtail; t != null && t != node; t = t.prev)
                            if (t.status != CANCELLED)
                                q = t;
                        // 6、若查找到的有效后继者q，就是node当前的后继者succ，不做任何操作
                        // 若不是当前的后继者succ，CAS更新node的后继者
                        if (succ == q || U.compareAndSwapObject(node, WNEXT, succ, succ = q)) {
                            // 运行到这里说明从node到“距离node最近的一个有效节点q”之间可能存在已经取消的节点
                            // CAS替换node的后继节点为“距离node最近的一个有效节点”，也就是说解除了“所有已经取消但是还存在在链表上的无效节点”的链接
                            if (succ == null && node == wtail)
                                // 运行到这里说明node为尾节点，
                                // 利用CAS先修改尾节点为node的前继有效节点，后面再解除node的链接
                                U.compareAndSwapObject(this, WTAIL, node, pred);
                            break;
                        }
                    }
                    // 7、解除node节点的链接，因为node节点本来就要被取消
                    if (pred.next == node) //
                        U.compareAndSwapObject(pred, WNEXT, node, succ);
                    // 8、唤醒后继节点的线程
                    if (succ != null && (w = succ.thread) != null) {
                        succ.thread = null;
                        U.unpark(w);       // wake up succ to observe new pred
                    }
                    // 9、如果前继节点已经取消，向前查找一个有效节点继续循环，如果这个节点为空则直接跳出循环
                    if (pred.status != CANCELLED || (pp = pred.prev) == null)
                        break;
                    node.prev = pp;        // repeat if new pred wrong/cancelled
                    U.compareAndSwapObject(pp, WNEXT, pred, succ);
                    pred = pp;
                }
            }
        }
        //检查是否可唤醒head节点的后继节点线程
        WNode h; // Possibly release first waiter
        while ((h = whead) != null) {
            long s; WNode q; // similar to release() but check eligibility
            // 头结点的后继者为空，或者，后继者为取消状态
            if ((q = h.next) == null || q.status == CANCELLED) {
                // 从尾节点向前查找一个未取消的节点，作为头节点的next节点
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            // 期间头结点无变化，头结点后继者非null且为初始状态，且不存在读写锁，或者不存在写锁且后继者是读节点，就唤醒后继者
            if (h == whead) {
                if (q != null && h.status == 0 &&
                    ((s = state) & ABITS) != WBIT && //
                    (s == 0L || q.mode == RMODE)) // 锁可用，或者后继节点是读线程
                    release(h); // 可以唤醒头节点的后继节点线程
                break;
            }
        }
        return (interrupted || Thread.interrupted()) ? INTERRUPTED : 0L;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long STATE;
    private static final long WHEAD;
    private static final long WTAIL;
    private static final long WNEXT;
    private static final long WSTATUS;
    private static final long WCOWAIT;
    private static final long PARKBLOCKER;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = StampedLock.class;
            Class<?> wk = WNode.class;
            STATE = U.objectFieldOffset
                (k.getDeclaredField("state"));
            WHEAD = U.objectFieldOffset
                (k.getDeclaredField("whead"));
            WTAIL = U.objectFieldOffset
                (k.getDeclaredField("wtail"));
            WSTATUS = U.objectFieldOffset
                (wk.getDeclaredField("status"));
            WNEXT = U.objectFieldOffset
                (wk.getDeclaredField("next"));
            WCOWAIT = U.objectFieldOffset
                (wk.getDeclaredField("cowait"));
            Class<?> tk = Thread.class;
            PARKBLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));

        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
