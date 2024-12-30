package com.example.stamboot.bl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.StringUnicodeEncoderDecoder;
import com.mgnt.utils.TextUtils;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class BsonSerializer {

    @Resource
    private ObjectMapper bsonObjectMapper;

    public String serialize(Object obj) {
        String result = null;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            bsonObjectMapper.writeValue(baos, obj);
            String binaryContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            String unicodes = StringUnicodeEncoderDecoder.encodeStringToUnicodeSequence(binaryContent);
            String test = JsonUtils.writeObjectToJsonString(obj);
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    baos.toByteArray());
            Object binaryRestored = bsonObjectMapper.readValue(bais, obj.getClass());
            Object textRestored = JsonUtils.readObjectFromJsonString(test, obj.getClass());
            result = test;
        } catch (Exception e) {
            result = TextUtils.getStacktrace(e);
        }
        return result;
    }
}
