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
@RequestMapping("/error/test")
public class ErrorHandlingTestController {
    @GetMapping
    public ResponseEntity<String> randomErrorGenerator(HttpServletRequest request) throws Exception {
        String message;
        long currentTime = System.currentTimeMillis();
        if (currentTime % 2 == 0) {
            throw new Exception("Random Exception:\nSome server glitch occurred");
        } else {
            message = LocalDateTime.now().toString() + " : No Exception";
        }
        return ResponseEntity.ok(message);
    }
}
