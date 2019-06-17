package com.example.disruptordemo.factory;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 01:05
 */

import com.example.disruptordemo.event.LongEvent;
import com.example.disruptordemo.event.StringEvent;
import com.lmax.disruptor.EventFactory;

public class StringEventFactory implements EventFactory<StringEvent> {
    @Override
    public StringEvent newInstance() {
        return new StringEvent();
    }
}
