package com.heima;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.redis.pojo.User;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest
class RedisStringTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    void testString() {
        // 写入一条String数据
        stringRedisTemplate.opsForValue().set("dog2", "小斑点狗2");
        // 获取string数据
        Object dog = stringRedisTemplate.opsForValue().get("dog");
        System.out.println("dog = " + dog);
    }

    //ObjectMapper是SpringMVC里面默认使用的序列化工具，你也可以使用你熟悉的，比如fastjson
    private static final ObjectMapper mapper = new ObjectMapper();

    //保存一个用户对象
    @Test
    void testSaveUser() throws JsonProcessingException {
        //为了节约内存，不让redis存"@class": "com.heima.redis.pojo.User"，所以我们自己手动存
        //创建对象
        User user = new User("孙悟空", 520);
        //手动序列化——这样redis里面就不用存类名信息了
        String  json = mapper.writeValueAsString(user);
        stringRedisTemplate.opsForValue().set("user:200",json);
        //获取数据
        String jsonUser = stringRedisTemplate.opsForValue().get("user:200");
        //手动反序列化
        User user1 = mapper.readValue(jsonUser, User.class);
        System.out.println("user1:" + user1);
    }

    //测试hash
    @Test
    void testHash(){
        stringRedisTemplate.opsForHash().put("user:400","name","猪八戒");
        stringRedisTemplate.opsForHash().put("user:400","age","354");
        //注意：这里的数字也要是字符串
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries("user:400");
        System.out.println("entries:" + entries);
    }
}
