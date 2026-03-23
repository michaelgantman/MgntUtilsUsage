package com.example.stamboot.controller;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgnt.utils.TextUtils;

@RestController
@RequestMapping("/log")
public class LogFilteringDemoController {

    private static final Log logger = LogFactory.getLog(LogFilteringDemoController.class);

	@GetMapping
    public ResponseEntity<String> logEndPoint(@RequestParam Boolean cutTheBS, HttpServletRequest request) {
        String message;
        try {
            long currentTime = System.currentTimeMillis();
            if(currentTime % 2 == 0) {
                throw new Exception("Demo Exception");
            } else {
                message = LocalDateTime.now().toString() + " : No Exception";
                logger.info(message);
            }
        } catch (Exception e) {
        	String currentTime = LocalDateTime.now().toString();
            message = currentTime + TextUtils.getStacktrace(e, cutTheBS, "com.example.");
            logger.info(currentTime + " Raw Exception:", e);
            logger.info("Exception converted to String:\n" + message);
            message = TextUtils.formatStringToPreserveIndentationForHtml(message);
        }
        return ResponseEntity.ok(message);
    }
}
