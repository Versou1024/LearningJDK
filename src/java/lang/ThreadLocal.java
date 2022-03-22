/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;
import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * This class provides thread-local variables.  These variables differ from
 * their normal counterparts in that each thread that accesses one (via its
 * {@code get} or {@code set} method) has its own, independently initialized
 * copy of the variable.  {@code ThreadLocal} instances are typically private
 * static fields in classes that wish to associate state with a thread (e.g.,
 * a user ID or Transaction ID).
 *
 * <p>For example, the class below generates unique identifiers local to each
 * thread.
 * A thread's id is assigned the first time it invokes {@code ThreadId.get()}
 * and remains unchanged on subsequent calls.
 * <pre>
 * import java.util.concurrent.atomic.AtomicInteger;
 *
 * public class ThreadId {
 *     // Atomic integer containing the next thread ID to be assigned
 *     private static final AtomicInteger nextId = new AtomicInteger(0);
 *
 *     // Thread local variable containing each thread's ID
 *     private static final ThreadLocal&lt;Integer&gt; threadId =
 *         new ThreadLocal&lt;Integer&gt;() {
 *             &#64;Override protected Integer initialValue() {
 *                 return nextId.getAndIncrement();
 *         }
 *     };
 *
 *     // Returns the current thread's unique ID, assigning it if necessary
 *     public static int get() {
 *         return threadId.get();
 *     }
 * }
 * </pre>
 * <p>Each thread holds an implicit reference to its copy of a thread-local
 * variable as long as the thread is alive and the {@code ThreadLocal}
 * instance is accessible; after a thread goes away, all of its copies of
 * thread-local instances are subject to garbage collection (unless other
 * references to these copies exist).
 *
 * @author  Josh Bloch and Doug Lea
 * @since   1.2
 */
public class ThreadLocal<T> {
    /**
     * ThreadLocals rely on per-thread linear-probe hash maps attached
     * to each thread (Thread.threadLocals and
     * inheritableThreadLocals).  The ThreadLocal objects act as keys,
     * searched via threadLocalHashCode.  This is a custom hash code
     * (useful only within ThreadLocalMaps) that eliminates collisions
     * in the common case where consecutively constructed ThreadLocals
     * are used by the same threads, while remaining well-behaved in
     * less common cases.
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * The next hash code to be given out. Updated atomically. Starts at
     * zero.
     */
    private static AtomicInteger nextHashCode = new AtomicInteger();

    /**
     * 生成hash code间隙为这个魔数，可以让生成出来的值或者说ThreadLocal的ID较为均匀地分布在2的幂大小的数组中。
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Returns the next hash code.
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * Returns the current thread's "initial value" for this
     * thread-local variable.  This method will be invoked the first
     * time a thread accesses the variable with the {@link #get}
     * method, unless the thread previously invoked the {@link #set}
     * method, in which case the {@code initialValue} method will not
     * be invoked for the thread.  Normally, this method is invoked at
     * most once per thread, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     *
     * <p>This implementation simply returns {@code null}; if the
     * programmer desires thread-local variables to have an initial
     * value other than {@code null}, {@code ThreadLocal} must be
     * subclassed, and this method overridden.  Typically, an
     * anonymous inner class will be used.
     *
     * @return the initial value for this thread-local
     */
    protected T initialValue() {
        return null;
    }

    /**
     * Creates a thread local variable. The initial value of the variable is
     * determined by invoking the {@code get} method on the {@code Supplier}.
     *
     * @param <S> the type of the thread local's value
     * @param supplier the supplier to be used to determine the initial value
     * @return a new thread local variable
     * @throws NullPointerException if the specified supplier is null
     * @since 1.8
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * Creates a thread local variable.
     * @see #withInitial(java.util.function.Supplier)
     */
    public ThreadLocal() {
    }

    /**
     * Returns the value in the current thread's copy of this
     * thread-local variable.  If the variable has no value for the
     * current thread, it is first initialized to the value returned
     * by an invocation of the {@link #initialValue} method.
     *
     * @return the current thread's value of this thread-local
     */
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            // entry不为空，就获取其value
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        // -- 因此我们可以继承ThreadLocal，重写initValue()方法，然后直接调用这个特殊的threadLocal.get()，然后会自动调用initValue
        // 特点 -- 懒加载，线程安全的
        return setInitialValue();
    }

    /**
     * Variant of set() to establish initialValue. Used instead
     * of set() in case user has overridden the set() method.
     *
     * @return the initial value
     */
    private T setInitialValue() {
        // 当调用get()方法时若map为空，调用该方法进行初始化
        T value = initialValue(); // 即null值
        Thread t = Thread.currentThread(); // 当前线程
        ThreadLocalMap map = getMap(t);
        if (map != null) // 获取map再次判断，由于多线程的原因 -- 可能期间有线程已经将线程的ThreadLocalMap创建起来啦
            map.set(this, value); //将空值null设置进去
        else
            createMap(t, value); // 调用creatMap()创建
        return value; // 返回null
    }

    /**
     * Sets the current thread's copy of this thread-local variable
     * to the specified value.  Most subclasses will have no need to
     * override this method, relying solely on the {@link #initialValue}
     * method to set the values of thread-locals.
     *
     * @param value the value to be stored in the current thread's copy of
     *        this thread-local.
     */
    public void set(T value) {
        /*
         * 每个线程都一个ThreadLocalMap属性
         * 每个线程可以创建多个ThreadLocal，调用set方法
         * 那么实质上就是向ThreadLocalMap的entry[]数组中，添加一个以ThreadLocal为key，以用户给定值作为value的entry进去
         */
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value); // 向线程的ThreadLocalMap中存入value -- 可以发现同一个线程的同一个ThreadLocal多次set会造成value的更新
        else
            createMap(t, value); // 线程的ThreadLocalMap还尚未初始化，创建这个线程的ThreadLocalMap
    }

    /**
     * Removes the current thread's value for this thread-local
     * variable.  If this thread-local variable is subsequently
     * {@linkplain #get read} by the current thread, its value will be
     * reinitialized by invoking its {@link #initialValue} method,
     * unless its value is {@linkplain #set set} by the current thread
     * in the interim.  This may result in multiple invocations of the
     * {@code initialValue} method in the current thread.
     *
     * @since 1.5
     */
     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
     }

    /**
     * Get the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param  t the current thread
     * @return the map
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals; // 获取当前线程的ThreadLocalMap
    }

    /**
     * Create the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param t the current thread
     * @param firstValue value for the initial entry of the map
     */
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue); // 给线程t的属性threadLocals创建ThreadLocalMap
    }

    /**
     * Factory method to create map of inherited thread locals.
     * Designed to be called only from Thread constructor.
     *
     * @param  parentMap the map associated with parent thread
     * @return a map containing the parent's inheritable bindings
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * Method childValue is visibly defined in subclass
     * InheritableThreadLocal, but is internally defined here for the
     * sake of providing createInheritedMap factory method without
     * needing to subclass the map class in InheritableThreadLocal.
     * This technique is preferable to the alternative of embedding
     * instanceof tests in methods.
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * An extension of ThreadLocal that obtains its initial value from
     * the specified {@code Supplier}.
     */
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    /**
     * ThreadLocalMap is a customized hash map suitable only for
     * maintaining thread local values. No operations are exported
     * outside of the ThreadLocal class. The class is package private to
     * allow declaration of fields in class Thread.  To help deal with
     * very large and long-lived usages, the hash table entries use
     * WeakReferences for keys. However, since reference queues are not
     * used, stale entries are guaranteed to be removed only when
     * the table starts running out of space.
     */
    static class ThreadLocalMap {

        /**
         * 读到这里，如果不问不答为什么是这样的定义形式，为什么要用弱引用，等于没读懂源码。
         * 因为如果这里使用普通的key-value形式来定义存储结构，实质上就会造成节点的生命周期与线程强绑定，只要线程没有销毁，
         * 那么节点在GC分析中一直处于可达状态，没办法被回收，而程序本身也无法判断是否可以清理节点。
         * 弱引用是Java中四档引用的第三档，比软引用更加弱一些，如果一个对象没有强引用链可达，那么一般活不过下一次GC。
         * 当某个ThreadLocal已经没有强引用可达，则随着它被垃圾回收，在ThreadLocalMap里对应的Entry的键值会失效，这为ThreadLocalMap本身的垃圾清理提供了便利。
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /**
             * key为简单任务就是这个线程拥有过的ThreadLocal
             * value存有用户需要线程安全隔离的值
             */
            Object value;


            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * 初始容量，必须为2的幂
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         *  Entry表，大小必须为2的幂
         */
        private Entry[] table;

        /**
         * 表里entry的个数
         */
        private int size = 0;

        /**
         * 重新分配表大小的阈值，默认为0
         */
        private int threshold; // Default to 0

        /**
         * 设置resize阈值以维持最坏2/3的装载因子
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * 环形意义的下一个索引
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * 环形意义的上一个索引
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * 由于ThreadLocalMap使用线性探测法来解决散列冲突，所以实际上Entry[]数组在程序逻辑上是作为一个环形存在的
         */

        /**
         * 构造一个包含firstKey和firstValue的map。
         * ThreadLocalMap是惰性构造的，所以只有当至少要往里面放一个元素的时候才会构建它。
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY]; // 初始化table数组
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1); // 用firstKey的threadLocalHashCode与初始大小16取模得到哈希值
            table[i] = new Entry(firstKey, firstValue); // // 初始化该节点
            size = 1; // 设置节点表大小为1
            setThreshold(INITIAL_CAPACITY); // 设定扩容阈值
        }

        /**
         * Construct a new map including all Inheritable ThreadLocals
         * from given parent map. Called only by createInheritedMap.
         *
         * @param parentMap the map associated with parent thread.
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            // 用于将父线程的parentMap继承来子线程的ThreadLocalMap
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * Get the entry associated with key.  This method
         * itself handles only the fast path: a direct hit of existing
         * key. It otherwise relays to getEntryAfterMiss.  This is
         * designed to maximize performance for direct hits, in part
         * by making this method readily inlinable.
         *
         * @param  key the thread local object
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntry(ThreadLocal<?> key) {
            // 根据key即ThreadLocal的哈希值，计算索引位置
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            // 对应的entry存在，且未失效即弱引用指向的ThreadLocal就是key，则命中返回
            if (e != null && e.get() == key)
                return e;
            else
                // 因为用的是线性探测，所以往后找还是有可能能够找到目标Entry的。i为key散列的第一个索引值
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * 调用getEntry未直接命中的时候调用此方法
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            // 基于线性探测法不断向后探测直到遇到空entry。
            while (e != null) {
                ThreadLocal<?> k = e.get(); // 注意：entry.get()获取的是弱引用中的key
                // 找到目标
                if (k == key)
                    return e;
                // 该entry对应的key即ThreadLocal作为一个弱引用，已经被回收，满足k==null，则调用expungeStaleEntry来清理无效的entry
                if (k == null)
                    expungeStaleEntry(i);
                else
                    // 环形意义下往后面走
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * Set the value associated with key.
         *
         * @param key the thread local object
         * @param value the value to be set
         */
        private void set(ThreadLocal<?> key, Object value) {

            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            // 线性探测 -- 直到找到一个新的位置放入value
            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
                // 相同的TheadLocal更新valul
                if (k == key) {
                    e.value = value;
                    return;
                }
                // 索引i上entry存在，但是无效，但是key由于是弱引用已经被GC调，就需要进行清理替换
                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }
            // 执行到此处说明，table[i]就是null，因此需要创建entry
            tab[i] = new Entry(key, value);
            // 有效元素数量+1
            int sz = ++size;
            // 重新hash
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         * Remove the entry for key.
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * Replace a stale entry encountered during a set operation
         * with an entry for the specified key.  The value passed in
         * the value parameter is stored in the entry, whether or not
         * an entry already exists for the specified key.
         *
         * As a side effect, this method expunges all stale entries in the
         * "run" containing the stale entry.  (A run is a sequence of entries
         * between two null slots.)
         *
         * @param  key the key
         * @param  value the value to be associated with key
         * @param  staleSlot index of the first stale entry encountered while
         *         searching for key.
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                       int staleSlot) {
            // 用来替换失效的entry
            Entry[] tab = table;
            int len = tab.length;
            Entry e;


            // 向前扫描，查找最前的一个无效的slot
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len); (e = tab[i]) != null; i = prevIndex(i, len))
                if (e.get() == null) // e.get()是获取弱引用中的ThreadLocal
                    slotToExpunge = i;


            // 向后遍历table，
            for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                // 找到了key，将其与无效的slot交换 -- 线性探测
                // 将找到key的节点e，更新其value，然后放置到之前失效的位置上级staleSlot
                if (k == key) {
                    e.value = value; // key相同，需要更新value

                    tab[i] = tab[staleSlot]; // 将失效staleSlot上的entry放置到当前位置上
                    tab[staleSlot] = e; // 将e放到失效的staleSlot位置上

                    /*
                     * 如果在整个扫描过程中（包括函数一开始的向前扫描与i之前的向后扫描）
                     * 找到了之前的无效slot则以那个位置作为清理的起点，
                     * 否则则以当前的i作为清理起点
                     */
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    // 从slotToExpunge开始做一次连续段的清理，再做一次启发式清理
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // 如果当前的slot已经无效，并且向前扫描过程中没有无效slot，则更新slotToExpunge为当前位置
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // 如果key在table中不存在，则在原地放一个即可
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // 在探测过程中如果发现任何无效slot，则做一次清理（连续段清理+启发式清理）
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }


        /**
         * 这个函数是ThreadLocal中核心清理函数，它做的事情很简单：
         * 就是从staleSlot开始遍历，将无效（弱引用指向对象被回收）清理，即对应entry中的value置为null，将指向这个entry的table[i]置为null，直到扫到空entry。
         * 另外，在过程中还会对非空的entry作rehash。
         * 可以说这个函数的作用就是从staleSlot开始清理连续段中的slot（断开强引用，rehash slot等）
         */
        private int expungeStaleEntry(int staleSlot) {
            // staleSlot 表示陈旧的槽，需要被清理掉

            Entry[] tab = table;
            int len = tab.length;

            // 因为entry对应的ThreadLocal已经被回收，value设为null，显式断开强引用
            tab[staleSlot].value = null;
            // 显式设置该entry为null，以便垃圾回收
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            // i是staleSlot位置上的槽的下一个槽的索引
            // e是staleSlot槽的下一个槽的entry项
            // 从staleSlot的下一个位置遍历到第一个为null的entry上 -- 这个段，然后对这个d段做亲亲你操作
            for (i = nextIndex(staleSlot, len);  (e = tab[i]) != null;  i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                // 下一个槽仍然是entry存在，key不存在，这里就需要继续递归，并做好数组清理工作
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                // 否则就是，下一个槽有值，同时key也存在 -- 即有效
                } else {
                    /*
                     * 对于有效的slot的情况，需要做一次rehash。将其移动到其他段上，因为这个段需要做清理
                     *
                     * 如果对应的ThreadLocal的ID对len取模出来的索引h不为当前位置i，
                     * 则从h向后线性探测到第一个空的slot，把当前的entry给挪过去。
                     */
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null) // 线性探测
                            h = nextIndex(h, len);
                        tab[h] = e; // 在新位置上插入
                    }
                }
            }
            return i;
        }

        /**
         * Heuristically scan some cells looking for stale entries.
         * This is invoked when either a new element is added, or
         * another stale one has been expunged. It performs a
         * logarithmic number of scans, as a balance between no
         * scanning (fast but retains garbage) and a number of scans
         * proportional to number of elements, that would find all
         * garbage but would cause some insertions to take O(n) time.
         *
         * @param i a position known NOT to hold a stale entry. The
         * scan starts at the element after i.
         *
         * @param n scan control: {@code log2(n)} cells are scanned,
         * unless a stale entry is found, in which case
         * {@code log2(table.length)-1} additional cells are scanned.
         * When called from insertions, this parameter is the number
         * of elements, but when from replaceStaleEntry, it is the
         * table length. (Note: all this could be changed to be either
         * more or less aggressive by weighting n instead of just
         * using straight log n. But this version is simple, fast, and
         * seems to work well.)
         *
         * @return true if any stale entries have been removed.
         */
        private boolean cleanSomeSlots(int i, int n) {
            /**
             * 启发式地清理slot,
             * i对应entry是非无效（指向的ThreadLocal没被回收，或者entry本身为空）
             * n是用于控制控制扫描次数的
             * 正常情况下如果log n次扫描没有发现无效slot，函数就结束了
             * 但是如果发现了无效的slot，将n置为table的长度len，做一次连续段的清理
             * 再从下一个空的slot开始继续扫描
             *
             * 这个函数有两处地方会被调用，一处是插入的时候可能会被调用，另外个是在替换无效slot的时候可能会被调用，
             * 区别是前者传入的n为元素个数，后者为table的容量
             */
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                // i在任何情况下自己都不会是一个无效slot，所以从下一个开始判断
                i = nextIndex(i, len);
                //
                Entry e = tab[i];
                if (e != null && e.get() == null) {
                    // 扩大扫描控制因子
                    n = len;
                    removed = true;
                    // 清理一个连续段
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        /**
         * Re-pack and/or re-size the table. First scan the entire
         * table removing stale entries. If this doesn't sufficiently
         * shrink the size of the table, double the table size.
         */
        private void rehash() {
            // 做一次全量清理
            expungeStaleEntries();

            /*
             * 因为做了一次清理，所以size很可能会变小。
             * ThreadLocalMap这里的实现是调低阈值来判断是否需要扩容，
             * threshold默认为len*2/3，所以这里的threshold - threshold / 4相当于len/2
             */
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * Double the capacity of the table.
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        // 线性探测来存放Entry
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * Expunge all stale entries in the table.
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
}
