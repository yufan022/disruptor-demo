package com.example.disruptordemo.event;

import lombok.Data;

/**
 * @Description:
 * @Author: Wyufan
 * @create: 2019-01-19 01:04
 */
@Data
public class StringEvent {
    private String value;

    public void set(String value) {
        this.value = value;
    }
}