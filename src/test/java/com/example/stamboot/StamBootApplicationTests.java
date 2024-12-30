package com.example.stamboot;

import com.example.stamboot.bl.BsonSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class StamBootApplicationTests {

    @Resource
    private BsonSerializer bsonSerializer;

    @Test
    void contextLoads() {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("name", "Michael");
        ByteBuffer buff = ByteBuffer.allocate(16);
        buff.putInt(0x10203040);
        buff.putInt(0xA5B43DFF);
        buff.putInt(0xABCDEF30);
        buff.putInt(0x40302010);
        myMap.put("binary", buff.array());
        String res = bsonSerializer.serialize(myMap);
        Assert.notNull(res, "Error");
    }

}
