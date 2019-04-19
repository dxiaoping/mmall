package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));
    private static Boolean testBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));
    private static Boolean testReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","true"));
    private static String password = PropertiesUtil.getProperty("redis.password1");
    private static String ip = PropertiesUtil.getProperty("redis.ip1");
    private static Integer port = Integer.parseInt(PropertiesUtil.getProperty("redis.port2"));
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testBorrow);
        config.setTestOnReturn(testReturn);
        config.setBlockWhenExhausted(true);//连接耗尽时是否阻塞
        pool = new JedisPool(config,ip,port,1000*2,password);
    }
    static {
        initPool();
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }
    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }
    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
            Jedis jedis = pool.getResource();
            for(int i =0;i<10;i++){
                jedis.set("key"+i,"value"+i);
            }
            returnResource(jedis);
    }
}
