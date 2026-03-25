package com.example.stamboot.controller;

import com.mgnt.utils.TextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/log")
public class LogFilteringDemoController {

    private static final Log logger = LogFactory.getLog(LogFilteringDemoController.class);

	@GetMapping("/display")
    public ResponseEntity<String> logDisplayEndPoint(@RequestParam Boolean cutTheBS) {
        String message;
        try {
            long currentTime = System.currentTimeMillis();
            if(currentTime % 2 == 0) {
                throw new Exception("Demo Exception");
            } else {
                message = LocalDateTime.now() + " : No Exception";
            }
        } catch (Exception e) {
            message = TextUtils.formatStringToPreserveIndentationForHtml(LocalDateTime.now() + ": " +
                    TextUtils.getStacktrace(e, cutTheBS, "com.example.").substring(1));
        }
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<String> logEndPoint() {
        String message;
        try {
            long currentTime = System.currentTimeMillis();
            if(currentTime % 2 == 0) {
                throw new Exception("Demo Exception");
            } else {
                message = LocalDateTime.now() + " : No Exception";
                logger.info(message);
            }
        } catch (Exception e) {
            logger.info("Raw Exception:", e);
            message = LocalDateTime.now() + ": Exception occurred, check the logs";
        }
        return ResponseEntity.ok(message);
    }

    }
