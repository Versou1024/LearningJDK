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
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * An unbounded thread-safe {@linkplain Queue queue} based on linked nodes.
 * This queue orders elements FIFO (first-in-first-out).
 * The <em>head</em> of the queue is that element that has been on the
 * queue the longest time.
 * The <em>tail</em> of the queue is that element that has been on the
 * queue the shortest time. New elements
 * are inserted at the tail of the queue, and the queue retrieval
 * operations obtain elements at the head of the queue.
 * A {@code ConcurrentLinkedQueue} is an appropriate choice when
 * many threads will share access to a common collection.
 * Like most other concurrent collection implementations, this class
 * does not permit the use of {@code null} elements.
 *
 * <p>This implementation employs an efficient <em>non-blocking</em>
 * algorithm based on one described in <a
 * href="http://www.cs.rochester.edu/u/michael/PODC96.html"> Simple,
 * Fast, and Practical Non-Blocking and Blocking Concurrent Queue
 * Algorithms</a> by Maged M. Michael and Michael L. Scott.
 *
 * <p>Iterators are <i>weakly consistent</i>, returning elements
 * reflecting the state of the queue at some point at or since the
 * creation of the iterator.  They do <em>not</em> throw {@link
 * java.util.ConcurrentModificationException}, and may proceed concurrently
 * with other operations.  Elements contained in the queue since the creation
 * of the iterator will be returned exactly once.
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
 * <p>This class and its iterator implement all of the <em>optional</em>
 * methods of the {@link Queue} and {@link Iterator} interfaces.
 *
 * <p>Memory consistency effects: As with other concurrent
 * collections, actions in a thread prior to placing an object into a
 * {@code ConcurrentLinkedQueue}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions subsequent to the access or removal of that element from
 * the {@code ConcurrentLinkedQueue} in another thread.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
        implements Queue<E>, java.io.Serializable {
    private static final long serialVersionUID = 196745693267521676L;


    private static class Node<E> {
        volatile E item;
        volatile Node<E> next;

        /**
         * Constructs a new node.  Uses relaxed write because item can
         * only be seen after publication via casNext.
         */
        Node(E item) {
            UNSAFE.putObject(this, itemOffset, item);
        }

        boolean casItem(E cmp, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        // Unsafe mechanics

        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * A node from which the first live (non-deleted) node (if any)
     * can be reached in O(1) time.
     * Invariants:
     * - all live nodes are reachable from head via succ()
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * Non-invariants:
     * - head.item may or may not be null.
     * - it is permitted for tail to lag behind head, that is, for tail
     *   to not be reachable from head!
     */
    private transient volatile Node<E> head;

    /**
     * A node from which the last node on list (that is, the unique
     * node with node.next == null) can be reached in O(1) time.
     * Invariants:
     * - the last node is always reachable from tail via succ()
     * - tail != null
     * Non-invariants:
     * - tail.item may or may not be null.
     * - it is permitted for tail to lag behind head, that is, for tail
     *   to not be reachable from head!
     * - tail.next may or may not be self-pointing to tail.
     */
    private transient volatile Node<E> tail;

    /**
     * Creates a {@code ConcurrentLinkedQueue} that is initially empty.
     */
    public ConcurrentLinkedQueue() {
        // 初始化 node = tail，允许元素值为null
        head = tail = new Node<E>(null);
    }

    /**
     * Creates a {@code ConcurrentLinkedQueue}
     * initially containing the elements of the given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        //
        Node<E> h = null, t = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            // 必须保证，初始化时若只有一个node，h需要等于t
            if (h == null)
                h = t = newNode;
            else {
                // next本来是volatile
                // 如果直接Unsafe.putObjectVolatile()在主内存中会立即可见
                // 相比于 Unsafe.putOrderedObject() 节省一定性能，不会立即在主内存可见
                t.lazySetNext(newNode);
                t = newNode;
            }
        }
        // 集合c为空集合
        if (h == null)
            h = t = new Node<E>(null);
        head = h;
        tail = t;
    }

    // Have to override just to update the javadoc

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never throw
     * {@link IllegalStateException} or return {@code false}.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * Tries to CAS head to p. If successful, repoint old head to itself
     * as sentinel for succ(), below.
     */
    final void updateHead(Node<E> h, Node<E> p) {
        // h不等于p，才需要尝试更新 -- 程序效率考虑:h等于p完全不需要CAS更新
        // 懒加载h
        if (h != p && casHead(h, p))
            h.lazySetNext(h);
    }

    /**
     * Returns the successor of p, or the head node if p.next has been
     * linked to self, which will only be true if traversing with a
     * stale pointer that is now off the list.
     */
    final Node<E> succ(Node<E> p) {
        Node<E> next = p.next;
        return (p == next) ? head : next; // 检查node是否为自引用的node，若是直接返回新的head，否则返回next
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never return {@code false}.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws NullPointerException if the specified element is null
     * <p>
     *     在此队列末尾插入指定的元素。由于队列是无界的，此方法永远不会返回false。
     * </p>
     */
    public boolean offer(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);
        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next; // ②
            // (1) q 为空，表示没有线程在竞争做offer()并成功，否则
            // 由于别的线程抢先设置追加到队列尾部，p就不是尾节点
            if (q == null) {
                // p 是最后一个节点，将newNode连接到p后面
                if (p.casNext(null, newNode)) {
                    // cas成功，则说明新增节点已经被放入链表，然后设置当前尾节点
                    // 一次跳过两个节点
                    // 假设链表为1-2-3，有线程ABC都同时进入(1)，线程A将设置next成功，即1-2-3-4，p还是指向3，t也是指向3，因此直接返回
                    // 线程B和C自旋第一次，②更新q指向4，p仍然指向3，t也指向3，tail也没更新还是指向3，进入(3)，将p更新为指向q的4
                    // 线程B和C自旋第二次，②更新q指向null，因此会再次加入(1),线程B将设置next成功，即1-2-3-4-5，而tail还是指向3，
                    // t指向3，p已经指向4，因此会执行①，一次跳过3和4两个节点，将5设置为tail
                    // 线程C继续自旋第三次，此刻tail已经更新为5，t还是指向3，p仍然指向3，经过②后q等于null
                    if (p != t)
                        casTail(t, newNode);  // ①
                    return true;
                }
                // Lost CAS race to another thread; re-read next
            }
            // (2) 只有在Poll()时出现
            else if (p == q)
                // 多线程操作时候，由于poll操作移除元素后有可能会把head变为自引用，然后head的next变为新head，所以这里需要重新找新的head，因为新的head后面的节点才是正常的节点。
                // 如果p的next等于p，说明p已经被删除了（已经出队了）-- 常用帮助GC的手段，node.next = next -- 自引用的节点，会被 GC 回收
                // 需要重新设置p的值
                p = (t != (t = tail)) ? t : head;
            // (3) 多个线程竞争offer()时可能会出现
            else
                // 竞争失败的节点，需要寻找尾节点
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }

    public E poll() {
        restartFromHead:
        // ②
        for (;;) {
            // p是遍历的节点，h是头结点，q是p的next节点，item是当前遍历的值
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item; // ③
                // (1)如果节点的值不为空，并且将其更新为null成功了
                if (item != null && p.casItem(item, null)) {
                    if (p != h)
                        updateHead(h, ((q = p.next) != null) ? q : p); // ①一次性跳过两个节点
                    return item;
                }
                // (2) 由于head节点指向的是item为null的哨兵节点，
                else if ((q = p.next) == null) {
                    updateHead(h, p);
                    return null;
                }
                // (3) 在(2)中将q指向p.next,若q等于p，说明该node已经被删除，即help GC
                else if (p == q)
                    continue restartFromHead;
                // (4) 更新p，指向q -- 使得整个循环中，p不断向后移动
                else
                    p = q;
            }
        }
    }

    public E peek() {
        /*
         * peek操作的代码与poll操作类似：
         * 只是前者只获取队列头元素但不从队列中将其删除，而后者需要从队列中将其删除
         * 例外请注意：第一次调用peek操作时，会删除哨兵节点，并让队列的head节点指向队列里面第一个元素或者null
         */
        restartFromHead:
        for (;;) {
            // 注意头结点head是哨兵节点，且head.item为null，因此会先执行p=q
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;
                // 当队列不存在有效元素，仅有一个哨兵节点时：
                // item为null，则执行 q = p.next -- item为null对应就是头结点即哨兵节点，然后p.next为null，说明不存在有效的队列头
                // 因此进入循环体，返回一个null，并更新head指向一个null

                // 当队列至少存在一个元素
                // 那么哨兵的item为null，存在有效元素使得哨兵的后一个节点，即p.next非null，导致不满足条件，执行一遍①
                // 后续进入时，item已不为null，进入updateHead，将导致哨兵节点被移除
                if (item != null || (q = p.next) == null) {
                    updateHead(h, p);
                    return item;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q; // ①
            }
        }
    }

    /**
     * Returns the first live (non-deleted) node on list, or null if none.
     * This is yet another variant of poll/peek; here returning the
     * first node, not element.  We could make peek() a wrapper around
     * first(), but that would cost an extra volatile read of item,
     * and the need to add a retry loop to deal with the possibility
     * of losing a race to a concurrent poll().
     */
    Node<E> first() {
        /*
         * 注意：first()和peek()操作实际上都会将哨兵节点移除，将head指向第一个真实的有效的节点
         */
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                boolean hasItem = (p.item != null);
                // ① 头结点p有value -- 即头节点就是有效节点，更新head为头节点本身 -- 即哨兵节点已被移除，目前头结点就是有效节点
                // ② 头结点的item为null但next为null -- 头结点非有效节点，但是只有头结点一个点，更新头节点为其本身 -- 即只有一个哨兵节点
                // ③ 经过一次遍历后，找到一个节点p是有效节点 -- 更新head为当前查找到的有效节点 -- 说明链表前面有哨兵节点，将头结点指向第一个有效节点
                if (hasItem || (q = p.next) == null) {
                    updateHead(h, p);
                    return hasItem ? p : null; // 进入该循环后，因为有①②情况，需要判断hasItem
                }
                // 由于poll()原因
                // 例如：线程A获取到head后，head可能是哨兵节点或者有效节点，
                // 若是哨兵节点，可能在peek()等操作中被help GC掉
                // 若是有效节点，可能是在poll()中将head改为自引用啦
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q; // 继续递归，类似i++操作
            }
        }
    }

    /**
     * Returns {@code true} if this queue contains no elements.
     *
     * @return {@code true} if this queue contains no elements
     */
    public boolean isEmpty() {
        return first() == null;
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
     * Additionally, if elements are added or removed during execution
     * of this method, the returned result may be inaccurate.  Thus,
     * this method is typically not very useful in concurrent
     * applications.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        /*
         * 计算当前队列元素个数，在并发环境下并不是很有用，因为CAS没有加锁
         */
        int count = 0;
        // first()用于返回队列第一个有效节点
        // 遍历计算size
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                // Collection.size() spec says to max out
                if (++count == Integer.MAX_VALUE)
                    break;
        return count;
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
        // 从队列的第一个有效节点开始遍历，直到查询到一个值与o相等的节点
        // 否则返回false
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item))
                return true;
        }
        return false;
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
        /*
         * 从该队列中删除指定元素的单个实例（如果存在）。
         * 更正式地说，如果该队列包含一个或多个这样的元素，则删除元素e，使o等于（e）。
         * 如果此队列包含指定元素，则返回true（如果此队列因调用而更改，则返回等效值）。
         */
        if (o != null) {
            Node<E> next, pred = null;
            // p 是当前遍历到的node，pred是p的前一个node，next是p的后一个node
            for (Node<E> p = first(); p != null; pred = p, p = next) {
                boolean removed = false;
                E item = p.item;
                // ① 遍历到的item元素值非空
                if (item != null) {
                    // 当前遍历的节点非目标值，计算其next，并重新循环
                    if (!o.equals(item)) {
                        next = succ(p);
                        continue;
                    }
                    // 找到相等的元素，进行逻辑删除
                    // 逻辑删除：将当前node的item更新null，表示这个node已经被删除
                    removed = p.casItem(item, null); // ② 更新item为null
                }
                // 执行到此处情况：
                // 1、遍历中发现有逻辑删除的节点，其item为null ①
                // 2、遍历中已经找到相等的节点 ②
                // 以上情况：都需要帮助将虚拟删除的节点给移除
                next = succ(p);
                if (pred != null && next != null)
                    // 在上述情况1和2中，将pred设置的后继者更新为next
                    pred.casNext(p, next);
                if (removed)
                    return true;
            }
        }
        return false;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this queue, in the order that they are returned by the specified
     * collection's iterator.  Attempts to {@code addAll} of a queue to
     * itself result in {@code IllegalArgumentException}.
     *
     * @param c the elements to be inserted into this queue
     * @return {@code true} if this queue changed as a result of the call
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     * @throws IllegalArgumentException if the collection is this queue
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == this)
            // As historically specified in AbstractQueue#addAll
            throw new IllegalArgumentException();

        // 将c复制到私有节点链beginningOfTheEnd中
        Node<E> beginningOfTheEnd = null, last = null;
        for (E e : c) {
            checkNotNull(e);
            // 将集合c所有的元素添加到一个链表中，以beginningOfTheEnd为头
            Node<E> newNode = new Node<E>(e);
            if (beginningOfTheEnd == null)
                beginningOfTheEnd = last = newNode;
            else {
                last.lazySetNext(newNode);
                last = newNode;
            }
        }
        if (beginningOfTheEnd == null)
            return false;

        // 以原子方式将beginningOfTheEnd链附加到此集合的尾部
        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next;
            if (q == null) {
                // p是最后一个节点
                if (p.casNext(null, beginningOfTheEnd)) {
                    // 成功的CAS是要添加到此队列的所有元素的线性化点。
                    if (!casTail(t, last)) {
                        // 尝试更新tail，因为我们可能会添加很多元素。
                        if (last.next == null)
                            casTail(t, last);
                    }
                    return true;
                }
                // 输掉了与另一个线程的竞争；接下来重读
            }
            else if (p == q)
                // 我们从名单上掉了下来。如果tail保持不变，它也将从列表中删除，在这种情况下，我们需要跳转到head，所有活动节点都可以从head访问。否则，新尾巴是更好的选择。
                p = (t != (t = tail)) ? t : head;
            else
                // 两次跳跃后检查尾部更新。
                p = (p != t && t != (t = tail)) ? t : q;
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
        // 使用ArrayList帮助处理大小调整。
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray();
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
        // try to use sent-in array
        int k = 0;
        Node<E> p;
        for (p = first(); p != null && k < a.length; p = succ(p)) {
            E item = p.item;
            if (item != null)
                a[k++] = (T)item;
        }
        if (p == null) {
            if (k < a.length)
                a[k] = null;
            return a;
        }

        // If won't fit, use ArrayList version
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> q = first(); q != null; q = succ(q)) {
            E item = q.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray(a);
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

    private class Itr implements Iterator<E> {
        /**
         * Next node to return item for.
         */
        private Node<E> nextNode;

        /**
         * nextItem holds on to item fields because once we claim
         * that an element exists in hasNext(), we must return it in
         * the following next() call even if it was in the process of
         * being removed when hasNext() was called.
         */
        private E nextItem;

        /**
         * 最后返回的项目的节点，以支持删除。
         */
        private Node<E> lastRet;

        Itr() {
            advance();
        }

        /**
         * Moves to next valid node and returns item to return for
         * next(), or null if no such.
         * <p>移动到下一个有效节点，并为next（）返回要返回的项，如果没有，则返回null。</p>
         */
        private E advance() {
            lastRet = nextNode;
            E x = nextItem;

            Node<E> pred, p;
            // nextNode=null 初始化
            if (nextNode == null) {
                p = first();
                pred = null;
            } else {
                pred = nextNode;
                p = succ(nextNode);
            }
            // nextNode 存有为当前遍历到的节点
            // p 存有下次遍历的节点，需要赋值给 nextNode
            // item 存有下次遍历的值，需要赋值给 nextItem
            for (;;) {
                if (p == null) {
                    nextNode = null;
                    nextItem = null;
                    return x;
                }
                E item = p.item;
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    return x;
                } else {
                    // item为空，表示该节点非有效值，需要通过①来移除
                    Node<E> next = succ(p);
                    if (pred != null && next != null)
                        pred.casNext(p, next); // ①
                    p = next;
                }
            }
        }

        public boolean hasNext() {
            return nextNode != null;
        }

        public E next() {
            if (nextNode == null) throw new NoSuchElementException();
            return advance();
        }

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            // rely on a future traversal to relink.
            l.item = null;
            lastRet = null;
        }
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

        // Write out any hidden stuff
        s.defaultWriteObject();

        // Write out all elements in the proper order.
        for (Node<E> p = first(); p != null; p = succ(p)) {
            Object item = p.item;
            if (item != null)
                s.writeObject(item);
        }

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

        // Read in elements until trailing null sentinel found
        Node<E> h = null, t = null;
        Object item;
        while ((item = s.readObject()) != null) {
            @SuppressWarnings("unchecked")
            Node<E> newNode = new Node<E>((E) item);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                t = newNode;
            }
        }
        if (h == null)
            h = t = new Node<E>(null);
        head = h;
        tail = t;
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class CLQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final ConcurrentLinkedQueue<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        CLQSpliterator(ConcurrentLinkedQueue<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node<E> p;
            final ConcurrentLinkedQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null) &&
                p.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                do {
                    if ((a[i] = p.item) != null)
                        ++i;
                    if (p == (p = p.next))
                        p = q.first();
                } while (p != null && i < n);
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

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                exhausted = true;
                do {
                    E e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                E e;
                do {
                    e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                } while (e == null && p != null);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
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
    @Override
    public Spliterator<E> spliterator() {
        return new CLQSpliterator<E>(this);
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

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentLinkedQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
