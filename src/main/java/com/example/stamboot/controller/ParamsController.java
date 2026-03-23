package com.example.stamboot.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.stamboot.bl.tasks.ParamLoggingTask;
import com.example.stamboot.controller.errorhandling.ExceptionHandlerController;

@RestController
@RequestMapping("/params")
public class ParamsController {

    private static final Log logger = LogFactory.getLog(ExceptionHandlerController.class);

    @Resource
    private ParamLoggingTask paramLoggingTask;

    @GetMapping
    public ResponseEntity<String> logEndPoint(@RequestParam MultiValueMap<String, String> paramMap, HttpServletRequest request) {
        logger.info("Params passed: " + paramMap.toString() );
        paramLoggingTask.collectPassedParams(paramMap);
        return ResponseEntity.ok(paramMap.toString());
    }

//    @PostMapping(value = "/post", consumes = "application/json; charset=UTF-8")
    @PostMapping(value = "/post")
    @ResponseBody
    public String postParams(@RequestBody String body) {
        String message = "Retrieved Body: " + body;
        logger.info(message);
        return message;
    }
}
