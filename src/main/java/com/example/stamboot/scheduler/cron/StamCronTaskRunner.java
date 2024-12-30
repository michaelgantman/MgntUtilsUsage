package com.example.stamboot.scheduler.cron;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@EnableAsync
//@Component
public class StamCronTaskRunner {
    private static final Log logger = LogFactory.getLog(StamCronTaskRunner.class);

    @Async
    @Scheduled(cron = "*/10 * * * * *")
//    @Scheduled(fixedRate = 10000)
    public void runTask() {
        logger.info("Current time is: " + LocalTime.now());
    }

}
