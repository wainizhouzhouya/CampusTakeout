package com.sky.test;

import com.sky.config.RedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/10 13:29
 *Description:测试redis
 */
//@SpringBootTest
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate(){
        System.out.println(redisTemplate);
    }

    /**
     * 操作字符串类型的数据
     */
    @Test
    public void testString(){
        //set
        redisTemplate.opsForValue().set("city","广州");
        //get
        String city = (String) redisTemplate.opsForValue().get("city");
        System.out.println(city);
        //setex  key value 时间数值 时间单位  以下表示为3分钟
        redisTemplate.opsForValue().set("code",1234,3, TimeUnit.MINUTES);
        //setnx
        redisTemplate.opsForValue().setIfAbsent("lock",1);
        redisTemplate.opsForValue().setIfAbsent("lock",2);
    }

    /**
     * 操作哈希类型的数据
     */
    @Test
    public void testHash(){
        HashOperations hashOperations = redisTemplate.opsForHash();
        //hset
        hashOperations.put("100","name","张三");
        hashOperations.put("100","age","20");
        //hget
        String name = (String) hashOperations.get("100", "name");
        System.out.println(name);
        //hkeys
        Set keys = hashOperations.keys("100");
        System.out.println(keys);
        //hvals
        List values = hashOperations.values("100");
        System.out.println(values);
        //hdel
        hashOperations.delete("100","age");
    }
    /**
     * 操作列表类型的数据
     */
    @Test
    public void testList(){
        ListOperations listOperations = redisTemplate.opsForList();

        //lpush
        listOperations.leftPushAll("myList","a","b","c");
        listOperations.leftPush("myList","d");
        //lrange  0为首元素  -1表示最后一个元素
        listOperations.range("myList",0,-1);
        //rpop
        listOperations.rightPop("myList");
        //llen
        Long myList = listOperations.size("myList");
        System.out.println(myList);
    }

    /**
     * 操作集合类型的数据
     */
    @Test
    public void testSet(){
        SetOperations setOperations = redisTemplate.opsForSet();

        //sadd
        setOperations.add("set1","1","2","3");
        setOperations.add("set2","4","2","3");
        //smembers
        Set set1 = setOperations.members("set1");
        Set set2 = setOperations.members("set2");
        System.out.println(set1);
        System.out.println(set2);
        //scard
        Long set11 = setOperations.size("set1");
        Long set22 = setOperations.size("set2");
        System.out.println(set11);
        System.out.println(set22);
        //sinter
        Set intersect = setOperations.intersect("set1", "set2");
        System.out.println(intersect);
        //sunion
        Set union = setOperations.union("set1", "set2");
        System.out.println(union);
        //srem
        setOperations.remove("set1","1");
    }

    /**
     * 操作有序集合类型的数据
     */
    @Test
    public void testZSet(){
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //zadd
        zSetOperations.add("zset","a",10);
        zSetOperations.add("zset","b",11);
        zSetOperations.add("zset","c",12);
        //zrange
        Set zset = zSetOperations.range("zset", 0, -1);
        System.out.println(zset);
        //zincrby
        zSetOperations.incrementScore("zset","a",5);
        //zrem
        zSetOperations.remove("zset","a","b");
    }

    /**
     * 通用命令操作
     */
    @Test
    public void Common(){
        //keys pattern
        Set keys = redisTemplate.keys("*");
        System.out.println(keys);
        //exists
        Boolean name = redisTemplate.hasKey("name");
        Boolean set1 = redisTemplate.hasKey("set1");
        System.out.println(name);
        System.out.println(set1);
        //type
        for (Object key :keys) {
            DataType type = redisTemplate.type(key);
            System.out.println(type);
        }
        //del
        redisTemplate.delete("myList");
    }
}
