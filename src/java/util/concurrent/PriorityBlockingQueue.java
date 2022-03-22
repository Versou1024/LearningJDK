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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 * An unbounded {@linkplain BlockingQueue blocking queue} that uses
 * the same ordering rules as class {@link PriorityQueue} and supplies
 * blocking retrieval operations.  While this queue is logically
 * unbounded, attempted additions may fail due to resource exhaustion
 * (causing {@code OutOfMemoryError}). This class does not permit
 * {@code null} elements.  A priority queue relying on {@linkplain
 * Comparable natural ordering} also does not permit insertion of
 * non-comparable objects (doing so results in
 * {@code ClassCastException}).
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.  The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the PriorityBlockingQueue in any particular order. If you need
 * ordered traversal, consider using
 * {@code Arrays.sort(pq.toArray())}.  Also, method {@code drainTo}
 * can be used to <em>remove</em> some or all elements in priority
 * order and place them in another collection.
 *
 * <p>Operations on this class make no guarantees about the ordering
 * of elements with equal priority. If you need to enforce an
 * ordering, you can define custom classes or comparators that use a
 * secondary key to break ties in primary priority values.  For
 * example, here is a class that applies first-in-first-out
 * tie-breaking to comparable elements. To use it, you would insert a
 * {@code new FIFOEntry(anEntry)} instead of a plain entry object.
 *
 *  <pre> {@code
 * class FIFOEntry<E extends Comparable<? super E>>
 *     implements Comparable<FIFOEntry<E>> {
 *   static final AtomicLong seq = new AtomicLong(0);
 *   final long seqNum;
 *   final E entry;
 *   public FIFOEntry(E entry) {
 *     seqNum = seq.getAndIncrement();
 *     this.entry = entry;
 *   }
 *   public E getEntry() { return entry; }
 *   public int compareTo(FIFOEntry<E> other) {
 *     int res = entry.compareTo(other.entry);
 *     if (res == 0 && other.entry != this.entry)
 *       res = (seqNum < other.seqNum ? -1 : 1);
 *     return res;
 *   }
 * }}</pre>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
@SuppressWarnings("unchecked")
public class PriorityBlockingQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = 5595510919245408276L;

    /*
     * The implementation uses an array-based binary heap, with public
     * operations protected with a single lock. However, allocation
     * during resizing uses a simple spinlock (used only while not
     * holding main lock) in order to allow takes to operate
     * concurrently with allocation.  This avoids repeated
     * postponement of waiting consumers and consequent element
     * build-up. The need to back away from lock during allocation
     * makes it impossible to simply wrap delegated
     * java.util.PriorityQueue operations within a lock, as was done
     * in a previous version of this class. To maintain
     * interoperability, a plain PriorityQueue is still used during
     * serialization, which maintains compatibility at the expense of
     * transiently doubling overhead.
     */

    /**
     * Default array capacity.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n <= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     */
    private transient Object[] queue;

    /**
     * The number of elements in the priority queue.
     */
    private transient int size;

    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private transient Comparator<? super E> comparator;

    /**
     * Lock used for all public operations
     */
    private final ReentrantLock lock;

    /**
     * Condition for blocking when empty
     */
    private final Condition notEmpty; // 为什么只有notEmpty，没有notFull，因为优先级队列是一个无界阻塞队列，因此生产者不会被阻塞，消费者在队列为空的情况下会阻塞住

    /**
     * Spinlock for allocation, acquired via CAS.
     */
    private transient volatile int allocationSpinLock; // 用于分配自旋锁

    /**
     * A plain PriorityQueue used only for serialization,
     * to maintain compatibility with previous versions
     * of this class. Non-null only during serialization/deserialization.
     */
    private PriorityQueue<E> q;

    /**
     * Creates a {@code PriorityBlockingQueue} with the default
     * initial capacity (11) that orders its elements according to
     * their {@linkplain Comparable natural ordering}.
     */
    public PriorityBlockingQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    /**
     * Creates a {@code PriorityBlockingQueue} with the specified
     * initial capacity that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *         than 1
     */
    public PriorityBlockingQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Creates a {@code PriorityBlockingQueue} with the specified initial
     * capacity that orders its elements according to the specified
     * comparator.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @param  comparator the comparator that will be used to order this
     *         priority queue.  If {@code null}, the {@linkplain Comparable
     *         natural ordering} of the elements will be used.
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *         than 1
     */
    public PriorityBlockingQueue(int initialCapacity,
                                 Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.comparator = comparator;
        this.queue = new Object[initialCapacity];
    }

    /**
     * Creates a {@code PriorityBlockingQueue} containing the elements
     * in the specified collection.  If the specified collection is a
     * {@link SortedSet} or a {@link PriorityQueue}, this
     * priority queue will be ordered according to the same ordering.
     * Otherwise, this priority queue will be ordered according to the
     * {@linkplain Comparable natural ordering} of its elements.
     *
     * @param  c the collection whose elements are to be placed
     *         into this priority queue
     * @throws ClassCastException if elements of the specified collection
     *         cannot be compared to one another according to the priority
     *         queue's ordering
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public PriorityBlockingQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        boolean heapify = true; // true if not known to be in heap order
        boolean screen = true;  // true if must screen for nulls
        if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            heapify = false;
        }
        else if (c instanceof PriorityBlockingQueue<?>) {
            PriorityBlockingQueue<? extends E> pq =
                (PriorityBlockingQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            screen = false;
            if (pq.getClass() == PriorityBlockingQueue.class) // exact match
                heapify = false;
        }
        Object[] a = c.toArray();
        int n = a.length;
        // If c.toArray incorrectly doesn't return Object[], copy it.
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf(a, n, Object[].class);
        if (screen && (n == 1 || this.comparator != null)) {
            for (int i = 0; i < n; ++i)
                if (a[i] == null)
                    throw new NullPointerException();
        }
        this.queue = a;
        this.size = n;
        if (heapify)
            heapify();
    }

    /**
     * Tries to grow array to accommodate at least one more element
     * (but normally expand by about 50%), giving up (allowing retry)
     * on contention (which we expect to be rare). Call only while
     * holding lock.
     *
     * @param array the heap array
     * @param oldCap the length of the array
     */
    private void tryGrow(Object[] array, int oldCap) {
        /*
         * tryGrow()只在offer()中被调用，扩容操作这里需要放锁，降低并发度
         * 原因在于：当一个线程尝试offer()时发现，数组需要扩容，进入tryGrow()方法后，如果不释放锁，
         * 那么读取poll等操作将会被阻塞住，因此释放锁的好处就是这个
         */
        lock.unlock();
        Object[] newArray = null;
        /*
         * allocationSpinLock=0表示没有线程在做扩容，将其CAS设置为1，否则就是有线程在做数组扩容，当前线程只需要让步
         */
        if (allocationSpinLock == 0 && UNSAFE.compareAndSwapInt(this, allocationSpinLockOffset, 0, 1)) {
            try {
                int newCap = oldCap + ((oldCap < 64) ?
                                       (oldCap + 2) : // 当小于64，扩容数量为oldCap+2；当大于64时，扩容数量为oldCap*0.5
                                       (oldCap >> 1));
                if (newCap - MAX_ARRAY_SIZE > 0) {    // 可能发生溢出
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError();
                    newCap = MAX_ARRAY_SIZE;
                }
                // 数组没有变化，创建新数组newArray
                if (newCap > oldCap && queue == array)
                    newArray = new Object[newCap];
            } finally {
                // 释放锁
                allocationSpinLock = 0;
            }
        }
        // 当其余线程尝试做数组扩容，由于allocationSpingLock等于1，那么就会进入当前方法，自旋让步
        if (newArray == null)
            Thread.yield();
        // 扩容结束后尝试加锁，两种情况
        // 1、实际做数组扩容的线程，做完后获取到锁，做完数组复制，返回到offer()中的①的循环中
        // 2、没有挣抢到做数组扩容的线程，在Thread.yield()后，直接返回到offer()中的①的循环中，有可能数组还没有完扩容，会再次进入tryGrow()方法
        lock.lock();
        if (newArray != null && queue == array) { // newArray数组非空null，queue还等于array
            queue = newArray;
            System.arraycopy(array, 0, newArray, 0, oldCap); // 做数组复制，依次从array的0复制oldCap的长度到newArray的0
        }
    }

    /**
     * Mechanics for poll().  Call only while holding lock.
     */
    private E dequeue() {
        // 由poll/take调用的出队列方法
        int n = size - 1; // n最后一个节点的index
        if (n < 0)
            return null;
        else {
            Object[] array = queue;
            E result = (E) array[0]; // 队列头的值
            E x = (E) array[n]; // 队列尾的值
            array[n] = null; // 清空队列尾部
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftDownComparable(0, x, array, n); // 将最后一个节点的值x放在根节点索引为0上，然后开始做降序操作，只需要判断是否需要降序操作 -- 注意这里，传入的第四个参数，已经将size做了减一
            else
                siftDownUsingComparator(0, x, array, n, cmp); // 队列头位置即树根节点，只需要判断是否需要降序操作
            size = n;
            return result;
        }
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root.
     *
     * To simplify and speed up coercions and comparisons. the
     * Comparable and Comparator versions are separated into different
     * methods that are otherwise identical. (Similarly for siftDown.)
     * These methods are static, with heap state as arguments, to
     * simplify use in light of possible comparator exceptions.
     *
     * @param k the position to fill
     * @param x the item to insert
     * @param array the heap array
     */
    private static <T> void siftUpComparable(int k, T x, Object[] array) {
        // 原理桶：siftUpUsingComparator，不过使用的是自带的自然排序
        Comparable<? super T> key = (Comparable<? super T>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (key.compareTo((T) e) >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = key;
    }

    private static <T> void siftUpUsingComparator(int k, T x, Object[] array,
                                       Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1; // 父节点的index
            Object e = array[parent]; // 父节点的value
            if (cmp.compare(x, (T) e) >= 0) // 如果插入值x大于父节点的值e，那么满足小顶堆，直接break
                break;
            array[k] = e; // 否则，不满足小顶堆，将父节点的值交换到位置k上
            k = parent; // 继续判断当x插入到parent位置上时，是否满足小顶堆
        }
        array[k] = x; // 直到找到合适的插入位置k
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * demoting x down the tree repeatedly until it is less than or
     * equal to its children or is a leaf.
     *
     * @param k the position to fill
     * @param x the item to insert
     * @param array the heap array
     * @param n heap size
     */
    private static <T> void siftDownComparable(int k, T x, Object[] array, int n) {
        /*
         *  降序操作
         */
        if (n > 0) {
            Comparable<? super T> key = (Comparable<? super T>)x;
            int half = n >>> 1;  //n除以2，得到half，即第一个叶子节点
            while (k < half) {
                int child = (k << 1) + 1; // k索引的左孩子-- 一定存在
                Object c = array[child];
                int right = child + 1; // k索引的右孩子 -- 不一定存在
                // right < n  说明右孩子存在，左孩子的值大于右孩子，因此左右孩子中最小的是右孩子
                if (right < n && ((Comparable<? super T>) c).compareTo((T) array[right]) > 0)
                    c = array[child = right]; // 将c更新为右孩子
                if (key.compareTo((T) c) <= 0)  // 若降序的key比最小的孩子还要小，满足小顶堆返回true，即break
                    break;
                array[k] = c; // 交换，将k索引上的值更改为c
                k = child; // 并将k值改为child的值
            }
            array[k] = key;
        }
    }

    private static <T> void siftDownUsingComparator(int k, T x, Object[] array,
                                                    int n,
                                                    Comparator<? super T> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare((T) c, (T) array[right]) > 0)
                    c = array[child = right];
                if (cmp.compare(x, (T) c) <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     */
    private void heapify() {
        Object[] array = queue;
        int n = size;
        int half = (n >>> 1) - 1;
        Comparator<? super E> cmp = comparator;
        if (cmp == null) {
            for (int i = half; i >= 0; i--)
                siftDownComparable(i, (E) array[i], array, n);
        }
        else {
            for (int i = half; i >= 0; i--)
                siftDownUsingComparator(i, (E) array[i], array, n, cmp);
        }
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws ClassCastException if the specified element cannot be compared
     *         with elements currently in the priority queue according to the
     *         priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        // BlockingQueue中add()添加成功返回true，添加失败抛出异常的情况
        // 原因 -- 对于无界的优先级队列来说，add()只会成功，不会失败，因为无界扩容操作的存在，所以不存在无法向队列天啊及元素的情况
        return offer(e);
    }

    /**
     * Inserts the specified element into this priority queue.
     * As the queue is unbounded, this method will never return {@code false}.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException if the specified element cannot be compared
     *         with elements currently in the priority queue according to the
     *         priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        // BlockingQueue中offer()操作会立即返回true或者false --- 对于无界的优先级队列来说，offer()只会成功，不会失败
        if (e == null)
            throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock(); // 加锁
        int n, cap; // n是当前size大小，cap是数组容量，array是queue的引用
        Object[] array;
        // ① while循环判断是否需要扩容
        while ((n = size) >= (cap = (array = queue).length))
            tryGrow(array, cap);
        try {
            Comparator<? super E> cmp = comparator;
            // 在位置n上准备插入元素e，通过siftUp()两个方法判断是否需要做堆升序操作 -- 小顶堆
            if (cmp == null)
                siftUpComparable(n, e, array);
            else
                siftUpUsingComparator(n, e, array, cmp);
            size = n + 1;
            notEmpty.signal(); // 有元素入队列，唤醒消费者
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * Inserts the specified element into this priority queue.
     * As the queue is unbounded, this method will never block.
     *
     * @param e the element to add
     * @throws ClassCastException if the specified element cannot be compared
     *         with elements currently in the priority queue according to the
     *         priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public void put(E e) {
        // BlockingQueue中put()操作会阻塞
        // 原因 -- 对于无界的优先级队列来说，put()由于无界扩容操作的存在，不存在队列满需要阻塞的情况
        offer(e); // never need to block
    }

    /**
     * Inserts the specified element into this priority queue.
     * As the queue is unbounded, this method will never block or
     * return {@code false}.
     *
     * @param e the element to add
     * @param timeout This parameter is ignored as the method never blocks
     * @param unit This parameter is ignored as the method never blocks
     * @return {@code true} (as specified by
     *  {@link BlockingQueue#offer(Object,long,TimeUnit) BlockingQueue.offer})
     * @throws ClassCastException if the specified element cannot be compared
     *         with elements currently in the priority queue according to the
     *         priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        // BlockingQueue中offer(timeout)操作会超时等待阻塞
        // 原因 -- 对于无界的优先级队列来说，offer(timeout)由于无界扩容操作的存在，不存在队列满需要阻塞的情况，因此也不存在超时阻塞等待的说法
        // 注意：这个timeout针对的是队列已满后，插入操作需要阻塞的超时等待时间，而非扩容的超时等待时间
        return offer(e); // never need to block
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 加锁，调用出队列的方法
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ( (result = dequeue()) == null)
                notEmpty.await();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ( (result = dequeue()) == null && nanos > 0)
                nanos = notEmpty.awaitNanos(nanos);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        // 加锁读取顶部元素
        lock.lock();
        try {
            return (size == 0) ? null : (E) queue[0];
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the comparator used to order the elements in this queue,
     * or {@code null} if this queue uses the {@linkplain Comparable
     * natural ordering} of its elements.
     *
     * @return the comparator used to order the elements in this queue,
     *         or {@code null} if this queue uses the natural
     *         ordering of its elements
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always returns {@code Integer.MAX_VALUE} because
     * a {@code PriorityBlockingQueue} is not capacity constrained.
     * @return {@code Integer.MAX_VALUE} always
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private int indexOf(Object o) {
        // 时间复杂度 O(n)
        if (o != null) {
            Object[] array = queue;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.equals(array[i]))
                    return i; // 返回索引
        }
        return -1; // 元素不存在
    }

    /**
     * Removes the ith element from queue.
     */
    private void removeAt(int i) {
        Object[] array = queue;
        int n = size - 1; // size是下一个add\put\offer的位置，因此size-1表示当前数组的最后一个元素
        // 移除的是数组最后一个元素
        if (n == i)
            array[i] = null;
        else {
            /*
             * 已小顶堆删除而言：将删除位置index的元素y与最后一个叶子节点的值x交换，并将size-1，然后再去判断将x插入到index后，是否依然满足小顶堆的需求，否则调整
             */
            // 需要移动的元素moved -
            E moved = (E) array[n];
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            // 先尝试升序
            if (cmp == null)
                siftDownComparable(i, moved, array, n);
            else
                siftDownUsingComparator(i, moved, array, n, cmp);
            if (array[i] == moved) { // 插入位置i上还是moved元素，表示没有发生升序操作，尝试做降序操作
                if (cmp == null)
                    siftUpComparable(i, moved, array);
                else
                    siftUpUsingComparator(i, moved, array, cmp);
            }
        }
        size = n;
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.  Returns {@code true} if and only if this queue contained
     * the specified element (or equivalently, if this queue changed as a
     * result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i == -1)
                return false;
            removeAt(i);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Identity-based version for use in Itr.remove
     */
    void removeEQ(Object o) {
        /*
         * 默认方法，主要在Itr中使用的基于身份的版本
         */
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            for (int i = 0, n = size; i < n; i++) {
                if (o == array[i]) {
                    removeAt(i);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
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
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return indexOf(o) != -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The returned array elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return Arrays.copyOf(queue, size);
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (n == 0)
                return "[]";
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < n; ++i) {
                Object e = queue[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (i != n - 1)
                    sb.append(',').append(' ');
            }
            return sb.append(']').toString();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(size, maxElements);
            for (int i = 0; i < n; i++) {
                c.add((E) queue[0]); // In this order, in case add() throws.
                dequeue();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        // 以原子方式从此队列中删除所有元素。此呼叫返回后，队列将为空。
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            int n = size;
            size = 0;
            for (int i = 0; i < n; i++)
                array[i] = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(T[] a) {
        // 将优先级队列的数组复制给数组a
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            // 如果目标数组a的长度小于当前优先级拥有的元素数量，调用Arrays.copyOf()
            if (a.length < n)
                // Make a new array of a's runtime type, but my contents:
                return (T[]) Arrays.copyOf(queue, size, a.getClass());
            System.arraycopy(queue, 0, a, 0, n);
            if (a.length > n)
                a[n] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an iterator over the elements in this queue. The
     * iterator does not return the elements in any particular order.
     *
     * <p>The returned iterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * @return an iterator over the elements in this queue
     */
    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    /**
     * Snapshot iterator that works off copy of underlying q array.
     */
    final class Itr implements Iterator<E> {
        final Object[] array; // Array of all elements
        int cursor;           // index of next element to return
        int lastRet;          // index of last element, or -1 if no such

        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E)array[cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            removeEQ(array[lastRet]);
            lastRet = -1;
        }
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * For compatibility with previous version of this class, elements
     * are first copied to a java.util.PriorityQueue, which is then
     * serialized.
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        lock.lock();
        try {
            // avoid zero capacity argument
            q = new PriorityQueue<E>(Math.max(size, 1), comparator);
            q.addAll(this);
            s.defaultWriteObject();
        } finally {
            q = null;
            lock.unlock();
        }
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
        try {
            s.defaultReadObject();
            int sz = q.size();
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, sz);
            this.queue = new Object[sz];
            comparator = q.comparator();
            addAll(q);
        } finally {
            q = null;
        }
    }

    // Similar to Collections.ArraySnapshotSpliterator but avoids
    // commitment to toArray until needed
    static final class PBQSpliterator<E> implements Spliterator<E> {
        final PriorityBlockingQueue<E> queue;
        Object[] array;
        int index;
        int fence;

        PBQSpliterator(PriorityBlockingQueue<E> queue, Object[] array,
                       int index, int fence) {
            this.queue = queue;
            this.array = array;
            this.index = index;
            this.fence = fence;
        }

        final int getFence() {
            int hi;
            if ((hi = fence) < 0)
                hi = fence = (array = queue.toArray()).length;
            return hi;
        }

        public Spliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new PBQSpliterator<E>(queue, array, lo, index = mid);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Object[] a; int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array) == null)
                fence = (a = queue.toArray()).length;
            if ((hi = fence) <= a.length &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept((E)a[i]); } while (++i < hi);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            if (getFence() > index && index >= 0) {
                @SuppressWarnings("unchecked") E e = (E) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() { return (long)(getFence() - index); }

        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    /**
     * Returns a {@link Spliterator} over the elements in this queue.
     *
     * <p>The returned spliterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#NONNULL}.
     *
     * @implNote
     * The {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new PBQSpliterator<E>(this, null, 0, -1);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long allocationSpinLockOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = PriorityBlockingQueue.class;
            allocationSpinLockOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("allocationSpinLock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
