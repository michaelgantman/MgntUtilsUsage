package com.example.stamboot.controller;

import com.example.stamboot.controller.errorhandling.ExceptionHandlerController;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.TextUtils;
import com.mgnt.utils.TimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@RestController
@RequestMapping("/upload")
public class UploadTestController {
    private static final Log logger = LogFactory.getLog(ExceptionHandlerController.class);

    @PostMapping
    public ResponseEntity<String> uploadTest(HttpServletRequest request) {
        try {
            String lengthStr = request.getHeader("content-length");
            int length = TextUtils.parseStringToInt(lengthStr, -1);
            if (length > 0) {
                byte[] buff = new byte[length];
                ServletInputStream sis = request.getInputStream();
                int counter = 0;
                while (counter < length) {
                    int chunkLength = sis.available();
                    byte[] chunk = new byte[chunkLength];
                    sis.read(chunk);
                    for (int i = counter, j = 0; i < counter + chunkLength; i++, j++) {
                        buff[i] = chunk[j];
                    }
                    counter += chunkLength;
                    if (counter < length) {
                        TimeUtils.sleepFor(5, TimeUnit.MILLISECONDS);
                    }
                }
                Files.write(Paths.get("C:\\Michael\\tmp\\testPic.jpg"), buff);
            }
        } catch (Exception e) {
            System.out.println(TextUtils.getStacktrace(e));
        }
        return ResponseEntity.ok("Success");
    }

    @PutMapping
    public void uploadDownloadTest(HttpServletRequest request, HttpServletResponse response) {
        try {
            String lengthStr = request.getHeader("content-length");
            int length = TextUtils.parseStringToInt(lengthStr, -1);
            if (length > 0) {
                byte[] buff = new byte[length];
                ServletInputStream sis = request.getInputStream();
                int counter = 0;
                while (counter < length) {
                    int chunkLength = sis.available();
                    byte[] chunk = new byte[chunkLength];
                    sis.read(chunk);
                    for (int i = counter, j = 0; i < counter + chunkLength; i++, j++) {
                        buff[i] = chunk[j];
                    }
                    counter += chunkLength;
                    if (counter < length) {
                        TimeUtils.sleepFor(5, TimeUnit.MILLISECONDS);
                    }
                }
                response.getOutputStream().write(buff);
            }
        } catch (Exception e) {
            System.out.println(TextUtils.getStacktrace(e));
        }
    }

    @PatchMapping
    public ResponseEntity<Map> patchTest(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        ResponseEntity<Map> responseEntity;
        Map<String, Object> inputMap = null;
        try {
            String lengthStr = request.getHeader("content-length");
            int length = TextUtils.parseStringToInt(lengthStr, -1);
            if (length > 0) {
                byte[] buff = new byte[length];
                ServletInputStream sis = request.getInputStream();
                int counter = 0;
                while (counter < length) {
                    int chunkLength = sis.available();
                    byte[] chunk = new byte[chunkLength];
                    sis.read(chunk);
                    for (int i = counter, j = 0; i < counter + chunkLength; i++, j++) {
                        buff[i] = chunk[j];
                    }
                    counter += chunkLength;
                    if (counter < length) {
                        TimeUtils.sleepFor(5, TimeUnit.MILLISECONDS);
                    }
                }
                String input = new String(buff, StandardCharsets.UTF_8);
                inputMap = JsonUtils.readObjectFromJsonString(input, Map.class);
            } else {
                throw new IllegalArgumentException("Wrong input");
            }
            result.put("input", inputMap);
            result.put("result", "Success");
            result.put("message", "Patch method executed successfully");
            responseEntity = ResponseEntity.ok(result);
        } catch (Exception e) {
            result.clear();
            result.put("result", "Failure");
            result.put("message", TextUtils.getStacktrace(e, "com.example.stamboot."));
            responseEntity = ResponseEntity.ok(result);
            System.out.println(TextUtils.getStacktrace(e));
        }
        return responseEntity;
    }

    @PostMapping("/body/text")
    public ResponseEntity<String> textBodyContentTest(HttpServletRequest request) {
        try {
            ServletInputStream sis = request.getInputStream();
            int chunkLength = sis.available();
            StringBuilder sb = new StringBuilder();
            while (chunkLength > 0) {
                byte[] chunk = new byte[chunkLength];
                sis.read(chunk);
                String contentChunk = new String(chunk, StandardCharsets.UTF_8);
                sb.append(contentChunk);
                TimeUtils.sleepFor(5, TimeUnit.MILLISECONDS);
                chunkLength = sis.available();
            }
            logger.info("Received body content: " + sb.toString());
        } catch (Exception e) {
            System.out.println(TextUtils.getStacktrace(e));
        }
        return ResponseEntity.ok("Success");
    }
}
