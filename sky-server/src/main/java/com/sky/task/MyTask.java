package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/19 17:25
 *Description:定时任务类
 */
//@Component
@Slf4j
public class MyTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理超时订单状态
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void processTimeOutOrder() {
        log.info("定时处理超时订单状态" + LocalDateTime.now());

        //大于15分钟未支付的订单就取消
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        List<Orders> list =
                orderMapper.getStatusAndOrderTime(Orders.PENDING_PAYMENT, time);

        if (list != null && list.size() > 0) {
            for (Orders orders : list) {
                //设置状态，取消原因，取消时间
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时未支付，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                //进行数据库操作
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 定时处理未送达订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点自动完成订单
    public void processDeliveryOrder() {
        log.info("定时处理未送达订单" + LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusHours(1);

        List<Orders> list =
                orderMapper.getStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);

        if (list != null && list.size() > 0) {
            for (Orders orders :list) {
                //设置订单状态
                orders.setStatus(Orders.COMPLETED);

                //进行数据库操作
                orderMapper.update(orders);
            }

        }
    }
}
