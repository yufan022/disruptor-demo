package com.example.disruptordemo;

import com.example.disruptordemo.event.LongEvent;
import com.example.disruptordemo.factory.LongEventFactory;
import com.example.disruptordemo.handler.LongEventHandler;
import com.example.disruptordemo.producer.LongEventProducerWithTranslator;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {

    /**
     * 默认等待策略，使用锁和条件变量。CPU资源的占用少，延迟大。
     * 是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现；
     * 和BlockingQueue的实现很类似，通过使用锁和条件（Condition）进行线程同步和唤醒。
     * 此策略对于线程切换来说，最节约CPU资源，但在高并发场景下性能有限。
     * 真正的加锁阻塞策略，采用的就是ReentrantLock以及Condition来控制阻塞与唤醒。
     */
    public static WaitStrategy BLOCKING_WAIT = new BlockingWaitStrategy();
    /**
     * BlockingWaitStrategy中条件带超时的版本
     */
    public static WaitStrategy TIMEOUT_BLOCKING_WAIT = new TimeoutBlockingWaitStrategy(500, TimeUnit.SECONDS);
    /**
     * BlockingWaitStrategy的改进版，走了ReentrantLock和CAS轻量级锁结合的方式，
     * 不过注释说这算是实验性质的微性能改进。
     */
    public static WaitStrategy LITE_BLOCKING_WAIT = new LiteBlockingWaitStrategy();

    /**
     * 性能表现跟 BlockingWaitStrategy 差不多，对 CPU 的消耗也类似，但其对生产者线程的影响最小
     * CPU友好型策略。会在循环中不断等待数据。
     * 首先进行自旋等待，若不成功，则使用Thread.yield()让出CPU，并使用LockSupport.parkNanos(1)进行线程睡眠。
     * 所以，此策略数据处理数据可能会有较高的延迟，适合用于对延迟不敏感的场景。优点是对生产者线程影响小，典型应用场景是异步日志。
     * YieldingWaitStrategy的一种改进，SleepingWaitStrategy头100次先自旋，
     * 如果期间没有达成退出条件，则接下来100次主动让出cpu作为惩罚，如果还没有达成条件，则不再计数，每次睡1纳秒。
     * 在多次循环尝试不成功后，选择让出CPU，等待下次调度，多次调度后仍不成功，
     * 尝试前睡眠一个纳秒级别的时间再尝试。这种策略平衡了延迟和CPU资源占用，但延迟不均匀。
     */
    public static WaitStrategy SLEEPING_WAIT = new SleepingWaitStrategy();
    /**
     * 性能最好，适合用于低延迟的系统。
     * 低延时策略。消费者线程会不断循环监控RingBuffer的变化，在循环内部使用Thread.yield()让出CPU给其他线程。
     * 在要求极高性能且事件处理线数小于CPU，逻辑核心数的场景中，推荐使用此策略；例如，CPU开启超线程的特性。
     * 是自旋锁的一种改进，自旋锁对于cpu来说太重，于是YieldingWaitStrategy先自旋100次，
     * 如果期间没有达成退出等待的条件，则主动让出cpu给其他线程作为惩罚。
     * 在多次循环尝试不成功后，选择让出CPU，等待下次调。平衡了延迟和CPU资源占用，但延迟也比较均匀。
     */
    public static WaitStrategy YIELDING_WAIT = new YieldingWaitStrategy();
    /**
     * 死循环策略。消费者线程会尽最大可能监控缓冲区的变化，会占用所有CPU资源。
     * 自旋等待，类似Linux Kernel使用的自旋锁。低延迟但同时对CPU资源的占用也多。
     * 其实现很有趣，即不停的调用Thread类的onSpinWait方法。
     */
    public static WaitStrategy BUSY_SPIN_WAIT = new BusySpinWaitStrategy();
    /**
     * 上面多种策略的综合，CPU资源的占用少，延迟大。
     * 基本上是10000次自旋以后要么出让cpu，然后继续自旋，要么就采取新的等待策略。
     */
    public static WaitStrategy PHASED_BACK_OF_WAIT = new PhasedBackoffWaitStrategy(1000, 1000, TimeUnit.SECONDS, YIELDING_WAIT);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(DemoApplication.class, args);

        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 4096 * 4096;
        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, YIELDING_WAIT);

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

        long start = System.currentTimeMillis();
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < 10000000; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
            //            Thread.sleep(1000);
        }
        disruptor.shutdown();
        long end = System.currentTimeMillis();
        long costTime = end - start;
        System.out.println("end : " + costTime);
    }

}