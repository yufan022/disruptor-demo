package com.example.disruptordemo.event;

import lombok.Data;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 01:04
 */
@Data
public class LongEvent {
    private long value;

    public void set(long value) {
        this.value = value;
    }
}