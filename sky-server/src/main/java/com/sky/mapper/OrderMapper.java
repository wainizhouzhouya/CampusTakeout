package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/14 13:28
 *Description:
 */
@Mapper
public interface OrderMapper {

    /**
     * 用户下单
     */
    void insert(Orders orders);

    /**
     * 历史订单查询
     */
    Page<Orders> pageHistory(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 插入订单数据
     * @param order
     */
    void insertBatch(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单id获取订单基本信息
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    /**
     * 获取所有订单待派送的数量
     */
    @Select("select count(*) from orders where status = 3")
    Integer confirmed();

    /**
     * 获取所有订单派送中的数量
     */
    @Select("select count(*) from orders where status = 4")
    Integer deliveryInProgress();

    /**
     * 获取所有订单待接单的数量
     */
    @Select("select count(*) from orders where status = 2")
    Integer toBeConfirmed();

    /**
     * 根据订单状态和下单时间查询订单
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getStatusAndOrderTime(Integer pendingPayment, LocalDateTime time);


    /**
     * 查询每天的营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);


    /**
     * 获取每天的订单总数
     * @param map
     * @return
     */
    Integer getorderCountByMap(Map map);

    /**
     * 统计指定时间区间内的销量排名
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop(LocalDateTime begin,LocalDateTime end);
}
