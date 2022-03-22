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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

/**
 * Provides a framework for implementing blocking locks and related
 * synchronizers (semaphores, events, etc) that rely on
 * first-in-first-out (FIFO) wait queues.  This class is designed to
 * be a useful basis for most kinds of synchronizers that rely on a
 * single atomic {@code int} value to represent state. Subclasses
 * must define the protected methods that change this state, and which
 * define what that state means in terms of this object being acquired
 * or released.  Given these, the other methods in this class carry
 * out all queuing and blocking mechanics. Subclasses can maintain
 * other state fields, but only the atomically updated {@code int}
 * value manipulated using methods {@link #getState}, {@link
 * #setState} and {@link #compareAndSetState} is tracked with respect
 * to synchronization.
 *
 * <p>Subclasses should be defined as non-public internal helper
 * classes that are used to implement the synchronization properties
 * of their enclosing class.  Class
 * {@code AbstractQueuedSynchronizer} does not implement any
 * synchronization interface.  Instead it defines methods such as
 * {@link #acquireInterruptibly} that can be invoked as
 * appropriate by concrete locks and related synchronizers to
 * implement their public methods.
 *
 * <p>This class supports either or both a default <em>exclusive</em>
 * mode and a <em>shared</em> mode. When acquired in exclusive mode,
 * attempted acquires by other threads cannot succeed. Shared mode
 * acquires by multiple threads may (but need not) succeed. This class
 * does not &quot;understand&quot; these differences except in the
 * mechanical sense that when a shared mode acquire succeeds, the next
 * waiting thread (if one exists) must also determine whether it can
 * acquire as well. Threads waiting in the different modes share the
 * same FIFO queue. Usually, implementation subclasses support only
 * one of these modes, but both can come into play for example in a
 * {@link ReadWriteLock}. Subclasses that support only exclusive or
 * only shared modes need not define the methods supporting the unused mode.
 *
 * <p>This class defines a nested {@link ConditionObject} class that
 * can be used as a {@link Condition} implementation by subclasses
 * supporting exclusive mode for which method {@link
 * #isHeldExclusively} reports whether synchronization is exclusively
 * held with respect to the current thread, method {@link #release}
 * invoked with the current {@link #getState} value fully releases
 * this object, and {@link #acquire}, given this saved state value,
 * eventually restores this object to its previous acquired state.  No
 * {@code AbstractQueuedSynchronizer} method otherwise creates such a
 * condition, so if this constraint cannot be met, do not use it.  The
 * behavior of {@link ConditionObject} depends of course on the
 * semantics of its synchronizer implementation.
 *
 * <p>This class provides inspection, instrumentation, and monitoring
 * methods for the internal queue, as well as similar methods for
 * condition objects. These can be exported as desired into classes
 * using an {@code AbstractQueuedSynchronizer} for their
 * synchronization mechanics.
 *
 * <p>Serialization of this class stores only the underlying atomic
 * integer maintaining state, so deserialized objects have empty
 * thread queues. Typical subclasses requiring serializability will
 * define a {@code readObject} method that restores this to a known
 * initial state upon deserialization.
 *
 * <h3>Usage</h3>
 *
 * <p>To use this class as the basis of a synchronizer, redefine the
 * following methods, as applicable, by inspecting and/or modifying
 * the synchronization state using {@link #getState}, {@link
 * #setState} and/or {@link #compareAndSetState}:
 *
 * <ul>
 * <li> {@link #tryAcquire}
 * <li> {@link #tryRelease}
 * <li> {@link #tryAcquireShared}
 * <li> {@link #tryReleaseShared}
 * <li> {@link #isHeldExclusively}
 * </ul>
 *
 * Each of these methods by default throws {@link
 * UnsupportedOperationException}.  Implementations of these methods
 * must be internally thread-safe, and should in general be short and
 * not block. Defining these methods is the <em>only</em> supported
 * means of using this class. All other methods are declared
 * {@code final} because they cannot be independently varied.
 *
 * <p>You may also find the inherited methods from {@link
 * AbstractOwnableSynchronizer} useful to keep track of the thread
 * owning an exclusive synchronizer.  You are encouraged to use them
 * -- this enables monitoring and diagnostic tools to assist users in
 * determining which threads hold locks.
 *
 * <p>Even though this class is based on an internal FIFO queue, it
 * does not automatically enforce FIFO acquisition policies.  The core
 * of exclusive synchronization takes the form:
 *
 * <pre>
 * Acquire:
 *     while (!tryAcquire(arg)) {
 *        <em>enqueue thread if it is not already queued</em>;
 *        <em>possibly block current thread</em>;
 *     }
 *
 * Release:
 *     if (tryRelease(arg))
 *        <em>unblock the first queued thread</em>;
 * </pre>
 *
 * (Shared mode is similar but may involve cascading signals.)
 *
 * <p id="barging">Because checks in acquire are invoked before
 * enqueuing, a newly acquiring thread may <em>barge</em> ahead of
 * others that are blocked and queued.  However, you can, if desired,
 * define {@code tryAcquire} and/or {@code tryAcquireShared} to
 * disable barging by internally invoking one or more of the inspection
 * methods, thereby providing a <em>fair</em> FIFO acquisition order.
 * In particular, most fair synchronizers can define {@code tryAcquire}
 * to return {@code false} if {@link #hasQueuedPredecessors} (a method
 * specifically designed to be used by fair synchronizers) returns
 * {@code true}.  Other variations are possible.
 *
 * <p>Throughput and scalability are generally highest for the
 * default barging (also known as <em>greedy</em>,
 * <em>renouncement</em>, and <em>convoy-avoidance</em>) strategy.
 * While this is not guaranteed to be fair or starvation-free, earlier
 * queued threads are allowed to recontend before later queued
 * threads, and each recontention has an unbiased chance to succeed
 * against incoming threads.  Also, while acquires do not
 * &quot;spin&quot; in the usual sense, they may perform multiple
 * invocations of {@code tryAcquire} interspersed with other
 * computations before blocking.  This gives most of the benefits of
 * spins when exclusive synchronization is only briefly held, without
 * most of the liabilities when it isn't. If so desired, you can
 * augment this by preceding calls to acquire methods with
 * "fast-path" checks, possibly prechecking {@link #hasContended}
 * and/or {@link #hasQueuedThreads} to only do so if the synchronizer
 * is likely not to be contended.
 *
 * <p>This class provides an efficient and scalable basis for
 * synchronization in part by specializing its range of use to
 * synchronizers that can rely on {@code int} state, acquire, and
 * release parameters, and an internal FIFO wait queue. When this does
 * not suffice, you can build synchronizers from a lower level using
 * {@link java.util.concurrent.atomic atomic} classes, your own custom
 * {@link java.util.Queue} classes, and {@link LockSupport} blocking
 * support.
 *
 * <h3>Usage Examples</h3>
 *
 * <p>Here is a non-reentrant mutual exclusion lock class that uses
 * the value zero to represent the unlocked state, and one to
 * represent the locked state. While a non-reentrant lock
 * does not strictly require recording of the current owner
 * thread, this class does so anyway to make usage easier to monitor.
 * It also supports conditions and exposes
 * one of the instrumentation methods:
 *
 *  <pre> {@code
 * class Mutex implements Lock, java.io.Serializable {
 *
 *   // Our internal helper class
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     // Reports whether in locked state
 *     protected boolean isHeldExclusively() {
 *       return getState() == 1;
 *     }
 *
 *     // Acquires the lock if state is zero
 *     public boolean tryAcquire(int acquires) {
 *       assert acquires == 1; // Otherwise unused
 *       if (compareAndSetState(0, 1)) {
 *         setExclusiveOwnerThread(Thread.currentThread());
 *         return true;
 *       }
 *       return false;
 *     }
 *
 *     // Releases the lock by setting state to zero
 *     protected boolean tryRelease(int releases) {
 *       assert releases == 1; // Otherwise unused
 *       if (getState() == 0) throw new IllegalMonitorStateException();
 *       setExclusiveOwnerThread(null);
 *       setState(0);
 *       return true;
 *     }
 *
 *     // Provides a Condition
 *     Condition newCondition() { return new ConditionObject(); }
 *
 *     // Deserializes properly
 *     private void readObject(ObjectInputStream s)
 *         throws IOException, ClassNotFoundException {
 *       s.defaultReadObject();
 *       setState(0); // reset to unlocked state
 *     }
 *   }
 *
 *   // The sync object does all the hard work. We just forward to it.
 *   private final Sync sync = new Sync();
 *
 *   public void lock()                { sync.acquire(1); }
 *   public boolean tryLock()          { return sync.tryAcquire(1); }
 *   public void unlock()              { sync.release(1); }
 *   public Condition newCondition()   { return sync.newCondition(); }
 *   public boolean isLocked()         { return sync.isHeldExclusively(); }
 *   public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
 *   public void lockInterruptibly() throws InterruptedException {
 *     sync.acquireInterruptibly(1);
 *   }
 *   public boolean tryLock(long timeout, TimeUnit unit)
 *       throws InterruptedException {
 *     return sync.tryAcquireNanos(1, unit.toNanos(timeout));
 *   }
 * }}</pre>
 *
 * <p>Here is a latch class that is like a
 * {@link java.util.concurrent.CountDownLatch CountDownLatch}
 * except that it only requires a single {@code signal} to
 * fire. Because a latch is non-exclusive, it uses the {@code shared}
 * acquire and release methods.
 *
 *  <pre> {@code
 * class BooleanLatch {
 *
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     boolean isSignalled() { return getState() != 0; }
 *
 *     protected int tryAcquireShared(int ignore) {
 *       return isSignalled() ? 1 : -1;
 *     }
 *
 *     protected boolean tryReleaseShared(int ignore) {
 *       setState(1);
 *       return true;
 *     }
 *   }
 *
 *   private final Sync sync = new Sync();
 *   public boolean isSignalled() { return sync.isSignalled(); }
 *   public void signal()         { sync.releaseShared(1); }
 *   public void await() throws InterruptedException {
 *     sync.acquireSharedInterruptibly(1);
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;
    /**
     * 应用场景：
     * ReentrantLock	        使用AQS保存锁重复持有的次数。当一个线程获取锁时，ReentrantLock记录当前获得锁的线程标识，用于检测是否重复获取，以及错误线程试图解锁操作时异常情况的处理。
     * Semaphore	            使用AQS同步状态来保存信号量的当前计数。tryRelease会增加计数，acquireShared会减少计数。
     * CountDownLatch	        使用AQS同步状态来表示计数。计数为0时，所有的Acquire操作（CountDownLatch的await方法）才可以通过。
     * ReentrantReadWriteLock	使用AQS同步状态中的16位保存写锁持有的次数，剩下的16位用于保存读锁的持有次数。
     * ThreadPoolExecutor	    Worker利用AQS同步状态实现对独占线程变量的设置（tryAcquire和tryRelease）。
     */

    /**
     * Creates a new {@code AbstractQueuedSynchronizer} instance
     * with initial synchronization state of zero.
     */
    protected AbstractQueuedSynchronizer() { } //创建一个初始同步状态为零的新AbstractQueuedSynchronizer实例。

    /**
     * Wait queue node class.
     *
     * <p>The wait queue is a variant of a "CLH" (Craig, Landin, and
     * Hagersten) lock queue. CLH locks are normally used for
     * spinlocks.  We instead use them for blocking synchronizers, but
     * use the same basic tactic of holding some of the control
     * information about a thread in the predecessor of its node.  A
     * "status" field in each node keeps track of whether a thread
     * should block.  A node is signalled when its predecessor
     * releases.  Each node of the queue otherwise serves as a
     * specific-notification-style monitor holding a single waiting
     * thread. The status field does NOT control whether threads are
     * granted locks etc though.  A thread may try to acquire if it is
     * first in the queue. But being first does not guarantee success;
     * it only gives the right to contend.  So the currently released
     * contender thread may need to rewait.
     *
     * <p>To enqueue into a CLH lock, you atomically splice it in as new
     * tail. To dequeue, you just set the head field.
     * <pre>
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * </pre>
     *
     * <p>Insertion into a CLH queue requires only a single atomic
     * operation on "tail", so there is a simple atomic point of
     * demarcation from unqueued to queued. Similarly, dequeuing
     * involves only updating the "head". However, it takes a bit
     * more work for nodes to determine who their successors are,
     * in part to deal with possible cancellation due to timeouts
     * and interrupts.
     *
     * <p>The "prev" links (not used in original CLH locks), are mainly
     * needed to handle cancellation. If a node is cancelled, its
     * successor is (normally) relinked to a non-cancelled
     * predecessor. For explanation of similar mechanics in the case
     * of spin locks, see the papers by Scott and Scherer at
     * http://www.cs.rochester.edu/u/scott/synchronization/
     *
     * <p>We also use "next" links to implement blocking mechanics.
     * The thread id for each node is kept in its own node, so a
     * predecessor signals the next node to wake up by traversing
     * next link to determine which thread it is.  Determination of
     * successor must avoid races with newly queued nodes to set
     * the "next" fields of their predecessors.  This is solved
     * when necessary by checking backwards from the atomically
     * updated "tail" when a node's successor appears to be null.
     * (Or, said differently, the next-links are an optimization
     * so that we don't usually need a backward scan.)
     *
     * <p>Cancellation introduces some conservatism to the basic
     * algorithms.  Since we must poll for cancellation of other
     * nodes, we can miss noticing whether a cancelled node is
     * ahead or behind us. This is dealt with by always unparking
     * successors upon cancellation, allowing them to stabilize on
     * a new predecessor, unless we can identify an uncancelled
     * predecessor who will carry this responsibility.
     *
     * <p>CLH queues need a dummy header node to get started. But
     * we don't create them on construction, because it would be wasted
     * effort if there is never contention. Instead, the node
     * is constructed and head and tail pointers are set upon first
     * contention.
     *
     * <p>Threads waiting on Conditions use the same nodes, but
     * use an additional link. Conditions only need to link nodes
     * in simple (non-concurrent) linked queues because they are
     * only accessed when exclusively held.  Upon await, a node is
     * inserted into a condition queue.  Upon signal, the node is
     * transferred to the main queue.  A special value of status
     * field is used to mark which queue a node is on.
     *
     * <p>Thanks go to Dave Dice, Mark Moir, Victor Luchangco, Bill
     * Scherer and Michael Scott, along with members of JSR-166
     * expert group, for helpful ideas, discussions, and critiques
     * on the design of this class.
     */
    static final class Node {
        /** Marker to indicate a node is waiting in shared mode */
        static final Node SHARED = new Node(); //指示节点在【共享模式】下等待的标记 -- 非null
        /** Marker to indicate a node is waiting in exclusive mode */
        static final Node EXCLUSIVE = null; //指示节点以【独占模式】下等待的标记 -- null

        // waitStatus为0，表示当前节点在sync queue中，等待着获取锁
        /** waitStatus value to indicate thread has cancelled */
        static final int CANCELLED =  1; //值为1，该节点由于超时或中断而被取消。 节点永远不会离开这个状态。 特别是，具有CANCELLED的线程永远不会再次阻塞
        /** waitStatus value to indicate successor's thread needs unparking */
        static final int SIGNAL    = -1; //值为-1，此节点的后继节点已（或即将）被阻塞（通过park），因此当前节点在释放或取消时必须unpark后继节点。 为了避免竞争，获取方法必须首先表明它们需要一个信号，然后重试原子获取，然后在失败时阻塞
        /** waitStatus value to indicate thread is waiting on condition */
        static final int CONDITION = -2; //值为-2，节点在等待队列中，节点线程等待在Condition上，不过当其他的线程对Condition调用了signal()方法后，该节点就会从condition条件队列转移到sync同步队列中，然后开始尝试对同步状态的获取
        /**
         * waitStatus value to indicate the next acquireShared should
         * unconditionally propagate
         */
        static final int PROPAGATE = -3; //值为-3，表示释放共享资源时需要通知其他节点 ，即 表示当前场景下后续的acquireShared能够得以执行。

        // waitStatus 非负值意味着节点不需要发出SIGNAL。 因此，大多数代码不需要检查特定值，只需检查符号即可。
        // waitStatus 对于正常同步节点，该字段初始化为 0，对于条件节点，该字段初始化为 CONDITION。
        // waitStatus 它使用 CAS 进行修改（或者在可能的情况下，无条件的 volatile 写入）。
        /**
         * Status field, taking on only the values:
         *   SIGNAL:     The successor of this node is (or will soon be)
         *               blocked (via park), so the current node must
         *               unpark its successor when it releases or
         *               cancels. To avoid races, acquire methods must
         *               first indicate they need a signal,
         *               then retry the atomic acquire, and then,
         *               on failure, block.
         *   CANCELLED:  This node is cancelled due to timeout or interrupt.
         *               Nodes never leave this state. In particular,
         *               a thread with cancelled node never again blocks.
         *   CONDITION:  This node is currently on a condition queue.
         *               It will not be used as a sync queue node
         *               until transferred, at which time the status
         *               will be set to 0. (Use of this value here has
         *               nothing to do with the other uses of the
         *               field, but simplifies mechanics.)
         *   PROPAGATE:  A releaseShared should be propagated to other
         *               nodes. This is set (for head node only) in
         *               doReleaseShared to ensure propagation
         *               continues, even if other operations have
         *               since intervened.
         *   0:          None of the above
         *
         * The values are arranged numerically to simplify use.
         * Non-negative values mean that a node doesn't need to
         * signal. So, most code doesn't need to check for particular
         * values, just for sign.
         *
         * The field is initialized to 0 for normal sync nodes, and
         * CONDITION for condition nodes.  It is modified using CAS
         * (or when possible, unconditional volatile writes).
         */
        volatile int waitStatus; //

        /**
         * Link to predecessor node that current node/thread relies on
         * for checking waitStatus. Assigned during enqueuing, and nulled
         * out (for sake of GC) only upon dequeuing.  Also, upon
         * cancellation of a predecessor, we short-circuit while
         * finding a non-cancelled one, which will always exist
         * because the head node is never cancelled: A node becomes
         * head only as a result of successful acquire. A
         * cancelled thread never succeeds in acquiring, and a thread only
         * cancels itself, not any other node.
         */
        volatile Node prev; //前一个节点

        /**
         * Link to the successor node that the current node/thread
         * unparks upon release. Assigned during enqueuing, adjusted
         * when bypassing cancelled predecessors, and nulled out (for
         * sake of GC) when dequeued.  The enq operation does not
         * assign next field of a predecessor until after attachment,
         * so seeing a null next field does not necessarily mean that
         * node is at end of queue. However, if a next field appears
         * to be null, we can scan prev's from the tail to
         * double-check.  The next field of cancelled nodes is set to
         * point to the node itself instead of null, to make life
         * easier for isOnSyncQueue.
         */
        volatile Node next; //后一个节点

        /**
         * The thread that enqueued this node.  Initialized on
         * construction and nulled out after use.
         */
        volatile Thread thread; // 线程

        /**
         * Link to next node waiting on condition, or the special
         * value SHARED.  Because condition queues are accessed only
         * when holding in exclusive mode, we just need a simple
         * linked queue to hold nodes while they are waiting on
         * conditions. They are then transferred to the queue to
         * re-acquire. And because conditions can only be exclusive,
         * we save a field by using special value to indicate shared
         * mode.
         */
        Node nextWaiter; //下一个等待者

        /**
         * Returns true if node is waiting in shared mode.
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * Returns previous node, or throws NullPointerException if null.
         * Use when predecessor cannot be null.  The null check could
         * be elided, but is present to help the VM.
         *
         * @return the predecessor of this node
         */
        final Node predecessor() throws NullPointerException { //返回前一个节点
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // Used to establish initial head or SHARED marker
        }

        Node(Thread thread, Node mode) {     // Used by addWaiter -- 用于 addWaiter 方法，此时waitStatus为0，nextWaiter为new node即
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition -- 用于 Condition 方法，此时waitStatus为CONDITION即-2，nextWaiter为null
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * Head of the wait queue, lazily initialized.  Except for
     * initialization, it is modified only via method setHead.  Note:
     * If head exists, its waitStatus is guaranteed not to be
     * CANCELLED.
     */
    private transient volatile Node head; //队列头

    /**
     * Tail of the wait queue, lazily initialized.  Modified only via
     * method enq to add new wait node.
     */
    private transient volatile Node tail; //队列尾

    /**
     * The synchronization state.
     */
    private volatile int state; // 同步状态

    /**
     * Returns the current value of synchronization state.
     * This operation has memory semantics of a {@code volatile} read.
     * @return current state value
     */
    protected final int getState() {
        return state;
    }

    /**
     * Sets the value of synchronization state.
     * This operation has memory semantics of a {@code volatile} write.
     * @param newState the new state value
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * Atomically sets synchronization state to the given updated
     * value if the current state value equals the expected value.
     * This operation has memory semantics of a {@code volatile} read
     * and write.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that the actual
     *         value was not equal to the expected value.
     */
    protected final boolean compareAndSetState(int expect, int update) { //CAS自旋修改状态
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // Queuing utilities

    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices
     * to improve responsiveness with very short timeouts.
     */
    static final long spinForTimeoutThreshold = 1000L; //自旋比使用定时park更快的纳秒数。 粗略的估计足以在非常短的超时时间内提高响应能力。

    /**
     * Inserts node into queue, initializing if necessary. See picture above.
     * @param node the node to insert
     * @return node's predecessor
     */
    private Node enq(final Node node) { // 节点入队操作,包含 队列头的初始化操作、多个线程竞争加入队尾时AQS失败后的Node需要入队尾的操作
        for (;;) { // 经典的 循环 + AQS
            Node t = tail;
            if (t == null) { // Must initialize -- 如果tail为空,则新建一个head节点,并且tail指向head
                if (compareAndSetHead(new Node())) // 新建的Node的Thread、waitNode都是null -- 不难发现队列头就是一个普通的Node
                    tail = head;// 注意：tail与head之间的初始化时存在延迟的，先创建Head然后再去赋值给tail -- 注意：初始化操作完成后，还需要再次进入循环，开始创建Node入队尾
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) { // 自旋设置队尾
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * Creates and enqueues node for current thread and given mode.
     *
     * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
     * @return the new node
     */
    private Node addWaiter(Node mode) {
        /*
         * 1、为当前线程和给定模式创建节点，并且尝试加入到等待队列的队尾
         * 2、期间，允许初始化同步队列
         */
        Node node = new Node(Thread.currentThread(), mode); //创建新的节点
        Node pred = tail; // 获取尾节点
        if (pred != null) { // 尾节点不为空,说明队列已经初始化
            node.prev = pred; // 注意：620行与622行 设置双向关联关系是存在延迟的
            if (compareAndSetTail(pred, node)) { //设置新节点为尾节点
                pred.next = node; //建立队列的双向链表关系
                return node; //返回加入的Node
            }
        }
        enq(node); // 入队操作：包含初始化队列头、加入上面CAS竞争失败的节点
        return node;
    }

    /**
     * Sets head of queue to be node, thus dequeuing. Called only by
     * acquire methods.  Also nulls out unused fields for sake of GC
     * and to suppress unnecessary signals and traversals.
     *
     * @param node the node
     */
    private void setHead(Node node) { //设置队列头
        head = node;
        node.thread = null; //为了 GC 和抑制不必要的信号和遍历，还清空未使用的字段。
        node.prev = null;
    }

    /**
     * Wakes up node's successor, if one exists.
     *
     * @param node the node
     */
    private void unparkSuccessor(Node node) {
        /*
         释放锁的操作中唤醒node的后继者，让后继者有资格去尝试获取锁
         */
        int ws = node.waitStatus;
        // 1、该步骤主要是为了保护，避免重复释放。
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        // 2、需要唤醒的线程保存在next节点中。但如果next是被取消或明显无效，就需要从尾部向后遍历到node第一个实际的未取消的继任者。
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        // 3、找到有效后继者后尝试唤醒他
        if (s != null)
            /*
            唤醒线程，释放锁的逻辑代码已经结束，那调用LockSupport.unpark(s.thread)后，会进入到哪呢？
            此时，再次进入获取锁代码的acquireQueue()方法和shouldParkAfterFailedAcquire()方法
            此时会有两种情况
            HEAD --> Node  ... > 其中 Node 为  LockSupport.unpark 中的 s;
            HEAD --> A Cancel Node -->  Node(s)
                第二种情况，shouldParkAfterFailedAcquire(prev,node) 中的 prev 为一个取消的节点，然后会重构整个 CLH 链表，
                删除Node 到 head 节点直接的取消节点，使得被唤醒线程的节点的上一个节点变为 head,从而满足acquireQueued中的if (p == head && tryAcquire(arg)) 处的条件，进入获取锁方法。至此，lock 方法与 unlock 方法流畅。
             */
            LockSupport.unpark(s.thread); // 唤醒线程
    }

    /**
     * Release action for shared mode -- signals successor and ensures
     * propagation. (Note: For exclusive mode, release just amounts
     * to calling unparkSuccessor of head if it needs signal.)
     */
    private void doReleaseShared() {
        /*
         * 共享模式下：发出信号唤醒后面获取读锁的线程，告诉他们去获取读锁，以此去报传播特性
         */
        for (;;) { // 死循环
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                /*
                 * ① 该节点在之前获取锁中的shouldParkAfterFailedAcquire()已经设置为SIGNAL，且期间没有中断或超时就不会是CANCELLED状态;
                 * 那么需要唤醒h的后一个有效节点中的线程去竞争锁
                 */
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) // 更新头结点的状态为初始化状态0
                        continue;
                    // 将h的节点从SIGNAL改为0后，就需要唤醒h的后继者
                    unparkSuccessor(h); // 由于共享锁被释放或者多余的，这里去尝试唤醒获取读锁的线程 -- 这里就会和doAcquireShared()中的shouldParkAfterFailedAcquire()阻塞相呼应
                }
                /*
                 * 若node的状态为0，只能是：该节点在之前没有经过shouldParkAfterFailedAcquire()
                 * 如果状态为0，则设置设为为传播状态，PROPAGATE 会在什么时候变化呢？
                 * 答：在判断该节点的下一个节点是否需要阻塞时，在shouldParkAfterFailed()中会做判断，如果状态不是SIGNAL或CANCELED状态，为了保险起见，就会修改为SIGNAL状态
                 *  那么当前线程就会从②中的自旋结束，进入 ①
                 */
                else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // ② 继续自旋，若没有发生别的情况，ws就会是PROPAGATE，从而退出循环
            }
            /*
             * 如果处理过一次 unparkSuccessor() 方法后，头节点没有发生变化，就退出该方法.
             * 那head在什么时候会改变呢？
             * 当然在是抢占锁成功的时候，head节点代表获取锁的节点。
             * 一旦获取共享锁成功，则又会进入setHeadAndPropagate方法，当然又会触发doReleaseShared方法，传播特性应该就是表现在这里吧。
             * 再想一下，同一时间，可以有多个多线程占有锁，那在锁释放时：
             *      写锁的释放比较简单，就是从头部节点下的第一个非取消节点，唤醒线程即可，
             *      共享锁的释放就比较复杂，为了释放获取共享锁的线程，需将信息存入在 readHolds ThreadLocal变量中。
             */
            if (h == head)
            /*
             * 而在releaseShared()中，由于是直接释放了读锁，证明就有额外的读锁，当head存在，若head是SIGNAL状态，就去唤醒head的有效后继者去获取锁
             * 【不清楚是读锁还是写锁，但都是可以的，如果是获取读锁的node可以直接去获取，如果是写锁，就可以判断当前是否还有没有读锁，没有就去获取写锁】
             *      a.若期间h还是等于head，说明唤醒的节点获取锁失败啦，因为唤醒成功后会更新head。基于h=head那么就无须再尝试继续唤醒
             *      b.若期间h已经不等于head，说明唤醒的节点获取锁成功
             *          假设获取的写锁成功，那么尝试继续唤醒，但由于写锁已经被占用，因此下一个唤醒的node必定是获取锁失败的，最终也会走向h=head，终止结束
             *          假设获取的读锁成功，那么就有必要继续唤醒，看看是否还可以去获取读锁
             */
                break;
        }
    }

    /**
     * Sets head of queue, and checks if successor may be waiting
     * in shared mode, if so propagating if either propagate > 0 or
     * PROPAGATE status was set.
     *
     * @param node the node
     * @param propagate the return value from a tryAcquireShared
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        /*
         * node应该是head节点的next节点，在自旋AQS中获取到锁的node
         * 设置队列头，并检查后续队列是否可能在共享模式下等待，如果是，则在设置了“传播>0”或“传播状态”时进行传播。
         */
        Node h = head; // 记录 old 头结点 -- 后续检查
        setHead(node); // 获取到共享锁后，需要更新head
        /*
         * 共享锁成功：propagate 就是共享锁剩余的数量
         * propagate 传播数大于0，允许继续传播给下一个获取读锁的node
         * propagate 传播数小于0，表示共享锁数量不够唤醒别的视图获取共享锁的node，就不需要传播给下一个获取读锁的node
         * 但是若：此刻新的头结点即node
         * 头结点node非空，且状态非取消状态和非初始化状态，仍然尝试传播
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) { // head已经是新的node -- 需要注意
            Node s = node.next;
            // node的下一个next节点不为空，同时next是共享模式的节点 -- 才会允许传播
            if (s == null || s.isShared())
                /*
                * 假设doAcquireSharedInterruptibly()中node获取到读锁后，r等于2，表示还有剩余两把读锁，
                * 因此在setHeadAndPropagate()中的propagate=2，表示剩余有两把读锁，node在这之前有已经获取过一把读锁
                * 执行到此处时，已将node作为新的head，并且发现下一个仍是获取读锁的线程，由于现在有两把读锁，
                * 所以可以调用 doReleaseShared() 来发出信号唤醒后面获取读锁的线程，告诉他们去获取锁
                */
                doReleaseShared();
        }
    }

    // Utilities for various versions of acquire

    /**
     * Cancels an ongoing attempt to acquire.
     *
     * @param node the node
     */
    private void cancelAcquire(Node node) {
        /*
        cancelAcquire 由于中断或者超时原因，需要将尝试获取锁或者等待阻塞的线程的node设置为取消状态
         */
        // 1、将无效节点过滤
        if (node == null)
            return;

        // 2、将取消尝试获取锁的node的thread设置为null
        node.thread = null;

        // 3、设置prev的值为从当前准备取消的node往head节点方向的第一个非取消节点。以此并将中间的取消节点脱离这条链。
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev; // 注意虽然找到第一个非 Cancelled 状态的节点作为pred，但是此刻其next还未等于node

        //4、获取有效pred前驱节点的后继节点next
        Node predNext = pred.next;

        //5、当前取消获取锁的node的等待状态为 CANCELLED
        node.waitStatus = Node.CANCELLED;

        /*
        6、如果被取消的节点是尾节点的话，那么将pred设置为尾节点，compareAndSetTail(node, pred)，
            如果设置失败，说明，有别的线程在申请锁，使得尾部节点发生了变化，那这样的话，我这个当前节点取消的工作，就到此可以结束了
                -- 原因在于：那个获取锁的线程，后面再获取锁失败后，进入shouldParkAfterFailed()后帮忙负责来删除取消节点
                -- [这也是为什么其他地方会存在Cancelled状态节点的原因，因为cancelAcquire()做节点取消工作不一定会成功，会将工作交给其他线程来完成，
                    因为其他本来就没有获取到锁，进入shouldParkAfterFailed()后帮忙负责来删除取消节点，也算是没有浪费CPU]
            如果设置成功，既然pred是尾节点，那么再次将pred的next域设置为null;
                当然也能设置失败，表明又有新的线程在申请说，创建了节点。所以取消操作，也到此结束。
         */
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            //7、如果取消的节点不是尾部节点，并且node的有效前继者pred不是头节点，pred的线程状态为SIGNAL或者CAS设置为SIGNAL后，这时需要维护CLH链，直接将pred与node.next连接起来
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL || (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)// node.next不为空，并且非Cancelled状态
                    compareAndSetNext(pred, predNext, next); // 跳过node节点，将pred的next设为node的next节点，连接起来
            } else {
                // 不满足上述条件，即一种情况
                // pred是头结点，说明被取消的node前一个有效节点是头结点，就需要唤醒node的后一个节点
                // 1、去尝试获取锁，2、获取锁失败后，还可以帮助将中pred和node中的cancelled节点去除
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
        /**
         * 流程：
         * 1、获取当前节点的前驱节点，如果前驱节点的状态是CANCELLED，那就一直往前遍历，找到第一个waitStatus <= 0的节点，将找到的Pred节点和当前Node关联，将当前Node设置为CANCELLED。
         * 2、根据当前节点的位置，考虑以下三种情况：
         *  a.当前节点是尾节点。 对应 if (node == tail && compareAndSetTail(node, pred)) {
         *  b.当前节点是Head的后继节点， 对应 else { unparkSuccessor(node);
         *  c.当前节点不是Head的后继节点，也不是尾节点 对应 if (pred != head &&
         */
    }

    /**
     * Checks and updates status for a node that failed to acquire.
     * Returns true if thread should block. This is the main signal
     * control in all acquire loops.  Requires that pred == node.prev.
     *
     * @param pred node's predecessor holding status
     * @param node the node
     * @return {@code true} if thread should block
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        /*
        情况：已acquireQueued()中的调用的shouldParkAfterFailedAcquire()为例：
        1、pred状态为0，会将pred设置为SIGNAL，然后再次进入调用该方法的循环中，如果获取锁失败，那么此刻pred的状态为SIGNAL后就会允许阻塞起来
        2、pred状态为取消，那么会重构CLH队列，删除同步队列里中间这段取消状态的节点，然后再次进入调用该方法的循环中，再来判断pred的状态
        3、pred状态为SIGNAL，那么当前node就可以阻塞起来，因为pred释放锁后，将在unparkSuccessor()中，判断pred是否为SIGNAL，同时node不是CANCELLED状态，就会唤醒它
         */
        int ws = pred.waitStatus; // 前置节点状态
        //  1、前置节点状态处于SIGNAL，即表示后面会通过unparkSuccessor()唤醒后面的节点，因此后面的node可以直接进入等待阻塞，返回true
        if (ws == Node.SIGNAL)
            return true;
        // 2、ws大于0表示 CANCELLED 状态的节点，需要帮助清理 CANCELLED 状态的节点
        if (ws > 0) {
            do {
                node.prev = pred = pred.prev; // 从前置节点向前寻找第一个状态不为CANCELLED的节点 -- 跳过node为Cancelled状态的前置节点
            } while (pred.waitStatus > 0); // 取消状态
            pred.next = node; // 建立关联关系
            /*
            在重构 CLH 队列后，返回 false, 再次进入到 acquireQueued() 等方法的无限循环中，可能在竞争锁失败或者没资格获取锁时再次进入shouldParkAfterFailedAcquire方法
            */
        } else {
            /*
             3、
                a.如果前置节点状态为0，表示还没有其他节点通过(prev)来判断该 prev 的后继节点是否需要阻塞过，所以，通过 CAS 设置前置节点为 Node.SIGNAL,
                当在调用该方法的死循环例如acquireQueued()中可能因为再次获取锁失败或者没有资格获取锁的原因，
                会再次进入该方法，并执行第一个判断 ws == Node.SIGNAL 成功，进而避免不必要CPU消耗从而将线程阻塞气起来。

                b.如果前置节点状态为PROPAGATE，PROPAGATE只能是从状态0转换过去，看doReleaseShared()代码可知：
            */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL); // 将前一个节点的状态设置为 SIGNAL 状态；
        }
        return false;
    }

    /**
     * Convenience method to interrupt current thread.
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * Convenience method to park and then check if interrupted
     *
     * @return {@code true} if interrupted
     */
    private final boolean parkAndCheckInterrupt() { // 阻塞当前线程
        LockSupport.park(this);
        /*
         * 唤醒的情况：
         * 1、中断唤醒
         * 2、unPark()指定唤醒
         */
        return Thread.interrupted();
    }

    /*
     * Various flavors of acquire, varying in exclusive/shared and
     * control modes.  Each is mostly the same, but annoyingly
     * different.  Only a little bit of factoring is possible due to
     * interactions of exception mechanics (including ensuring that we
     * cancel if tryAcquire throws exception) and other control, at
     * least not without hurting performance too much.
     */

    /**
     * Acquires in exclusive uninterruptible mode for thread already in
     * queue. Used by condition wait methods as well as acquire.
     *
     * @param node the node
     * @param arg the acquire argument
     * @return {@code true} if interrupted while waiting
     */
    final boolean acquireQueued(final Node node, int arg) {
        // acquireQueued作用： 所有没有获取到锁的node，进入同步队列后，接下来就需要进入以下死循环：
        // 具体包括 --
        // 1、若node是第二个节点，将有机会尝试获取锁；
        // 2、如果node获取锁失败 或者 node根本不是第二个节点即没有获取锁的资格时，检查是否需要去阻塞等待，
        //  2.1、如果是则阻塞等待直到前一个node释放唤醒，否则继续死循环
        // 3、如果特殊情况，进入finally块中，failed为true，即获取到锁失败但超时或者执行中断操作，接下来需要取消获取锁，即修改node状态
        boolean failed = true; // 是否争夺锁失败
        try {
            boolean interrupted = false; // 是否中断
            for (;;) {
                final Node p = node.predecessor(); // 获取前继者
                if (p == head && tryAcquire(arg)) { // 只有前继者为头结点才有资格去尝试获取锁
                    setHead(node); // 获取锁成功
                    p.next = null; // help GC
                    failed = false;
                    return interrupted; // 结束循环的唯一条件 -- 否则会在下面条件中进入阻塞状态，或者再次循环
                }
                // 若p为头节点且当前没有获取到锁（可能是非公平锁被抢占了）或者是p根本不为头结点，这个时候就要判断当前node是否需要等待阻塞（阻塞条件：前驱节点的waitStatus为-1），防止无限循环浪费资源。
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt()) // 如果需要阻塞，执行shouldParkAfterFailedAcquire阻塞起来，同时不确实是中断唤醒还是unPark()指定唤醒，检查线程的中断标志位即可
                    interrupted = true; // 需要注意的是，如果是parkAndCheckInterrupt()在阻塞后被中断唤醒，interrupted则设为true，但忽略中断唤醒，继续死循环
            }
        } finally {
            if (failed)
                // 只有在上述的return interrupted执行前进入该finally块中
                // 因此保证可见性的情况下：failed应该是一直为false
                cancelAcquire(node); // 将node设置为cancelled状态
        }
    }

    /**
     * Acquires in exclusive interruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireInterruptibly(int arg) throws InterruptedException {
        // 以独占可中断模式获取。
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) { // 死循环 + AQS
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    // acquireQueued
                    // 1、会将 interrupted 状态设置为true，而不是抛出异常
                    // 2、acquireQueued 方法返回是否中断标志，而 doAcquireInterruptibly 不会返回
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L) // 小于0直接返回false
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true; // 是否失败
        try {
            for (;;) { // 死循环
                final Node p = node.predecessor(); // 前驱节点
                if (p == head && tryAcquire(arg)) { // p 是头节点并且尝试获取锁
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L) // 超时返回false
                    return false;
                // 超时时间未到,且需要挂起
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout); // 阻塞当前线程直到超时时间到期
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                //相应中断
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared uninterruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED); // mode为Node.SHARED，创建node并且加入到等待队列队尾中
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) { // 前一个node是head，就有资格尝试获取共享锁[读锁]，
                    int r = tryAcquireShared(arg);
                    if (r >= 0) { // 获取共享锁成功
                        setHeadAndPropagate(node, r); // 读锁获取成功，执行setHeadAndPropagate，r大于等于0 --  与acquireQueued的不同点
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt(); // 如若线程是被中断唤醒后，获取到锁的，当前线程自我中断 --  与acquireQueued()的不同点，实际上是在acquire()中的执行的，区别不大
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                // 什么情况下会取消node -- 不懂
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared interruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    /*
                     * r < 0 的情况即获取共享锁失败的情况：
                     * 1、共享锁数量不足够
                     * 2、共享锁数量足够，但在非公平锁下竞争失败
                     */
                    if (r >= 0) {
                        setHeadAndPropagate(node, r); // 获取读锁成功后的操作
                        p.next = null; // help GC 帮助GC清理调头结点
                        failed = false;
                        return;
                    }
                }
                /*
                 * 其余线程：即
                 * 1、没有资格获取锁；
                 * 2、获取锁失败
                 * 需要检查是否需要阻塞，是则进行阻塞线程
                 */
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    /*
                    1、如果是中断唤醒，进入此方法，抛出异常，然后进入finally块
                    2、如果是unPark唤醒，会继续循环，尝试获取锁
                     */
                    throw new InterruptedException();
            }
        } finally {
            // 线程执行失败，需要取消当前node
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout; // 超过deadline即表示已过期
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime(); // 剩余过期时间
                if (nanosTimeout <= 0L)
                    return false; // 已经过期，返回false
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout); // 超时等待nanosTimeout的时间
                /*
                 * parkNanos() 此方法不报告导致该方法返回的原因。
                 * 调用者应该首先重新检查导致线程停止的条件。
                 * 可能被：unpark、interrupt、timeout、毫无原因的失败
                 */
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    // Main exported methods

    /**
     * Attempts to acquire in exclusive mode. This method should query
     * if the state of the object permits it to be acquired in the
     * exclusive mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread. This can be used
     * to implement method {@link Lock#tryLock()}.
     *
     * <p>The default
     * implementation throws {@link UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return {@code true} if successful. Upon success, this object has
     *         been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryAcquire(int arg) { // 获取写锁
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in exclusive
     * mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this object is now in a fully released
     *         state, so that any waiting threads may attempt to acquire;
     *         and {@code false} otherwise.
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryRelease(int arg) { // 释放写锁
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to acquire in shared mode. This method should query if
     * the state of the object permits it to be acquired in the shared
     * mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread.
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return a negative value on failure; zero if acquisition in shared
     *         mode succeeded but no subsequent shared-mode acquire can
     *         succeed; and a positive value if acquisition in shared
     *         mode succeeded and subsequent shared-mode acquires might
     *         also succeed, in which case a subsequent waiting thread
     *         must check availability. (Support for three different
     *         return values enables this method to be used in contexts
     *         where acquires only sometimes act exclusively.)  Upon
     *         success, this object has been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected int tryAcquireShared(int arg) { // 获取共享锁
        /**
         * 尝试在共享模式下获取锁。此方法应查询对象的状态是否允许在共享模式下获取该对象。
         * 如果允许，则获取该对象，执行acquire的线程总是调用此方法。
         * 如果此方法返回失败，acquire方法可能会将线程排队（如果尚未排队），直到其他线程发出释放信号。
         * 默认实现抛出UnsupportedOperationException。
         */
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this release of shared mode may permit a
     *         waiting acquire (shared or exclusive) to succeed; and
     *         {@code false} otherwise
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if synchronization is held exclusively with
     * respect to the current (calling) thread.  This method is invoked
     * upon each call to a non-waiting {@link ConditionObject} method.
     * (Waiting methods instead invoke {@link #release}.)
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}. This method is invoked
     * internally only within {@link ConditionObject} methods, so need
     * not be defined if conditions are not used.
     *
     * @return {@code true} if synchronization is held exclusively;
     *         {@code false} otherwise
     * @throws UnsupportedOperationException if conditions are not supported
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * Acquires in exclusive mode, ignoring interrupts.  Implemented
     * by invoking at least once {@link #tryAcquire},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquire} until success.  This method can be used
     * to implement method {@link Lock#lock}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     */
    public final void acquire(int arg) { //该方法被多个重入锁、CountDownLatch等多个类所使用 -- 由AQS锁实现的模板模式，具体的tryAcquire方法由
        /**
         * 以独占模式获取，忽略中断。
         * 通过调用至少一次tryAcquire来实现，并在成功后返回。否则线程将排队，可能会反复阻塞和取消阻塞，调用tryAcquire直到成功。
         */
        if (!tryAcquire(arg) && //tryAcquire由AQS的实现类完成，返回false表示加锁，则需要Node入等待队列
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) //如果，加锁失败 -- 创建Node、加入队尾（没有初始化时还需要帮助初始化），然后对已经入队的线程尝试获取锁
            selfInterrupt(); // 返回是否被中断
    }

    /**
     * Acquires in exclusive mode, aborting if interrupted.
     * Implemented by first checking interrupt status, then invoking
     * at least once {@link #tryAcquire}, returning on
     * success.  Otherwise the thread is queued, possibly repeatedly
     * blocking and unblocking, invoking {@link #tryAcquire}
     * until success or the thread is interrupted.  This method can be
     * used to implement method {@link Lock#lockInterruptibly}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    /**
     * acquireInterruptibly 相比于 acquire 方法，能够支持将中断抛出
     * acquire()即线程在阻塞起来后即使被中断唤醒，不会因为中断而抛出异常，她还是继续获取资源或者被挂起，也就是说不对中断进行响应，忽略中断，
     */
    public final void acquireInterruptibly(int arg) throws InterruptedException {
        // 首先检查中断状态，然后调用至少一次tryAcquire，并在成功后返回。否则线程将排队，可能会重复阻塞和取消阻塞，调用tryAcquire，直到成功或线程中断。但是会返回中断标识给上层用户
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg)) // 获取锁
            doAcquireInterruptibly(arg); // 获取锁失败 - 执行该方法
    }

    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.  This method can be used to implement
     * method {@link Lock#tryLock(long, TimeUnit)}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout) // 在有限的试讲中获取写锁
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * Releases in exclusive mode.  Implemented by unblocking one or
     * more threads if {@link #tryRelease} returns true.
     * This method can be used to implement method {@link Lock#unlock}.
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryRelease} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @return the value returned from {@link #tryRelease}
     */
    public final boolean release(int arg) { // 释放锁
        if (tryRelease(arg)) { // 释放锁成功
            Node h = head;
            /*
            如果 head 为空，则说明 CLH 队列为空，压根就不会有线程阻塞，故无需执行 unparkSuccessor(h),
            同样的道理，如果根节点的waitStatus=0，则说明压根就没有 head 后继节点判断是否要绑定的逻辑，故也没有线程被阻塞这一说。

             * 1、h == null Head还没初始化。初始情况下，head == null，第一个节点入队，Head会被初始化一个虚拟节点。所以说，这里如果还没来得及入队，就会出现head == null 的情况。
             * 2、h != null && waitStatus == 0 表明后继节点对应的线程仍在运行中，不需要唤醒。
             * 3、h != null && waitStatus < 0 表明后继节点可能被阻塞了，需要唤醒。
             */
            if (h != null && h.waitStatus != 0) // 头节点非空，并且头节点不是初始化状态 -- 此刻头结点含有特殊含义
                /*
                改进后的CLH，head如果不为空，该节点代表获取锁的那个线程对于的Node,
                请看获取锁代码acquireQueued中的代码处，如果获得锁，setHead(node);
                知道这一点，就不难理解为什么在释放锁时调用unparkSuccessor(h)时，参数为head了。
                 */
                unparkSuccessor(h);
            return true;
        }
        return false; //释放锁失败
    }

    /**
     * Acquires in shared mode, ignoring interrupts.  Implemented by
     * first invoking at least once {@link #tryAcquireShared},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquireShared} until success.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     */
    /**
     * 以共享模式获取，忽略中断即不会抛出中断异常
     * 首先调用至少一次tryAcquireShared来实现，成功返回。
     * 否则线程排队，可能重复阻塞和解除阻塞，调用tryAcquireShared直到成功。
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0) // tryAcquireShared 需要AQS的实现类进行重写 -- 实现共享加锁
            doAcquireShared(arg); // 由AQS来实现，加锁失败如何进入等待队列
    }

    /**
     * Acquires in shared mode, aborting if interrupted.  Implemented
     * by first checking interrupt status, then invoking at least once
     * {@link #tryAcquireShared}, returning on success.  Otherwise the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted.
     * @param arg the acquire argument.
     * This value is conveyed to {@link #tryAcquireShared} but is
     * otherwise uninterpreted and can represent anything
     * you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
        /*
         *  先判断当前线程是否执行过interrupt()方法，如果执行过，抛出中断异常
         *  如果没有执行不会抛出异常，并清空中断标识，保证中断标识的干净
         */
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * Attempts to acquire in shared mode, aborting if interrupted, and
     * failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquireShared}, returning on success.  Otherwise, the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted or the timeout elapses.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout) // 有限的时间中获取共享锁
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
                // 等待加锁，仅仅体现在加锁失败后，在队列中阻塞等待的过程
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * Releases in shared mode.  Implemented by unblocking one or more
     * threads if {@link #tryReleaseShared} returns true.
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryReleaseShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @return the value returned from {@link #tryReleaseShared}
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) { // 直接尝试释放锁
            doReleaseShared(); // 释放锁成功，唤醒后继线程
            return true;
        }
        return false; // 释放锁失败，也不会阻塞起来
    }

    // Queue inspection methods

    /**
     * Queries whether any threads are waiting to acquire. Note that
     * because cancellations due to interrupts and timeouts may occur
     * at any time, a {@code true} return does not guarantee that any
     * other thread will ever acquire.
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     *
     * @return {@code true} if there may be other threads waiting to acquire
     */
    public final boolean hasQueuedThreads() {
        /*
        查询是否有线程正在等待获取。请注意，由于中断和超时导致的取消可能随时发生，因此真正的返回并不保证任何其他线程都会获得。
        同步队列中是否有排队的线程
         */
        return head != tail;
    }

    /**
     * Queries whether any threads have ever contended to acquire this
     * synchronizer; that is if an acquire method has ever blocked.
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     *
     * @return {@code true} if there has ever been contention
     */
    public final boolean hasContended() {
        /*
        查询是否有线程曾争夺过该同步器；如果acquire方法曾经被阻塞block过。
         */
        return head != null;
    }

    /**
     * Returns the first (longest-waiting) thread in the queue, or
     * {@code null} if no threads are currently queued.
     *
     * <p>In this implementation, this operation normally returns in
     * constant time, but may iterate upon contention if other threads are
     * concurrently modifying the queue.
     *
     * @return the first (longest-waiting) thread in the queue, or
     *         {@code null} if no threads are currently queued
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * Returns true if the given thread is currently queued.
     *
     * <p>This implementation traverses the queue to determine
     * presence of the given thread.
     *
     * @param thread the thread
     * @return {@code true} if the given thread is on the queue
     * @throws NullPointerException if the thread is null
     */
    public final boolean isQueued(Thread thread) {
        /*
        检查线程是否在同步队列的某一个node中
         */
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    /**
     * Returns {@code true} if the apparent first queued thread, if one
     * exists, is waiting in exclusive mode.  If this method returns
     * {@code true}, and the current thread is attempting to acquire in
     * shared mode (that is, this method is invoked from {@link
     * #tryAcquireShared}) then it is guaranteed that the current thread
     * is not the first queued thread.  Used only as a heuristic in
     * ReentrantReadWriteLock.
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        /*
        apparentlyFirstQueuedIsExclusive 含义：显然第一个排队等待者是排他锁的
         */
        Node h, s;
        /*
         如果第一个排队线程（如果存在）以独占模式等待，则返回true

         * 该方法如果头节点不为空，并头节点的下一个节点不为空，并且不是共享模式【独占模式，写锁】、并且线程不为空。则返回true,
         * 说明有当前申请读锁的线程占有写锁，并且有其他写锁在申请。
         * 为什么要判断head节点的下一个节点不为空，或是thread不为空呢？
         * 因为第一个节点head节点是当前持有写锁的线程，也就是当前申请读锁的线程，这里，也就是锁降级（即持有写锁的线程申请读锁）的关键所在，
         * 如果占有的写锁不是当前线程，那线程申请读锁会直接失败。
         */
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
     * Queries whether any threads have been waiting to acquire longer
     * than the current thread.
     *
     * <p>An invocation of this method is equivalent to (but may be
     * more efficient than):
     *  <pre> {@code
     * getFirstQueuedThread() != Thread.currentThread() &&
     * hasQueuedThreads()}</pre>
     *
     * <p>Note that because cancellations due to interrupts and
     * timeouts may occur at any time, a {@code true} return does not
     * guarantee that some other thread will acquire before the current
     * thread.  Likewise, it is possible for another thread to win a
     * race to enqueue after this method has returned {@code false},
     * due to the queue being empty.
     *
     * <p>This method is designed to be used by a fair synchronizer to
     * avoid <a href="AbstractQueuedSynchronizer#barging">barging</a>.
     * Such a synchronizer's {@link #tryAcquire} method should return
     * {@code false}, and its {@link #tryAcquireShared} method should
     * return a negative value, if this method returns {@code true}
     * (unless this is a reentrant acquire).  For example, the {@code
     * tryAcquire} method for a fair, reentrant, exclusive mode
     * synchronizer might look like this:
     *
     *  <pre> {@code
     * protected boolean tryAcquire(int arg) {
     *   if (isHeldExclusively()) {
     *     // A reentrant acquire; increment hold count
     *     return true;
     *   } else if (hasQueuedPredecessors()) {
     *     return false;
     *   } else {
     *     // try to acquire normally
     *   }
     * }}</pre>
     *
     * @return {@code true} if there is a queued thread preceding the
     *         current thread, and {@code false} if the current thread
     *         is at the head of the queue or the queue is empty
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() { // hasQueuedPredecessors是公平锁加锁时判断等待队列中是否存在有效节点的方法。
        /*
         返回结果：
         如果返回False，说明同步队列中存在有效节点，当前线程可以争取共享资源；
         如果返回True，说明同步队列中不存在有效节点，当前线程必须加入到等待队列中 -- 公平锁的意义所在：只要等待队列有node，就必须直接进入队列
         */

        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        /**
         * 双向链表中，第一个节点为虚节点，其实并不存储任何信息，只是占位，真正的第一个有数据的节点，是在第二个节点开始的。
         * 当h != t时： 如果(s = h.next) == null，等待队列正在有线程进行初始化，但只是进行到了Tail指向Head，没有将Head指向Tail，此时队列中有元素，需要返回True（这块具体见下边代码分析）。
         * 如果(s = h.next) != null，说明此时队列中至少有一个有效节点。
         * 如果s.thread == Thread.currentThread()，说明等待队列的第一个有效节点中的线程与当前线程相同，那么当前线程是可以获取资源的；
         * 如果s.thread != Thread.currentThread()，说明等待队列的第一个有效节点线程与当前线程不同，当前线程必须加入进等待队列。
         */
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
        /**
         * 返回值的情况：
         * head 等于 tail -- 队列还未初始化，都是null，因此返回false，表示队列不存在有效节点，允许公平锁直接被当前线程获取
         * head 不等于 tail，但是 (s = h.next) == null ，等待队列正在线程初始化中，因此队列有元素，返回true，表示有有效节点
         * head 不等于 tail，且 (s = h.next) ！= null 并且非当前当前线程，返回true，表示队列中有有效节点
         * head 不等于 tail，且 (s = h.next) ！= null 并且为当前当前线程，返回false，表示队列中有有效节点就是自身
         */
    }


    // Instrumentation and monitoring methods

    /**
     * Returns an estimate of the number of threads waiting to
     * acquire.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring system state, not for synchronization
     * control.
     *
     * @return the estimated number of threads waiting to acquire
     */
    public final int getQueueLength() {
        /*
        获取同步队列中有效节点的长度
         */
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in exclusive mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to an exclusive acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a string identifying this synchronizer, as well as its state.
     * The state, in brackets, includes the String {@code "State ="}
     * followed by the current value of {@link #getState}, and either
     * {@code "nonempty"} or {@code "empty"} depending on whether the
     * queue is empty.
     *
     * @return a string identifying this synchronizer, as well as its state
     */
    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }


    // Internal support methods for Conditions

    /**
     * Returns true if a node, always one that was initially placed on
     * a condition queue, is now waiting to reacquire on sync queue.
     * @param node the node
     * @return true if is reacquiring
     */
    final boolean isOnSyncQueue(Node node) {
        /*
        返回值：是否重新获取进入sync队列
        如果一个node，始终是最初放置在Condition队列中的node，正在等待重新获取并进入sync队列，则返回true。
         */
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            /*
            1、CONDITION 的 node
            2、非CONDITION 的node但prev为空，await()中的addWaiter()方法没有给node添加prev指针
             */
            return false;
        if (node.next != null) // 由于只有一个线程会执行await()，node在条件队列时node一定会在队尾，其next为null，因此如果有next，那么它必定在队列中
            return true;
        /*
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         */
        return findNodeFromTail(node); // 如果node不是Condition状态，并且prev不等于null，next等于null，调用findNodeFromTail
    }

    /**
     * Returns true if node is on sync queue by searching backwards from tail.
     * Called only when needed by isOnSyncQueue.
     * @return true if present
     */
    private boolean findNodeFromTail(Node node) {
        /*
        如果节点位于sync队列上，则通过从尾部向后搜索到node时返回true。
        仅在isOnSyncQueue需要时调用。
         */
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * Transfers a node from a condition queue onto sync queue.
     * Returns true if successful.
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     */
    final boolean transferForSignal(Node node) {
        /*
        transfer 表示从Condition条件队列移入到sync同步队列中
        transferForSignal 表示由Sinnal()唤醒时的移动队列操作；
         */
        // 如果无法更改waitStatus，则节点已被Cancelled，返回false
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false; // 唤醒失败

        /*
         拼接到队列上，并尝试设置前置线程的waitStatus，以指示线程（可能）正在等待。如果取消或尝试设置waitStatus失败，请唤醒以重新同步（在这种情况下，waitStatus可能会短暂且无害地出错）。
         */
        Node p = enq(node); // 入同步队列的队尾，返回node在同步队列的前继者
        int ws = p.waitStatus;
        /*
         在同步队列中：
         如果node的前继者为Cancelled状态，就立即马上唤醒node的线程
         如果node的前继者非Cancelled状态，进行CAS将p的等待状态更换为SIGNAL -- 在后续的Sync同步队列中，会被唤醒，而非立即同步唤醒
         如果CAS更新失败，也立即马上尝试唤醒node的线程

         注意：唤醒并不等于就拥有了锁，而是会在sync队列中，逐步获取到锁
         */
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true; // 唤醒成功
    }

    /**
     * Transfers node, if necessary, to sync queue after a cancelled wait.
     * Returns true if thread was cancelled before being signalled.
     *
     * @param node the node
     * @return true if cancelled before the node was signalled
     */
    final boolean transferAfterCancelledWait(Node node) {
        /*
        transferAfterCancelledWait 表示由于超时或者中断，需要取消node的等待操作；
        transfer 表示从Condition条件队列移入到sync同步队列中

        返回true，表示接收到Signal()之前被中断的；
        返回false，表示接收到Signal()之后被中断的；
         */
        /*
        首先需要知道一点，如果收到正常的signal()信号而被唤醒的节点，状态变为Node.SINGAL,不会是Node.CONDITION状态，
        所以如果代码compareAndSetWaitStatus(node, Node.CONDITION, 0)设置成功，说明线程是调用signal之前，调用了t.interrupt方法而使得LockSupport.park解除阻塞的，
        然后将该节点加入到同步队列中，使得 await()方法中的 while(!isOnSyncQueue(node)) 的条件为真，结束await的等待条件触发语义，
        进入到 抢占锁阶段。
         */
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) { // 1、将条件队列中的node的状态社为Condition设为0，并加入到同步队列中，并返回true
            enq(node); // 进入Sync队列 -- 有获取锁的资格啦！！
            return true; // 返回true -- 中断
        }
        /*
         如果node不是CONDITION状态，就表示已经接受了signal()
         但如果接受了signal()后被中断，导致Signal()丢失, 那么在它完成入队列的enq()之前我们无法继续运行【因为await()中需要判断node是否在同步队列中，而退出唤醒】。所以只需旋转即可。
         */
        while (!isOnSyncQueue(node))
            Thread.yield(); // node是同步队列的node，但是有可能当前正在enq()，因此让步yield一会儿
        return false; // 同步队列中的node的唤醒，返回false
    }

    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     * @param node the condition node for this wait
     * @return previous sync state
     */
    final int fullyRelease(Node node) {
        /*
        1、 获取同步状态值state,释放相关资源,返回同步状态值
        2、 支持响应中断
         */
        boolean failed = true;
        try {
            int savedState = getState(); // 获取同步状态值
            if (release(savedState)) { // 释放相关资源
                failed = false;
                return savedState; // 返回同步状态值
            } else {
                throw new IllegalMonitorStateException(); // 释放相关资源失败，那么就很有可能是因为当前node并不是持有锁的线程，不可直接调用Condition.await方法
            }
        } finally {
            if (failed) // 中断或者超时引起的异常
                node.waitStatus = Node.CANCELLED;
        }
    }

    // Instrumentation methods for conditions

    /**
     * Queries whether the given ConditionObject
     * uses this synchronizer as its lock.
     *
     * @param condition the condition
     * @return {@code true} if owned
     * @throws NullPointerException if the condition is null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with this synchronizer. Note that because timeouts
     * and interrupts may occur at any time, a {@code true} return
     * does not guarantee that a future {@code signal} will awaken
     * any threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this synchronizer. Note that
     * because timeouts and interrupts may occur at any time, the
     * estimate serves only as an upper bound on the actual number of
     * waiters.  This method is designed for use in monitoring of the
     * system state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with this
     * synchronizer.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate. The elements of the
     * returned collection are in no particular order.
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * Condition implementation for a {@link
     * AbstractQueuedSynchronizer} serving as the basis of a {@link
     * Lock} implementation.
     *
     * <p>Method documentation for this class describes mechanics,
     * not behavioral specifications from the point of view of Lock
     * and Condition users. Exported versions of this class will in
     * general need to be accompanied by documentation describing
     * condition semantics that rely on those of the associated
     * {@code AbstractQueuedSynchronizer}.
     *
     * <p>This class is Serializable, but all fields are transient,
     * so deserialized conditions have no waiters.
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        /*
        作为锁实现基础的AbstractQueuedSynchronizer的【条件实现】。
        该类的方法文档从锁和条件用户的角度描述了机制，而不是行为规范。
        这个类是可序列化的，但所有字段都是transient的，所以反序列化的Conditions没有waiters。
         */
        private static final long serialVersionUID = 1173984872572414699L;
        /** First node of condition queue. */
        private transient Node firstWaiter; // 条件队列的第一个node
        /** Last node of condition queue. */
        private transient Node lastWaiter; // 条件队列的最后一个node

        /**
         * Creates a new {@code ConditionObject} instance.
         */
        public ConditionObject() { } // 创建ConditionObject实例

        // Internal methods

        /**
         * Adds a new waiter to wait queue.
         * @return its new wait node
         */
        private Node addConditionWaiter() {
            /*
             创建条件队列的Node，并加入到队尾中
             注意：由于使用条件变量await()的前提就是线程获取到lock对象，因此线程执行addConditionWaiter()时已经是单线程的那
             */
            Node t = lastWaiter;
            // If lastWaiter is cancelled, clean out.
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION); // 创建节点
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node; // 创建后的mode仅仅设置了next，并没有将node的prev指向前一个节点
            lastWaiter = node;
            return node;
        }

        /**
         * Removes and transfers nodes until hit non-cancelled one or
         * null. Split out from signal in part to encourage compilers
         * to inline the case of no waiters.
         * @param first (non-null) the first node on condition queue
         */
        private void doSignal(Node first) {
            /*
            唤醒指定的node
             */
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;  // 整个条件队列只有first这一个节点时，lastWaiter等于null
                first.nextWaiter = null; // help GC
            } while (!transferForSignal(first) && // transferForSignal 实际的唤醒操作
                     (first = firstWaiter) != null); // 将结点从condition队列转移到sync队列失败，并且condition队列中的头结点不为空，一直循环
        }

        /**
         * Removes and transfers all nodes.
         * @param first (non-null) the first node on condition queue
         */
        private void doSignalAll(Node first) {
            /*
            唤醒所有的node
             */
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * Unlinks cancelled waiter nodes from condition queue.
         * Called only while holding lock. This is called when
         * cancellation occurred during condition wait, and upon
         * insertion of a new waiter when lastWaiter is seen to have
         * been cancelled. This method is needed to avoid garbage
         * retention in the absence of signals. So even though it may
         * require a full traversal, it comes into play only when
         * timeouts or cancellations occur in the absence of
         * signals. It traverses all nodes rather than stopping at a
         * particular target to unlink all pointers to garbage nodes
         * without requiring many re-traversals during cancellation
         * storms.
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null; // 辅助节点：用于遍历过程中，作为在遍历的所有已知节点的最后一个有效节点（即非Condition节点）
            while (t != null) { // 从条件队列队头遍历到队尾
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) { // 只要非CONDITION状态的节点，就需要从条件队列中移除
                    t.nextWaiter = null; // help GC
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        // public methods

        /**
         * Moves the longest-waiting thread, if one exists, from the
         * wait queue for this condition to the wait queue for the
         * owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException(); // 持有排它锁的线程非当前线程，不允许执行Condition.signal()，抛出异常
            Node first = firstWaiter;
            if (first != null)
                doSignal(first); // 条件队列不为空 -- 尝试唤醒等待队列的第一个node
        }

        /**
         * Moves all threads from the wait queue for this condition to
         * the wait queue for the owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         * Implements uninterruptible condition wait.
         * <ol>
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true; // 若被中断，则将中断标志位设为true
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt(); // 不难得出：如果线程在调用condition的wait等待期间被中断，或者在Signal唤醒后，在acquireQueued()获取锁的过程中被中断 -- 当前线程自我中断
        }

        /*
         对于可中断的等待，我们需要跟踪是否抛出InterruptedException（如果在条件阻塞时中断），而不是重新中断当前线程（如果在阻塞等待重新获取时中断）。
         * For interruptible waits, we need to track whether to throw
         * InterruptedException, if interrupted while blocked on
         * condition, versus reinterrupt current thread, if
         * interrupted while blocked waiting to re-acquire.
         */

        /** Mode meaning to reinterrupt on exit from wait */
        private static final int REINTERRUPT =  1; // 该模式意味着退出wait时重新执行中断
        /** Mode meaning to throw InterruptedException on exit from wait */
        private static final int THROW_IE    = -1; // 该模式意味着退出wait时抛出InterruptedException

        /**
         * Checks for interrupt, returning THROW_IE if interrupted
         * before signalled, REINTERRUPT if after signalled, or
         * 0 if not interrupted.
         */
        private int checkInterruptWhileWaiting(Node node) { // 检查等待期间的中断情况
            /*
            非中断返回 0 ；同步队列返回 REINTERRUPT，条件队列返回 THROW_IE
            线程是否被中断：
                Y：调用 transferAfterCancelledWait(node) -- 如果在发出Signal之前中断，返回THROW_IE，如果在发出Signal之后中断，返回REINTERRUPT，
                    Y: THROW_IE 即 1
                    N: REINTERRUPT 即 -1
                N：直接返回 0
             */
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        /**
         * Throws InterruptedException, reinterrupts current thread, or
         * does nothing, depending on mode.
         */
        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException(); // 抛出异常
            else if (interruptMode == REINTERRUPT)
                selfInterrupt(); // 重新执行中断
        }

        /**
         * Implements interruptible condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled or interrupted.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final void await() throws InterruptedException { // 阻塞操作
            /*
            作用：该方法调用必须在临界区中（锁保护的代码段）被调用，线程如果在临界区中调用监视器的wait方法，然后线程会释放占有监视器monitorObject的锁，
            然后阻塞（等待条件的发生，该线程会保存在monitorObject的条件队列，当该线程收到信号或中断被唤醒后，
            首先需要尝试获取监视器的锁，然后继续执行操作，如果是被中断，需要在获取锁后，才会被中断。）

            1、如果当前线程被中断，抛出InterruptedException。
            2、创建node加入条件队列队尾，如果加入队尾前，队尾是cancelled的node，执行unlinkCancelledWaiters进行清除
            3、以node作为参数调用fullyRelease释放该node持有锁，如果失败，则抛出IllegalMonitorStateException，否则返回savedState
            4、释放锁后进入阻塞，直到接受到Signal()唤醒或被中断唤醒，唤醒后检查是否为中断唤醒的，是中断唤醒直接break，否则继续检查node是否已经进入sync同步队列
            5、通过调用acquireQueued将保存的状态savedState作为参数来重新尝试获取锁并继续执行，acquireQueued(node, savedState)返回获取锁的期间是否被中断
            6、执行一些收尾操作，清理整个条件队列，如果在步骤4中被阻止时被中断，则抛出InterruptedException。
             */
            if (Thread.interrupted())
                throw new InterruptedException(); // 检测当前线程的中断标记，如果中断位为1，则抛出异常。
            Node node = addConditionWaiter(); // 创建条件队列的Waiter，并添加到条件队列队尾
            int savedState = fullyRelease(node); // 释放当前node持有的锁，并返回当前node持有的同步状态值savedState，如果队尾已经是canceled的node，还会执行unlinkCancelledWaiters
            // 注意：执行到该步骤时，只能由当前持有锁的线程的创建的condition执行await方法，因为其余线程在执行fullyRelease(node)会抛出IllegalMonitorStateException异常
            int interruptMode = 0; // 初始化表示为非中断模式
            /*
            isOnSyncQueue判断node是否在同步队列，如果返回false，表示不在同步队列，则进入循环体阻塞起来
            */
            while (!isOnSyncQueue(node)) { // 在Signal()中会将当前node加入到sync同步队列中，使得isOnSyncQueue(node)返回true
                LockSupport.park(this); // 在Signal()中会可能会立即唤醒线程，也有可能仅仅将node在sync队列的前一个node设置为Signal，靠其他唤醒的线程执行acquireQueued(node, savedState)
                /*
                线程从条件等待队列被唤醒后，线程要从条件队列移除，进入到同步等待队列，被唤醒有有如下两种情况，
                一是条件满足，收到signal信号，
                二是线程被取消（中断）
                    如果是被中断，需要根据不同模式，处理中断。处理中断也有两种方式：1.继续设置中断位；2：直接抛出InterruptedException。
                */
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) // checkInterruptWhileWaiting(node)在条件队列中，返回0表示非中断，否则返回THROW_IE表示中断时的操作，
                    break; // 如果线程是被中断唤醒的，那么会直接break
            }
            /*
             运行到此处：说明线程已经结束了释放锁，已从条件队列移除，线程继续运行，在继续执行业务逻辑之前，
             必须先获取锁。【只有成功获取锁后，才会去判断线程的中断标志】，才能在中断标志为真时，抛出InterruptException。

              acquireQueued(node, savedState) 表示node在同步队列中尝试去自旋获取锁，返回中断标志位，

              interruptMode != THROW_IE 可能表示非中断，或者node在接收到Signal()后被中断
              因此：若node在同步队列中尝试获取锁期间被中断过，并且node非中断或者在接收到Signal()后被中断，就会将 interruptMode = REINTERRUPT；
            */
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters(); // 如果node的next是不等于null，执行一些收尾工作，清理整个条件队列
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode); // 处理 acquireQueued(node, savedState) 返回的中断标志位，是继续设置中断位，还是抛出InterruptException。
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout; // 到期时间 deadline
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) { // 死循环
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node); // 由于超时需要将node从条件队列移入同步队列，其返回值不重要
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout); // 定时阻塞 nanosTimeout 的时间
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime(); // 到期时间 deadline 减去 当前系统时间
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime(); // 返回剩余时间
        }

        /**
         * Implements absolute timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout; // 到期时间 deadline
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node); // 由于超时，执行取消操作，将node从条件队列移动到同步队列，返回true表示因超时移动到同步队列成功，否则就是已在sync同步队列中
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout); // 大于1000L，才认为有价值进行 超时等待操作
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime(); // 剩余的 nanosTimeout 超时时间
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  support for instrumentation

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * Queries whether any threads are waiting on this condition.
         * Implements {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
         *
         * @return {@code true} if there are any waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * Returns an estimate of the number of threads waiting on
         * this condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}.
         *
         * @return the estimated number of waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * Returns a collection containing those threads that may be
         * waiting on this Condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}.
         *
         * @return the collection of threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    /**
     * Setup to support compareAndSet. We need to natively implement
     * this here: For the sake of permitting future enhancements, we
     * cannot explicitly subclass AtomicInteger, which would be
     * efficient and useful otherwise. So, as the lesser of evils, we
     * natively implement using hotspot intrinsics API. And while we
     * are at it, we do the same for other CASable fields (which could
     * otherwise be done with atomic field updaters).
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * CAS head field. Used only by enq.
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail field. Used only by enq.
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS waitStatus field of a node.
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS next field of a node.
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
