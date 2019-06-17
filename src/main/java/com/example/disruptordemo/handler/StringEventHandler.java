package com.example.disruptordemo.handler;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 01:08
 */

import com.example.disruptordemo.event.LongEvent;
import com.example.disruptordemo.event.StringEvent;
import com.lmax.disruptor.EventHandler;

public class StringEventHandler implements EventHandler<StringEvent> {
    @Override
    public void onEvent(StringEvent event, long sequence, boolean endOfBatch) throws InterruptedException {
        System.out.println("Consumer Event: " + event);
    }
}
