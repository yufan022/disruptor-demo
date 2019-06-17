package com.example.disruptordemo.producer;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 12:12
 */

import com.example.disruptordemo.event.StringEvent;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StringEventProducerWithTranslator {
    private final RingBuffer<StringEvent> ringBuffer;

    public StringEventProducerWithTranslator(RingBuffer<StringEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    // 填充数据
    private static final EventTranslatorOneArg<StringEvent, ByteBuffer> TRANSLATOR =
            (event, sequence, bb) -> {
                bb.flip();
                byte[] dst = new byte[bb.limit()];
                bb.get(dst, 0, dst.length);
                bb.clear();
                event.setValue(new String(dst));
            };

    public void onData(ByteBuffer bb) {
        ringBuffer.publishEvent(TRANSLATOR, bb);
    }
}