package com.example.stamboot.controller;

import com.example.stamboot.bl.BsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.TextUtils;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bson")
public class BsonTestController {

    @Resource
    private BsonSerializer bsonSerializer;

    @GetMapping
    public ResponseEntity<String> bsonTest() {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("name", "Michael");
        return ResponseEntity.ok(TextUtils.formatStringToPreserveIndentationForHtml(bsonSerializer.serialize(myMap)));
    }
}
