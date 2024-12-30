package com.example.stamboot.controller;

import com.mgnt.utils.TextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/h-t-m-l-parsing")
public class HtmlParsingController {

    private static final String CLEAR_STRING =
            "This is non-indented line\n" +
            "  This is 2 spaces indented line\n" +
            "    This is 4 spaces indented line"

            + "\nAnd this is just a very long line with a lot               of                 spaces"
            ;

    private static final String FORMATTED_STRING =
            "This&nbsp;is&nbsp;non-indented&nbsp;line<br>" +
            "&nbsp;&nbsp;This&nbsp;is&nbsp;2&nbsp;spaces&nbsp;indented&nbsp;line<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;This&nbsp;is&nbsp;4&nbsp;spaces&nbsp;indented&nbsp;line"

            + "<br>And&nbsp;this&nbsp;is&nbsp;just&nbsp;a&nbsp;very&nbsp;long&nbsp;line&nbsp;with&nbsp;a&nbsp;lot&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;of&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;spaces"
            ;

    @GetMapping("/no-format")
    public ResponseEntity<String> simpleString() {
        return ResponseEntity.ok(CLEAR_STRING);
    }

    @GetMapping("/standard-format")
    public ResponseEntity<String> standardFormattedString() {
        return ResponseEntity.ok(FORMATTED_STRING);
    }

    @GetMapping("/code-format")
    public ResponseEntity<String> codeFormatedString() {
        return ResponseEntity.ok(TextUtils.formatStringToPreserveIndentationForHtml(CLEAR_STRING));
    }
}
