package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class RedissonManager {
    private Config config = new Config();

    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    private static String redis1Ip = PropertiesUtil.getProperty("redis.ip1");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis.port1"));
    private static String redis1password = PropertiesUtil.getProperty("redis.password1");
    private static String redis2Ip = PropertiesUtil.getProperty("redis.ip2");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis.port2"));
    private static String redis2password = PropertiesUtil.getProperty("redis.password2");

    @PostConstruct
    private void init(){
        try{
            config.useSingleServer().setAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());
            redisson = (Redisson)Redisson.create(config);
            log.info("初始化Redisson结束");
        }catch (Exception e){

        }

    }
}
