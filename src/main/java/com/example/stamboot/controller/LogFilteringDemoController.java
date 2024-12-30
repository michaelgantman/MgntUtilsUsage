package com.example.stamboot.controller;

import com.mgnt.utils.TextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/log")
public class LogFilteringDemoController {
    @GetMapping
    public ResponseEntity<String> logEndPoint(@RequestParam Boolean cutTheBS, HttpServletRequest request) {
        String message;
        try {
            long currentTime = System.currentTimeMillis();
            if(currentTime % 2 == 0) {
                throw new Exception("Demo Exception");
            } else {
                message = LocalDateTime.now().toString() + " : No Exception";
                System.out.println(message);
            }
        } catch (Exception e) {
            message = LocalDateTime.now().toString() + TextUtils.getStacktrace(e, cutTheBS, "com.example.");
            System.out.println(message);
            message = TextUtils.formatStringToPreserveIndentationForHtml(message);
        }
        return ResponseEntity.ok(message);
    }
}
