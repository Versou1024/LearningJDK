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
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.ref.WeakReference;
import java.util.Spliterators;
import java.util.Spliterator;

/**
 * A bounded {@linkplain BlockingQueue blocking queue} backed by an
 * array.  This queue orders elements FIFO (first-in-first-out).  The
 * <em>head</em> of the queue is that element that has been on the
 * queue the longest time.  The <em>tail</em> of the queue is that
 * element that has been on the queue the shortest time. New elements
 * are inserted at the tail of the queue, and the queue retrieval
 * operations obtain elements at the head of the queue.
 *
 * <p>This is a classic &quot;bounded buffer&quot;, in which a
 * fixed-sized array holds elements inserted by producers and
 * extracted by consumers.  Once created, the capacity cannot be
 * changed.  Attempts to {@code put} an element into a full queue
 * will result in the operation blocking; attempts to {@code take} an
 * element from an empty queue will similarly block.
 *
 * <p>This class supports an optional fairness policy for ordering
 * waiting producer and consumer threads.  By default, this ordering
 * is not guaranteed. However, a queue constructed with fairness set
 * to {@code true} grants threads access in FIFO order. Fairness
 * generally decreases throughput but reduces variability and avoids
 * starvation.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {

    /**
     * Serialization ID. This class relies on default serialization
     * even for the items array, which is default-serialized, even if
     * it is empty. Otherwise it could not be declared final, which is
     * necessary here.
     */
    private static final long serialVersionUID = -817911632652898426L;

    /** The queued items */
    final Object[] items; // 使用数组存放

    /** items index for next take, poll, peek or remove */
    int takeIndex; // 下一次 take、poll、peek、remove的index位置

    /** items index for next put, offer, or add */
    int putIndex; // 下一次put offer add的index位置

    /** Number of elements in the queue */
    int count; // 队列中的元素数

    /*
     * Concurrency control uses the classic two-condition algorithm
     * found in any textbook.
     */

    /** Main lock guarding all access */
    final ReentrantLock lock; // 整个类的锁

    /** Condition for waiting takes */
    private final Condition notEmpty; // 条件对象为等待直到队列非空

    /** Condition for waiting puts */
    private final Condition notFull; // 条件对象为等待直到队列非满

    /**
     * Shared state for currently active iterators, or null if there
     * are known not to be any.  Allows queue operations to update
     * iterator state.
     */
    transient Itrs itrs = null; // 当前活动迭代器的共享状态，如果已知没有，则为null。允许队列操作更新迭代器状态。

    // Internal helper methods

    /**
     * Circularly decrement i.
     */
    final int dec(int i) {
        return ((i == 0) ? items.length : i) - 1;
    }

    /**
     * Returns item at index i.
     */
    @SuppressWarnings("unchecked")
    final E itemAt(int i) {
        return (E) items[i];
    }

    /**
     * Throws NullPointerException if argument is null.
     *
     * @param v the element
     */
    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(E x) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        final Object[] items = this.items;
        items[putIndex] = x;
        // 注意：putIndex等于容量，那么putIndex=0，表示下次put、offer、add根据putIndex入队列即可，
        if (++putIndex == items.length)
            putIndex = 0;
        count++;
        notEmpty.signal(); // 重点：调用enqueue()，唤醒某个消费者
    }

    /**
     * Extracts element at current take position, advances, and signals.
     * Call only when holding lock.
     */
    private E dequeue() {
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        final Object[] items = this.items;
        @SuppressWarnings("unchecked")
        E x = (E) items[takeIndex]; //takeIndex为队列头部元素
        // NOTE：这就是为什么不支持null值的原因？
        // 采用逻辑删除+延迟删除操作：不会立即删除元素，而是将元素值为null，等待某个方法发现为null值后，将其删除
        items[takeIndex] = null;
        if (++takeIndex == items.length) // 整个队列的是一个循环队列，其中从takeIndex是队列头，putIndex是队列尾，中间就是有效的元素存储
            takeIndex = 0;
        count--;
        if (itrs != null)
            itrs.elementDequeued();
        notFull.signal(); // 有元素出队列，唤醒某个生产者
        return x;
    }

    /**
     * Deletes item at array index removeIndex.
     * Utility for remove(Object) and iterator.remove.
     * Call only when holding lock.
     */
    void removeAt(final int removeIndex) {
        // assert lock.getHoldCount() == 1;
        // assert items[removeIndex] != null;
        // assert removeIndex >= 0 && removeIndex < items.length;
        final Object[] items = this.items;
        // 情况1：lastRet==takeIndex
        if (removeIndex == takeIndex) {
            // removing front item; just advance
            items[takeIndex] = null;
            if (++takeIndex == items.length)
                takeIndex = 0;
            count--;
            // // 如果迭代器链存在迭代器会进一步处理各个迭代器的遍历状态
            if (itrs != null)
                itrs.elementDequeued();
        } else {
            // // 情况2：lastRet在takeIndex之后
            // an "interior" remove

            // slide over all others up through putIndex.
            final int putIndex = this.putIndex;
            // // 从lastRet位开始,前一位覆盖后一位
            for (int i = removeIndex;;) {
                int next = i + 1;
                if (next == items.length)
                    next = 0;
                if (next != putIndex) {
                    items[i] = items[next];
                    i = next;
                } else {
                    items[i] = null;
                    this.putIndex = i;
                    break;
                }
            }
            count--;
            if (itrs != null)
                itrs.removedAt(removeIndex);
        }
        notFull.signal();
    }

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity and default access policy.
     *
     * @param capacity the capacity of this queue
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity and the specified access policy.
     *
     * @param capacity the capacity of this queue
     * @param fair if {@code true} then queue accesses for threads blocked
     *        on insertion or removal, are processed in FIFO order;
     *        if {@code false} the access order is unspecified.
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Object[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity, the specified access policy and initially containing the
     * elements of the given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param capacity the capacity of this queue
     * @param fair if {@code true} then queue accesses for threads blocked
     *        on insertion or removal, are processed in FIFO order;
     *        if {@code false} the access order is unspecified.
     * @param c the collection of elements to initially contain
     * @throws IllegalArgumentException if {@code capacity} is less than
     *         {@code c.size()}, or less than 1.
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public ArrayBlockingQueue(int capacity, boolean fair,
                              Collection<? extends E> c) {
        this(capacity, fair);

        final ReentrantLock lock = this.lock;
        lock.lock(); // Lock only for visibility, not mutual exclusion
        try {
            int i = 0;
            try {
                for (E e : c) {
                    checkNotNull(e);
                    items[i++] = e;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalArgumentException();
            }
            count = i;
            putIndex = (i == capacity) ? 0 : i;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue if it is
     * possible to do so immediately without exceeding the queue's capacity,
     * returning {@code true} upon success and throwing an
     * {@code IllegalStateException} if this queue is full.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IllegalStateException if this queue is full
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        return super.add(e);
    }

    /**
     * Inserts the specified element at the tail of this queue if it is
     * possible to do so immediately without exceeding the queue's capacity,
     * returning {@code true} upon success and {@code false} if this queue
     * is full.  This method is generally preferable to method {@link #add},
     * which can fail to insert an element only by throwing an exception.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lock(); // 加锁，直到成功
        try {
            // 同步代码 - 使得加入元素是线程安全的；
            // 只要当前元素数量 不等于 容量，就允许入队列，返回true
            // 否则返回false
            if (count == items.length)
                return false;
            else {
                enqueue(e); // 线程安全的
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting
     * for space to become available if the queue is full.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            // 阻塞队列满，就进入等待 -- while用来避免虚假唤醒
            while (count == items.length)
                notFull.await();
            enqueue(e); // 线程安全的
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting
     * up to the specified wait time for space to become available if
     * the queue is full.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {

        checkNotNull(e);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                if (nanos <= 0)
                    return false; // 若超时，返回false
                nanos = notFull.awaitNanos(nanos); //超时等待
            }
            enqueue(e); // 线程安全的
            return true;
        } finally {
            lock.unlock();
        }
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 队列为空，立即返回null，否则取出元素
            return (count == 0) ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            // 队列为空，阻塞直到可用被唤醒
            while (count == 0)
                notEmpty.await();
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            // 队列为空，超时等待
            while (count == 0) {
                if (nanos <= 0)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // peek 查看队头元素
            return itemAt(takeIndex); // null when queue is empty
        } finally {
            lock.unlock();
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE
    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 这里需要注意：count没有被声明为volatile的
            // 因此需哟啊通过加锁，来保证获取锁后访问的共享变量都是从主内存中获取的，这保证了变量的内存可见性
            return count;
        } finally {
            lock.unlock();
        }
    }

    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.
    /**
     * Returns the number of additional elements that this queue can ideally
     * (in the absence of memory or resource constraints) accept without
     * blocking. This is always equal to the initial capacity of this queue
     * less the current {@code size} of this queue.
     *
     * <p>Note that you <em>cannot</em> always tell if an attempt to insert
     * an element will succeed by inspecting {@code remainingCapacity}
     * because it may be the case that another thread is about to
     * insert or remove an element.
     */
    public int remainingCapacity() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return items.length - count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.
     * Returns {@code true} if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * <p>Removal of interior elements in circular array based queues
     * is an intrinsically slow and disruptive operation, so should
     * be undertaken only in exceptional circumstances, ideally
     * only when the queue is known not to be accessible by other
     * threads.
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        if (o == null) return false;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    if (o.equals(items[i])) {
                        removeAt(i);
                        return true;
                    }
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
            }
            return false;
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
        if (o == null) return false;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    if (o.equals(items[i]))
                        return true;
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence.
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
        Object[] a;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final int count = this.count;
            a = new Object[count];
            int n = items.length - takeIndex;
            if (count <= n)
                System.arraycopy(items, takeIndex, a, 0, count);
            else {
                System.arraycopy(items, takeIndex, a, 0, n);
                System.arraycopy(items, 0, a, n, count - n);
            }
        } finally {
            lock.unlock();
        }
        return a;
    }

    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence; the runtime type of the returned array is that of
     * the specified array.  If the queue fits in the specified array, it
     * is returned therein.  Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this queue.
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
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final int count = this.count;
            final int len = a.length;
            if (len < count)
                a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), count);
            int n = items.length - takeIndex;
            if (count <= n)
                System.arraycopy(items, takeIndex, a, 0, count);
            else {
                System.arraycopy(items, takeIndex, a, 0, n);
                System.arraycopy(items, 0, a, n, count - n);
            }
            if (len > count)
                a[count] = null;
        } finally {
            lock.unlock();
        }
        return a;
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k == 0)
                return "[]";

            final Object[] items = this.items;
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = takeIndex; ; ) {
                Object e = items[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (--k == 0)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
                if (++i == items.length)
                    i = 0;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    items[i] = null;
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
                takeIndex = putIndex;
                count = 0;
                if (itrs != null)
                    itrs.queueIsEmpty();
                for (; k > 0 && lock.hasWaiters(notFull); k--)
                    notFull.signal();
            }
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
        checkNotNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(maxElements, count);
            int take = takeIndex;
            int i = 0;
            try {
                while (i < n) {
                    @SuppressWarnings("unchecked")
                    E x = (E) items[take];
                    c.add(x);
                    items[take] = null;
                    if (++take == items.length)
                        take = 0;
                    i++;
                }
                return n;
            } finally {
                // Restore invariants even if c.add() threw
                if (i > 0) {
                    count -= i;
                    takeIndex = take;
                    if (itrs != null) {
                        if (count == 0)
                            itrs.queueIsEmpty();
                        else if (i > take)
                            itrs.takeIndexWrapped();
                    }
                    for (; i > 0 && lock.hasWaiters(notFull); i--)
                        notFull.signal();
                }
            }
        } finally {
            lock.unlock();
        }
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

    /**
     * Shared data between iterators and their queue, allowing queue
     * modifications to update iterators when elements are removed.
     *
     * This adds a lot of complexity for the sake of correctly
     * handling some uncommon operations, but the combination of
     * circular-arrays and supporting interior removes (i.e., those
     * not at head) would cause iterators to sometimes lose their
     * places and/or (re)report elements they shouldn't.  To avoid
     * this, when a queue has one or more iterators, it keeps iterator
     * state consistent by:
     *
     * (1) keeping track of the number of "cycles", that is, the
     *     number of times takeIndex has wrapped around to 0.
     * (2) notifying all iterators via the callback removedAt whenever
     *     an interior element is removed (and thus other elements may
     *     be shifted).
     *
     * These suffice to eliminate iterator inconsistencies, but
     * unfortunately add the secondary responsibility of maintaining
     * the list of iterators.  We track all active iterators in a
     * simple linked list (accessed only when the queue's lock is
     * held) of weak references to Itr.  The list is cleaned up using
     * 3 different mechanisms:
     *
     * (1) Whenever a new iterator is created, do some O(1) checking for
     *     stale list elements.
     *
     * (2) Whenever takeIndex wraps around to 0, check for iterators
     *     that have been unused for more than one wrap-around cycle.
     *
     * (3) Whenever the queue becomes empty, all iterators are notified
     *     and this entire data structure is discarded.
     *
     * So in addition to the removedAt callback that is necessary for
     * correctness, iterators have the shutdown and takeIndexWrapped
     * callbacks that help remove stale iterators from the list.
     *
     * Whenever a list element is examined, it is expunged if either
     * the GC has determined that the iterator is discarded, or if the
     * iterator reports that it is "detached" (does not need any
     * further state updates).  Overhead is maximal when takeIndex
     * never advances, iterators are discarded before they are
     * exhausted, and all removals are interior removes, in which case
     * all stale iterators are discovered by the GC.  But even in this
     * case we don't increase the amortized complexity.
     *
     * Care must be taken to keep list sweeping methods from
     * reentrantly invoking another such method, causing subtle
     * corruption bugs.
     */
    class Itrs {

        /**
         * Node in a linked list of weak iterator references.
         */
        private class Node extends WeakReference<Itr> {
            Node next;

            Node(Itr iterator, Node next) {
                super(iterator);
                this.next = next;
            }
        }

        /** Incremented whenever takeIndex wraps around to 0
         * <p> 该属性非常重要，它记录takeIndex索引重新回到0号索引位的次数
         *    由此来描述takeIndex索引位的“圈数”</p>
         */
        int cycles = 0;

        /** Linked list of weak iterator references */
        private Node head; // 迭代器弱引用的链表头

        /**
         * Used to expunge stale iterators
         *  <p>Itrs迭代器组在特定的场景下会进行Node单向链表的清理，该属性表示上次一清理到的Node节点
         */
        private Node sweeper = null; // 记录上次探测的结束位置节点，下次从此开始往后探测。

        // 探测范围
        private static final int SHORT_SWEEP_PROBES = 4;
        private static final int LONG_SWEEP_PROBES = 16;

        Itrs(Itr initial) {
            register(initial);
        }

        /**
         * Sweeps itrs, looking for and expunging stale iterators.
         * If at least one was found, tries harder to find more.
         * Called only from iterating thread.
         *
         * @param tryHarder whether to start in try-harder mode, because
         * there is known to be at least one iterator to collect
         */
        void doSomeSweeping(boolean tryHarder) {
            // 清除迭代器链中无效的迭代器结点：
            // 1、结点持有的Itr对象为空，说明被GC回收，说明使用线程完成迭代
            // 2、迭代器Itr为 DETACHED 模式，对于迭代结束或数据过时的迭代器会被置于 DETACHED 模式

            // tryHarder 为true则 probes 为 16，否则为 4
            // probes 代表本次的探测长度
            int probes = tryHarder ? LONG_SWEEP_PROBES : SHORT_SWEEP_PROBES;
            //  o 代表 p 的前一个节点，用于链表中节点的删除
            Node o, p;
            final Node sweeper = this.sweeper;
            /**
             * passedGo：限制一次遍历
             * 	若从头开始遍历,passedGo设为true,
             * 		如果当前结点为null且probes>0可以直接break
             * 	若从中开始遍历,passedGo设为false,
             * 		如果当前结点为null且probes>0就会转回到头部继续遍历
             */
            boolean passedGo;   // 将搜索限制为一次完整扫描

            if (sweeper == null) {
                o = null;
                p = head;
                passedGo = true;
            } else {
                o = sweeper;
                p = o.next;
                passedGo = false;
            }
            // 如果遇到结点为空说明遍历到了链表尾
            // 就需要根据passedGo判断是否继续探测
            for (; probes > 0; probes--) {
                // (1) // passedGo 为true,从链表头开始探测,那就不需要继续探测,直接break
                if (p == null) {
                    if (passedGo)
                        break;
                    // passedGo 为false，说明本次遍历是从中间某位置开始，
                    // 也就是说链表前面有一段是未遍历的，
                    // 遍历到了尾部需要转回到头部继续遍历
                    o = null;
                    p = head;
                    passedGo = true;
                }
                final Itr it = p.get();
                final Node next = p.next;
                //	(2) 删除此节点
                // 1.节点持有的迭代器对象为null
                // 2.数组为空或数据过时导致的DETACHED模式
                if (it == null || it.isDetached()) {
                    /**
                     * ①当发现了一个被抛弃或过时的迭代器，
                     * 则将探测范围probes变为16，相当于延长探测范围。
                     * 这样做的目的:
                     * 		1.减少该方法持有锁的时间(在当前探测范围没有失效结点就退出)
                     * 		2.保证清理迭代器的高效(当发现存在失效迭代器,就扩大范围)
                     */
                    probes = LONG_SWEEP_PROBES; // "try harder"
                    p.clear(); // 清空引用
                    p.next = null; // 清空next
                    // ② 从链表头开始探测,在上面初始化时(o=null)或从链表尾转到链表头探测
                    if (o == null) {
                        head = next;
                        if (next == null) {
                            // We've run out of iterators to track; retire
                            itrs = null;
                            return;
                        }
                    }
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            // 记录本次遍历结束位置节点
            this.sweeper = (p == null) ? null : o;
        }

        /**
         * Adds a new iterator to the linked list of tracked iterators.
         */
        void register(Itr itr) {
            // 迭代器想itrs中注册，就是将其头插入头itrs的若引用链表中
            head = new Node(itr, head);
        }

        /**
         * Called whenever takeIndex wraps around to 0.
         *
         * Notifies all iterators, and expunges any that are now stale.
         */
        void takeIndexWrapped() {
            // 清除迭代器链失效迭代器以及新的一轮循环而导致失效的迭代
            // 轮数+1
            cycles++;
            // 遍历所有迭代器
            for (Node o = null, p = head; p != null;) {
                final Itr it = p.get();
                final Node next = p.next;
                // 迭代器失效 it.takeIndexWrapped() 判断当前迭代器是否失效
                if (it == null || it.takeIndexWrapped()) {
                    // unlink p
                    // assert it == null || it.isDetached();
                    p.clear();
                    p.next = null;
                    if (o == null)
                        head = next;
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            // 所有迭代器都失效,itrs置为null
            if (head == null)   // no more iterators to track
                itrs = null;
        }

        /**
         * Called whenever an interior remove (not at takeIndex) occurred.
         *
         * Notifies all iterators, and expunges any that are now stale.
         */
        void removedAt(int removedIndex) {
            for (Node o = null, p = head; p != null;) {
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.removedAt(removedIndex)) {
                    // unlink p
                    // assert it == null || it.isDetached();
                    p.clear();
                    p.next = null;
                    if (o == null)
                        head = next;
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            if (head == null)   // no more iterators to track
                itrs = null;
        }

        /**
         * Called whenever the queue becomes empty.
         *
         * Notifies all active iterators that the queue is empty,
         * clears all weak refs, and unlinks the itrs datastructure.
         */
        void queueIsEmpty() {
            // 遍历迭代器集合，将每个迭代器清除被关闭
            for (Node p = head; p != null; p = p.next) {
                Itr it = p.get();
                if (it != null) {
                    p.clear(); // 清除node中的弱引用
                    it.shutdown();
                }
            }
            head = null;
            itrs = null;
        }

        /**
         * Called whenever an element has been dequeued (at takeIndex).
         */
        void elementDequeued() {
            // ① 每次删除元素，都会从 dequeue() 或者 removeAt() 中被调用

            // 1.阻塞队列元素已经被消耗为空,迭代器链中所有迭代器失效。
            if (count == 0)
                // 清除所有迭代器
                queueIsEmpty();
            // 2.下一次takeIndex == 0表示新的一轮循环消费,可能导致takeIndex覆盖
            // 迭代器中记录的变量值,从而导致迭代器失效
            else if (takeIndex == 0)
                // 清除迭代器链失效迭代器以及新的一轮循环而导致失效的迭代器
                takeIndexWrapped();
        }
    }

    /**
     * Iterator for ArrayBlockingQueue.
     *
     * To maintain weak consistency with respect to puts and takes, we
     * read ahead one slot, so as to not report hasNext true but then
     * not have an element to return.
     *
     * We switch into "detached" mode (allowing prompt unlinking from
     * itrs without help from the GC) when all indices are negative, or
     * when hasNext returns false for the first time.  This allows the
     * iterator to track concurrent updates completely accurately,
     * except for the corner case of the user calling Iterator.remove()
     * after hasNext() returned false.  Even in this case, we ensure
     * that we don't remove the wrong element by keeping track of the
     * expected element to remove, in lastItem.  Yes, we may fail to
     * remove lastItem from the queue if it moved due to an interleaved
     * interior remove while in detached mode.
     */
    private class Itr implements Iterator<E> {
        /** Index to look for new nextItem; NONE at end */
        private int cursor; // 光标

        /** Element to be returned by next call to next(); null if none */
        private E nextItem; // 下一个值：专门为支持hashNext方法和next方法配合所使用的属性，用于在调用next方法返回数据

        /** Index of nextItem; NONE if none, REMOVED if removed elsewhere */
        private int nextIndex; // 下一个索引

        /** Last element returned; null if none or not detached. */
        private E lastItem; // 上一个值：最后一次（上一次）迭代器遍历操作时返回的元素

        /** Index of lastItem, NONE if none, REMOVED if removed elsewhere */
        private int lastRet; // 上一个索引

        /**
         * Previous value of takeIndex, or DETACHED when detached
         * <p> 该变量表示本迭代器最后一次（上一次）从ArrayBlockingQueue队列中获取到的takeIndex索引位
         *   该属性还有一个重要的作用，用来表示当前迭代器是否是“独立”工作模式（或者迭代器是否失效）
         */
        private int prevTakeIndex; // 代表本次遍历开始的索引，每此 next 都会进行修正

        /**
         * Previous value of iters.cycles
         * <p> 最后一次（上一次）从ArrayBlockingQueue队列获取的takeIndex索引位回到0号索引位的次数
         * 这个值非常重要，是判定迭代器是否有效的重要依据
         */
        private int prevCycles; // Itrs 管理 Itr 链，它里面有个变量 cycles 记录 takeIndex 回到 0 位置的次数，迭代器的 prevCycles 存储该值

        /** Special index value indicating "not available" or "undefined" */
        private static final int NONE = -1; // 用于三个下标变量：cursor，nextIndex，lastRet；这三个下标变量用于迭代功能的实现。表明该位置数据不可用或未定义

        /**
         * Special index value indicating "removed elsewhere", that is,
         * removed by some operation other than a call to this.remove().
         */
        private static final int REMOVED = -2; // 用于lastRet 与 nextIndex。表明数据过时或被删除

        /** Special value for prevTakeIndex indicating "detached mode" */
        private static final int DETACHED = -3; //专门用于prevTakeIndex ，isDetached方法通过其来判断迭代器状态是否失效

        Itr() {
            // assert lock.getHoldCount() == 0;
            lastRet = NONE;
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            // 在创建迭代器过程加锁，防止队列改变导致初始化错误
            lock.lock();
            try {
                if (count == 0) {
                    // assert itrs == null;
                    cursor = NONE;
                    nextIndex = NONE;
                    prevTakeIndex = DETACHED; // 迭代器为DETACHED模式 - 失效状态
                } else {
                    final int takeIndex = ArrayBlockingQueue.this.takeIndex;
                    // 初始化遍历起始索引prevTakeIndex为队头索引takeIndex
                    prevTakeIndex = takeIndex;
                    // 下一个遍历元素nextItem为队头元素itemAt(takeIndex)
                    // nextIndex为nextItem的索引
                    nextItem = itemAt(nextIndex = takeIndex);
                    // 获取nextIndex下一个元素的索引
                    cursor = incCursor(takeIndex);
                    // 如果itrs为null,就初始化;
                    if (itrs == null) {
                        itrs = new Itrs(this);
                    // 否则将当前迭代器注册到itrs中,并清理失效迭代器
                    } else {
                        itrs.register(this); // in this order
                        itrs.doSomeSweeping(false);
                    }
                    // 当前迭代器记录最新的轮数
                    prevCycles = itrs.cycles;
                }
            } finally {
                lock.unlock();
            }
        }

        boolean isDetached() {
            // assert lock.getHoldCount() == 1;
            // 只要可以消费的index小于0，就表示该迭代器已经结束
            return prevTakeIndex < 0;
        }

        private int incCursor(int index) {
            // assert lock.getHoldCount() == 1;
            // index 等于队列容量，循环到index=0
            // index 等于putIndex，表示当前位置无元素，返回NONE
            if (++index == items.length)
                index = 0;
            // 当遍历到 putIndex ，代表数据遍历结束，应该终止迭代，
            // 将 cursor 置为 NONE 标识，cursor 置为 NONE 后会引起迭代器的终止，逻辑在 next 与 hasNext 方法中。
            if (index == putIndex)
                index = NONE;
            return index;
        }

        /**
         * Returns true if index is invalidated by the given number of
         * dequeues, starting from prevTakeIndex.
         */
        private boolean invalidated(int index, int prevTakeIndex,
                                    long dequeues, int length) {
            /*
             * 下标变量小于0返回false，表明该下标变量的值不需要进行更改
             * 有三个状态 NONE ，REMOVED，DETACHED 它们皆小于0。
             * 	DETACHED：专门用于preTakeIndex使用，isDetached方法通过其来判断，迭代器是否过期
             * 	NONE：用于cursor，nextIndex，lastRet；这三个索引值用于迭代功能的实现
             * 	NONE：表明迭代结束可能因为数组为空或是遍历完
             * 	REMOVED：表示lastRet 与 nextIndex 的数据过时
             */

            // 如果需要判定的索引位本来就已经失效了（NONE、REMOVED、DETACHED这些常量都为负数）
            if (index < 0)
                return false;
            // 计算index索引位置和prevTakeIndex索引位置的距离
            // 最简单的就是当前index的索引位减去prevTakeIndex的索引位值
            int distance = index - prevTakeIndex;
            // 如果以上计算出来是一个负值，说明index的索引位已经“绕场一周”
            // 这时在distance的基础上面，增加一个队列长度值，
            if (distance < 0)
                distance += length;
            // dequeues 已经出队的元素数量，如果distance小于dequeues，说明还在出队元素的范围中，那么就是失效的，返回true表时需要更新这个index
            return dequeues > distance;
        }

        // 该方法在用于在Itr迭代器多次操作的间歇间，ArrayBlockingQueue队列状态发生变化的情况下
        // 对Itr的重要索引位置进行修正（甚至是让Itr在极端情况下无效
        private void incorporateDequeues() {
            // 由于多线程下为了确保安全迭代线程每次调用next都要先获取独占锁，得不到便需等待，
            // 等到被唤醒继续执行就需要对数组此时的状况进行判断，判断当前迭代器要获取的数据是否已经过时，
            // 将最新的 takeIndex 赋给迭代器的 cursor，从而确保迭代器不会返回过时的数据。
            /*
             * 请注意：incorporateDequeues()方法的目标是在Itr迭代器两次操作间隙ArrayBlockingQueue队列发生读写操作的情况下，
             * 尽可能修正Itr迭代器的索引位值，使它能从下一个正确的索引位置重新开始遍历数据，
             * 而不是“尽可能让Itr迭代器作废”。这从incorporateDequeues()方法中确认Itr迭代器过期
             * 所使用的相对苛刻的判定条件就可以看出来 “cursor < 0 && nextIndex < 0 && lastRet < 0”
             */

            final int cycles = itrs.cycles; // 生命周期
            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
            final int prevCycles = this.prevCycles;
            final int prevTakeIndex = this.prevTakeIndex;

            // 如果迭代器记录的轮数与最新的轮数不同
            // 或者迭代器记录的当前遍历元素的索引与最新的队列头的索引不同
            // 需要修改迭代器
            if (cycles != prevCycles || takeIndex != prevTakeIndex) {
                final int len = items.length;
                // 这句计算非常重要，就是计算在所有读取操作后，两次takeIndex索引产生的索引距离（已出队的数据量）
                long dequeues = (cycles - prevCycles) * len
                    + (takeIndex - prevTakeIndex);


                // 检查三个下标变量 -- lastRet，nextIndex，cursor
                // 查看它们指向的数据是否已过时，所谓过时指的是此时 takeIndex 已在其前
                // 若过时就将lastRet与nextIndex置为REMOVED，cursor置为此时的takeIndex
                if (invalidated(lastRet, prevTakeIndex, dequeues, len))
                    lastRet = REMOVED;
                if (invalidated(nextIndex, prevTakeIndex, dequeues, len))
                    nextIndex = REMOVED;
                if (invalidated(cursor, prevTakeIndex, dequeues, len))
                    cursor = takeIndex;

                // 如果cursor索引、nextIndex索引、lastRet索引，则表示当前Itr游标失效
                // 调用detach()方法将当前Itr迭代器标记为失效，并清理Itrs迭代器组中的Node信息
                if (cursor < 0 && nextIndex < 0 && lastRet < 0)
                    detach();
                //  否则（大部分情况）修正Itr迭代器中的状态，以便其能从修正的位置开始进行遍历
                else {
                    this.prevCycles = cycles;
                    this.prevTakeIndex = takeIndex;
                }
            }
        }

        // 该方法负责将当前Itr迭代器置为“独立/失效”工作状态，既将prevTakeIndex设置为DETACHED
        // 这个动作可能发生在以下多种场景下：
        // 1、当Itrs迭代器组要停止对某个Itr迭代器进行状态跟踪时。
        // 2、当迭代器中已经没有更多的索引位可以遍历时。
        // 3、当迭代器发生了一些处理异常时，
        // 4、当incorporateDequeues()方法中判定三个关键索引位全部失效时（cursor < 0 && nextIndex < 0 && lastRet < 0）
        // 5、.....
        private void detach() {
            // Switch to detached mode
            // assert lock.getHoldCount() == 1;
            // assert cursor == NONE;
            // assert nextIndex < 0;
            // assert lastRet < 0 || nextItem == null;
            // assert lastRet < 0 ^ lastItem != null;
            if (prevTakeIndex >= 0) {
                // 设定一个Itr迭代器失效，就是设定prevTakeIndex属性为DETACHED常量
                prevTakeIndex = DETACHED;
                // 一旦该迭代器被标识为“独立”（无效）工作模式，则试图清理该迭代器对象在Itrs迭代器组中的监控信息
                itrs.doSomeSweeping(true);
            }
        }

        /**
         * For performance reasons, we would like not to acquire a lock in
         * hasNext in the common case.  To allow for this, we only access
         * fields (i.e. nextItem) that are not modified by update operations
         * triggered by queue modifications.
         */
        public boolean hasNext() {
            // 如果存在下一个元素
            if (nextItem != null)
                return true;
            // 能进入noNext方法,说明不存在下一个元素
            // 即 cursor == NONE 且 nextIndex == NONE
            noNext();
            return false;
        }

        private void noNext() {
            /*
             * hasNext()失败后的处理，既没有下一个元素存在
             * 即能够进入该方法说明数组元素已经全部遍历完，
             * 即cursor == putIndex且cursor = NONE ，nextIndex = NONE ， nextItem = null，这些状态会在上一次调用next方法时被设置。
             * noNext()作用：将迭代器的状态置为 DETACHED，这样才能被doSomeSweeping方法清除。
             */
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock(); // 锁住迭代器
            try {
                /**
                 * 判断该迭代器是否已经在上一次调用next方法时被变为失效
                 *  如果已经失效,那么就不需要进入代码块
                 */
                if (!isDetached()) {
                    /**
                     * 那么有的读者会有这个疑问，为什么当迭代器没有任何数据可以遍历的时候，还要通过incorporateDequeues()方法修正各索引位的值，
                     * 并且还要视图在取出lastRet索引位上的数据后，才设定迭代器失效呢？
                     * 为什么不是直接设定Itr迭代器失效就可以了呢？这个原因和remove()方法的处理逻辑有关系。
                     *
                     * remove()方法的作用是删除Itr迭代器上一次从next()方法获取数据时，其索引位上的数据（lastRet索引位上的数据）——
                     * 真正的从ArrayBlockingQueue队列中删除。一定要注意不是删除当前cursor游标指向的索引位上的数据。
                     */
                    incorporateDequeues(); // might update lastRet
                    // 如果lastRet没有过时,获取lastItem
                    if (lastRet >= 0) {
                        lastItem = itemAt(lastRet);
                        // 调用detach,将迭代器置为DETACHED状态
                        detach();
                    }
                }
                // assert isDetached(); 迭代器处于了DETACHED状态
                // assert lastRet < 0 ^ lastItem != null; 且已满足
            } finally {
                lock.unlock();
            }
        }

        public E next() {
            // assert lock.getHoldCount() == 0;
            final E x = nextItem;
            // 注意，如果nextItem中没有数据，则直接抛出异常，这就是为什么在执行next()方法前，
            // 一定要先使用hasNext()方法检查迭代器的有效性
            if (x == null)
                throw new NoSuchElementException();
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                // 如果当前迭代器不是“独立”模式（也就是说没有失效）
                // 则通过incorporateDequeues方法对lastRet、nextIndex、cursor、prevCycles、prevTakeIndex属性进行修正
                // 保证以上这些属性的状态值，和当前ArrayBlockingQueue队列集合的状态一致。
                // incorporateDequeues方法很重要，下文中立即会进行介绍
                if (!isDetached())
                    // 保证返回的元素不是过时的数据
                    incorporateDequeues();
                // assert nextIndex != NONE;
                // assert lastItem == null;
                lastRet = nextIndex;
                final int cursor = this.cursor;
                // 如果当前游标有效（不为NONE）
                if (cursor >= 0) {
                    // 那么游标的索引位置就成为下一个取数的位置
                    nextItem = itemAt(nextIndex = cursor);
                    // 接着游标索引位+1，注意：这是游标索引位可能为None
                    // 代表取出下一个数后，就再无数可取，遍历结束
                    this.cursor = incCursor(cursor);
                // 否则就认为已无数可取，迭代器工作结束
                } else {
                    // 如果cursor<0表示遍历完成,就将nextIndex置为NONE
                    // 以便下一次hasNext方法将迭代器置为DETACHED模式
                    nextIndex = NONE;
                    nextItem = null;
                }
            } finally {
                lock.unlock();
            }
            return x;
        }

        public void remove() {
            // remove方法的操作意义并不是移除当前cursor游标所指向的索引位上的数据
            // 而是移除上一次通过next()方法返回的索引位上的数据，也就是当前lastRet所指向的索引位上的数据
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                // 同样，获取操作权后，首先通过incorporateDequeues()方法
                if (!isDetached())
                    // 主要是修正lastRet是否过时,也可能让迭代器失效
                    incorporateDequeues(); // might update lastRet or detach
                final int lastRet = this.lastRet;
                this.lastRet = NONE;
                /**
                 *  迭代器上一次调用next方法返回的索引有效
                 * 	需要注意下面方法能够调用removeAt()方法说明
                 * 	lastRet==takeIndex 或 lastRet 在 takeIndex之后,
                 * 	所以在removeAt()方法中会分情况来处理.
                 */
                // 如果lastRet的索引位有效，且Itr迭代器有效，则移除ArrayBlockingQueue队列中lastRet索引位上的数据
                if (lastRet >= 0) {
                    // 如果迭代器未失效,直接删除索引lastRet对应的元素
                    if (!isDetached())
                        removeAt(lastRet);
                    // 如果lastRet的索引位有效，但Itr迭代器无效，
                    // 则移除ArrayBlockingQueue队列中lastRet索引位上的数据
                    // 还要取消lastItem对数据对象的引用
                    else {
                        final E lastItem = this.lastItem;
                        // assert lastItem != null;
                        this.lastItem = null;
                        // 如果迭代器记录的索引lastRet对应元素与lastItem相同就会删除
                        if (itemAt(lastRet) == lastItem)
                            removeAt(lastRet);
                    }
                // 如果lastRet已被标识为无效
                // 出现这种情况的场景最有可能是Itr迭代器创建时ArrayBlockingQueue队列中没有任何数据
                // 或者是Itr迭代器创建后，虽然有数据可以遍历，但是还没有使用next()方法读取任何索引位上的数据
                // 这是抛出IllegalStateException异常
                } else if (lastRet == NONE)
                    throw new IllegalStateException();
                // else lastRet == REMOVED and the last returned element was
                // previously asynchronously removed via an operation other
                // than this.remove(), so nothing to do.
                // lastRet < 0 && cursor < 0 && nextIndex < 0:迭代器遍历完成
                if (cursor < 0 && nextIndex < 0)
                    detach();
            } finally {
                lock.unlock();
                // assert lastRet == NONE;
                // assert lastItem == null;
            }
        }

        /**
         * Called to notify the iterator that the queue is empty, or that it
         * has fallen hopelessly behind, so that it should abandon any
         * further iteration, except possibly to return one more element
         * from next(), as promised by returning true from hasNext().
         */
        void shutdown() {
            // assert lock.getHoldCount() == 1;
            // 清空三个索引：cursor、nextIndex、lastRet
            cursor = NONE;
            if (nextIndex >= 0)
                nextIndex = REMOVED;
            if (lastRet >= 0) {
                lastRet = REMOVED;
                lastItem = null;
            }
            prevTakeIndex = DETACHED;
            // Don't set nextItem to null because we must continue to be
            // able to return it on next().
            //
            // Caller will unlink from itrs when convenient.
        }

        private int distance(int index, int prevTakeIndex, int length) {
            int distance = index - prevTakeIndex;
            if (distance < 0)
                distance += length;
            return distance;
        }

        /**
         * Called whenever an interior remove (not at takeIndex) occurred.
         *
         * @return true if this iterator should be unlinked from itrs
         */
        boolean removedAt(int removedIndex) {
            // assert lock.getHoldCount() == 1;
            if (isDetached())
                return true;

            final int cycles = itrs.cycles;
            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
            final int prevCycles = this.prevCycles;
            final int prevTakeIndex = this.prevTakeIndex;
            final int len = items.length;
            int cycleDiff = cycles - prevCycles;
            if (removedIndex < takeIndex)
                cycleDiff++;
            final int removedDistance =
                (cycleDiff * len) + (removedIndex - prevTakeIndex);
            // assert removedDistance >= 0;
            int cursor = this.cursor;
            if (cursor >= 0) {
                int x = distance(cursor, prevTakeIndex, len);
                if (x == removedDistance) {
                    if (cursor == putIndex)
                        this.cursor = cursor = NONE;
                }
                else if (x > removedDistance) {
                    // assert cursor != prevTakeIndex;
                    this.cursor = cursor = dec(cursor);
                }
            }
            int lastRet = this.lastRet;
            if (lastRet >= 0) {
                int x = distance(lastRet, prevTakeIndex, len);
                if (x == removedDistance)
                    this.lastRet = lastRet = REMOVED;
                else if (x > removedDistance)
                    this.lastRet = lastRet = dec(lastRet);
            }
            int nextIndex = this.nextIndex;
            if (nextIndex >= 0) {
                int x = distance(nextIndex, prevTakeIndex, len);
                if (x == removedDistance)
                    this.nextIndex = nextIndex = REMOVED;
                else if (x > removedDistance)
                    this.nextIndex = nextIndex = dec(nextIndex);
            }
            else if (cursor < 0 && nextIndex < 0 && lastRet < 0) {
                this.prevTakeIndex = DETACHED;
                return true;
            }
            return false;
        }

        /**
         * Called whenever takeIndex wraps around to zero.
         *
         * @return true if this iterator should be unlinked from itrs
         */
        boolean takeIndexWrapped() {
            // 判断当前迭代器是否失效，返回true表示迭代器失效。
            if (isDetached()) // 判断是否失效
                return true;
            // 如果迭代器记录的轮数小于最新的轮数2轮或以上
            // 说明新的数据已经将当时迭代器记录的变量索引全部覆盖
            // 即迭代器中的变量索引过时,就让该迭代器失效
            if (itrs.cycles - prevCycles > 1) {
                // All the elements that existed at the time of the last
                // 关闭迭代器,将迭代器记录的变量索引都<0
                shutdown();
                return true;
            }
            return false;
        }

//         /** Uncomment for debugging. */
//         public String toString() {
//             return ("cursor=" + cursor + " " +
//                     "nextIndex=" + nextIndex + " " +
//                     "lastRet=" + lastRet + " " +
//                     "nextItem=" + nextItem + " " +
//                     "lastItem=" + lastItem + " " +
//                     "prevCycles=" + prevCycles + " " +
//                     "prevTakeIndex=" + prevTakeIndex + " " +
//                     "size()=" + size() + " " +
//                     "remainingCapacity()=" + remainingCapacity());
//         }
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
        return Spliterators.spliterator
            (this, Spliterator.ORDERED | Spliterator.NONNULL |
             Spliterator.CONCURRENT);
    }

    /**
     * Deserializes this queue and then checks some invariants.
     *
     * @param s the input stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.InvalidObjectException if invariants are violated
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        // Read in items array and various fields
        s.defaultReadObject();

        // Check invariants over count and index fields. Note that
        // if putIndex==takeIndex, count can be either 0 or items.length.
        if (items.length == 0 ||
            takeIndex < 0 || takeIndex >= items.length ||
            putIndex  < 0 || putIndex  >= items.length ||
            count < 0     || count     >  items.length ||
            Math.floorMod(putIndex - takeIndex, items.length) !=
            Math.floorMod(count, items.length)) {
            throw new java.io.InvalidObjectException("invariants violated");
        }
    }
}
