package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/14 13:27
 *Description:
 */
public interface OrderService {


    /**
     *用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    PageResult pageHistory(int page, int pageSize, Integer status);


    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO orderDetail(Long id);


    /**
     * 取消订单
     * @param id 订单id
     * @return
     */
    void cancelOrder(Long id) throws Exception;


    /**
     * 再来一单
     * @param id 订单id
     * @return
     */
    void repetition(Long id);


    /**
     * 商家订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();


    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);


    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);


    /**
     * 商家取消订单
     * @param ordersCancelDTO
     * @return
     */
    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO);


    /**
     * 派送订单
     * @param id
     * @return
     */
    void deliveryOrder(Long id);


    /**
     * 完成订单
     * @param id
     * @return
     */
    void completeOrder(Long id);

    /**
     * 催单
     * @param id
     */
    void reminder(Long id);
}
