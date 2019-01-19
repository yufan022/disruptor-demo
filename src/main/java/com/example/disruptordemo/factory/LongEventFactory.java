package com.example.disruptordemo.factory;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 01:05
 */

import com.example.disruptordemo.event.LongEvent;
import com.lmax.disruptor.EventFactory;

public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
