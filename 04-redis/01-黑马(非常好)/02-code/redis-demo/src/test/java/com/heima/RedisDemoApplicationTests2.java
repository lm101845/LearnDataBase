package com.heima;

import com.heima.redis.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisDemoApplicationTests2 {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    @Test
    void testString() {
        // 写入一条String数据
        redisTemplate.opsForValue().set("dog", "小斑点狗");
        // 获取string数据
        Object dog = redisTemplate.opsForValue().get("dog");
        System.out.println("dog = " + dog);
    }

    //保存一个用户对象
    @Test
    void testSaveUser(){
        //写入数据
        redisTemplate.opsForValue().set("user:100",new User("胡歌",21));
        //获取数据
        User user = (User)redisTemplate.opsForValue().get("user:100");
        System.out.println("user:" + user);
    }
}
