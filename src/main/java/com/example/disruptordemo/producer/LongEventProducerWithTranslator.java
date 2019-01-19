package com.example.disruptordemo.producer;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 12:12
 */

import com.example.disruptordemo.event.LongEvent;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.EventTranslatorOneArg;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class LongEventProducerWithTranslator {
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
            (event, sequence, bb) -> event.set(bb.getLong(0));

    public static ArrayBlockingQueue queue = new ArrayBlockingQueue<LongEvent>(10240000);
    public void onData(ByteBuffer bb) {
//        ringBuffer.publishEvent(TRANSLATOR, bb);
        queue.add(bb.getLong(0));
    }
}