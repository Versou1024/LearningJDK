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
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * A synchronization point at which threads can pair and swap elements
 * within pairs.  Each thread presents some object on entry to the
 * {@link #exchange exchange} method, matches with a partner thread,
 * and receives its partner's object on return.  An Exchanger may be
 * viewed as a bidirectional form of a {@link SynchronousQueue}.
 * Exchangers may be useful in applications such as genetic algorithms
 * and pipeline designs.
 *
 * <p><b>Sample Usage:</b>
 * Here are the highlights of a class that uses an {@code Exchanger}
 * to swap buffers between threads so that the thread filling the
 * buffer gets a freshly emptied one when it needs it, handing off the
 * filled one to the thread emptying the buffer.
 *  <pre> {@code
 * class FillAndEmpty {
 *   Exchanger<DataBuffer> exchanger = new Exchanger<DataBuffer>();
 *   DataBuffer initialEmptyBuffer = ... a made-up type
 *   DataBuffer initialFullBuffer = ...
 *
 *   class FillingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialEmptyBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           addToBuffer(currentBuffer);
 *           if (currentBuffer.isFull())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ... }
 *     }
 *   }
 *
 *   class EmptyingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialFullBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           takeFromBuffer(currentBuffer);
 *           if (currentBuffer.isEmpty())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ...}
 *     }
 *   }
 *
 *   void start() {
 *     new Thread(new FillingLoop()).start();
 *     new Thread(new EmptyingLoop()).start();
 *   }
 * }}</pre>
 *
 * <p>Memory consistency effects: For each pair of threads that
 * successfully exchange objects via an {@code Exchanger}, actions
 * prior to the {@code exchange()} in each thread
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * those subsequent to a return from the corresponding {@code exchange()}
 * in the other thread.
 *
 * @since 1.5
 * @author Doug Lea and Bill Scherer and Michael Scott
 * @param <V> The type of objects that may be exchanged
 */
public class Exchanger<V> {

    /*
     * 概述:核心算法是,一个交换槽位和另一个带有item的partner线程.
     * for (;;) {
     *   if (slot is empty) { //槽位为空,则放入item到节点   // offer
     *     place item in a Node;
     *     if (can CAS slot from empty to node) {  //CAS操作将节点放入空槽位
     *       wait for release;//等待释放槽位
     *       return matching item in node;//返回匹配节点的item
     *     }
     *   }
     *   else if (can CAS slot from node to empty) { //槽位不为空,CAS操作将槽位节点移除 release
     *     get the item in node; //获取节点的item
     *     set matching item in node; //设定节点中匹配的内容
     *     release waiting thread; //释放等待线程
     *   }
     *   // CAS失败则继续循环操作
     * }
     *
     * 这是“双重数据结构”的最简单形式之一
     *
     * 上述工作机制原则上可以工作的很好,但实际上，像许多在单个位置上进行原子更新的算法一样，当使用同一个Exchanger的线程不止一个时，
     * 则存在严重的伸缩性问题.因此我们的实现采用了一种消除竞争的形式,它通过安排一些线程使用不同的槽位来分散竞争压力,这样做最终依旧能
     * 保证两个匹配的线程可以交换item.
     *
     * 一个有效的竞争实现需要分配大量的空间(因为需要分配很多slot),因此只有在检测到竞争时,我们才会这么做(因为单cpu时,分配很多slot没有什么用,
     * 所以也不会这么做).否则,exchanges就会使用单槽位的槽位交换方法.在竞争中,不仅仅槽位应该在不同的位置,而且没有slot在相同的缓存行上(更一般的讲,
     * 就是相同的相干单元),因此不会出现内存竞争.因为在撰写本文时，无法确定缓存行大小，因此我们定义了一个对于普通平台来说都足够的值.
     * 另外，在别处进行额外的保护以避免其他错误/非预期的共享，并增强局部性，包括对Node使用边距（通过sun.misc.Contended）;嵌入“bound”作
     * 为Exchange的字段;以及使用区别于LockSupport重排一些Park/unPark的机制。
     *
     * 开始时,只有一个槽位.我们通过跟踪冲突(exchange时失败的CAS)来扩展arena的大小;
     * 根据上述算法的性质，仅有的几种类型的冲突已经明确暗示了:竞争是两个线程尝试释放Node的冲突--一个线程的offer发生CAS操作失败是合法的,
     * 但是这不意味着2个及以上的线程同时发生CAS失败也是合理的(注意:在CAS操作失败后,通过读取槽位的值来检查冲突是可能的,但是这样做是不值得提倡
     * 的).在当前arena限制内,如果一个线程在每一个槽位都发生了冲突,此时会扩展arena大小.通过使用bound字段的版本号,在一定范围内进行冲突的跟踪,
     * 当线程发现界限值bound值已经被更改,则会保守的重置冲突个数.
     *
     * 通过放弃等待的一段时间,减少arena的有效规模(如果此时槽位个数>1).
     * “一段时间”的值应该定为多少,这是一个经验问题。我们利用spin->yield->block来实现一段合理的等待时间--在一个繁忙的exchanger中,资源获取后
     * 很快就会释放,在这种情况下,多处理器的上下文切换会非常慢,而且也造成了资源浪费.
     * arena等待只是省略阻塞部分，而不是取消。根据经验,自旋数被设定为:在一系列测试机器的最大持续交换率下,避免了99%的阻塞时间.
     * spin和yield都需要一些有限定的随机性(使用廉价的异或移位操作xorshift)以避免严格模式下会引起没必要的grow/shrink环。
     * (使用伪随机还有助于通过使分支不可预知来调整旋转周期的持续时间。)当然,在offer的过程中,等待线程能够"知道"当槽位被改变时,其它线程将对此槽位
     * 执行release操作,但是在匹配成功前,它依旧不能继续往下执行.同时,它也不能撤销offer操作,而只能是spin/yield操作.
     * 注意:通过将线性化点更改为匹配字段的CAS（如在Scott＆Scherer DISC论文中的一种情况中所做的），可以避免二次检查,这也会增加异步性,但代价是
     * 冲突检测会比较差且无法总是重用每个线程的节点.因此此方式是一种折中方案.
     *
     * 发生冲突时,索引会按逆序循环遍历arena,当界限发生改变时,以最大索引(此位置Node最稀疏)重新开始.(过期后,索引减半直到为0为止)
     * 通过使用随机数,素数步长或双重哈希式遍历，而不是简单的循环遍历来减少聚集是可能的(且已经做过尝试).
     * 但是从经验来说,这些可能带来的好处无法克服其额外开销:除非存在持续的竞争,否则我们目前的管理操作运行都很快,所以更简单/更快的控制策略比
     * 更准确但速度更慢的策略运作得更好。
     *
     * 因为我们使用过期来对arena的规模进行控制,因此在公有的exchange时间版本方法中不能抛出超时异常直到arena的规模大小缩为0(或者arena不能被
     * 使用).这可能在超时上延长响应但是这种延迟是可以接受的.
     *
     * 基本上所有的实现都在方法slotExchange和arenaExchange中。
     * 这些方法的宏观架构是类似的,但在组成的细节上有很多不同.slotExchange方法使用了单一的Exchanger类型字段slot,而arena使用了一个数组.
     * 然而,它仍旧需要最少的冲突检测来触发arena的构建.(在这两个方法被调用时,最麻烦的部分就是确定中断状态以及转换期间正常出现的中断异常)
     *
     * 这种类型的代码中,这种方法太常见了,因为大多数逻辑都依赖于作为局部变量维护的字段来读取,所以不能很好的对方法进行分解--主要表现在这里:体积庞大
     * 的spin-yield-block/cancel代码,以及严重依赖于内部函数(Unsafe)来使用内联嵌入式CAS和相关的内存访问操作(当它们被隐藏在命名友好且封装了
     * 预期效果的方法后面时,动态编译器往往不会将其内联).
     * 这包括使用putOrderedX来清除每个线程节点之间使用的字段.
     * 请注意，即使通过线程的release操作来读取字段Node.item，也并未将其声明为volatile类型,因为读取操作只会在CAS操作完成之后才发生,并且
     * 其它持有此字段的线程对其的使用都已经由其它操作确定了顺序.(因为实际的原子是对槽位的CAS操作,因此在release中对Node.match的写操作比完全
     * volatile写要弱是合法的.然而,并没有这样做是因为它可以允许进一步推迟写，延迟进度。)
     */

    /**
     * The byte distance (as a shift value) between any two used slots
     * in the arena.  1 << ASHIFT should be at least cacheline size.
     */
    private static final int ASHIFT = 7;

    /**
     * The maximum supported arena index. The maximum allocatable
     * arena size is MMASK + 1. Must be a power of two minus one, less
     * than (1<<(31-ASHIFT)). The cap of 255 (0xff) more than suffices
     * for the expected scaling limits of the main algorithms.
     */
    private static final int MMASK = 0xff;

    /**
     * Unit for sequence/version bits of bound field. Each successful
     * change to the bound also adds SEQ.
     */
    private static final int SEQ = MMASK + 1;

    /** The number of CPUs, for sizing and spin control */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The maximum slot index of the arena: The number of slots that
     * can in principle hold all threads without contention, or at
     * most the maximum indexable value.
     */
    static final int FULL = (NCPU >= (MMASK << 1)) ? MMASK : NCPU >>> 1;

    /**
     * The bound for spins while waiting for a match. The actual
     * number of iterations will on average be about twice this value
     * due to randomization. Note: Spinning is disabled when NCPU==1.
     */
    private static final int SPINS = 1 << 10;

    /**
     * Value representing null arguments/returns from public
     * methods. Needed because the API originally didn't disallow null
     * arguments, which it should have.
     */
    private static final Object NULL_ITEM = new Object();

    /**
     * Sentinel value returned by internal exchange methods upon
     * timeout, to avoid need for separate timed versions of these
     * methods.
     */
    private static final Object TIMED_OUT = new Object();

    /**
     * Nodes hold partially exchanged data, plus other per-thread
     * bookkeeping. Padded via @sun.misc.Contended to reduce memory
     * contention.
     */
    @sun.misc.Contended static final class Node {
        int index;              // 在竞争区中的arena的下标；
        int bound;              // 上一次记录的Exchanger.bound；
        int collides;           // 在当前bound下CAS失败的次数；
        int hash;               // 伪随机数，用于自旋；
        Object item;            // 这个线程的当前项，也就是需要交换的数据；
        volatile Object match;  // 做releasing操作的线程传递的项；
        volatile Thread parked; // 挂起时设置线程值，其他情况下为null，用来被其他线程唤醒时调用
    }

    /** The corresponding thread local class */
    static final class Participant extends ThreadLocal<Node> {
        // Participant参加者，继承TheadLocal，重写initialValue()的方法，初始化一个空的Node
        public Node initialValue() { return new Node(); }
    }
    /*
     * slot为单个槽，arena为数组槽。他们都是Node类型。在这里可能会感觉到疑惑，slot作为Exchanger交换数据的场景，应该只需要一个就可以了啊？
     * 为何还多了一个 Participant 和数组类型的arena呢？一个slot交换场所原则上来说应该是可以的，但实际情况却不是如此，多个参与者使用同一个交换场所时，
     * 会存在严重伸缩性问题。既然单个交换场所存在问题，那么我们就安排多个，也就是数组arena。通过数组arena来安排不同的线程使用不同的slot来
     * 降低竞争问题，并且可以保证最终一定会成对交换数据。但是Exchanger不是一来就会生成arena数组来降低竞争，只有当产生竞争是才会生成arena数组。
     * 那么怎么将Node与当前线程绑定呢？Participant ，Participant 的作用就是为每个线程保留唯一的一个Node节点，它继承ThreadLocal，同时在Node节点
     * 中记录在arena中的下标index。
     */

    /**
     * Per-thread state
     */
    private final Participant participant;

    /**
     * Elimination array; null until enabled (within slotExchange).
     * Element accesses use emulation of volatile gets and CAS.
     */
    private volatile Node[] arena; // arena初始化后就不会再扩容或者缩容，而是通过bound控制大小，而slot非空时，arena就表示无法使用

    /**
     * Slot used until contention detected.
     */
    private volatile Node slot;

    /**
     * The index of the largest valid arena position, OR'ed with SEQ
     * number in high bits, incremented on each update.  The initial
     * update from 0 to SEQ is used to ensure that the arena array is
     * constructed only once.
     */
    private volatile int bound; // 最大有效arena位置的索引，或用高位的序号表示，在每次更新时递增。从0到SEQ的初始更新用于确保竞技场阵列只构建一次。

    /**
     * Exchange function when arenas enabled. See above for explanation.
     *
     * @param item the (non-null) item to exchange
     * @param timed true if the wait is timed
     * @param ns if timed, the maximum wait time, else 0L
     * @return the other thread's item; or null if interrupted; or
     * TIMED_OUT if timed and timed out
     */
    private final Object arenaExchange(Object item, boolean timed, long ns) {
        Node[] a = arena;
        Node p = participant.get(); // 获取当前线程的 new Node()，初始化的index默认为0
        // p的index值初始化为0，下面的i表示索引，j表示在slot槽中的物理位置
        for (int i = p.index;;) {
            int b, m, c; long j;
            // 1、获取当前线程需要完成交易在arena中slot上的Node
            Node q = (Node)U.getObjectVolatile(a, j = (i << ASHIFT) + ABASE);
            // 2、q != null表示arena[i]上已经有交易者正在等待，那么当前线程会对方交换数据，并唤醒对方
            if (q != null && U.compareAndSwapObject(a, j, q, null)) {
                Object v = q.item;
                q.match = item;
                Thread w = q.parked;
                if (w != null)
                    U.unpark(w);
                return v;
            }
            // 3、q=null，对应的slot上没有交易者
             else if (i <= (m = (b = bound) & MMASK) && q == null) {
                p.item = item;                         // offer
                // 将p设置到arena[i]上
                if (U.compareAndSwapObject(a, j, null, p)) {
                    long end = (timed && m == 0) ? System.nanoTime() + ns : 0L;
                    Thread t = Thread.currentThread(); // wait
                    // 进入经典的spin+block模式
                    // 1、如果match非空，即匹配成功，即可退出返回match的值
                    // 2、否则，进入SPINS+随机自旋次数，做Thead.yield()操作，期间不断监测match值，做第一步操作
                    // 3、自旋次数为0时，若发现arena[i]上非当前的p，那就重新自旋
                    // 4、线程未中断，且m=0，且未超时或者非限时版，那就进入主赛或者限时阻塞
                    // 5、线程中断，或m不等于0，或超时：超时返回TIMED_OUT，m不等于0或中断返回null
                    for (int h = p.hash, spins = SPINS;;) {
                        Object v = p.match;
                        if (v != null) {
                            U.putOrderedObject(p, MATCH, null);
                            p.item = null;             // clear for next use
                            p.hash = h;
                            return v;
                        }
                        // 随机自旋
                        else if (spins > 0) {
                            h ^= h << 1;
                            h ^= h >>> 3;
                            h ^= h << 10; // xorshift
                            if (h == 0)                // initialize hash
                                h = SPINS | (int)t.getId();
                            else if (h < 0 &&          // approx 50% true
                                     (--spins & ((SPINS >>> 1) - 1)) == 0)
                                Thread.yield();        // two yields per wait
                        }
                        // slot槽位非当前节点p
                        else if (U.getObjectVolatile(a, j) != p)
                            spins = SPINS;       // releaser hasn't set match yet
                        else if (!t.isInterrupted() && m == 0 && (!timed || (ns = end - System.nanoTime()) > 0L)) {
                            U.putObject(t, BLOCKER, this); // emulate LockSupport
                            p.parked = t;              // minimize window
                            if (U.getObjectVolatile(a, j) == p)
                                U.park(false, ns); // 阻塞起来
                            p.parked = null;
                            U.putObject(t, BLOCKER, null);
                        }
                        else if (U.getObjectVolatile(a, j) == p && U.compareAndSwapObject(a, j, p, null)) {
                            if (m != 0)                // try to shrink
                                U.compareAndSwapInt(this, BOUND, b, b + SEQ - 1);
                            p.item = null;
                            p.hash = h;
                            i = p.index >>>= 1;        // descend
                            if (Thread.interrupted())
                                return null;
                            if (timed && m == 0 && ns <= 0L)
                                return TIMED_OUT;
                            break;                     // expired; restart
                        }
                    }
                }
                else
                    p.item = null;                     // clear offer
            }
            else {
                if (p.bound != b) {                    // bound过期; reset
                    p.bound = b;
                    p.collides = 0;
                    i = (i != m || m == 0) ? m : m - 1;
                }
                else if ((c = p.collides) < m || m == FULL ||
                         !U.compareAndSwapInt(this, BOUND, b, b + SEQ + 1)) {
                    p.collides = c + 1;
                    i = (i == 0) ? m : i - 1;          // cyclically traverse
                }
                else
                    i = m + 1;                         // grow
                p.index = i;
            }
        }
    }

    /**
     * Exchange function used until arenas enabled. See above for explanation.
     *
     * @param item the item to exchange
     * @param timed true if the wait is timed
     * @param ns if timed, the maximum wait time, else 0L
     * @return the other thread's item; or null if either the arena
     * was enabled or the thread was interrupted before completion; or
     * TIMED_OUT if timed and timed out
     */
    private final Object slotExchange(Object item, boolean timed, long ns) {
        // 获取当前线程的节点 p
        Node p = participant.get(); // 注意：participant有初始化的Node
        // 当前线程
        Thread t = Thread.currentThread();
        // 线程中断，直接返回
        if (t.isInterrupted()) // preserve interrupt status so caller can recheck
            return null;
        // 自旋
        for (Node q;;) {
            // 尝试CAS替换 -- slot非空，表示对方先使用exchange()将数据写入slot中
            if ((q = slot) != null) {
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    Object v = q.item;  // 当前线程的项，也就是交换的数据 -- 把对方的item拿出来
                    q.match = item;     // 做releasing操作的线程传递的项 -- 把我的item给对方
                    Thread w = q.parked;
                    if (w != null)
                        U.unpark(w); // 匹配成功，将对方唤醒
                    return v; // 返回获取到的值
                }
                // 执行到这说明：CAS竞争失败了，表示slot中数据已经被其他线程交换走啦，说明有竞争 -- 需要创建arena
                // 条件：多核CPU、bound界限为0，尝试CAS更新BOUND
                if (NCPU > 1 && bound == 0 && U.compareAndSwapInt(this, BOUND, 0, SEQ))
                    arena = new Node[(FULL + 2) << ASHIFT]; // 假如我的机器NCPU = 8 ，则得到的是768大小的arena数组。
            }

            // 执行到在说明：slot为空，有两种情况
            // 1、当前线程时第一个到达的，所以slot就是null
            // 2、多个线程同时到达争夺slot，失败的线程就会发现slot为null

            // 情况2：如果 arena != null，表示不需要在slot上交换，直接返回，进入arenaExchange()逻辑处理
            else if (arena != null)
                return null; // caller must reroute to arenaExchange


            // 情况1：执行到这说明：slot为空，arena也为空，证明该线程是最先使用exchange()交换数据的
            // 步骤：将item保存到node上，CAS更新slot为p
            else {
                p.item = item;
                if (U.compareAndSwapObject(this, SLOT, null, p))
                    break;
                p.item = null;
            }
        }

        // 执行到这说明：slot为空，arena也为空，证明该线程是最先使用exchange()交换数据的，需要进入spin+block

        /*
         * 等待 release
         * 进入spin+block模式
         */
        int h = p.hash;
        long end = timed ? System.nanoTime() + ns : 0L;
        int spins = (NCPU > 1) ? SPINS : 1;
        Object v;
        // spin + blcok模式 -- 直到匹配交换数据成功
        // 在自旋+阻塞模式中，首先取得结束时间和自旋次数。
        // 如果match(做releasing操作的线程传递的项)为null，其首先尝试spins+随机次自旋（改自旋使用当前节点中的hash，并改变之）和退让。
        // 当自旋数为0后，假如slot发生了改变（slot != p）则重置自旋数并重试。
        // 否则假如：当前未中断&arena为null&（当前不是限时版本或者限时版本+当前时间未结束）：阻塞或者限时阻塞。
        // 假如：当前中断或者arena不为null或者当前为限时版本+时间已经结束：
        //      不限时版本：置v为null；
        //      限时版本：如果时间结束以及未中断则TIMED_OUT；
        //      否则给出null（原因是探测到arena非空或者当前线程中断）。
        while ((v = p.match) == null) {
            // 1、尝试spins+随机次自旋
            if (spins > 0) {
                h ^= h << 1;
                h ^= h >>> 3;
                h ^= h << 10;
                if (h == 0)
                    h = SPINS | (int)t.getId();
                else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0)
                    Thread.yield();
            }
            // 2、执行到此：自旋次数为0
            // slot已改变，重新尝试自旋
            else if (slot != p)
                spins = SPINS;
            // 3、线程未中断，且arena为null，且(为永久等待timed=false或者还未超时timed=true) -- 进入永久阻塞或限时阻塞
            else if (!t.isInterrupted() && arena == null && (!timed || (ns = end - System.nanoTime()) > 0L)) {
                U.putObject(t, BLOCKER, this);
                p.parked = t;
                if (slot == p)
                    U.park(false, ns);
                p.parked = null; // 唤醒，清空node中的parked
                U.putObject(t, BLOCKER, null); // 唤醒，清空线程的BLOCKER监视器
            }
            // 执行到此：线程可能中断，或者arena不为null，或者超时
            // 步骤：
            // 1、更新SLOT为null，表示不使用SLOT啦，改为数组槽arena
            // 2、若已经超时且非中断，返回TIMED_OUT；若已经超时且中断，返回null
            else if (U.compareAndSwapObject(this, SLOT, p, null)) {
                v = timed && ns <= 0L && !t.isInterrupted() ? TIMED_OUT : null;
                break;
            }
        }
        U.putOrderedObject(p, MATCH, null);
        p.item = null;
        p.hash = h;
        return v;
    }

    /**
     * Creates a new Exchanger.
     */
    public Exchanger() {
        participant = new Participant();
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@linkplain Thread#interrupt interrupted}),
     * and then transfers the given object to it, receiving its object
     * in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread.  The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of two things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * for the exchange,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @param x the object to exchange
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting
     */
    @SuppressWarnings("unchecked")
    public V exchange(V x) throws InterruptedException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x; // null有特殊的作用，因此对于交换的的item为null则使用NULL_ITEM替换
        // 1、arena为数组槽如果为null，则说明只有一个slot，因此执行slotExchange()方法，
        //          slotExchange()执行结果非null,即交换成功，直接就结束；
        //          slotExchange()执行结果为null,即交换失败，升级为使用arenaExchange()进行交换数据
        // 2、arena为数组槽如果已经不为null，则直接调用arenaExchange();
        //
        // 结论：如果slotExchange(Object item, boolean timed, long ns)方法执行失败了就执行arenaExchange(Object item, boolean timed, long ns)方法，最后返回结果V。
        if ((arena != null || (v = slotExchange(item, false, 0L)) == null) &&
            ((Thread.interrupted() || (v = arenaExchange(item, false, 0L)) == null)))
            throw new InterruptedException();

        // 3、返回交换的结果
        return (v == NULL_ITEM) ? null : (V)v;
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@linkplain Thread#interrupt interrupted} or
     * the specified waiting time elapses), and then transfers the given
     * object to it, receiving its object in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread.  The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of three things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * for the exchange,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link
     * TimeoutException} is thrown.  If the time is less than or equal
     * to zero, the method will not wait at all.
     *
     * @param x the object to exchange
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting
     * @throws TimeoutException if the specified waiting time elapses
     *         before another thread enters the exchange
     */
    @SuppressWarnings("unchecked")
    public V exchange(V x, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x;
        long ns = unit.toNanos(timeout);
        if ((arena != null ||
             (v = slotExchange(item, true, ns)) == null) &&
            ((Thread.interrupted() ||
              (v = arenaExchange(item, true, ns)) == null)))
            throw new InterruptedException();
        if (v == TIMED_OUT)
            throw new TimeoutException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long BOUND;
    private static final long SLOT;
    private static final long MATCH;
    private static final long BLOCKER;
    private static final int ABASE;
    static {
        int s;
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> ek = Exchanger.class;
            Class<?> nk = Node.class;
            Class<?> ak = Node[].class;
            Class<?> tk = Thread.class;
            BOUND = U.objectFieldOffset
                (ek.getDeclaredField("bound"));
            SLOT = U.objectFieldOffset
                (ek.getDeclaredField("slot"));
            MATCH = U.objectFieldOffset
                (nk.getDeclaredField("match"));
            BLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            s = U.arrayIndexScale(ak);
            // ABASE absorbs padding in front of element 0
            ABASE = U.arrayBaseOffset(ak) + (1 << ASHIFT);

        } catch (Exception e) {
            throw new Error(e);
        }
        if ((s & (s-1)) != 0 || s > (1 << ASHIFT))
            throw new Error("Unsupported array scale");
    }

}
