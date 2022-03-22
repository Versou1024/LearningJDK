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
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * A {@link ThreadPoolExecutor} that can additionally schedule
 * commands to run after a given delay, or to execute
 * periodically. This class is preferable to {@link java.util.Timer}
 * when multiple worker threads are needed, or when the additional
 * flexibility or capabilities of {@link ThreadPoolExecutor} (which
 * this class extends) are required.
 *
 * <p>Delayed tasks execute no sooner than they are enabled, but
 * without any real-time guarantees about when, after they are
 * enabled, they will commence. Tasks scheduled for exactly the same
 * execution time are enabled in first-in-first-out (FIFO) order of
 * submission.
 *
 * <p>When a submitted task is cancelled before it is run, execution
 * is suppressed. By default, such a cancelled task is not
 * automatically removed from the work queue until its delay
 * elapses. While this enables further inspection and monitoring, it
 * may also cause unbounded retention of cancelled tasks. To avoid
 * this, set {@link #setRemoveOnCancelPolicy} to {@code true}, which
 * causes tasks to be immediately removed from the work queue at
 * time of cancellation.
 *
 * <p>Successive executions of a task scheduled via
 * {@code scheduleAtFixedRate} or
 * {@code scheduleWithFixedDelay} do not overlap. While different
 * executions may be performed by different threads, the effects of
 * prior executions <a
 * href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * those of subsequent ones.
 *
 * <p>While this class inherits from {@link ThreadPoolExecutor}, a few
 * of the inherited tuning methods are not useful for it. In
 * particular, because it acts as a fixed-sized pool using
 * {@code corePoolSize} threads and an unbounded queue, adjustments
 * to {@code maximumPoolSize} have no useful effect. Additionally, it
 * is almost never a good idea to set {@code corePoolSize} to zero or
 * use {@code allowCoreThreadTimeOut} because this may leave the pool
 * without threads to handle tasks once they become eligible to run.
 *
 * <p><b>Extension notes:</b> This class overrides the
 * {@link ThreadPoolExecutor#execute(Runnable) execute} and
 * {@link AbstractExecutorService#submit(Runnable) submit}
 * methods to generate internal {@link ScheduledFuture} objects to
 * control per-task delays and scheduling.  To preserve
 * functionality, any further overrides of these methods in
 * subclasses must invoke superclass versions, which effectively
 * disables additional task customization.  However, this class
 * provides alternative protected extension method
 * {@code decorateTask} (one version each for {@code Runnable} and
 * {@code Callable}) that can be used to customize the concrete task
 * types used to execute commands entered via {@code execute},
 * {@code submit}, {@code schedule}, {@code scheduleAtFixedRate},
 * and {@code scheduleWithFixedDelay}.  By default, a
 * {@code ScheduledThreadPoolExecutor} uses a task type extending
 * {@link FutureTask}. However, this may be modified or replaced using
 * subclasses of the form:
 *
 *  <pre> {@code
 * public class CustomScheduledExecutor extends ScheduledThreadPoolExecutor {
 *
 *   static class CustomTask<V> implements RunnableScheduledFuture<V> { ... }
 *
 *   protected <V> RunnableScheduledFuture<V> decorateTask(
 *                Runnable r, RunnableScheduledFuture<V> task) {
 *       return new CustomTask<V>(r, task);
 *   }
 *
 *   protected <V> RunnableScheduledFuture<V> decorateTask(
 *                Callable<V> c, RunnableScheduledFuture<V> task) {
 *       return new CustomTask<V>(c, task);
 *   }
 *   // ... add constructors, etc.
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

    /*
     * This class specializes ThreadPoolExecutor implementation by
     *
     * 1. Using a custom task type, ScheduledFutureTask for
     *    tasks, even those that don't require scheduling (i.e.,
     *    those submitted using ExecutorService execute, not
     *    ScheduledExecutorService methods) which are treated as
     *    delayed tasks with a delay of zero.
     *
     * 2. Using a custom queue (DelayedWorkQueue), a variant of
     *    unbounded DelayQueue. The lack of capacity constraint and
     *    the fact that corePoolSize and maximumPoolSize are
     *    effectively identical simplifies some execution mechanics
     *    (see delayedExecute) compared to ThreadPoolExecutor.
     *
     * 3. Supporting optional run-after-shutdown parameters, which
     *    leads to overrides of shutdown methods to remove and cancel
     *    tasks that should NOT be run after shutdown, as well as
     *    different recheck logic when task (re)submission overlaps
     *    with a shutdown.
     *
     * 4. Task decoration methods to allow interception and
     *    instrumentation, which are needed because subclasses cannot
     *    otherwise override submit methods to get this effect. These
     *    don't have any impact on pool control logic though.
     */

    /**
     * False if should cancel/suppress periodic tasks on shutdown.
     */
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;

    /**
     * False if should cancel non-periodic tasks on shutdown.
     */
    private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

    /**
     * True if ScheduledFutureTask.cancel should remove from queue
     */
    private volatile boolean removeOnCancel = false;

    /**
     * Sequence number to break scheduling ties, and in turn to
     * guarantee FIFO order among tied entries.
     */
    private static final AtomicLong sequencer = new AtomicLong();

    /**
     * Returns current nanosecond time.
     */
    final long now() {
        return System.nanoTime();
    }

    private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

        /** Sequence number to break ties FIFO */
        private final long sequenceNumber; // 任务的序列号,在排序中会用到 -- 比如两个任务目标执行时间time相同时，就比较该字段

        /** The time the task is enabled to execute in nanoTime units */
        private long time; // 任务可执行时的时间，以纳秒表示。

        /**
         * Period in nanoseconds for repeating tasks.  A positive
         * value indicates fixed-rate execution.  A negative value
         * indicates fixed-delay execution.  A value of 0 indicates a
         * non-repeating task.
         */
        private final long period; // 0表示非周期任务。正数表示fixed-rate固定频率模式，负数表示fixed-delay固定延迟模式。

        /**
         * ScheduledThreadPoolExecutor#decorateTask允许我们包装一下Executor构造的RunnableScheduledFuture(实现为ScheduledFutureTask)并重新返回一个RunnableScheduledFuture给Executor。
         * 所以ScheduledFutureTask.outerTask实际上就是decorateTask方法包装出来的结果。
         * decorateTask默认返回的就是参数中的RunnableScheduledFuture，也就是不进行包装，这种情况下outerTask就是ScheduledFutureTask自身了。
         */
        RunnableScheduledFuture<V> outerTask = this;

        /**
         * Index into delay queue, to support faster cancellation.
         */
        int heapIndex; // 用于维护该任务在DelayedWorkQueue内部堆中的索引(在堆数组中的index)。

        /**
         * Creates a one-shot action with given nanoTime-based trigger time.
         */
        ScheduledFutureTask(Runnable r, V result, long ns) {
            super(r, result);
            this.time = ns; // ns等于0，就类似ThreadPoolExecutor中的任务，ns不等于0，就是一次性延迟任务
            this.period = 0; // 默认是非周期任务
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * Creates a periodic action with given nano time and period.
         */
        ScheduledFutureTask(Runnable r, V result, long ns, long period) {
            super(r, result);
            this.time = ns;
            this.period = period;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * Creates a one-shot action with given nanoTime-based trigger time.
         */
        ScheduledFutureTask(Callable<V> callable, long ns) {
            super(callable);
            this.time = ns;
            this.period = 0;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        public long getDelay(TimeUnit unit) {
            // 任务剩余执行时间 time - now
            return unit.convert(time - now(), NANOSECONDS);
        }

        public int compareTo(Delayed other) {
            // 完成小顶堆 -- 由于ScheduledThreadPool是优先级延迟队列，因此需要试下compareTo
            if (other == this) // compare zero if same object
                return 0;
            if (other instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
                long diff = time - x.time;
                // this的time 小于 x的time返回-1
                if (diff < 0)
                    return -1;
                // this的time 大于 x的time返回-1
                else if (diff > 0)
                    return 1;
                // this的time 等于 x的time返回，谁的序号小，谁就更小
                else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
            return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
        }

        /**
         * Returns {@code true} if this is a periodic (not a one-shot) action.
         *
         * @return {@code true} if periodic
         */
        public boolean isPeriodic() {
            // 不等于0，即周期任务
            return period != 0;
        }

        /**
         * Sets the next time to run for a periodic task.
         */
        private void setNextRunTime() {
            /*
             * p大于0，fixed-rate固定频率模式，时间设置为上一次时间+p即可
             * 注意：fixed-rate固定频率下这里的时间其实只是可以被执行的最小时间，不代表到点就要执行。
             * 如果这次任务还没执行完是肯定不会执行下一次的。
             */
            long p = period;
            if (p > 0)
                time += p;
            /*
             * p小于0，fixed-delay固定延迟模式，计算下一次任务可以被执行的时间。
             * 简单来说差不多就是当前时间+delay值。因为代码走到这里任务就已经结束了,now()可以认为就是任务结束时间。
             */
            else
                time = triggerTime(-p);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            // 调用FutureTask的cancel方法，mayInterruptIfRunning对于NEW状态的任务若在运行中的是否需要强制中断
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            // 取消任务成功，
            if (cancelled && removeOnCancel && heapIndex >= 0)
                remove(this);
            return cancelled;
        }

        /**
         * Overrides FutureTask version so as to reset/requeue if periodic.
         */
        public void run() {
            /*
             * 任务scheduleFutureTask将会在ThreadPoolExecutor.getTask()取出来，然后再ThreadPoolExecutor.runWorker()中执行这段run()代码
             *      此处有一点需要注意：
             *          在ThreadPoolExecutor.getTask()中，核心线程调用阻塞队列的take()，因此跳转到DelayWorkQueue.take()方法中，
             *          找到小顶堆的元素，即任务执行开始时间最小的任务，通过任务的getDelay()发现任务已过期，就可以获取出来，然后执行；
             *          否则就会使用leader/follower模式进行阻塞等待 -- worker线程最终被阻塞的位置
             *
             * run 方法的4种情况工作：
             * 1、检查当前线程池状态，任务是否可以继续执行，不可以就取消任务，结束run()
             * 2、任务可以执行，非周期性的任务，直接调用run()方法即可
             * 3、任务可以执行，周期性的任务，依次调用
             *          runAndReset() -- 从上述可，当任务从getTask()取出来的时候，就已经是可执行的，这里由于是周期性任务，不但需要执行任务还需要将其状态恢复，以便还可以继续使用
             *          setNextRunTime() -- 周期性任务执行完一次后需要重新设置开始执行时间，因为过下又会入小顶堆的，
             *          reExecutePeriodic(outerTask) -- 将task重新设置到优先级延迟阻塞队列中，并添加一个worker
             *                                          -- 这里会引起一些列多个线程的联动，通过offer()添加一个worker，那么就会唤醒阻塞在优先级延迟阻塞队列的take()中的线程B
             *                                             线程B会拿到小顶堆的任务，然后判断getDelay()是否过期，没有过期就不会允许执行
             */
            // 是否周期性，就是判断period是否为0。
            boolean periodic = isPeriodic();
            // 检查当前线程池状态下，任务是否可以继续被执行。
            if (!canRunInCurrentRunState(periodic))
                cancel(false);
            // 任务可以继续运行，是非周期性任务直接调用run运行即可。
            else if (!periodic)
                ScheduledFutureTask.super.run();
            // 任务不是非周期任务，则设置下次运行时间并调用reExecutePeriodic。 -- 重点重点重点
            else if (ScheduledFutureTask.super.runAndReset()) {
                setNextRunTime();
                // 需要重新将任务(outerTask)放到工作队列中。此方法源码会在后文介绍ScheduledThreadPoolExecutor本身API时提及。
                reExecutePeriodic(outerTask);
            }
        }
    }

    /**
     * Returns true if can run a task given current run state
     * and run-after-shutdown parameters.
     *
     * @param periodic true if this task periodic, false if delayed
     */
    boolean canRunInCurrentRunState(boolean periodic) {
        /*
         * 这个方法在任务提交时，任务运行时都会被调用以校验当前线程池状态是否可以运行任务。
         * isRunningOrShutdown的参数为布尔值，true则表示shutdown状态也返回true,否则只有running状态返回ture。
         * 如果为周期性任务则根据continueExistingPeriodicTasksAfterShutdown来判断是否shutdown后还可以执行。
         * 否则根据executeExistingDelayedTasksAfterShutdown来判断是否shutdown了仍然可以执行。
         *
         * 分析：
         * 非周期periodic为false，executeExistingDelayedTasksAfterShutdown默认是false，则当线程池在Running下才会true，允许任务运行
         * 周期periodic为true，取决于continueExistingPeriodicTasksAfterShutdown，为true就表示线程池即使为SHUTDOWN也需要继续运行
         */
        return isRunningOrShutdown(periodic ?
                                   continueExistingPeriodicTasksAfterShutdown :
                                   executeExistingDelayedTasksAfterShutdown);
    }

    /**
     * Main execution method for delayed or periodic tasks.  If pool
     * is shut down, rejects the task. Otherwise adds task to queue
     * and starts a thread, if necessary, to run it.  (We cannot
     * prestart the thread to run the task because the task (probably)
     * shouldn't be run yet.)  If the pool is shut down while the task
     * is being added, cancel and remove it if required by state and
     * run-after-shutdown parameters.
     *
     * @param task the task
     */
    private void delayedExecute(RunnableScheduledFuture<?> task) {
        /*
         * 延迟执行RunnableScheduledFuture
         */
        // 1、拒绝策略在线程池正常工作时无效[因为阻塞队列无界]，但在SHUTDOWN状态下需要来帮忙做出拒绝
        if (isShutdown())
            reject(task);
        else {
            super.getQueue().add(task); // 2、添加到DelayedWorkQueue中[优先级队列，小顶堆，任务开始执行时间最小的就会在堆顶]
            /*
             * 3、添加完任务后：检查任务是否可以被执行。
             * 若线程池已关闭，且任务在关闭状态下不可以继续运行，从队伍中删除任务task，并且中断任务task
             */
            if (isShutdown() &&
                !canRunInCurrentRunState(task.isPeriodic()) &&
                remove(task))
                task.cancel(false);
            else
                // 4、这是父类ThreadPoolExecutor的方法用于确保有worker线程来执行任务
                // 会创建worker() -- 创建好的worker()就会开始从上面super.getQueue().add(task)添加的任务中取出任务，这种worker是没有firstWork的
                ensurePrestart();
        }
    }

    /**
     * Requeues a periodic task unless current run state precludes it.
     * Same idea as delayedExecute except drops task rather than rejecting.
     *
     * @param task the task
     */
    void reExecutePeriodic(RunnableScheduledFuture<?> task) {
        /**
         * 主要任务：
         * 1、任务仍然可以运行的情况下，将task再次加入到优先级延迟阻塞队列中，这将引起ThreadPoolExecutor中的Worker线程唤醒
         * 2、启动一个新的Worker
         */
        if (canRunInCurrentRunState(true)) {
            super.getQueue().add(task);
            if (!canRunInCurrentRunState(true) && remove(task))
                task.cancel(false);
            else
                ensurePrestart();
        }
    }

    /**
     * Cancels and clears the queue of all tasks that should not be run
     * due to shutdown policy.  Invoked within super.shutdown.
     */
    @Override void onShutdown() {
        BlockingQueue<Runnable> q = super.getQueue();
        boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy(); // 线程池关闭后是否继续执行已的延迟任务的策略
        boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy(); // 线程池关闭后是否继续已存在的周期任务的策略
        if (!keepDelayed && !keepPeriodic) {
            for (Object e : q.toArray())
                if (e instanceof RunnableScheduledFuture<?>)
                    ((RunnableScheduledFuture<?>) e).cancel(false);
            q.clear();
        }
        else {
            // Traverse snapshot to avoid iterator exceptions
            for (Object e : q.toArray()) {
                if (e instanceof RunnableScheduledFuture) {
                    RunnableScheduledFuture<?> t = (RunnableScheduledFuture<?>)e;
                    // 周期任务下：如果不需要继续执行，且任务t已被取消
                    // 延迟任务下：如果不需要继续执行，且任务t已被取消
                    // 从
                    if ((t.isPeriodic() ? !keepPeriodic : !keepDelayed) ||
                        t.isCancelled()) { // also remove if already cancelled
                        if (q.remove(t))
                            t.cancel(false);
                    }
                }
            }
        }
        tryTerminate();
    }

    /**
     * Modifies or replaces the task used to execute a runnable.
     * This method can be used to override the concrete
     * class used for managing internal tasks.
     * The default implementation simply returns the given task.
     *
     * @param runnable the submitted Runnable
     * @param task the task created to execute the runnable
     * @param <V> the type of the task's result
     * @return a task that can execute the runnable
     * @since 1.6
     */
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return task;
    }

    /**
     * Modifies or replaces the task used to execute a callable.
     * This method can be used to override the concrete
     * class used for managing internal tasks.
     * The default implementation simply returns the given task.
     *
     * @param callable the submitted Callable
     * @param task the task created to execute the callable
     * @param <V> the type of the task's result
     * @return a task that can execute the callable
     * @since 1.6
     */
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return task;
    }

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given core pool size.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ScheduledThreadPoolExecutor(int corePoolSize) {
        // 线程池是无界的，存活时间为0，使用自定义的DelayedWorkQueue作为阻塞队列
        // 默认拒绝策略是抛出异常，但由于阻塞队列无界，线程池无界，所以啥拒绝策略都是狗屁无用的
        // 另一个方面由于阻塞队列是无界的，因此线程池永远只能有corePoolSize个线程，
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue());
    }

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if {@code threadFactory} is null
     */
    public ScheduledThreadPoolExecutor(int corePoolSize,
                                       ThreadFactory threadFactory) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory);
    }

    /**
     * Creates a new ScheduledThreadPoolExecutor with the given
     * initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if {@code handler} is null
     */
    public ScheduledThreadPoolExecutor(int corePoolSize,
                                       RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), handler);
    }

    /**
     * Creates a new ScheduledThreadPoolExecutor with the given
     * initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if {@code threadFactory} or
     *         {@code handler} is null
     */
    public ScheduledThreadPoolExecutor(int corePoolSize,
                                       ThreadFactory threadFactory,
                                       RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory, handler);
    }

    /**
     * Returns the trigger time of a delayed action.
     */
    private long triggerTime(long delay, TimeUnit unit) {
        return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
    }

    /**
     * 将延迟时间转换为绝对时间
     */
    long triggerTime(long delay) {
        /*
         * 如果 delay < Long.Max_VALUE/2, 则下次执行时间为当前时间+delay。
         * 否则为了避免队列中出现由于溢出导致的排序紊乱,需要调用overflowFree来修正一下delay(如果有必要的话)。
         *
         * 绝对时间：
         * 当前时间 + delay
         * 或
         * 当前时间 + overflowFree(delay)
         */
        return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
    }

    /**
     * 主要就是有这么一种情况：
     * 某个任务的delay为负数，说明当前可以执行(其实早该执行了)。
     * 工作队列中维护任务顺序是基于compareTo的，在compareTo中比较两个任务的顺序会用time相减，负数则说明优先级高。
     *
     * 那么就有可能出现一个delay为正数,减去另一个为负数的delay，结果上溢为负数，则会导致compareTo产生错误的结果。
     *
     * 为了特殊处理这种情况，首先判断一下队首的delay是不是负数，如果是正数不用管了,怎么减都不会溢出。
     * 否则可以拿当前delay减去队首的delay来比较看，如果不出现上溢，则整个队列都ok，排序不会乱。
     * 不然就把当前delay值给调整为Long.MAX_VALUE + 队首delay。
     */
    private long overflowFree(long delay) {
        Delayed head = (Delayed) super.getQueue().peek();
        if (head != null) {
            long headDelay = head.getDelay(NANOSECONDS);
            if (headDelay < 0 && (delay - headDelay < 0))
                delay = Long.MAX_VALUE + headDelay;
        }
        return delay;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        // new ScheduledFutureTask<V>(callable, triggerTime(delay, unit) -- 一次性延迟任务
        RunnableScheduledFuture<?> t = decorateTask(command, new ScheduledFutureTask<Void>(command, null,triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if (callable == null || unit == null)
            throw new NullPointerException();
        // new ScheduledFutureTask<V>(callable, triggerTime(delay, unit) -- 一次性延迟任务
        // triggerTime(delay, unit) -- 转相对时间为绝对时间
        RunnableScheduledFuture<V> t = decorateTask(callable, new ScheduledFutureTask<V>(callable, triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     * @throws IllegalArgumentException   {@inheritDoc}
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        // unit.toNanos(delay) : 周期fixed-rate模式
        ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit), unit.toNanos(period));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t;
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     * @throws IllegalArgumentException   {@inheritDoc}
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (delay <= 0)
            throw new IllegalArgumentException();
        // unit.toNanos(-delay) : 周期fixed-delay模式
        ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit), unit.toNanos(-delay));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t; // 包装器的任务
        delayedExecute(t);
        return t;
    }

    /**
     * Executes {@code command} with zero required delay.
     * This has effect equivalent to
     * {@link #schedule(Runnable,long,TimeUnit) schedule(command, 0, anyUnit)}.
     * Note that inspections of the queue and of the list returned by
     * {@code shutdownNow} will access the zero-delayed
     * {@link ScheduledFuture}, not the {@code command} itself.
     *
     * <p>A consequence of the use of {@code ScheduledFuture} objects is
     * that {@link ThreadPoolExecutor#afterExecute afterExecute} is always
     * called with a null second {@code Throwable} argument, even if the
     * {@code command} terminated abruptly.  Instead, the {@code Throwable}
     * thrown by such a task can be obtained via {@link Future#get}.
     *
     * @throws RejectedExecutionException at discretion of
     *         {@code RejectedExecutionHandler}, if the task
     *         cannot be accepted for execution because the
     *         executor has been shut down
     * @throws NullPointerException {@inheritDoc}
     */
    public void execute(Runnable command) {
        /**
         * 调用schedule做零延迟任务即可
         */
        schedule(command, 0, NANOSECONDS);
    }

    // Override AbstractExecutorService methods

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public Future<?> submit(Runnable task) {
        return schedule(task, 0, NANOSECONDS);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <T> Future<T> submit(Runnable task, T result) {
        // 零延迟
        return schedule(Executors.callable(task, result), 0, NANOSECONDS);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <T> Future<T> submit(Callable<T> task) {
        // 零延迟
        return schedule(task, 0, NANOSECONDS);
    }

    /**
     * Sets the policy on whether to continue executing existing
     * periodic tasks even when this executor has been {@code shutdown}.
     * In this case, these tasks will only terminate upon
     * {@code shutdownNow} or after setting the policy to
     * {@code false} when already shutdown.
     * This value is by default {@code false}.
     *
     * @param value if {@code true}, continue after shutdown, else don't
     * @see #getContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        // setContinueExistingPeriodicTasksAfterShutdownPolicy 即设置线程池SHUTDOWN后是否继续执行现有定期任务策略
        continueExistingPeriodicTasksAfterShutdown = value;
        if (!value && isShutdown()) // value为false，且线程池非Running状态，
            onShutdown();
    }

    /**
     * Gets the policy on whether to continue executing existing
     * periodic tasks even when this executor has been {@code shutdown}.
     * In this case, these tasks will only terminate upon
     * {@code shutdownNow} or after setting the policy to
     * {@code false} when already shutdown.
     * This value is by default {@code false}.
     *
     * @return {@code true} if will continue after shutdown
     * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return continueExistingPeriodicTasksAfterShutdown;
    }

    /**
     * Sets the policy on whether to execute existing delayed
     * tasks even when this executor has been {@code shutdown}.
     * In this case, these tasks will only terminate upon
     * {@code shutdownNow}, or after setting the policy to
     * {@code false} when already shutdown.
     * This value is by default {@code true}.
     *
     * @param value if {@code true}, execute after shutdown, else don't
     * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        executeExistingDelayedTasksAfterShutdown = value;
        // executeExistingDelayedTasksAfterShutdown 即线程池关闭后是否需要执行已存在的延迟任务
        if (!value && isShutdown())
            onShutdown();
    }

    /**
     * Gets the policy on whether to execute existing delayed
     * tasks even when this executor has been {@code shutdown}.
     * In this case, these tasks will only terminate upon
     * {@code shutdownNow}, or after setting the policy to
     * {@code false} when already shutdown.
     * This value is by default {@code true}.
     *
     * @return {@code true} if will execute after shutdown
     * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return executeExistingDelayedTasksAfterShutdown;
    }

    /**
     * Sets the policy on whether cancelled tasks should be immediately
     * removed from the work queue at time of cancellation.  This value is
     * by default {@code false}.
     *
     * @param value if {@code true}, remove on cancellation, else don't
     * @see #getRemoveOnCancelPolicy
     * @since 1.7
     */
    public void setRemoveOnCancelPolicy(boolean value) {
        removeOnCancel = value;
    }

    /**
     * Gets the policy on whether cancelled tasks should be immediately
     * removed from the work queue at time of cancellation.  This value is
     * by default {@code false}.
     *
     * @return {@code true} if cancelled tasks are immediately removed
     *         from the queue
     * @see #setRemoveOnCancelPolicy
     * @since 1.7
     */
    public boolean getRemoveOnCancelPolicy() {
        return removeOnCancel;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     *
     * <p>This method does not wait for previously submitted tasks to
     * complete execution.  Use {@link #awaitTermination awaitTermination}
     * to do that.
     *
     * <p>If the {@code ExecuteExistingDelayedTasksAfterShutdownPolicy}
     * has been set {@code false}, existing delayed tasks whose delays
     * have not yet elapsed are cancelled.  And unless the {@code
     * ContinueExistingPeriodicTasksAfterShutdownPolicy} has been set
     * {@code true}, future executions of existing periodic tasks will
     * be cancelled.
     *
     * @throws SecurityException {@inheritDoc}
     */
    public void shutdown() {
        super.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks
     * that were awaiting execution.
     *
     * <p>This method does not wait for actively executing tasks to
     * terminate.  Use {@link #awaitTermination awaitTermination} to
     * do that.
     *
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  This implementation
     * cancels tasks via {@link Thread#interrupt}, so any task that
     * fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution.
     *         Each element of this list is a {@link ScheduledFuture},
     *         including those tasks submitted using {@code execute},
     *         which are for scheduling purposes used as the basis of a
     *         zero-delay {@code ScheduledFuture}.
     * @throws SecurityException {@inheritDoc}
     */
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    /**
     * Returns the task queue used by this executor.  Each element of
     * this queue is a {@link ScheduledFuture}, including those
     * tasks submitted using {@code execute} which are for scheduling
     * purposes used as the basis of a zero-delay
     * {@code ScheduledFuture}.  Iteration over this queue is
     * <em>not</em> guaranteed to traverse tasks in the order in
     * which they will execute.
     *
     * @return the task queue
     */
    public BlockingQueue<Runnable> getQueue() {
        return super.getQueue();
    }

    /**
     * Specialized delay queue. To mesh with TPE declarations, this
     * class must be declared as a BlockingQueue<Runnable> even though
     * it can only hold RunnableScheduledFutures.
     */
    static class DelayedWorkQueue extends AbstractQueue<Runnable>
        implements BlockingQueue<Runnable> {

        /*
         * DelayedWorkQueue是ScheduledThreadPoolExecutor中阻塞队列的实现，
         * 它内部使用了小顶堆来使得自身具有优先队列的功能，并且通过Leader/Follower模式避免线程不必要的等待。
         * 从DelayedWorkQueue中取出任务时，任务一定已经至少到了可以被执行的时间。
         *
         * DelayedWorkQueue是ScheduledThreadPoolExecutor使用的工作队列。它内部维护了一个小根堆，
         * 根据任务的执行开始时间来维护任务顺序。但不同的地方在于，
         * 它对于ScheduledFutureTask类型的元素额外维护了元素在队列中堆数组的索引，用来实现快速取消。
         * DelayedWorkQueue用了ReentrantLock+Condition来实现管程保证数据的线程安全性。
         */

        private static final int INITIAL_CAPACITY = 16;
        private RunnableScheduledFuture<?>[] queue =
            new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
        private final ReentrantLock lock = new ReentrantLock();
        private int size = 0;

        /**
         * Thread designated to wait for the task at the head of the
         * queue.  This variant of the Leader-Follower pattern
         * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to
         * minimize unnecessary timed waiting.  When a thread becomes
         * the leader, it waits only for the next delay to elapse, but
         * other threads await indefinitely.  The leader thread must
         * signal some other thread before returning from take() or
         * poll(...), unless some other thread becomes leader in the
         * interim.  Whenever the head of the queue is replaced with a
         * task with an earlier expiration time, the leader field is
         * invalidated by being reset to null, and some waiting
         * thread, but not necessarily the current leader, is
         * signalled.  So waiting threads must be prepared to acquire
         * and lose leadership while waiting.
         */
        private Thread leader = null;

        /**
         * Condition signalled when a newer task becomes available at the
         * head of the queue or a new thread may need to become leader.
         */
        private final Condition available = lock.newCondition();

        /**
         * Sets f's heapIndex if it is a ScheduledFutureTask.
         */
        private void setIndex(RunnableScheduledFuture<?> f, int idx) {
            if (f instanceof ScheduledFutureTask)
                ((ScheduledFutureTask)f).heapIndex = idx;
        }

        /**
         * Sifts element added at bottom up to its heap-ordered spot.
         * Call only when holding lock.
         */
        private void siftUp(int k, RunnableScheduledFuture<?> key) {
            while (k > 0) {
                int parent = (k - 1) >>> 1;
                RunnableScheduledFuture<?> e = queue[parent];
                if (key.compareTo(e) >= 0)
                    break;
                queue[k] = e;
                setIndex(e, k);
                k = parent;
            }
            queue[k] = key;
            setIndex(key, k);
        }

        /**
         * Sifts element added at top down to its heap-ordered spot.
         * Call only when holding lock.
         */
        private void siftDown(int k, RunnableScheduledFuture<?> key) {
            int half = size >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                RunnableScheduledFuture<?> c = queue[child];
                int right = child + 1;
                if (right < size && c.compareTo(queue[right]) > 0)
                    c = queue[child = right];
                if (key.compareTo(c) <= 0)
                    break;
                queue[k] = c;
                setIndex(c, k);
                k = child;
            }
            queue[k] = key;
            setIndex(key, k);
        }

        /**
         * Resizes the heap array.  Call only when holding lock.
         */
        private void grow() {
            int oldCapacity = queue.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
            if (newCapacity < 0) // overflow
                newCapacity = Integer.MAX_VALUE;
            queue = Arrays.copyOf(queue, newCapacity);
        }

        /**
         * Finds index of given object, or -1 if absent.
         */
        private int indexOf(Object x) {
            // 再次判断i确实是本线程池的，因为remove方法的参数x完全可以是个其它池子里拿到的ScheduledFutureTask。
            if (x != null) {
                if (x instanceof ScheduledFutureTask) {
                    int i = ((ScheduledFutureTask) x).heapIndex;
                    // Sanity check; x could conceivably be a
                    // ScheduledFutureTask from some other pool.
                    if (i >= 0 && i < size && queue[i] == x)
                        return i;
                } else {
                    for (int i = 0; i < size; i++)
                        if (x.equals(queue[i]))
                            return i;
                }
            }
            return -1;
        }

        public boolean contains(Object x) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return indexOf(x) != -1;
            } finally {
                lock.unlock();
            }
        }

        public boolean remove(Object x) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = indexOf(x);
                if (i < 0)
                    return false;

                setIndex(queue[i], -1); // 清空
                /*
                 * 堆上删除某个元素操作就是将最后一个元素移到那个元素。
                 * 这时候有可能需要向上调整堆，也可能需要向下维护。
                 *
                 * 对于小根堆而言，如果移过去后比父元素小，则需要向上维护堆结构，
                 * 否则将左右两个子节点中较小值与当前元素比较，如果当前元素较大，则需要向下维护堆结构。
                 */
                int s = --size;
                RunnableScheduledFuture<?> replacement = queue[s]; // 获取小顶堆最后一个叶子节点
                queue[s] = null;
                // 如果参数x就是堆数组中最后一个元素则删除操作已经完毕了。
                if (s != i) {
                    // 尝试向下维护堆。
                    siftDown(i, replacement);
                    // 相等说明replacement比子节点都要小，尝试向上维护堆。
                    if (queue[i] == replacement)
                        siftUp(i, replacement);
                }
                return true;
            } finally {
                lock.unlock();
            }
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

        public boolean isEmpty() {
            return size() == 0;
        }

        public int remainingCapacity() {
            return Integer.MAX_VALUE;
        }

        public RunnableScheduledFuture<?> peek() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return queue[0];
            } finally {
                lock.unlock();
            }
        }

        public boolean offer(Runnable x) {
            if (x == null)
                throw new NullPointerException();
            RunnableScheduledFuture<?> e = (RunnableScheduledFuture<?>)x;
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = size;
                if (i >= queue.length)
                    // 容量扩增50%。
                    grow();
                // 第一个元素,其实这里也可以统一进行sift-up操作,没必要特判。
                size = i + 1;
                if (i == 0) {
                    queue[0] = e;
                    setIndex(e, 0);
                } else {
                    // 插入堆尾。
                    siftUp(i, e);
                }
                // 如果新加入的元素成为了堆顶,则原先的leader就无效了。
                if (queue[0] == e) {
                    leader = null;
                    // 由于原先leader已经无效被设置为null了,这里随便唤醒一个线程(未必是原先的leader)来取走堆顶任务。
                    available.signal();
                }
            } finally {
                lock.unlock();
            }
            return true;
        }

        public void put(Runnable e) {
            offer(e);
        }

        public boolean add(Runnable e) {
            return offer(e);
        }

        public boolean offer(Runnable e, long timeout, TimeUnit unit) {
            return offer(e);
        }

        /**
         * Performs common bookkeeping for poll and take: Replaces
         * first element with last and sifts it down.  Call only when
         * holding lock.
         * @param f the task to remove and return
         */
        private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
            int s = --size;
            RunnableScheduledFuture<?> x = queue[s];
            queue[s] = null;
            if (s != 0)
                siftDown(0, x);
            setIndex(f, -1);
            return f;
        }

        public RunnableScheduledFuture<?> poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> first = queue[0];
                if (first == null || first.getDelay(NANOSECONDS) > 0)
                    return null;
                else
                    return finishPoll(first);
            } finally {
                lock.unlock();
            }
        }

        public RunnableScheduledFuture<?> take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                /*
                 * 循环读取当前堆中最小也就执行开始时间最近的任务。
                 * 如果当前队列为空无任务,则在available条件上等待。
                 * 否则如果最近任务的delay<=0则返回这个任务以执行,否则的话根据是否可以作为leader分类 -- leader/follower模式
                 *     如果可以作为leader,则根据delay进行有时限等待。
                 *     否则无限等待直至被leader唤醒。
                 */
                for (;;) {
                    RunnableScheduledFuture<?> first = queue[0];
                    // 如果当前队列无元素，则在available条件上无限等待直至有任务通过offer入队并唤醒。
                    if (first == null)
                        available.await();
                    else {
                        long delay = first.getDelay(NANOSECONDS);
                        // 如果delay小于0说明任务该立刻执行了。
                        if (delay <= 0)
                            // ②从堆中移除元素并返回结果。
                            return finishPoll(first);
                        /*
                         * 在接下来等待的过程中,first应该清为null。
                         * 因为下一轮重新拿到的最近需要执行的任务很可能已经不是这里的first了。
                         * 所以对于接下来的逻辑来说first已经没有任何用处了，不该持有引用。
                         */
                        first = null;
                        // 如果目前有leader的话,当前线程作为follower在available条件上无限等待直至唤醒。
                        if (leader != null)
                            available.await();
                        else {
                            /*
                             * 没有leader时--设置leader，并进行超时等待
                             * 如果从available条件中被唤醒当前线程仍然是leader,则清空leader。
                             *
                             * 分析一下①这里相等需要将leader设置为null的原因:
                             * 1. 有线程offer一个任务执行开始时间更小的任务，导致堆顶更新了,那么当前线程就不适合作为leader，需要将leader的位置释放出来
                             * 2. 在情况1的上面堆顶更新之后,offer方法释放锁后,有其它线程通过take/poll从②拿到元素消费,然后读到③leader == null，然后将自身更新为leader。
                             *
                             * 对于这两种情况统一的处理逻辑就是只要leader为thisThread，则清leader为null用以接下来判断是否需要唤醒后继线程。
                             */
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                available.awaitNanos(delay);
                            } finally {
                                if (leader == thisThread) // ①
                                    leader = null;
                            }
                        }
                    }
                }
            } finally {
                /*
                 * ③ 如果当前堆中无元素(根据堆顶判断)则直接释放锁。
                 *
                 * 否则如果leader有值，说明当前线程一定不是leader，当前线程不用去唤醒后续等待线程。
                 *     否则由当前线程来唤醒后继等待线程。不过这并不代表当前线程原来是leader。
                 */
                if (leader == null && queue[0] != null)
                    available.signal();
                lock.unlock();
            }
        }

        public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    RunnableScheduledFuture<?> first = queue[0];
                    if (first == null) {
                        if (nanos <= 0)
                            return null;
                        else
                            nanos = available.awaitNanos(nanos);
                    } else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0)
                            return finishPoll(first);
                        if (nanos <= 0)
                            return null;
                        first = null; // don't retain ref while waiting
                        if (nanos < delay || leader != null)
                            nanos = available.awaitNanos(nanos);
                        else {
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                long timeLeft = available.awaitNanos(delay);
                                nanos -= delay - timeLeft;
                            } finally {
                                if (leader == thisThread)
                                    leader = null;
                            }
                        }
                    }
                }
            } finally {
                if (leader == null && queue[0] != null)
                    available.signal();
                lock.unlock();
            }
        }

        public void clear() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                for (int i = 0; i < size; i++) {
                    RunnableScheduledFuture<?> t = queue[i];
                    if (t != null) {
                        queue[i] = null;
                        setIndex(t, -1);
                    }
                }
                size = 0;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns first element only if it is expired.
         * Used only by drainTo.  Call only when holding lock.
         */
        private RunnableScheduledFuture<?> peekExpired() {
            // assert lock.isHeldByCurrentThread();
            RunnableScheduledFuture<?> first = queue[0];
            return (first == null || first.getDelay(NANOSECONDS) > 0) ?
                null : first;
        }

        public int drainTo(Collection<? super Runnable> c) {
            if (c == null)
                throw new NullPointerException();
            if (c == this)
                throw new IllegalArgumentException();
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> first;
                int n = 0;
                while ((first = peekExpired()) != null) {
                    c.add(first);   // In this order, in case add() throws.
                    finishPoll(first);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        public int drainTo(Collection<? super Runnable> c, int maxElements) {
            if (c == null)
                throw new NullPointerException();
            if (c == this)
                throw new IllegalArgumentException();
            if (maxElements <= 0)
                return 0;
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> first;
                int n = 0;
                while (n < maxElements && (first = peekExpired()) != null) {
                    c.add(first);   // In this order, in case add() throws.
                    finishPoll(first);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        public Object[] toArray() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return Arrays.copyOf(queue, size, Object[].class);
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (a.length < size)
                    return (T[]) Arrays.copyOf(queue, size, a.getClass());
                System.arraycopy(queue, 0, a, 0, size);
                if (a.length > size)
                    a[size] = null;
                return a;
            } finally {
                lock.unlock();
            }
        }

        public Iterator<Runnable> iterator() {
            return new Itr(Arrays.copyOf(queue, size));
        }

        /**
         * Snapshot iterator that works off copy of underlying q array.
         */
        private class Itr implements Iterator<Runnable> {
            final RunnableScheduledFuture<?>[] array;
            int cursor = 0;     // index of next element to return
            int lastRet = -1;   // index of last element, or -1 if no such

            Itr(RunnableScheduledFuture<?>[] array) {
                this.array = array;
            }

            public boolean hasNext() {
                return cursor < array.length;
            }

            public Runnable next() {
                if (cursor >= array.length)
                    throw new NoSuchElementException();
                lastRet = cursor;
                return array[cursor++];
            }

            public void remove() {
                if (lastRet < 0)
                    throw new IllegalStateException();
                DelayedWorkQueue.this.remove(array[lastRet]);
                lastRet = -1;
            }
        }
    }
}
