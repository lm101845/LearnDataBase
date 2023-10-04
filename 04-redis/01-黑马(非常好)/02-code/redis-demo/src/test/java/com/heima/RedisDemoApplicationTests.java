package com.heima;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisDemoApplicationTests {
    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    void testString() {
        // 写入一条String数据
        redisTemplate.opsForValue().set("banana", "香蕉");
        // 获取string数据
        Object banana = redisTemplate.opsForValue().get("banana");
        System.out.println("banana = " + banana);
    }
}
