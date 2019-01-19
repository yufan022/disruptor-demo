package com.example.disruptordemo;

import com.example.disruptordemo.event.LongEvent;
import com.example.disruptordemo.factory.LongEventFactory;
import com.example.disruptordemo.handler.LongEventHandler;
import com.example.disruptordemo.producer.LongEventProducerWithTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        SpringApplication.run(DemoApplication.class, args);

        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE);

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        FutureTask<Integer> task = new FutureTask<>(() -> {
            while (true) {
                Object take = LongEventProducerWithTranslator.queue.poll();
//                System.out.println(take);
                if (take == null) {
                    break;
                }

            }
            return 1;
        });


        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);
        long start = System.currentTimeMillis();
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < 10000000; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
            //            Thread.sleep(1000);
        }
        new Thread(task).start();
        task.get();
        long end = System.currentTimeMillis();
        long costTime = end - start;



        System.out.println("end : " + costTime);
    }

}