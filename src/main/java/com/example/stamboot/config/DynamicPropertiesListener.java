package com.example.stamboot.config;

import com.mgnt.utils.TextUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
public class DynamicPropertiesListener /*implements ApplicationListener<ApplicationEnvironmentPreparedEvent>*/ {

//    @Override
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        TextUtils.setRelevantPackage("com.example.stamboot.");
    }
}
