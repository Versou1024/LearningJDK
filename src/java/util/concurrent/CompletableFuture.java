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
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.LockSupport;

/**
 * A {@link Future} that may be explicitly completed (setting its
 * value and status), and may be used as a {@link CompletionStage},
 * supporting dependent functions and actions that trigger upon its
 * completion.
 *
 * <p>When two or more threads attempt to
 * {@link #complete complete},
 * {@link #completeExceptionally completeExceptionally}, or
 * {@link #cancel cancel}
 * a CompletableFuture, only one of them succeeds.
 *
 * <p>In addition to these and related methods for directly
 * manipulating status and results, CompletableFuture implements
 * interface {@link CompletionStage} with the following policies: <ul>
 *
 * <li>Actions supplied for dependent completions of
 * <em>non-async</em> methods may be performed by the thread that
 * completes the current CompletableFuture, or by any other caller of
 * a completion method.</li>
 *
 * <li>All <em>async</em> methods without an explicit Executor
 * argument are performed using the {@link ForkJoinPool#commonPool()}
 * (unless it does not support a parallelism level of at least two, in
 * which case, a new Thread is created to run each task).  To simplify
 * monitoring, debugging, and tracking, all generated asynchronous
 * tasks are instances of the marker interface {@link
 * AsynchronousCompletionTask}. </li>
 *
 * <li>All CompletionStage methods are implemented independently of
 * other public methods, so the behavior of one method is not impacted
 * by overrides of others in subclasses.  </li> </ul>
 *
 * <p>CompletableFuture also implements {@link Future} with the following
 * policies: <ul>
 *
 * <li>Since (unlike {@link FutureTask}) this class has no direct
 * control over the computation that causes it to be completed,
 * cancellation is treated as just another form of exceptional
 * completion.  Method {@link #cancel cancel} has the same effect as
 * {@code completeExceptionally(new CancellationException())}. Method
 * {@link #isCompletedExceptionally} can be used to determine if a
 * CompletableFuture completed in any exceptional fashion.</li>
 *
 * <li>In case of exceptional completion with a CompletionException,
 * methods {@link #get()} and {@link #get(long, TimeUnit)} throw an
 * {@link ExecutionException} with the same cause as held in the
 * corresponding CompletionException.  To simplify usage in most
 * contexts, this class also defines methods {@link #join()} and
 * {@link #getNow} that instead throw the CompletionException directly
 * in these cases.</li> </ul>
 *
 * @author Doug Lea
 * @since 1.8
 */
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {

    /*
     * result的4种情况
     * 1、null，表示没有任何结果，即任务还在执行中，或者任务正在执行中
     * 2、NIL，表示正常完成，结果为null，用来区分第一种情况
     * 3、AltResult，表示异常完成，封装的是异常结果。
     * 4、value，表示正常完成，结果是一个非null的value
     */
    volatile Object result;       // CompletableFuture的结果值或者是一个异常的报装对象AltResult
    volatile Completion stack;    // 栈表任务的队头

    /**
     * 完成时，设置任务结果
     */
    final boolean internalComplete(Object r) { // CAS from null to r
        return UNSAFE.compareAndSwapObject(this, RESULT, null, r);
    }

    /**
     * CAS更换任务栈顶
     */
    final boolean casStack(Completion cmp, Completion val) {
        return UNSAFE.compareAndSwapObject(this, STACK, cmp, val);
    }

    /** 返回true，如果将c压入栈顶成功 */
    final boolean tryPushStack(Completion c) {
        Completion h = stack;
        lazySetNext(c, h);
        return UNSAFE.compareAndSwapObject(this, STACK, h, c);
    }

    /** 不断尝试，直到将c压入栈顶 */
    final void pushStack(Completion c) {
        do {} while (!tryPushStack(c));
    }

    /* ------------- 编码和解码outcom输出 -------------- */

    static final class AltResult {
        final Throwable ex;        // 仅仅为NIL下为null
        AltResult(Throwable x) { this.ex = x; }
    }

    /** 适用于返回结果为NULL的，将其封装为NIL */
    static final AltResult NIL = new AltResult(null);

    /** 以NULL的结果完成时，更新结果。*/
    final boolean completeNull() {
        return UNSAFE.compareAndSwapObject(this, RESULT, null, NIL);
    }

    /** 返回给定非异常值的编码。 */
    final Object encodeValue(T t) {
        return (t == null) ? NIL : t;
    }

    /** 以正常完成包括结果为null或者正常值，更新结果，除非已完成。 */
    final boolean completeValue(T t) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null, (t == null) ? NIL : t);
    }

    /**
     * 对异常进行编码，即封装在AltResult中
     */
    static AltResult encodeThrowable(Throwable x) {
        return new AltResult((x instanceof CompletionException) ? x : new CompletionException(x));
    }

    /** 当以一个异常作为结果完成时，更新RESULT为对应的异常编码结果AltResult */
    final boolean completeThrowable(Throwable x) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null, encodeThrowable(x));
    }

    /**
     * Returns the encoding of the given (non-null) exception as a
     * wrapped CompletionException unless it is one already.  May
     * return the given Object r (which must have been the result of a
     * source future) if it is equivalent, i.e. if this is a simple
     * relay of an existing CompletionException.
     */
    static Object encodeThrowable(Throwable x, Object r) {
        /**
         * 1、x 不属于 CompletionException，将其包装为 CompletionException 的 cause，然后封装在AltResult中
         * 2、x 属于 CompletionException，且 r 属于 AltResult，且 r 中封装的异常就是 x，直接返回r
         *
         * 结论：返回一个封装了 x 的AltResult
         */
        if (!(x instanceof CompletionException))
            x = new CompletionException(x);
        else if (r instanceof AltResult && x == ((AltResult)r).ex)
            return r;
        return new AltResult(x);
    }

    /**
     * Completes with the given (non-null) exceptional result as a
     * wrapped CompletionException unless it is one already, unless
     * already completed.  May complete with the given Object r
     * (which must have been the result of a source future) if it is
     * equivalent, i.e. if this is a simple propagation of an
     * existing CompletionException.
     */
    final boolean completeThrowable(Throwable x, Object r) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null, encodeThrowable(x, r));
    }

    /**
     * Returns the encoding of the given arguments: if the exception
     * is non-null, encodes as AltResult.  Otherwise uses the given
     * value, boxed as NIL if null.
     */
    Object encodeOutcome(T t, Throwable x) {
        return (x == null) ? (t == null) ? NIL : t : encodeThrowable(x);
    }

    /**
     * Returns the encoding of a copied outcome; if exceptional,
     * rewraps as a CompletionException, else returns argument.
     */
    static Object encodeRelay(Object r) {
        Throwable x;
        return (((r instanceof AltResult) &&
                 (x = ((AltResult)r).ex) != null &&
                 !(x instanceof CompletionException)) ?
                new AltResult(new CompletionException(x)) : r);
    }

    /**
     * Completes with r or a copy of r, unless already completed.
     * If exceptional, r is first coerced to a CompletionException.
     */
    final boolean completeRelay(Object r) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null,
                                           encodeRelay(r));
    }

    /**
     * Reports result using Future.get conventions.
     */
    private static <T> T reportGet(Object r) throws InterruptedException, ExecutionException {
        if (r == null) // by convention below, null means interrupted
            throw new InterruptedException();
        if (r instanceof AltResult) {
            Throwable x, cause;
            if ((x = ((AltResult)r).ex) == null) // 正常完成，结果为null
                return null;
            if (x instanceof CancellationException) // 异常完成，异常为取消异常CancellationException -- 直接抛出
                throw (CancellationException)x;
            if ((x instanceof CompletionException) && (cause = x.getCause()) != null) // 异常完成，结果为执行中异常CompletionException --封装为ExecutionException后抛出
                x = cause;
            throw new ExecutionException(x);
        }
        @SuppressWarnings("unchecked") T t = (T) r; // 正常完成，结果非null
        return t;
    }

    /**
     * Decodes outcome to return result or throw unchecked exception.
     */
    private static <T> T reportJoin(Object r) {
        if (r instanceof AltResult) {
            Throwable x;
            if ((x = ((AltResult)r).ex) == null) // 正常完成，结果为null
                return null;
            if (x instanceof CancellationException) // 异常完成，异常为取消异常CancellationException -- 直接抛出
                throw (CancellationException)x;
            if (x instanceof CompletionException)  // 异常完成，结果为执行中异常CompletionException -- 直接抛出，不封装 与 reportGet() 相比
                throw (CompletionException)x;
            throw new CompletionException(x);
        }
        @SuppressWarnings("unchecked") T t = (T) r; // 正常完成，结果非null
        return t;
    }

    /* ------------- Async task preliminaries -------------- */

    /**
     * A marker interface identifying asynchronous tasks produced by
     * {@code async} methods. This may be useful for monitoring,
     * debugging, and tracking asynchronous activities.
     *
     * @since 1.8
     */
    public static interface AsynchronousCompletionTask {
    }

    /**
     * 是否使用ForkJoinPool的common池的标准时并行度大于1
     */
    private static final boolean useCommonPool = (ForkJoinPool.getCommonPoolParallelism() > 1);

    /**
     * 默认的Exrcutor线程调度器
     */
    private static final Executor asyncPool = useCommonPool ? ForkJoinPool.commonPool() : new ThreadPerTaskExecutor();

    /** Fallback if ForkJoinPool.commonPool() cannot support parallelism */
    static final class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) { new Thread(r).start(); }
    }

    /**
     * Null-checks user executor argument, and translates uses of
     * commonPool to asyncPool in case parallelism disabled.
     */
    static Executor screenExecutor(Executor e) {
        /*
         * 目的：在并行度小于1，即不支持使用并发池的时候，检查用户提交的Executor是否为commonPool，如果是的话，就会改为使用 ThreadPerTaskExecutor
         */
        if (!useCommonPool && e == ForkJoinPool.commonPool())
            return asyncPool;
        if (e == null) throw new NullPointerException();
        return e;
    }

    // Modes for Completion.tryFire. Signedness matters.
    static final int SYNC   =  0; // 同步等于0
    static final int ASYNC  =  1; // 异步大于0
    static final int NESTED = -1; // 嵌套就是-1

    /* ------------- Base Completion classes and operations -------------- */

    @SuppressWarnings("serial")
    abstract static class Completion extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        /*
         *  回调函数均是 Completion 类的子类，抽取 Completion 类与子类的关键代码：
         * Completion 的子类含有两个 CompletableFuture 类型的参数，dep 是新建的、用于下一步的 CompletableFuture 对象，src 则是引用它的 CompletableFuture 对象。
         * 当 Completion 执行完回调方法后，一般会返回 dep 对象，用于迭代遍历。
         */
        volatile Completion next;      // Treiber Stack的含义，是一个可扩展的无锁栈，利用细粒度的并发原语CAS来实现的，

        /**
         * 工作Completion的action如果被触发，返回一个dependent，其有可能被需要传播下去
         *
         * @param mode SYNC, ASYNC, or NESTED
         */
        abstract CompletableFuture<?> tryFire(int mode);

        /** 如果仍可能被触发triggerable，则返回true。由cleanStack使用。*/
        abstract boolean isLive();

        public final void run()                { tryFire(ASYNC); } // 异步
        public final boolean exec()            { tryFire(ASYNC); return true; } // 异步
        public final Void getRawResult()       { return null; }
        public final void setRawResult(Void v) {}
    }

    static void lazySetNext(Completion c, Completion next) {
        UNSAFE.putOrderedObject(c, NEXT, next);
    }

    /**
     * 通知后续回调函数的执行。
     */
    final void postComplete() {
        // this指的就是运行完supplier后的CompletableFuture
        CompletableFuture<?> f = this;
        Completion h; // 注意这里是：Completion，而非CompletableFuture
        // h 即任务栈顶的任务CompletableFuture
        // 1、栈顶任务非空，则继续遍历
        while ((h = f.stack) != null || (f != this && (h = (f = this).stack) != null)) {  //判断stack栈是否为空
            CompletableFuture<?> d; Completion t;
            if (f.casStack(h, t = h.next)) { // pop 弹出栈顶任务，通过CAS出栈，
                if (t != null) { // h.next 不为空
                    /*
                     * 情况 2：如果当前迭代到的 CompletableFuture 对象 f 不是 this （即不是链表头）的话，
                     * 会将回调函数 h 压入 this （链表头）的 stack 中。然后从链表头再次迭代遍历。
                     * 这样下去，对象 f 中的回调方法栈假设为 3-2-1，从 f 的栈顶推出再压入 this 的栈顶，顺序就变为了 1-2-3。这时候，情况就变成了第 1 种。
                     */
                    if (f != this) {
                        pushStack(h);  //如果f不是this，将刚出栈的h入this的栈顶
                        continue;
                    }
                    /*
                     * 情况 1：如果当前迭代到的 CompletableFuture 对象f是 this （也就是 CompletableFuture 链表头），会令 h.next = null ，
                     * 因为 h.next 也就是 t 通过 CAS 的方式压到了 this 对象的 stack 栈顶。
                     */
                    h.next = null;    // detach 帮助GC，清理调执行完任务的h
                }
                //回调方法执行结束要么返回下一个 CompletableFuture 对象，要么返回 null 然后手动设置为 f = this，再次从头遍历。
                f = (d = h.tryFire(NESTED)) == null ? this : d; //调用栈顶任务的tryFire，嵌套模式
            }
        }
    }

    /** 遍历堆栈并取消死完成的链接。 */
    final void cleanStack() {
        for (Completion p = null, q = stack; q != null;) {
            Completion s = q.next;
            if (q.isLive()) {
                p = q;
                q = s;
            }
            else if (p == null) {
                casStack(q, s);
                q = stack;
            }
            else {
                p.next = s;
                if (p.isLive())
                    q = s;
                else {
                    p = null;  // restart
                    q = stack;
                }
            }
        }
    }

    /* ------------- One-input Completions -------------- */

    /** A Completion with a source, dependent, and executor. */
    @SuppressWarnings("serial")
    abstract static class UniCompletion<T,V> extends Completion {
        Executor executor;                 // executor to use (null if none)
        CompletableFuture<V> dep;          // 代表的依赖的CompletableFuture
        CompletableFuture<T> src;          // 代表的是源CompletableFuture

        UniCompletion(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src) {
            this.executor = executor;
            this.dep = dep;
            this.src = src;
        }

        /**
         * 确保当前Completion可以被调用；并且使用ForkJoinPool标记为来确保只有一个线程可以调用，
         * 如果是异步的，则在任务启动之后通过tryFire来进行调用。tryFire方法时在UniAccept类中。
         *
         * claim方法要在执行action前调用，若claim方法返回false，则不能调用action，原则上要保证action只执行一次。
         */
        final boolean claim() {
            Executor e = executor;
            // if（task.compareAndSetForkJoinTaskTag（（short）0，（short）1）），否则退出，因为节点已被访问。
            if (compareAndSetForkJoinTaskTag((short)0, (short)1)) {
                if (e == null)
                    return true; // 执行器为空，还未被执行，返回true，表示需要将任务执行
                executor = null;
                e.execute(this); // 执行任务
            }
            return false;
        }

        final boolean isLive() { return dep != null; }
    }

    /** Pushes the given completion (if it exists) unless done. */
    final void push(UniCompletion<?,?> c) {
        if (c != null) {
            while (result == null && !tryPushStack(c))
                lazySetNext(c, null); // 失败后清除c的next，因为在tryPushStack()中会首先设置c.next就是当前栈顶Completion
        }
    }

    /**
     * 在成功完成 UniCompletion.tryFire() 后，依赖项进行后处理。尝试清理任务源a的堆栈，
     * 然后运行postComplete或将其返回给调用者，具体取决于模式。
     */
    final CompletableFuture<T> postFire(CompletableFuture<?> a, int mode) {
        if (a != null && a.stack != null) {
            if (mode < 0 || a.result == null)
                a.cleanStack(); // 如果源任务a存在，且stack存在，并且模式为嵌套模式，就清空栈顶的任务，
            else
                a.postComplete(); // 源任务a存在，且stack存在，并且模式不为嵌套模式，继续递归任务
        }
        // 执行到这：
        // 1、说明：src源任务的stack会比dep当前任务的stack先去递归完成
        if (result != null && stack != null) {
            if (mode < 0)
                return this;  // 当前任务dep已经运行完毕，且栈非空，但为嵌套模式，直接返回this
            else
                postComplete(); // 当前任务dep存在，且栈非空，非嵌套模式，则继续递归执行stack中的任务
        }
        return null;
    }

    @SuppressWarnings("serial")
    static final class UniApply<T,V> extends UniCompletion<T,V> {
        Function<? super T,? extends V> fn;
        UniApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T,? extends V> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            /*
             * 主要流程：
             * 1、dep为null，直接返回null
             * 2、dep非null，调用uniApply尝试执行当前任务
             *      a.若mode为ASYNC、SYNC，则传入的UniApply<S,T>为null
             *      b.若mode为NETSED，则传入的就是this对象 -- 在postCompletion()中触发
             * 3、若uniApply尝试执行当前任务失败，则返回null
             * 4、若dep非null，且尝试执行任务成功，清空dep、src、fn
             * 5、调用当前任务的postFire()继续执行下一个任务
             */
            CompletableFuture<V> d; CompletableFuture<T> a;
            if ((d = dep) == null || !d.uniApply(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    // 被 uniApplyStage 调用
    final <S> boolean uniApply(CompletableFuture<S> a, Function<? super S,? extends T> f, UniApply<S,T> c) {
        Object r; Throwable x;
        // a为前一个任务，
        // 1、如果a等于null，
        // 2、或者a的结果为null即前一个任务没有结束，
        // 3、或者函数为null，都返回false -- 即执行成功
        if (a == null || (r = a.result) == null || f == null)
            return false;

        // 执行到这：一般是前一个任务执行结束
        // 此刻this就是当前任务，this.result=null，就表示当前任务尚未结束
        tryComplete: if (result == null) {
            // ②
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    // 上一个任务是异常结束，将异常x作为CompletionException封装到AltResult中
                    completeThrowable(x, r);
                    break tryComplete; // 跳过后续的，将r设为null
                }
                r = null; // ①
            }
            // 执行到这说明：
            // 1、上一个任务若为正常完成，且返回结果为null，因此从①中将结果为null
            // 2、上一个任务若为正常完成，且返回结果为null，直接跳过②，结果就是r本身
            try {
                // 1、在uniApplyStage()中调用时c就是null
                // 2、或在tryFire()中mode为嵌套时，
                if (c != null && !c.claim()) // c.claim()返回false表示已经执行过？？
                    return false;
                // 执行到这即可继续执行：
                // 1、在uniApplyStage()中调用时c就是null
                // 2、或者，c.claim()结果为true
                @SuppressWarnings("unchecked") S s = (S) r;
                completeValue(f.apply(s)); // 调用f，将前一个结果设置进去
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        // 执行到这：
        // 1、上一个任务若为异常完成，直接返回true，并不会调用 f.apply(s)
        // 2、上一个任务若为正常完成，经过try-catch执行f.apply(s)然后到达此处
        return true;
    }

    // 被thenApply、thenApplyAsync调用
    private <V> CompletableFuture<V> uniApplyStage(Executor e, Function<? super T,? extends V> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<V> d =  new CompletableFuture<V>(); //为当前的任务f，新建一个CompletableFuture任务
        // 情况说明：
        // 1、执行器为空，尝试调用 d.uniApply(this, f, null)，返回false表示同步执行f失败啦【前一个任务即这里的this仍在执行】
        // 2、执行器不为null，直接进入循环体中
        if (e != null || !d.uniApply(this, f, null)) {
            UniApply<T,V> c = new UniApply<T,V>(e, d, this, f); // dep就是新建的任务，src就是上一个任务this，
            push(c); // 将c压入前一个任务this的stack中 -- 说明：只有前一个任务未结束，后一个任务来临后却无法立即执行，就会进入前一个任务this的stack中
            c.tryFire(SYNC); //调用c的tryFire()继续尝试执行当前的任务
        }
        // 执行到这的情况：
        // 1、执行器为空，第一次调用uniApply执行当前任务就成功啦；
        // 2、执行器为空，第一次调用uniApply尝试执行任务失败，新建一个c，压入前一个任务的栈顶中，调用tryFire()再次尝试去执行任务
        //      a.tryFire()返回null，就表示第二次尝试执行仍然失败，直接返回d，直到前一个任务执行完后，调用其postCompletion就会发现当前任务，取出来执行
        //      b.tryFire()返回非null，就表示第二次尝试执行任务成功，直接返回d
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniAccept<T> extends UniCompletion<T,Void> {
        Consumer<? super T> fn;
        // dep: 新创建的CompletableFuture
        // src: 驱动thenAccept的CompletableFuture
        UniAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Consumer<? super T> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }
        /**
         * 尝试去调用当前任务。uniAccept()方法为核心逻辑。
         */
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d; CompletableFuture<T> a;
            // dep就是下一个任务，src就是当前任务，mode=SYNC就是0，mode=ASYNC就是1，mode=NESTED就是-1，
            // 因此在SYNC与NESTED模式上，会传入this，触发uniAccept的①的代码
            if ((d = dep) == null || !d.uniAccept(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null;
            src = null;
            fn = null; // 执行完清空dep、src、fn
            // 尝试驱动下一个Completion。代码合前面相似
            return d.postFire(a, mode);
        }
    }

    // 继承UniCompletion，点进去发现是继承CompletableFuture
    // 我们可以看得出来，具体逻辑是把每个操作都封装成了Completion，放入了栈中
    final <S> boolean uniAccept(CompletableFuture<S> a, Consumer<? super S> f, UniAccept<S> c) {
        // uniAccept的入参中，CompletableFuture a表示的是源任务，UniAccept c中报装有依赖的任务，这点需要清除。

        Object r; Throwable x;
        // 传入的任务为null，或者仍在运行中即a.result为null，或者函数式接口为null
        // 返回false，表示无法由调用 thenAccept() 的线程执行
        if (a == null || (r = a.result) == null || f == null)
            return false; // 返回false，表示尝试执行失败

        // 执行到这说明：前一个任务已经完成

        // this即下一个需要执行的任务，其result为null，表示还未初始化
        // 但是从上面可知，由于前一个任务已经完成，那么下一个就是this对象的任务
        tryComplete: if (result == null) {
            if (r instanceof AltResult) { //判断任务结果是否是AltResult类型
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r); // 前一个任务是异常完成的，执行completeThrowable
                    break tryComplete; // 提前tryComplete:
                }
                r = null;
            }

            // 执行到这说明：上一个任务正常完成
            try {
                if (c != null && !c.claim()) // ① 判断当前任务是否可以执行
                    return false;
                @SuppressWarnings("unchecked") S s = (S) r; //获取任务结果
                f.accept(s); // 去调用 Comsumer 函数
                completeNull(); // 由于 f 是consumer函数，因此正常完成后，completeNull()
            } catch (Throwable ex) {
                completeThrowable(ex); // 异常完成，调用completeThrowable(ex)
            }
        }
        return true;
    }

    private CompletableFuture<Void> uniAcceptStage(Executor e, Consumer<? super T> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>(); // 新创建的CompletableFuture
        // e为空，调用d.uniAccept(this, f, null)，注意传入的第一个形参就是当前执行的CompletableFuture，
        // executor 传入的是null, d.uniaccept判断驱动thenAccept的CompletableFuture是否运行完/是否运行
        if (e != null || !d.uniAccept(this, f, null)) {
            // 执行到这：若是d.uniAccept(this, f, null)返回false，表示上一个任务仍在执行中
            // 用新创建的CompletableFuture和驱动thenAccept的CompletableFuture构建一个UniAccept
            UniAccept<T> c = new UniAccept<T>(e, d, this, f); // 创建一个UniAccept，属于Completion类型，src就是this，dep就是新建的任务d，函数就是f，执行器就是e
            // 放入栈中
            // 注意： 此栈是第一步执行完，返回的CompletableFuture
            push(c); // 尝试压入栈顶 -- 不一定会成功，失败的话，c.next为null
            c.tryFire(SYNC); //
        }
        // 指定到这：
        // 如果为非异步的方法，即e等于null，会尝试执行阻塞直到执行
        // 如果为异步的方法，即e不等于null，直接返回新建的任务d
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniRun<T> extends UniCompletion<T,Void> {
        Runnable fn;
        UniRun(Executor executor, CompletableFuture<Void> dep,
               CompletableFuture<T> src, Runnable fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniRun(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniRun(CompletableFuture<?> a, Runnable f, UniRun<?> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                completeThrowable(x, r);
            else
                try {
                    if (c != null && !c.claim())
                        return false;
                    f.run();
                    completeNull();
                } catch (Throwable ex) {
                    completeThrowable(ex);
                }
        }
        return true;
    }

    private CompletableFuture<Void> uniRunStage(Executor e, Runnable f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.uniRun(this, f, null)) {
            UniRun<T> c = new UniRun<T>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniWhenComplete<T> extends UniCompletion<T,T> {
        BiConsumer<? super T, ? super Throwable> fn;
        UniWhenComplete(Executor executor, CompletableFuture<T> dep,
                        CompletableFuture<T> src,
                        BiConsumer<? super T, ? super Throwable> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniWhenComplete(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniWhenComplete(CompletableFuture<T> a,
                                  BiConsumer<? super T,? super Throwable> f,
                                  UniWhenComplete<T> c) {
        Object r; T t; Throwable x = null;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    x = ((AltResult)r).ex;
                    t = null;
                } else {
                    @SuppressWarnings("unchecked") T tr = (T) r;
                    t = tr;
                }
                f.accept(t, x);
                if (x == null) {
                    internalComplete(r);
                    return true;
                }
            } catch (Throwable ex) {
                if (x == null)
                    x = ex;
            }
            completeThrowable(x, r);
        }
        return true;
    }

    private CompletableFuture<T> uniWhenCompleteStage(
        Executor e, BiConsumer<? super T, ? super Throwable> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<T> d = new CompletableFuture<T>();
        if (e != null || !d.uniWhenComplete(this, f, null)) {
            UniWhenComplete<T> c = new UniWhenComplete<T>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniHandle<T,V> extends UniCompletion<T,V> {
        BiFunction<? super T, Throwable, ? extends V> fn;
        UniHandle(Executor executor, CompletableFuture<V> dep,
                  CompletableFuture<T> src,
                  BiFunction<? super T, Throwable, ? extends V> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniHandle(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final <S> boolean uniHandle(CompletableFuture<S> a,
                                BiFunction<? super S, Throwable, ? extends T> f,
                                UniHandle<S,T> c) {
        Object r; S s; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    x = ((AltResult)r).ex;
                    s = null;
                } else {
                    x = null;
                    @SuppressWarnings("unchecked") S ss = (S) r;
                    s = ss;
                }
                completeValue(f.apply(s, x));
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <V> CompletableFuture<V> uniHandleStage(
        Executor e, BiFunction<? super T, Throwable, ? extends V> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<V> d = new CompletableFuture<V>();
        if (e != null || !d.uniHandle(this, f, null)) {
            UniHandle<T,V> c = new UniHandle<T,V>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniExceptionally<T> extends UniCompletion<T,T> {
        Function<? super Throwable, ? extends T> fn;
        UniExceptionally(CompletableFuture<T> dep, CompletableFuture<T> src,
                         Function<? super Throwable, ? extends T> fn) {
            super(null, dep, src); this.fn = fn;
        }
        final CompletableFuture<T> tryFire(int mode) { // never ASYNC
            // assert mode != ASYNC;
            CompletableFuture<T> d; CompletableFuture<T> a;
            if ((d = dep) == null || !d.uniExceptionally(a = src, fn, this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniExceptionally(CompletableFuture<T> a,
                                   Function<? super Throwable, ? extends T> f,
                                   UniExceptionally<T> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            try {
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
                    if (c != null && !c.claim())
                        return false;
                    completeValue(f.apply(x));
                } else
                    internalComplete(r);
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private CompletableFuture<T> uniExceptionallyStage(
        Function<Throwable, ? extends T> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<T> d = new CompletableFuture<T>();
        if (!d.uniExceptionally(this, f, null)) {
            UniExceptionally<T> c = new UniExceptionally<T>(d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniRelay<T> extends UniCompletion<T,T> { // for Compose
        UniRelay(CompletableFuture<T> dep, CompletableFuture<T> src) {
            super(null, dep, src);
        }
        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d; CompletableFuture<T> a;
            if ((d = dep) == null || !d.uniRelay(a = src))
                return null;
            src = null; dep = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniRelay(CompletableFuture<T> a) {
        Object r;
        if (a == null || (r = a.result) == null)
            return false;
        if (result == null) // no need to claim
            completeRelay(r);
        return true;
    }

    @SuppressWarnings("serial")
    static final class UniCompose<T,V> extends UniCompletion<T,V> {
        Function<? super T, ? extends CompletionStage<V>> fn;
        UniCompose(Executor executor, CompletableFuture<V> dep,
                   CompletableFuture<T> src,
                   Function<? super T, ? extends CompletionStage<V>> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniCompose(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final <S> boolean uniCompose(
        CompletableFuture<S> a,
        Function<? super S, ? extends CompletionStage<T>> f,
        UniCompose<S,T> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            try {
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") S s = (S) r;
                CompletableFuture<T> g = f.apply(s).toCompletableFuture();
                if (g.result == null || !uniRelay(g)) {
                    UniRelay<T> copy = new UniRelay<T>(this, g);
                    g.push(copy);
                    copy.tryFire(SYNC);
                    if (result == null)
                        return false;
                }
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <V> CompletableFuture<V> uniComposeStage(
        Executor e, Function<? super T, ? extends CompletionStage<V>> f) {
        if (f == null) throw new NullPointerException();
        Object r; Throwable x;
        if (e == null && (r = result) != null) {
            // try to return function result directly
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    return new CompletableFuture<V>(encodeThrowable(x, r));
                }
                r = null;
            }
            try {
                @SuppressWarnings("unchecked") T t = (T) r;
                CompletableFuture<V> g = f.apply(t).toCompletableFuture();
                Object s = g.result;
                if (s != null)
                    return new CompletableFuture<V>(encodeRelay(s));
                CompletableFuture<V> d = new CompletableFuture<V>();
                UniRelay<V> copy = new UniRelay<V>(d, g);
                g.push(copy);
                copy.tryFire(SYNC);
                return d;
            } catch (Throwable ex) {
                return new CompletableFuture<V>(encodeThrowable(ex));
            }
        }
        CompletableFuture<V> d = new CompletableFuture<V>();
        UniCompose<T,V> c = new UniCompose<T,V>(e, d, this, f);
        push(c);
        c.tryFire(SYNC);
        return d;
    }

    /* ------------- Two-input Completions -------------- */

    /** A Completion for an action with two sources */
    @SuppressWarnings("serial")
    abstract static class BiCompletion<T,U,V> extends UniCompletion<T,V> {
        CompletableFuture<U> snd; // second source for action
        BiCompletion(Executor executor, CompletableFuture<V> dep,
                     CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(executor, dep, src); this.snd = snd;
        }
    }

    /** A Completion delegating to a BiCompletion */
    @SuppressWarnings("serial")
    static final class CoCompletion extends Completion {
        BiCompletion<?,?,?> base;
        CoCompletion(BiCompletion<?,?,?> base) { this.base = base; }
        final CompletableFuture<?> tryFire(int mode) {
            BiCompletion<?,?,?> c; CompletableFuture<?> d;
            if ((c = base) == null || (d = c.tryFire(mode)) == null)
                return null;
            base = null; // detach
            return d;
        }
        final boolean isLive() {
            BiCompletion<?,?,?> c;
            return (c = base) != null && c.dep != null;
        }
    }

    /** Pushes completion to this and b unless both done. */
    final void bipush(CompletableFuture<?> b, BiCompletion<?,?,?> c) {
        if (c != null) {
            Object r;
            while ((r = result) == null && !tryPushStack(c))
                lazySetNext(c, null); // clear on failure
            if (b != null && b != this && b.result == null) {
                Completion q = (r != null) ? c : new CoCompletion(c);
                while (b.result == null && !b.tryPushStack(q))
                    lazySetNext(q, null); // clear on failure
            }
        }
    }

    /** Post-processing after successful BiCompletion tryFire. */
    final CompletableFuture<T> postFire(CompletableFuture<?> a,
                                        CompletableFuture<?> b, int mode) {
        if (b != null && b.stack != null) { // clean second source
            if (mode < 0 || b.result == null)
                b.cleanStack();
            else
                b.postComplete();
        }
        return postFire(a, mode);
    }

    @SuppressWarnings("serial")
    static final class BiApply<T,U,V> extends BiCompletion<T,U,V> {
        BiFunction<? super T,? super U,? extends V> fn;
        BiApply(Executor executor, CompletableFuture<V> dep,
                CompletableFuture<T> src, CompletableFuture<U> snd,
                BiFunction<? super T,? super U,? extends V> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.biApply(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final <R,S> boolean biApply(CompletableFuture<R> a,
                                CompletableFuture<S> b,
                                BiFunction<? super R,? super S,? extends T> f,
                                BiApply<R,S,T> c) {
        Object r, s; Throwable x;
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            if (s instanceof AltResult) {
                if ((x = ((AltResult)s).ex) != null) {
                    completeThrowable(x, s);
                    break tryComplete;
                }
                s = null;
            }
            try {
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") R rr = (R) r;
                @SuppressWarnings("unchecked") S ss = (S) s;
                completeValue(f.apply(rr, ss));
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U,V> CompletableFuture<V> biApplyStage(
        Executor e, CompletionStage<U> o,
        BiFunction<? super T,? super U,? extends V> f) {
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<V> d = new CompletableFuture<V>();
        if (e != null || !d.biApply(this, b, f, null)) {
            BiApply<T,U,V> c = new BiApply<T,U,V>(e, d, this, b, f);
            bipush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class BiAccept<T,U> extends BiCompletion<T,U,Void> {
        BiConsumer<? super T,? super U> fn;
        BiAccept(Executor executor, CompletableFuture<Void> dep,
                 CompletableFuture<T> src, CompletableFuture<U> snd,
                 BiConsumer<? super T,? super U> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.biAccept(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final <R,S> boolean biAccept(CompletableFuture<R> a,
                                 CompletableFuture<S> b,
                                 BiConsumer<? super R,? super S> f,
                                 BiAccept<R,S> c) {
        Object r, s; Throwable x;
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            if (s instanceof AltResult) {
                if ((x = ((AltResult)s).ex) != null) {
                    completeThrowable(x, s);
                    break tryComplete;
                }
                s = null;
            }
            try {
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") R rr = (R) r;
                @SuppressWarnings("unchecked") S ss = (S) s;
                f.accept(rr, ss);
                completeNull();
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U> CompletableFuture<Void> biAcceptStage(Executor e, CompletionStage<U> o, BiConsumer<? super T,? super U> f) {
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.biAccept(this, b, f, null)) {
            BiAccept<T,U> c = new BiAccept<T,U>(e, d, this, b, f);
            bipush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class BiRun<T,U> extends BiCompletion<T,U,Void> {
        Runnable fn;
        BiRun(Executor executor, CompletableFuture<Void> dep,
              CompletableFuture<T> src,
              CompletableFuture<U> snd,
              Runnable fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.biRun(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final boolean biRun(CompletableFuture<?> a, CompletableFuture<?> b,
                        Runnable f, BiRun<?,?> c) {
        Object r, s; Throwable x;
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null || f == null)
            return false;
        if (result == null) {
            if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                completeThrowable(x, r);
            else if (s instanceof AltResult && (x = ((AltResult)s).ex) != null)
                completeThrowable(x, s);
            else
                try {
                    if (c != null && !c.claim())
                        return false;
                    f.run();
                    completeNull();
                } catch (Throwable ex) {
                    completeThrowable(ex);
                }
        }
        return true;
    }

    private CompletableFuture<Void> biRunStage(Executor e, CompletionStage<?> o,
                                               Runnable f) {
        CompletableFuture<?> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.biRun(this, b, f, null)) {
            BiRun<T,?> c = new BiRun<>(e, d, this, b, f);
            bipush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class BiRelay<T,U> extends BiCompletion<T,U,Void> { // for And
        BiRelay(CompletableFuture<Void> dep,
                CompletableFuture<T> src,
                CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null || !d.biRelay(a = src, b = snd))
                return null;
            src = null; snd = null; dep = null;
            return d.postFire(a, b, mode);
        }
    }

    boolean biRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        Object r, s; Throwable x;
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null)
            return false;
        if (result == null) {
            if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                completeThrowable(x, r);
            else if (s instanceof AltResult && (x = ((AltResult)s).ex) != null)
                completeThrowable(x, s);
            else
                completeNull();
        }
        return true;
    }

    /** Recursively constructs a tree of completions. */
    static CompletableFuture<Void> andTree(CompletableFuture<?>[] cfs,
                                           int lo, int hi) {
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (lo > hi) // empty
            d.result = NIL;
        else {
            CompletableFuture<?> a, b;
            int mid = (lo + hi) >>> 1;
            if ((a = (lo == mid ? cfs[lo] :
                      andTree(cfs, lo, mid))) == null ||
                (b = (lo == hi ? a : (hi == mid+1) ? cfs[hi] :
                      andTree(cfs, mid+1, hi)))  == null)
                throw new NullPointerException();
            if (!d.biRelay(a, b)) {
                BiRelay<?,?> c = new BiRelay<>(d, a, b);
                a.bipush(b, c);
                c.tryFire(SYNC);
            }
        }
        return d;
    }

    /* ------------- Projected (Ored) BiCompletions -------------- */

    /** Pushes completion to this and b unless either done. */
    final void orpush(CompletableFuture<?> b, BiCompletion<?,?,?> c) {
        if (c != null) {
            while ((b == null || b.result == null) && result == null) {
                if (tryPushStack(c)) {
                    if (b != null && b != this && b.result == null) {
                        Completion q = new CoCompletion(c);
                        while (result == null && b.result == null &&
                               !b.tryPushStack(q))
                            lazySetNext(q, null); // clear on failure
                    }
                    break;
                }
                lazySetNext(c, null); // clear on failure
            }
        }
    }

    @SuppressWarnings("serial")
    static final class OrApply<T,U extends T,V> extends BiCompletion<T,U,V> {
        Function<? super T,? extends V> fn;
        OrApply(Executor executor, CompletableFuture<V> dep,
                CompletableFuture<T> src,
                CompletableFuture<U> snd,
                Function<? super T,? extends V> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.orApply(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final <R,S extends R> boolean orApply(CompletableFuture<R> a,
                                          CompletableFuture<S> b,
                                          Function<? super R, ? extends T> f,
                                          OrApply<R,S,T> c) {
        Object r; Throwable x;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null) || f == null)
            return false;
        tryComplete: if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    if ((x = ((AltResult)r).ex) != null) {
                        completeThrowable(x, r);
                        break tryComplete;
                    }
                    r = null;
                }
                @SuppressWarnings("unchecked") R rr = (R) r;
                completeValue(f.apply(rr));
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U extends T,V> CompletableFuture<V> orApplyStage(
        Executor e, CompletionStage<U> o,
        Function<? super T, ? extends V> f) {
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<V> d = new CompletableFuture<V>();
        if (e != null || !d.orApply(this, b, f, null)) {
            OrApply<T,U,V> c = new OrApply<T,U,V>(e, d, this, b, f);
            orpush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class OrAccept<T,U extends T> extends BiCompletion<T,U,Void> {
        Consumer<? super T> fn;
        OrAccept(Executor executor, CompletableFuture<Void> dep,
                 CompletableFuture<T> src,
                 CompletableFuture<U> snd,
                 Consumer<? super T> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.orAccept(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final <R,S extends R> boolean orAccept(CompletableFuture<R> a,
                                           CompletableFuture<S> b,
                                           Consumer<? super R> f,
                                           OrAccept<R,S> c) {
        Object r; Throwable x;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null) || f == null)
            return false;
        tryComplete: if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    if ((x = ((AltResult)r).ex) != null) {
                        completeThrowable(x, r);
                        break tryComplete;
                    }
                    r = null;
                }
                @SuppressWarnings("unchecked") R rr = (R) r;
                f.accept(rr);
                completeNull();
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U extends T> CompletableFuture<Void> orAcceptStage(
        Executor e, CompletionStage<U> o, Consumer<? super T> f) {
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.orAccept(this, b, f, null)) {
            OrAccept<T,U> c = new OrAccept<T,U>(e, d, this, b, f);
            orpush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class OrRun<T,U> extends BiCompletion<T,U,Void> {
        Runnable fn;
        OrRun(Executor executor, CompletableFuture<Void> dep,
              CompletableFuture<T> src,
              CompletableFuture<U> snd,
              Runnable fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.orRun(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final boolean orRun(CompletableFuture<?> a, CompletableFuture<?> b,
                        Runnable f, OrRun<?,?> c) {
        Object r; Throwable x;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null) || f == null)
            return false;
        if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                    completeThrowable(x, r);
                else {
                    f.run();
                    completeNull();
                }
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private CompletableFuture<Void> orRunStage(Executor e, CompletionStage<?> o,
                                               Runnable f) {
        CompletableFuture<?> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.orRun(this, b, f, null)) {
            OrRun<T,?> c = new OrRun<>(e, d, this, b, f);
            orpush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class OrRelay<T,U> extends BiCompletion<T,U,Object> { // for Or
        OrRelay(CompletableFuture<Object> dep, CompletableFuture<T> src,
                CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }
        final CompletableFuture<Object> tryFire(int mode) {
            CompletableFuture<Object> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null || !d.orRelay(a = src, b = snd))
                return null;
            src = null; snd = null; dep = null;
            return d.postFire(a, b, mode);
        }
    }

    final boolean orRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        Object r;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null))
            return false;
        if (result == null)
            completeRelay(r);
        return true;
    }

    /** Recursively constructs a tree of completions. */
    static CompletableFuture<Object> orTree(CompletableFuture<?>[] cfs,
                                            int lo, int hi) {
        CompletableFuture<Object> d = new CompletableFuture<Object>();
        if (lo <= hi) {
            CompletableFuture<?> a, b;
            int mid = (lo + hi) >>> 1;
            if ((a = (lo == mid ? cfs[lo] :
                      orTree(cfs, lo, mid))) == null ||
                (b = (lo == hi ? a : (hi == mid+1) ? cfs[hi] :
                      orTree(cfs, mid+1, hi)))  == null)
                throw new NullPointerException();
            if (!d.orRelay(a, b)) {
                OrRelay<?,?> c = new OrRelay<>(d, a, b);
                a.orpush(b, c);
                c.tryFire(SYNC);
            }
        }
        return d;
    }

    /* ------------- 零输入 Async Forms -------------- */

    @SuppressWarnings("serial")
    static final class AsyncSupply<T> extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        /*
         * 实现了ForkJoinTask，
         *
         * 用来创建一个AsyncSupply类型的任务
         */
        CompletableFuture<T> dep;
        Supplier<T> fn; // 提供者函数式接口

        // 将 AsyncSupply 异步任务与返回给用户的 CompletableFuture 对象即dep进行绑定，用于在执行结束后【【回填】】结果到 CompletableFuture 对象，以及通知后续回调函数的运行。
        AsyncSupply(CompletableFuture<T> dep, Supplier<T> fn) {
            this.dep = dep;
            this.fn = fn;
        }

        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) {}
        public final boolean exec() { run(); return true; }

        public void run() {
            CompletableFuture<T> d; Supplier<T> f;
            if ((d = dep) != null && (f = fn) != null) {
                dep = null;
                fn = null; // help GC
                // d.result == null 说明当前这个Future还未运行或者未运行完 -- 注意：返回的结果为null时，会将result设为NIL
                // 因此d.result等于null就表示未运行或者未运行完
                if (d.result == null) {
                    try {
                        // f.get() 运行Supplier
                        d.completeValue(f.get());  // 正常完成，将结果直接赋值给result中，注意：结果若为null，会被额外封装为NIL后给Result
                    } catch (Throwable ex) {
                        d.completeThrowable(ex); // 异常完成
                    }
                }
                d.postComplete(); // 任务传播
            }
        }
    }

    static <U> CompletableFuture<U> asyncSupplyStage(Executor e, Supplier<U> f) {
        /*
         * 1. 任务丢进线程池，核心运行代码肯定在AsuncSupply#run中。
         * 2. CompletableFuture 丢进线程池后直接返回，这是一个异步任务。
         */
        if (f == null) throw new NullPointerException();
        // 创建一个CompletableFuture，表示当前任务，丢进了AsyncSupply中
        CompletableFuture<U> d = new CompletableFuture<U>();
        // 把新创建的CompletableFuture和Supplier丢到构建AsyncSupply，构建AsyncSupply任务
        e.execute(new AsyncSupply<U>(d, f)); // 交给common线程池去执行
        // 直接将CompletableFuture对象返回了。
        // 在线程池中执行AsyncSupply任务
        return d;
    }

    @SuppressWarnings("serial")
    static final class AsyncRun extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<Void> dep;
        Runnable fn;
        AsyncRun(CompletableFuture<Void> dep, Runnable fn) {
            this.dep = dep;
            this.fn = fn;
        }

        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) {}
        public final boolean exec() { run(); return true; }

        public void run() {
            CompletableFuture<Void> d;
            Runnable f;
            if ((d = dep) != null && (f = fn) != null) {
                dep = null;
                fn = null;
                if (d.result == null) {
                    try {
                        f.run(); // 运行 fn 函数 -- 无返回值
                        d.completeNull(); // 正常完成时，将result结果设置为NIL
                    } catch (Throwable ex) {
                        d.completeThrowable(ex); // 异常完成时，将result结果设置为封装异常的AltResult
                    }
                }
                d.postComplete();
            }
        }
    }

    static CompletableFuture<Void> asyncRunStage(Executor e, Runnable f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        e.execute(new AsyncRun(d, f));
        return d;
    }

    /* ------------- Signallers -------------- */

    /**
     * Completion for recording and releasing a waiting thread.  This
     * class implements ManagedBlocker to avoid starvation when
     * blocking actions pile up in ForkJoinPools.
     */
    @SuppressWarnings("serial")
    static final class Signaller extends Completion implements ForkJoinPool.ManagedBlocker {
        long nanos;                    // wait time if timed
        final long deadline;           // non-zero if timed
        volatile int interruptControl; // > 0: interruptible, < 0: interrupted
        volatile Thread thread;

        Signaller(boolean interruptible, long nanos, long deadline) {
            this.thread = Thread.currentThread();
            this.interruptControl = interruptible ? 1 : 0;
            this.nanos = nanos;
            this.deadline = deadline;
        }
        final CompletableFuture<?> tryFire(int ignore) {
            Thread w; // no need to atomically claim
            if ((w = thread) != null) {
                thread = null;
                LockSupport.unpark(w);
            }
            return null;
        }
        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (Thread.interrupted()) {
                int i = interruptControl;
                interruptControl = -1;
                if (i > 0)
                    return true;
            }
            if (deadline != 0L &&
                (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                thread = null;
                return true;
            }
            return false;
        }
        public boolean block() {
            if (isReleasable())
                return true;
            else if (deadline == 0L)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
        final boolean isLive() { return thread != null; }
    }

    /**
     * Returns raw result after waiting, or null if interruptible and
     * interrupted.
     */
    private Object waitingGet(boolean interruptible) {
        Signaller q = null;
        boolean queued = false;
        int spins = -1;
        Object r;
        // 经典 spin + lock -- 只有任务完成
        while ((r = result) == null) {
            // 初始话自旋次数
            if (spins < 0)
                spins = (Runtime.getRuntime().availableProcessors() > 1) ?
                    1 << 8 : 0; // Use brief spin-wait on multiprocessors
            // 随机自旋
            else if (spins > 0) {
                if (ThreadLocalRandom.nextSecondarySeed() >= 0)
                    --spins;
            }
            // 自旋次数用完，即spins=0

            else if (q == null)
                q = new Signaller(interruptible, 0L, 0L);// 初始化Signaller
            else if (!queued)
                queued = tryPushStack(q); // 初始化queued，并见其压入到当前栈中

            // 线程允许中断，且已经被中断啦，清空线程和栈
            else if (interruptible && q.interruptControl < 0) {
                q.thread = null;
                cleanStack();
                return null;
            }

            // 线程未中断，且自旋次数用完，且任务没有运行完，则阻塞起来
            else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q); // 阻塞，等待唤醒
                } catch (InterruptedException ie) {
                    q.interruptControl = -1; // 有中断异常，将q的interruptControl设为-1
                }
            }
        }

        // q等于null，表示在自旋期间已经拿到任务结果
        if (q != null) {
            q.thread = null; // help GC
            if (q.interruptControl < 0) {
                if (interruptible) // 是否允许中断
                    r = null; // report interruption
                else
                    Thread.currentThread().interrupt();
            }
        }
        postComplete(); // 继续运算下面的任务
        return r;
    }

    /**
     * Returns raw result after waiting, or null if interrupted, or
     * throws TimeoutException on timeout.
     */
    private Object timedGet(long nanos) throws TimeoutException {
        if (Thread.interrupted())
            return null;
        if (nanos <= 0L)
            throw new TimeoutException();
        long d = System.nanoTime() + nanos;
        Signaller q = new Signaller(true, nanos, d == 0L ? 1L : d); // avoid 0
        boolean queued = false;
        Object r;
        // We intentionally don't spin here (as waitingGet does) because
        // the call to nanoTime() above acts much like a spin.
        while ((r = result) == null) {
            if (!queued)
                queued = tryPushStack(q);
            else if (q.interruptControl < 0 || q.nanos <= 0L) {
                q.thread = null;
                cleanStack();
                if (q.interruptControl < 0)
                    return null;
                throw new TimeoutException();
            }
            else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ie) {
                    q.interruptControl = -1;
                }
            }
        }
        if (q.interruptControl < 0)
            r = null;
        q.thread = null;
        postComplete();
        return r;
    }

    /* ------------- public methods -------------- */

    /**
     * Creates a new incomplete CompletableFuture.
     */
    public CompletableFuture() {
    }

    /**
     * Creates a new complete CompletableFuture with given encoded result.
     */
    private CompletableFuture(Object r) {
        this.result = r;
    }

    /**
     * 由ForkJoinPool.commonPool中运行的异步任务，
     * supplier是无参有返回值的提供者函数式接口
     *
     * @param supplier a function returning the value to be used
     * to complete the returned CompletableFuture
     * @param <U> the function's return type
     * @return the new CompletableFuture
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return asyncSupplyStage(asyncPool, supplier);
    }

    /**
     * 由用户提供的执行器executor来运行异步任务，supplier是无参有返回值的提供者函数式接口
     *
     * @param supplier a function returning the value to be used
     * to complete the returned CompletableFuture
     * @param executor the executor to use for asynchronous execution
     * @param <U> the function's return type
     * @return the new CompletableFuture
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return asyncSupplyStage(screenExecutor(executor), supplier);
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the {@link ForkJoinPool#commonPool()} after
     * it runs the given action.
     *
     * @param runnable the action to run before completing the
     * returned CompletableFuture
     * @return the new CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return asyncRunStage(asyncPool, runnable);
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the given executor after it runs the given
     * action.
     *
     * @param runnable the action to run before completing the
     * returned CompletableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return asyncRunStage(screenExecutor(executor), runnable);
    }

    /**
     * Returns a new CompletableFuture that is already completed with
     * the given value.
     *
     * @param value the value
     * @param <U> the type of the value
     * @return the completed CompletableFuture
     */
    public static <U> CompletableFuture<U> completedFuture(U value) {
        return new CompletableFuture<U>((value == null) ? NIL : value);
    }

    /**
     * Returns {@code true} if completed in any fashion: normally,
     * exceptionally, or via cancellation.
     *
     * @return {@code true} if completed
     */
    public boolean isDone() {
        return result != null;
    }

    /**
     * 阻塞等待有结果可用
     *
     * @return the result value
     * @throws CancellationException if this future was cancelled
     * @throws ExecutionException if this future completed exceptionally
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
    public T get() throws InterruptedException, ExecutionException {
        Object r;
        // 1、没有结果时，调用waitingGet()进入阻塞
        // 2、有结果时，将r给reportGet()
        return reportGet((r = result) == null ? waitingGet(true) : r);
    }

    /**
     * Waits if necessary for at most the given time for this future
     * to complete, and then returns its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the result value
     * @throws CancellationException if this future was cancelled
     * @throws ExecutionException if this future completed exceptionally
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     * @throws TimeoutException if the wait timed out
     */
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Object r;
        long nanos = unit.toNanos(timeout);
        return reportGet((r = result) == null ? timedGet(nanos) : r); // 超时等待get
    }

    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally. To better
     * conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * CompletableFuture threw an exception, this method throws an
     * (unchecked) {@link CompletionException} with the underlying
     * exception as its cause.
     *
     * @return the result value
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed
     * exceptionally or a completion computation threw an exception
     */
    public T join() {
        Object r;
        return reportJoin((r = result) == null ? waitingGet(false) : r);
    }

    /**
     * Returns the result value (or throws any encountered exception)
     * if completed, else returns the given valueIfAbsent.
     *
     * @param valueIfAbsent the value to return if not completed
     * @return the result value, if completed, else the given valueIfAbsent
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed
     * exceptionally or a completion computation threw an exception
     */
    public T getNow(T valueIfAbsent) {
        // 执行未完成，立即返回valueIfAbsent
        // 执行完成，立即返回完成的结果
        Object r;
        return ((r = result) == null) ? valueIfAbsent : reportJoin(r);
    }

    /**
     * If not already completed, sets the value returned by {@link
     * #get()} and related methods to the given value.
     *
     * @param value the result value
     * @return {@code true} if this invocation caused this CompletableFuture
     * to transition to a completed state, else {@code false}
     */
    public boolean complete(T value) {
        boolean triggered = completeValue(value);
        postComplete();
        return triggered;
    }

    /**
     * If not already completed, causes invocations of {@link #get()}
     * and related methods to throw the given exception.
     *
     * @param ex the exception
     * @return {@code true} if this invocation caused this CompletableFuture
     * to transition to a completed state, else {@code false}
     */
    public boolean completeExceptionally(Throwable ex) {
        if (ex == null) throw new NullPointerException();
        boolean triggered = internalComplete(new AltResult(ex));
        postComplete();
        return triggered;
    }

    public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn) {
        return uniApplyStage(null, fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn) {
        return uniApplyStage(asyncPool, fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor) {
        return uniApplyStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return uniAcceptStage(null, action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return uniAcceptStage(asyncPool, action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return uniAcceptStage(screenExecutor(executor), action);
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return uniRunStage(null, action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return uniRunStage(asyncPool, action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return uniRunStage(screenExecutor(executor), action);
    }

    public <U,V> CompletableFuture<V> thenCombine(
        CompletionStage<? extends U> other,
        BiFunction<? super T,? super U,? extends V> fn) {
        return biApplyStage(null, other, fn);
    }

    public <U,V> CompletableFuture<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T,? super U,? extends V> fn) {
        return biApplyStage(asyncPool, other, fn);
    }

    public <U,V> CompletableFuture<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T,? super U,? extends V> fn, Executor executor) {
        return biApplyStage(screenExecutor(executor), other, fn);
    }

    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(null, other, action); // executor为null，就表示为同步执行
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(asyncPool, other, action); // executor非null，就表示为异步执行
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return biAcceptStage(screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other,
                                                Runnable action) {
        return biRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other,
                                                     Runnable action) {
        return biRunStage(asyncPool, other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other,
                                                     Runnable action,
                                                     Executor executor) {
        return biRunStage(screenExecutor(executor), other, action);
    }

    public <U> CompletableFuture<U> applyToEither(
        CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(null, other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(
        CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(asyncPool, other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(
        CompletionStage<? extends T> other, Function<? super T, U> fn,
        Executor executor) {
        return orApplyStage(screenExecutor(executor), other, fn);
    }

    public CompletableFuture<Void> acceptEither(
        CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(null, other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(
        CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(asyncPool, other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(
        CompletionStage<? extends T> other, Consumer<? super T> action,
        Executor executor) {
        return orAcceptStage(screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other,
                                                  Runnable action) {
        return orRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other,
                                                       Runnable action) {
        return orRunStage(asyncPool, other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other,
                                                       Runnable action,
                                                       Executor executor) {
        return orRunStage(screenExecutor(executor), other, action);
    }

    public <U> CompletableFuture<U> thenCompose(
        Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(null, fn);
    }

    public <U> CompletableFuture<U> thenComposeAsync(
        Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(asyncPool, fn);
    }

    public <U> CompletableFuture<U> thenComposeAsync(
        Function<? super T, ? extends CompletionStage<U>> fn,
        Executor executor) {
        return uniComposeStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<T> whenComplete(
        BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(null, action);
    }

    public CompletableFuture<T> whenCompleteAsync(
        BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(asyncPool, action);
    }

    public CompletableFuture<T> whenCompleteAsync(
        BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return uniWhenCompleteStage(screenExecutor(executor), action);
    }

    public <U> CompletableFuture<U> handle(
        BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(null, fn);
    }

    public <U> CompletableFuture<U> handleAsync(
        BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(asyncPool, fn);
    }

    public <U> CompletableFuture<U> handleAsync(
        BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return uniHandleStage(screenExecutor(executor), fn);
    }

    /**
     * Returns this CompletableFuture.
     *
     * @return this CompletableFuture
     */
    public CompletableFuture<T> toCompletableFuture() {
        return this;
    }

    // not in interface CompletionStage

    /**
     * Returns a new CompletableFuture that is completed when this
     * CompletableFuture completes, with the result of the given
     * function of the exception triggering this CompletableFuture's
     * completion when it completes exceptionally; otherwise, if this
     * CompletableFuture completes normally, then the returned
     * CompletableFuture also completes normally with the same value.
     * Note: More flexible versions of this functionality are
     * available using methods {@code whenComplete} and {@code handle}.
     *
     * @param fn the function to use to compute the value of the
     * returned CompletableFuture if this CompletableFuture completed
     * exceptionally
     * @return the new CompletableFuture
     */
    public CompletableFuture<T> exceptionally(
        Function<Throwable, ? extends T> fn) {
        return uniExceptionallyStage(fn);
    }

    /* ------------- Arbitrary-arity constructions -------------- */

    /**
     * Returns a new CompletableFuture that is completed when all of
     * the given CompletableFutures complete.  If any of the given
     * CompletableFutures complete exceptionally, then the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  Otherwise, the results,
     * if any, of the given CompletableFutures are not reflected in
     * the returned CompletableFuture, but may be obtained by
     * inspecting them individually. If no CompletableFutures are
     * provided, returns a CompletableFuture completed with the value
     * {@code null}.
     *
     * <p>Among the applications of this method is to await completion
     * of a set of independent CompletableFutures before continuing a
     * program, as in: {@code CompletableFuture.allOf(c1, c2,
     * c3).join();}.
     *
     * @param cfs the CompletableFutures
     * @return a new CompletableFuture that is completed when all of the
     * given CompletableFutures complete
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        return andTree(cfs, 0, cfs.length - 1);
    }

    /**
     * Returns a new CompletableFuture that is completed when any of
     * the given CompletableFutures complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  If no CompletableFutures
     * are provided, returns an incomplete CompletableFuture.
     *
     * @param cfs the CompletableFutures
     * @return a new CompletableFuture that is completed with the
     * result or exception of any of the given CompletableFutures when
     * one completes
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        return orTree(cfs, 0, cfs.length - 1);
    }

    /* ------------- Control and status methods -------------- */

    /**
     * If not already completed, completes this CompletableFuture with
     * a {@link CancellationException}. Dependent CompletableFutures
     * that have not already completed will also complete
     * exceptionally, with a {@link CompletionException} caused by
     * this {@code CancellationException}.
     *
     * @param mayInterruptIfRunning this value has no effect in this
     * implementation because interrupts are not used to control
     * processing.
     *
     * @return {@code true} if this task is now cancelled
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = (result == null) &&
            internalComplete(new AltResult(new CancellationException()));
        postComplete();
        return cancelled || isCancelled();
    }

    /**
     * Returns {@code true} if this CompletableFuture was cancelled
     * before it completed normally.
     *
     * @return {@code true} if this CompletableFuture was cancelled
     * before it completed normally
     */
    public boolean isCancelled() {
        Object r;
        return ((r = result) instanceof AltResult) &&
            (((AltResult)r).ex instanceof CancellationException);
    }

    /**
     * Returns {@code true} if this CompletableFuture completed
     * exceptionally, in any way. Possible causes include
     * cancellation, explicit invocation of {@code
     * completeExceptionally}, and abrupt termination of a
     * CompletionStage action.
     *
     * @return {@code true} if this CompletableFuture completed
     * exceptionally
     */
    public boolean isCompletedExceptionally() {
        Object r;
        return ((r = result) instanceof AltResult) && r != NIL;
    }

    /**
     * Forcibly sets or resets the value subsequently returned by
     * method {@link #get()} and related methods, whether or not
     * already completed. This method is designed for use only in
     * error recovery actions, and even in such situations may result
     * in ongoing dependent completions using established versus
     * overwritten outcomes.
     *
     * @param value the completion value
     */
    public void obtrudeValue(T value) {
        result = (value == null) ? NIL : value;
        postComplete();
    }

    /**
     * Forcibly causes subsequent invocations of method {@link #get()}
     * and related methods to throw the given exception, whether or
     * not already completed. This method is designed for use only in
     * error recovery actions, and even in such situations may result
     * in ongoing dependent completions using established versus
     * overwritten outcomes.
     *
     * @param ex the exception
     * @throws NullPointerException if the exception is null
     */
    public void obtrudeException(Throwable ex) {
        if (ex == null) throw new NullPointerException();
        result = new AltResult(ex);
        postComplete();
    }

    /**
     * Returns the estimated number of CompletableFutures whose
     * completions are awaiting completion of this CompletableFuture.
     * This method is designed for use in monitoring system state, not
     * for synchronization control.
     *
     * @return the number of dependent CompletableFutures
     */
    public int getNumberOfDependents() {
        int count = 0;
        for (Completion p = stack; p != null; p = p.next)
            ++count;
        return count;
    }

    /**
     * Returns a string identifying this CompletableFuture, as well as
     * its completion state.  The state, in brackets, contains the
     * String {@code "Completed Normally"} or the String {@code
     * "Completed Exceptionally"}, or the String {@code "Not
     * completed"} followed by the number of CompletableFutures
     * dependent upon its completion, if any.
     *
     * @return a string identifying this CompletableFuture, as well as its state
     */
    public String toString() {
        Object r = result;
        int count;
        return super.toString() +
            ((r == null) ?
             (((count = getNumberOfDependents()) == 0) ?
              "[Not completed]" :
              "[Not completed, " + count + " dependents]") :
             (((r instanceof AltResult) && ((AltResult)r).ex != null) ?
              "[Completed exceptionally]" :
              "[Completed normally]"));
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long RESULT;
    private static final long STACK;
    private static final long NEXT;
    static {
        try {
            final sun.misc.Unsafe u;
            UNSAFE = u = sun.misc.Unsafe.getUnsafe();
            Class<?> k = CompletableFuture.class;
            RESULT = u.objectFieldOffset(k.getDeclaredField("result"));
            STACK = u.objectFieldOffset(k.getDeclaredField("stack"));
            NEXT = u.objectFieldOffset
                (Completion.class.getDeclaredField("next"));
        } catch (Exception x) {
            throw new Error(x);
        }
    }
}
