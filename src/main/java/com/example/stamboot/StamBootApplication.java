package com.example.stamboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mgnt.utils.TextUtils;

@SpringBootApplication
public class StamBootApplication {

    public static void main(String[] args) {
        TextUtils.setRelevantPackage("com.example.stamboot.");
        SpringApplication.run(StamBootApplication.class, args);
    }
}
