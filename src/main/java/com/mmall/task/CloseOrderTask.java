package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CloseOrderTask {
    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonManager redissonManager;
//    @PreDestroy//在tomcat执行shutdown关闭前执行，弊端当要关闭的锁特别多时，关闭时间拉长
    public void delLock(){
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }
//    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务开始");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
//        orderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

//    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行
    public void closeOrderTaskV2(){
        log.info("关闭订单定时任务开始");
        long lockTimeout = Integer.parseInt(PropertiesUtil.getProperty("lock.timeout","2"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1){
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            log.info("没有获得分布式锁:{}" );
        }
        //        orderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    private void closeOrder(String lockName){
        //设置50s从redis自动删除
        RedisShardedPoolUtil.expire(lockName,50);
        log.info("获取{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
//        orderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{},ThreadName",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
    }

    //双重防死锁
//    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行
    public void closeOrderTaskV3(){//加入时间戳
        log.info("关闭订单定时任务开始");
        long lockTimeout = Integer.parseInt(PropertiesUtil.getProperty("lock.timeout","5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1){
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            //如果redis生命时长没有设置，通过当前时间与redis保存的过期时间对比判断是否关单
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK) ;
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                //重新获取锁
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + lockTimeout));
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr,getSetResult))){
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else {
                    log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        //        orderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行
    public void closeOrderTaskV4() {//框架实现
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            if (getLock = lock.tryLock(0,50, TimeUnit.SECONDS)){
                log.info("Redisson获取分布式锁{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
//                orderService.closeOrder(hour);
            }else {
                log.info("Redisson没有获取分布式锁{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
            }
        }catch (InterruptedException e){
            log.error("Redisson获取分布式锁异常");
        }finally {
            if (!getLock){
                return;
            }
            lock.unlock();
            log.info("Redisson获取分布式锁释放");
        }

    }
}
