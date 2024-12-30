package com.example.stamboot.controller;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.format.LetterFormatter;
import com.example.stamboot.letterformatting.format.LetterFormatterFactory;
import com.example.stamboot.letterformatting.postformat.LetterPostFormatter;
import com.example.stamboot.letterformatting.postformat.LetterPostFormatterFactory;
import com.example.stamboot.letterformatting.preformat.LetterPreFormatter;
import com.example.stamboot.letterformatting.preformat.LetterPreFormatterFactory;
import com.example.stamboot.letterformatting.service.LetterFormattingService;
import com.mgnt.utils.TextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/letter")
public class LetterFormattingController {

    private static final Log logger = LogFactory.getLog(LetterFormattingController.class);

    @Resource
    private LetterFormattingService letterFormattingService;

    @GetMapping
    public ResponseEntity<String> formatLetter(@RequestParam String content, @RequestParam String type) {
        ResponseEntity result;
        try {
            String formattedLetter = letterFormattingService.formatLetter(content, type);
            logger.info("Letter has been formatted according to document type: " + type + "\n" + formattedLetter);
            result = ResponseEntity.ok(TextUtils.formatStringToPreserveIndentationForHtml(formattedLetter));
        } catch (Exception e) {
            logger.error("Error occurred: " + TextUtils.getStacktrace(e));
            result = ResponseEntity.badRequest().body(TextUtils.formatStringToPreserveIndentationForHtml(e.getMessage()));
        }
        return result;
    }
}
