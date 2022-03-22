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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * An unbounded {@link TransferQueue} based on linked nodes.
 * This queue orders elements FIFO (first-in-first-out) with respect
 * to any given producer.  The <em>head</em> of the queue is that
 * element that has been on the queue the longest time for some
 * producer.  The <em>tail</em> of the queue is that element that has
 * been on the queue the shortest time for some producer.
 *
 * <p>Beware that, unlike in most collections, the {@code size} method
 * is <em>NOT</em> a constant-time operation. Because of the
 * asynchronous nature of these queues, determining the current number
 * of elements requires a traversal of the elements, and so may report
 * inaccurate results if this collection is modified during traversal.
 * Additionally, the bulk operations {@code addAll},
 * {@code removeAll}, {@code retainAll}, {@code containsAll},
 * {@code equals}, and {@code toArray} are <em>not</em> guaranteed
 * to be performed atomically. For example, an iterator operating
 * concurrently with an {@code addAll} operation might view only some
 * of the added elements.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 *
 * <p>Memory consistency effects: As with other concurrent
 * collections, actions in a thread prior to placing an object into a
 * {@code LinkedTransferQueue}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions subsequent to the access or removal of that element from
 * the {@code LinkedTransferQueue} in another thread.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.7
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public class LinkedTransferQueue<E> extends AbstractQueue<E>
    implements TransferQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;
    /*
     * LinkedTransferQueue采用一种预占模式。意思就是消费者线程取元素时，如果队列不为空，则直接取走数据，若队列为空，那就生成一个节点
     * （节点元素为null）入队，然后消费者线程被等待在这个节点上，后面生产者线程入队时发现有一个元素为null的节点，生产者线程就不入队了，
     * 直接就将元素填充到该节点，并唤醒该节点等待的线程，被唤醒的消费者线程取走元素，从调用的方法返回。我们称这种节点操作为“匹配”方式。
     *
     1. transfer(E e)：若当前存在一个正在等待获取的消费者线程，即立刻移交之；否则，会插入当前元素e到队列尾部，并且等待进入阻塞状态，到有消费者线程取走该元素。
     2. tryTransfer(E e)：若当前存在一个正在等待获取的消费者线程（使用take()或者poll()函数），使用该方法会即刻转移/传输对象元素e；
                          若不存在，则返回false，并且元素不进入队列。这是一个不阻塞的操作。
     3. tryTransfer(E e, long timeout, TimeUnit unit)：若当前存在一个正在等待获取的消费者线程，会立即传输给它;否则将插入元素e到队列尾部，并且等待被消费者线程获取消费掉；
                                                       若在指定的时间内元素e无法被消费者线程获取，则返回false，同时该元素被移除。
     4. hasWaitingConsumer()：判断是否存在消费者线程。
     5. getWaitingConsumerCount()：获取所有等待获取元素的消费线程数量。
     6.size()：因为队列的异步特性，检测当前队列的元素个数需要逐一迭代，可能会得到一个不太准确的结果，尤其是在遍历时有可能队列发生更改。
     7.批量操作：类似于addAll，removeAll, retainAll, containsAll, equals, toArray等方法，API不能保证一定会立刻执行。因此，我们在使用过程中，不能有所期待，这是一个具有异步特性的队列。
     */

    /** True if on multiprocessor */
    private static final boolean MP =
        Runtime.getRuntime().availableProcessors() > 1; // 是否为多CPU机器

    /**
     * The number of times to spin (with randomly interspersed calls
     * to Thread.yield) on multiprocessor before blocking when a node
     * is apparently the first waiter in the queue.  See above for
     * explanation. Must be a power of two. The value is empirically
     * derived -- it works pretty well across a variety of processors,
     * numbers of CPUs, and OSes.
     */
    private static final int FRONT_SPINS   = 1 << 7; // 作为第一个等待节点在阻塞之前的自旋次数

    /**
     * The number of times to spin before blocking when a node is
     * preceded by another node that is apparently spinning.  Also
     * serves as an increment to FRONT_SPINS on phase changes, and as
     * base average frequency for yielding during spins. Must be a
     * power of two.
     */
    private static final int CHAINED_SPINS = FRONT_SPINS >>> 1; // 前驱节点正在处理，当前节点在阻塞之前的自旋次数

    /**
     * The maximum number of estimated removal failures (sweepVotes)
     * to tolerate before sweeping through the queue unlinking
     * cancelled nodes that were not unlinked upon initial
     * removal. See above for explanation. The value must be at least
     * two to avoid useless sweeps when removing trailing nodes.
     */
    static final int SWEEP_THRESHOLD = 32;// 断开被删除节点失败的次数

    /**
     * Queue nodes. Uses Object, not E, for items to allow forgetting
     * them after use.  Relies heavily on Unsafe mechanics to minimize
     * unnecessary ordering constraints: Writes that are intrinsically
     * ordered wrt other accesses or CASes use simple relaxed forms.
     */
    static final class Node {
        /*
         * 数据节点【生产者】，匹配前item不为null且不为自身，匹配后设置为null。
         * 占位请求节点【消费者】，匹配前item为null，匹配后自连接。
         */
        final boolean isData;   // 是否为数据节点，是则表示为一个生产者，否则就是消费者
        volatile Object item;   // 值
        volatile Node next;     // 下一个node
        volatile Thread waiter; // 一直为null直到进入阻塞后

        // CAS methods for fields
        final boolean casNext(Node cmp, Node val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        final boolean casItem(Object cmp, Object val) {
            // assert cmp == null || cmp.getClass() != Node.class;
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        /**
         * Constructs a new node.  Uses relaxed write because item can
         * only be seen after publication via casNext.
         */
        Node(Object item, boolean isData) {
            UNSAFE.putObject(this, itemOffset, item); // 放松读 - 不需要Volatile读，或者order读
            this.isData = isData;
        }

        /**
         * Links node to itself to avoid garbage retention.  Called
         * only after CASing head field, so uses relaxed write.
         */
        final void forgetNext() {
            UNSAFE.putObject(this, nextOffset, this); // next设置为自身，帮助help GC
        }

        /**
         * Sets item to self and waiter to null, to avoid garbage
         * retention after matching or cancelling. Uses relaxed writes
         * because order is already constrained in the only calling
         * contexts: item is forgotten only after volatile/atomic
         * mechanics that extract items.  Similarly, clearing waiter
         * follows either CAS or return from park (if ever parked;
         * else we don't care).
         */
        final void forgetContents() {
            // item设置为自己，waiter设置为null
            UNSAFE.putObject(this, itemOffset, this);
            UNSAFE.putObject(this, waiterOffset, null);
        }

        /**
         * Returns true if this node has been matched, including the
         * case of artificial matches due to cancellation.
         */
        final boolean isMatched() {
            // 节点是否被匹配过了
            // node.item为自身，只有消费者线程即占位请求节点被匹配后，才有这个情况
            // node.item为空且为生产者线程 -- 初始的生产者Node有item，被消费后，item为null就是已被匹配
            // node.item不为空且为消费者线程 -- 初始的消费者Node没有item，获取任务后，item不为null就是已被匹配
            Object x = item;
            return (x == this) || ((x == null) == isData);
        }

        /**
         * Returns true if this is an unmatched request node.
         */
        final boolean isUnmatchedRequest() {
            // 是否是一个未匹配的请求节点
            // 如果是的话，则isData为false，且item为null，因为如果被匹配过了，item就不再为null，而是指向自己
            return !isData && item == null;
        }

        /**
         * Returns true if a node with the given mode cannot be
         * appended to this node because this node is unmatched and
         * has opposite data mode.
         */
        final boolean cannotPrecede(boolean haveData) {
            // 如果给定节点不能连接在当前节点后则返回true
            boolean d = isData;
            Object x;
            /*  (item != null) == isData  一定要明白这个判断：
             * 1、isData为true，item非null，表示数据节点，且数据item没有被消费，认为是还没匹配的
             * 2、isData为false，item为null，表示预占位节点，且数据item仍为null，认为是还没匹配的
             */
            // 返回true的情况：
            // 1、node为数据节点时，havaDate为false，且node不是已被删除的节点，node是未匹配的节点，即x还存在
            // 1、node为预占位节点时，havaDate为true，且node不是已被删除的节点，node是未匹配的节点，即x不为null
            return d != haveData && (x = item) != this && (x != null) == d;
        }

        /**
         * Tries to artificially match a data node -- used by remove.
         */
        final boolean tryMatchData() {
            // 匹配一个数据节点
            Object x = item;
            if (x != null && x != this && casItem(x, null)) {
                LockSupport.unpark(waiter);
                return true;
            }
            return false;
        }

        private static final long serialVersionUID = -3375979862319811754L;

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;
        private static final long waiterOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
                waiterOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiter"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** head of the queue; null until first enqueue */
    transient volatile Node head; // 消费者线程阻塞链表的头结点，只有有节点入队列唤醒

    /** tail of the queue; null until first append */
    private transient volatile Node tail;

    /** The number of apparent failures to unsplice removed nodes */
    private transient volatile int sweepVotes; // The number of apparent failures to unsplice removed nodes

    // CAS methods for fields
    private boolean casTail(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casSweepVotes(int cmp, int val) {
        return UNSAFE.compareAndSwapInt(this, sweepVotesOffset, cmp, val);
    }

    /*
     * xfer方法的how参数的可能取值
     */
    private static final int NOW   = 0; // for untimed poll, tryTransfer
    private static final int ASYNC = 1; // for offer, put, add
    private static final int SYNC  = 2; // for transfer, take
    private static final int TIMED = 3; // for timed poll, tryTransfer

    @SuppressWarnings("unchecked")
    static <E> E cast(Object item) {
        // assert item == null || item.getClass() != Node.class;
        return (E) item;
    }

    /**
     * 实现所有排队方法。请参见上面的解释。
     *
     * @param e 排队的值
     * @param haveData true表示入队操作，如put、offer、add，否则就是take、poll
     * @param how NOW, ASYNC, SYNC, or TIMED
     * @param nanos 超时时间，仅仅在TIMED情况下被使用
     * @return an item if matched, else e
     * @throws NullPointerException if haveData mode but e is null
     */
    private E xfer(E e, boolean haveData, int how, long nanos) {
        /*
         *  一些情况介绍：
         * 1、空队列，调用add、offer、put，进入⑦(4),
         *    队列只有未匹配的生产者Node，调用aaa/offer/put，进入③跳出循环，进入(4)追加节点
         *    队列既有未匹配的生产者NOoe，也有已匹配的Node，调用aaa/offer/put，会在②判断出未匹配节点，从而在(2)(3)找出下一个遍历的节点p
         *
         * 2、队列头只有一个未匹配的消费者Node，调用add、offer、put，进入到④做匹配操作，将插入值e直接交给消费者Node，并唤醒对方
         *    队列头有两个未匹配的消费者Node，两个线程调用add、offer、put，第一个线程在④做匹配操作，将插入值e交给消费Node，但不会进入(1)
         *        第二个线程调用add、offer、put，第一个节点已经被匹配了，经过(2)(3)修改p值，下一次循环进入到④进行匹配，然后既然进入到(1)⑤中，
         *        将当前匹配节点的第二个作为Head，第一个已经匹配节点next指向自己做逻辑删除，
         * 4、空队列或者队列中全是消费者节点，调用take/poll，在①中，无法查找到一个未匹配的生产者节点，进入⑦添加消费者节点到队尾，然后进入⑨阻塞等待
         *
         * 1、寻找和操作匹配的节点
         * 从head开始向后遍历寻找未被匹配的节点，找到一个未被匹配并且和本次操作的模式不同的节点，匹配节点成功就通过CAS 操作将匹配节点的item字段设置为e，若修改失败，则继续向后寻找节点。
         * 通过CAS操作更新head节点为匹配节点的next节点，旧head节点进行自连接，唤醒匹配节点的等待线程waiter，返回匹配的 item。如果CAS失败，并且松弛度大于等于2，就需要重新获取head重试。
         * 2、如果在上述操作中没有找到匹配节点，则根据参数how做不同的处理：
         * NOW：立即返回，也不会插入节点
         * SYNC：插入一个item为e（isData = haveData）到队列的尾部，然后自旋或阻塞当前线程直到节点被匹配或者取消。
         * ASYNC：插入一个item为e（isData = haveData）到队列的尾部，不阻塞直接返回。
         * TIMED：插入一个item为e（isData = haveData）到队列的尾部，然后自旋或阻塞当前线程直到节点被匹配或者取消或者超时。
         *
         * 松弛度：在节点被匹配（被删除）之后，不会立即更新head/tail，而是当 head/tail 节点和最近一个未匹配的节点之间的距离超过一个“松弛阀值”之后才会更新（
         * 在LinkedTransferQueue中，这个值为 2）。这个“松弛阀值”一般为1-3，如果太大会降低缓存命中率，并且会增加遍历链的长度；太小会增加 CAS 的开销。
         */
        // 检查：若为数据节点，若值e为null抛出异常
        if (haveData && (e == null))
            throw new NullPointerException();
        Node s = null;                        // 要追加的节点（如果需要）

        retry:
        for (;;) {                            // 在附加竞争中重新启动
            // ① 从首节点开始匹配，p为当前遍历的node，isData为当前遍历节点类型，item为当前节点的值
            for (Node h = head, p = h; p != null;) { // find & match first node
                boolean isData = p.isData;
                Object item = p.item;
                // ② 判断节点是否被逻辑删除即item德育自身，是否被匹配过
                // item != null有2种情况：一是生产者put产生的元素还没被消费，二是消费者take操作的item从null被修改了(匹配成功)
                // (item != null) == isData 表示p是一个put操作，要么表示p是一个还没匹配成功的take操作
                if (item != p && (item != null) == isData) { // 不匹配 - 即未匹配的生产者或者消费者
                    // ③ 节点与此次操作模式一致，无法匹配
                    if (isData == haveData)   // can't match
                        break;
                    // ④ 否则就是匹配成功
                    if (p.casItem(item, e)) { // match
                        // (1) 循环 -- 条件当前遍历的节点p不等于头结点，因此需要线程通过②(2)改变节点p，则要求之前的节点时已匹配的
                        // 那么这个时候就允许就进入以下循环体，帮助逻辑前面的已匹配的删除节点
                        for (Node q = p; q != h;) {
                            Node n = q.next;  // update by 2 unless singleton -- 延迟删除节点，
                            // ⑤ 更新head为匹配节点，或者匹配节点next节点
                            if (head == h && casHead(h, n == null ? q : n)) {
                                // 将旧节点自连接
                                h.forgetNext();
                                break;
                            }                 // advance and retry
                            // ⑥
                            if ((h = head)   == null ||
                                (q = h.next) == null || !q.isMatched())
                                break;        // unless slack < 2
                        }
                        // 匹配成功，则唤醒阻塞的线程
                        LockSupport.unpark(p.waiter);
                        // 类型转换，返回被匹配节点的元素 --
                        // 若haveData为true返回则就是匹配消费者的值即null
                        // 若haveData为false返回的就是匹配生产者的值item
                        return LinkedTransferQueue.<E>cast(item);
                    }
                }
                // (2) 若节点已经被匹配过了，则向后寻找下一个未被匹配的节点
                Node n = p.next;
                // (3) 如果当前节点已经离队，则从head开始寻找，节点的next就是节点自身那么当前节点已被删除
                // 从⑤可以看出，forgetNext()逻辑删除只会从链表头的已匹配的节点开始，因此在线程执行到这个期间，
                // 若有线程已经将已匹配的队头清空就会产生问题，即h持有的head无效，需要重新赋值
                p = (p != n) ? n : (h = head);
            }
            // ⑦ 若整个队列都遍历之后，还没有找到匹配的节点，则进行后续处理
            // 生产操作方法put\add\offer -- ASYNC，创建节点，但不阻塞等待
            // 生产操作方法tryTransfer，与消费操作poll -- NOW，不创建任何节点
            // 消费操作take -- SYNC，创建同步等待节点
            // 消费超时poll超时，或生产超时的tryTransfer -- TIMED，创建超时等待节点
            // 把当前节点加入到队列尾
            if (how != NOW) {                 // No matches available
                if (s == null)
                    s = new Node(e, haveData);
                //  (4) 将新节点s添加到队列尾并返回s的前驱节点
                Node pred = tryAppend(s, haveData);

                // ⑧ 前驱节点为null，说明有其他线程竞争，并修改了队列，则从retry重新开始
                if (pred == null)
                    continue retry;           // lost race vs opposite mode
                // ⑨ 不为ASYNC方法，则同步阻塞等待 -- 因此take()和超时poll()会超时等待
                if (how != ASYNC)
                    return awaitMatch(s, pred, e, (how == TIMED), nanos);
            }
            // ⑩ how == NOW，则立即返回
            return e; // not waiting
        }
    }

    /**
     * Tries to append node s as tail.
     *
     * @param s the node to append
     * @param haveData true if appending in data mode
     * @return null on failure due to losing race with append in
     * different mode, else s's predecessor, or s itself if no
     * predecessor
     */
    private Node tryAppend(Node s, boolean haveData) {

        /*
         * 添加节点s到队列尾并返回s的前继节点，失败时（与其他不同模式线程竞争失败）返回null，没有前继节点则返回自身。
         * 情况：假设为添加数据节点，假设tail永不和插入的node冲突，即不进入代码块②
         * 1、无竞争，单线程追加，进入⑤，循环再进入⑦，
         * 2、若两个线程同时竞争，同时进入⑤，
         *          一个线程进入CAS成功后，循环进入⑦，不满足松弛度大于2
         *          一个线程进入CAS失败后，循环进入⑤，再循环进入⑥，满足松弛度大于等于2，更新t
         * 3、
         */

        // ① 从尾节点开始
        for (Node t = tail, p = t;;) {        // move p to last node and append
            Node n, u;                        // temps for reads of next & tail
            // ② tail以及head都为空，需要尝试初始化
            if (p == null && (p = head) == null) {
                if (casHead(null, s))
                    return s;
            }
            // ③ p如果不支持在后面插入havaData，就直接返回
            // haveData为true，若前一个为未匹配的消费节点，不允许附加到tail的next上
            // haveData为false，若前一个为未匹配的生产节点，不允许附加到tail的next上
            else if (p.cannotPrecede(haveData))
                return null;                  // lost race vs opposite mode
            // ④ 多线程操作，p之前作为tail节点，若p.next不为空，说明有线程已经执行过⑤
            else if ((n = p.next) != null)    // not last; keep traversing
                p = p != t && t != (u = tail) ? (t = u) : // stale tail
                    (p != n) ? n : null;      // restart if off list
            // ⑤ 将s作为tail尾结点的next
            else if (!p.casNext(null, s))
                p = p.next;                   // CAS失败重新加载尾结点
            else {
                // ⑥ 如果现在松弛>=2，则更新
                if (p != t) {
                    while ((tail != t || !casTail(t, s)) &&
                           (t = tail)   != null &&
                           (s = t.next) != null && // 提前重试
                           (s = s.next) != null && s != t);
                }
                return p; // ⑦
            }
        }
    }

    /**
     * Spins/yields/blocks until node s is matched or caller gives up.
     *
     * @param s the waiting node
     * @param pred the predecessor of s, or s itself if it has no
     * predecessor, or null if unknown (the null case does not occur
     * in any current calls but may in possible future extensions)
     * @param e the comparison value for checking match
     * @param timed if true, wait only until timeout elapses
     * @param nanos timeout in nanosecs, used only if timed is true
     * @return matched item, or e if unmatched on interrupt or timeout
     */
    private E awaitMatch(Node s, Node pred, E e, boolean timed, long nanos) {
        final long deadline = timed ? System.nanoTime() + nanos : 0L; // 计算超时时间点
        Thread w = Thread.currentThread(); // 获取当前线程对象
        int spins = -1; // 自旋次数
        ThreadLocalRandom randomYields = null; // 随机数
        // take()\超时poll()产生的消费者节点会进入阻塞等待
        for (;;) {
            Object item = s.item;
            // 节点s的item在这期间已经改变，不等于e，说明node已经被匹配啦
            // tale\poll，e为null，s被匹配后item为非null
            if (item != e) {                  // matched
                // assert item != s;
                s.forgetContents();           // 防止产生垃圾
                return LinkedTransferQueue.<E>cast(item);
            }
            // 当前线程已被中断，或者已经超时，则CAS更新s.item=s帮助GC
            if ((w.isInterrupted() || (timed && nanos <= 0)) &&
                    s.casItem(e, s)) {        // cancel
                unsplice(pred, s);
                return e;
            }
            // 自旋次数小于0，还未初始化，调用spinsFor()初始化spins为一个正数
            if (spins < 0) {                  // establish spins at/near front
                if ((spins = spinsFor(pred, s.isData)) > 0)
                    randomYields = ThreadLocalRandom.current();
            }
            // 自旋次数大于0，已初始化 -- 绝大多数的线程的node都会在这里面做自旋操作，
            else if (spins > 0) {             // spin
                --spins; // spins自旋直到为0，才会有机会进入下面的超时阻塞等待和永久阻塞中
                if (randomYields.nextInt(CHAINED_SPINS) == 0)
                    Thread.yield();           // 偶尔yield让步
            }
            // 初始化的node进入awaitMatch()后，waiter都为空，将其设置为当前线程
            else if (s.waiter == null) {
                s.waiter = w;                 // request unpark then recheck
            }
            // 超时阻塞等待
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos > 0L)
                    LockSupport.parkNanos(this, nanos);
            }
            // 永久阻塞
            else {
                LockSupport.park(this);
            }
        }
    }

    /**
     * Returns spin/yield value for a node with given predecessor and
     * data mode. See above for explanation.
     */
    private static int spinsFor(Node pred, boolean haveData) {
        /*
         * 返回具有给定前置节点pred和数据模式haveData的节点的spin/yield值。请参见上面的解释。
         */
        // 多核CPU，且pred不为空，
        if (MP && pred != null) {
            if (pred.isData != haveData)      // 不是同一种类型的node，1 << 7 + FRONT_SPINS >>> 1
                return FRONT_SPINS + CHAINED_SPINS;
            if (pred.isMatched())             // 同一种类型的node，且前节点已经匹配过，1 << 7
                return FRONT_SPINS;
            if (pred.waiter == null)          // 同一种类型的node，前节点也没有匹配过，前节点没有waiter，自旋次数 1 << 6
                return CHAINED_SPINS;
        }
        return 0;
    }

    /* -------------- Traversal methods -------------- */

    /**
     * Returns the successor of p, or the head node if p.next has been
     * linked to self, which will only be true if traversing with a
     * stale pointer that is now off the list.
     */
    final Node succ(Node p) {
        Node next = p.next;
        return (p == next) ? head : next;
    }

    /**
     * Returns the first unmatched node of the given mode, or null if
     * none.  Used by methods isEmpty, hasWaitingConsumer.
     */
    private Node firstOfMode(boolean isData) {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return (p.isData == isData) ? p : null;
        }
        return null;
    }

    /**
     * Version of firstOfMode used by Spliterator. Callers must
     * recheck if the returned node's item field is null or
     * self-linked before using.
     */
    final Node firstDataNode() {
        for (Node p = head; p != null;) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return p;
            }
            else if (item == null)
                break;
            if (p == (p = p.next))
                p = head;
        }
        return null;
    }

    /**
     * Returns the item in the first unmatched node with isData; or
     * null if none.  Used by peek.
     */
    private E firstDataItem() {
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p) // 队头是未匹配未逻辑删除的，就可移除
                    return LinkedTransferQueue.<E>cast(item);
            }
            else if (item == null) // 队头不是生产者Node，返回null
                return null;
        }
        return null;
    }

    /**
     * Traverses and counts unmatched nodes of the given mode.
     * Used by methods size and getWaitingConsumerCount.
     */
    private int countOfMode(boolean data) {
        int count = 0;
        // 计算当前链表中某种类型的node的未匹配的数量
        for (Node p = head; p != null; ) {
            if (!p.isMatched()) {
                if (p.isData != data) // 从之前描述的4种队列情况可知，该链表中同一时刻只能存在一种类型的Node
                    return 0; // 因此另一种类型的Node是0个
                if (++count == Integer.MAX_VALUE) // saturated
                    break;
            }
            Node n = p.next;
            // 从之前描述的5种队列情况可知，有两种队列即，
            // 队列前部分是已匹配的消费者节点，后部分是未匹配的消费者Node
            // 队列前部分是已匹配的生产者节点，后部分是未匹配的生产者Node
            // 因此若有一个线程正在计算countOfMode()，可能有线程正在处理上述两种队列[将已匹配节点逻辑删除，更新Head]，因此这里需要更新head
            if (n != p)
                p = n;
            else {
                count = 0;
                p = head;
            }
        }
        return count;
    }

    final class Itr implements Iterator<E> {
        private Node nextNode;   // next node to return item for
        private E nextItem;      // the corresponding item
        private Node lastRet;    // last returned node, to support remove
        private Node lastPred;   // predecessor to unlink lastRet

        /**
         * Moves to next node after prev, or first node if prev null.
         */
        private void advance(Node prev) {
            /*
             * To track and avoid buildup of deleted nodes in the face
             * of calls to both Queue.remove and Itr.remove, we must
             * include variants of unsplice and sweep upon each
             * advance: Upon Itr.remove, we may need to catch up links
             * from lastPred, and upon other removes, we might need to
             * skip ahead from stale nodes and unsplice deleted ones
             * found while advancing.
             */

            Node r, b; // reset lastPred upon possible deletion of lastRet
            if ((r = lastRet) != null && !r.isMatched())
                lastPred = r;    // next lastPred is old lastRet
            else if ((b = lastPred) == null || b.isMatched())
                lastPred = null; // at start of list
            else {
                Node s, n;       // help with removal of lastPred.next
                while ((s = b.next) != null &&
                       s != b && s.isMatched() &&
                       (n = s.next) != null && n != s)
                    b.casNext(s, n);
            }

            this.lastRet = prev;

            for (Node p = prev, s, n;;) {
                s = (p == null) ? head : p.next;
                if (s == null)
                    break;
                else if (s == p) {
                    p = null;
                    continue;
                }
                Object item = s.item;
                if (s.isData) {
                    if (item != null && item != s) {
                        nextItem = LinkedTransferQueue.<E>cast(item);
                        nextNode = s;
                        return;
                    }
                }
                else if (item == null)
                    break;
                // assert s.isMatched();
                if (p == null)
                    p = s;
                else if ((n = s.next) == null)
                    break;
                else if (s == n)
                    p = null;
                else
                    p.casNext(s, n);
            }
            nextNode = null;
            nextItem = null;
        }

        Itr() {
            advance(null);
        }

        public final boolean hasNext() {
            return nextNode != null;
        }

        public final E next() {
            Node p = nextNode;
            if (p == null) throw new NoSuchElementException();
            E e = nextItem;
            advance(p);
            return e;
        }

        public final void remove() {
            final Node lastRet = this.lastRet;
            if (lastRet == null)
                throw new IllegalStateException();
            this.lastRet = null;
            if (lastRet.tryMatchData())
                unsplice(lastPred, lastRet);
        }
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LTQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedTransferQueue<E> queue;
        Node current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        LTQSpliterator(LinkedTransferQueue<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node p;
            final LinkedTransferQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null) &&
                p.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                do {
                    Object e = p.item;
                    if (e != p && (a[i] = e) != null)
                        ++i;
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (p != null && i < n && p.isData);
                if ((current = p) == null)
                    exhausted = true;
                if (i > 0) {
                    batch = i;
                    return Spliterators.spliterator
                        (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                         Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Node p;
            if (action == null) throw new NullPointerException();
            final LinkedTransferQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null)) {
                exhausted = true;
                do {
                    Object e = p.item;
                    if (e != null && e != p)
                        action.accept((E)e);
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (p != null && p.isData);
            }
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super E> action) {
            Node p;
            if (action == null) throw new NullPointerException();
            final LinkedTransferQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null)) {
                Object e;
                do {
                    if ((e = p.item) == p)
                        e = null;
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (e == null && p != null && p.isData);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept((E)e);
                    return true;
                }
            }
            return false;
        }

        public long estimateSize() { return Long.MAX_VALUE; }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    /**
     * Returns a {@link Spliterator} over the elements in this queue.
     *
     * <p>The returned spliterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
     * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
     *
     * @implNote
     * The {@code Spliterator} implements {@code trySplit} to permit limited
     * parallelism.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LTQSpliterator<E>(this);
    }

    /* -------------- Removal methods -------------- */

    /**
     * Unsplices (now or later) the given deleted/cancelled node with
     * the given predecessor.
     *
     * @param pred a node that was at one time known to be the
     * predecessor of s, or null or s itself if s is/was at head
     * @param s the node to be unspliced
     */
    final void unsplice(Node pred, Node s) {
        // 在等待期间如果线程被中断或等待超时，则取消匹配，并调用unsplice方法解除节点s和其前继节点的链接 -- 设置item自连接，waiter为null
        s.forgetContents(); // forget unneeded fields
        /*
         * 如果pred的next仍然指向s，请尝试取消s的链接。
         * 如果s无法取消链接，因为它是尾随节点，或者pred可能已取消链接，
         * 并且pred和s都不是head或offlist，请添加到sweepVotes，如果累积了足够的投票，请进行扫描。
         */
        if (pred != null && pred != s && pred.next == s) {
            // 获取s的后继节点
            Node n = s.next;
            // s的后继节点为null，或不为null，就将s的前驱节点的后继节点设置为n
            if (n == null ||
                (n != s && pred.casNext(s, n) && pred.isMatched())) {
                for (;;) {               // 检查是否在或可能在头部
                    Node h = head;
                    if (h == pred || h == s || h == null)
                        return;          // 在头结点上，或者链表为空
                    if (!h.isMatched())
                        break;          // 头结点非匹配的
                    Node hn = h.next;
                    if (hn == null)
                        return;          // now empty
                    if (hn != h && casHead(h, hn))
                        h.forgetNext();  // advance head
                }
                // 重新检查是否关闭列表
                if (pred.next != pred && s.next != s) {
                    for (;;) {           // sweep now if enough votes
                        int v = sweepVotes;
                        // 未达到阀值，更新sweepVotes
                        if (v < SWEEP_THRESHOLD) {
                            if (casSweepVotes(v, v + 1))
                                break;
                        }
                        // 达到阀值，进行“大扫除”，清除队列中的无效节点
                        else if (casSweepVotes(v, 0)) {
                            sweep();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Unlinks matched (typically cancelled) nodes encountered in a
     * traversal from head.
     */
    private void sweep() {
        for (Node p = head, s, n; p != null && (s = p.next) != null; ) {
            if (!s.isMatched()) // next节点没有被匹配，继续迭代
                // Unmatched nodes are never self-linked
                p = s;
            else if ((n = s.next) == null) // 尾随节点为null，即到队尾，提前结束
                break;
            else if (s == n)    // 被移除的node，重新从头检查
                // No need to also check for p == s, since that implies s == n
                p = head;
            else
                p.casNext(s, n); // 节点s是已经匹配的节点，且节点s不是被移除的节点，在链表跳过节点s
        }
    }

    /**
     * Main implementation of remove(Object)
     */
    private boolean findAndRemove(Object e) {
        if (e != null) {
            for (Node pred = null, p = head; p != null; ) {
                Object item = p.item;
                if (p.isData) {
                    if (item != null && item != p && e.equals(item) &&
                        p.tryMatchData()) {
                        unsplice(pred, p);
                        return true;
                    }
                }
                else if (item == null)
                    break;
                pred = p;
                if ((p = p.next) == pred) { // stale
                    pred = null;
                    p = head;
                }
            }
        }
        return false;
    }

    /**
     * Creates an initially empty {@code LinkedTransferQueue}.
     */
    public LinkedTransferQueue() {
    }

    /**
     * Creates a {@code LinkedTransferQueue}
     * initially containing the elements of the given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public LinkedTransferQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never block.
     *
     * @throws NullPointerException if the specified element is null
     */
    public void put(E e) {
        xfer(e, true, ASYNC, 0);
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never block or
     * return {@code false}.
     *
     * @return {@code true} (as specified by
     *  {@link java.util.concurrent.BlockingQueue#offer(Object,long,TimeUnit)
     *  BlockingQueue.offer})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never return {@code false}.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never throw
     * {@link IllegalStateException} or return {@code false}.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        // 正常队列5五种情况
        // 队列空，直接插入生产者者Node，立即返回
        // 队列只有未匹配的生产者Node，发现Head就是未匹配的生产者Node时，就可以决定插入生产者Node，然后立即返回
        // 队列前面是已匹配的生产者Node[尚未清除]，发现Head是已匹配的节点，需要向下查找到一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，然后插入生产者Node，立即返回
        // 队列只有未匹配的消费者Node，发现Head是未匹配的消费者Node，直接将插入值e交给对方
        // 队列前面是已匹配的消费者[尚未清除]，发现Head是已匹配的节点，需要向下查找一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，然后将插入值e交给对方
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * Transfers the element to a waiting consumer immediately, if possible.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * otherwise returning {@code false} without enqueuing the element.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean tryTransfer(E e) {
        return xfer(e, true, NOW, 0) == null;
    }

    /**
     * Transfers the element to a consumer, waiting if necessary to do so.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else inserts the specified element at the tail of this queue
     * and waits until the element is received by a consumer.
     *
     * @throws NullPointerException if the specified element is null
     */
    public void transfer(E e) throws InterruptedException {
        if (xfer(e, true, SYNC, 0) != null) {
            Thread.interrupted(); // failure possible only due to interrupt
            throw new InterruptedException();
        }
    }

    /**
     * Transfers the element to a consumer if it is possible to do so
     * before the timeout elapses.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else inserts the specified element at the tail of this queue
     * and waits until the element is received by a consumer,
     * returning {@code false} if the specified wait time elapses
     * before the element can be transferred.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean tryTransfer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (xfer(e, true, TIMED, unit.toNanos(timeout)) == null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public E take() throws InterruptedException {
        // 正常队列5五种情况
        // 队列空，插入同步等待的消费者Node，立即返回
        // 队列只有未匹配的生产者Node，发现Head就是未匹配的生产者Node时，直接获取对方Node的值，然后立即返回
        // 队列前面是已匹配的生产者Node[尚未清除]，发现Head是已匹配的节点，需要向下查找到一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，直接获取对方Node的值，立即返回
        // 队列只有未匹配的消费者Node，发现Head是未匹配的消费者Node，插入同步等待的消费者Node，立即返回
        // 队列前面是已匹配的消费者[尚未清除]，发现Head是已匹配的节点，需要向下查找一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，插入同步等待的消费者Node，立即返回
        E e = xfer(null, false, SYNC, 0);
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        // 正常队列5五种情况
        // 队列空，插入超时等待的消费者Node，立即返回
        // 队列只有未匹配的生产者Node，发现Head就是未匹配的生产者Node时，直接获取对方Node的值，然后立即返回
        // 队列前面是已匹配的生产者Node[尚未清除]，发现Head是已匹配的节点，需要向下查找到一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，直接获取对方Node的值，立即返回
        // 队列只有未匹配的消费者Node，发现Head是未匹配的消费者Node，插入超时等待的消费者Node，立即返回
        // 队列前面是已匹配的消费者[尚未清除]，发现Head是已匹配的节点，需要向下查找一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，插入超时等待的消费者Node，立即返回
        E e = xfer(null, false, TIMED, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    public E poll() {
        // 正常队列5五种情况
        // 队列空，不会插入消费者Node，立即返回
        // 队列只有未匹配的生产者Node，发现Head就是未匹配的生产者Node时，直接获取对方Node的值，然后立即返回
        // 队列前面是已匹配的生产者Node[尚未清除]，发现Head是已匹配的节点，需要向下查找到一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，直接获取对方Node的值，立即返回
        // 队列只有未匹配的消费者Node，发现Head是未匹配的消费者Node，但不会插入消费者Node，立即返回
        // 队列前面是已匹配的消费者[尚未清除]，发现Head是已匹配的节点，需要向下查找一个没有被匹配的节点，然后帮助做逻辑删除，更新链表头，但不会插入消费者Node，立即返回
        return xfer(null, false, NOW, 0);
    }

    /**
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; n < maxElements && (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     * The elements will be returned in order from first (head) to last (tail).
     *
     * <p>The returned iterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * @return an iterator over the elements in this queue in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    public E peek() {
        return firstDataItem();
    }

    /**
     * Returns {@code true} if this queue contains no elements.
     *
     * @return {@code true} if this queue contains no elements
     */
    public boolean isEmpty() {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return !p.isData;
        }
        return true;
    }

    public boolean hasWaitingConsumer() {
        return firstOfMode(false) != null;
    }

    /**
     * Returns the number of elements in this queue.  If this queue
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these queues, determining the current
     * number of elements requires an O(n) traversal.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        return countOfMode(true);
    }

    public int getWaitingConsumerCount() {
        return countOfMode(false);
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.
     * Returns {@code true} if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        return findAndRemove(o);
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) { // 计算生产者Node中是否存在该值
                if (item != null && item != p && o.equals(item)) // 未匹配且未逻辑删除且item等于o
                    return true;
            }
            else if (item == null)
                break;
        }
        return false;
    }

    /**
     * Always returns {@code Integer.MAX_VALUE} because a
     * {@code LinkedTransferQueue} is not capacity constrained.
     *
     * @return {@code Integer.MAX_VALUE} (as specified by
     *         {@link java.util.concurrent.BlockingQueue#remainingCapacity()
     *         BlockingQueue.remainingCapacity})
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     * @serialData All of the elements (each an {@code E}) in
     * the proper order, followed by a null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        for (E e : this)
            s.writeObject(e);
        // Use trailing null as sentinel
        s.writeObject(null);
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E) s.readObject();
            if (item == null)
                break;
            else
                offer(item);
        }
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long sweepVotesOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = LinkedTransferQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
            sweepVotesOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("sweepVotes"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
