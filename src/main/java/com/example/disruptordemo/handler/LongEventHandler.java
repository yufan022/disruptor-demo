package com.example.disruptordemo.handler;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 01:08
 */

import com.example.disruptordemo.event.LongEvent;
import com.lmax.disruptor.EventHandler;

public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Consumer Event: " + event);
    }
}
