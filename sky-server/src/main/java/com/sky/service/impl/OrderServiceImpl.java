package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import org.apache.http.MessageConstraintException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/14 13:27
 *Description:
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     *用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1 处理各种业务异常(地址簿为空/购物车业务为空)
        //1.1 获取订单地址
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        //1.2 判断地址是否为空
        if (addressBook == null) {
            //为空，抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //1.4 获取用户id
        Long userId = BaseContext.getCurrentId();
        //1.5 通过用户Id获取购物车商品数据
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //1.6 判断购物车商品是否为空
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2 向订单表插入1条数据
        //2.1 创建Order对象 并 封装
        Orders orders = new Orders();
        //2.2 将ordersSubmitDTO数据拷贝到orders中
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setPayStatus(Orders.PENDING_PAYMENT);//支付状态 待支付
        orders.setStatus(Orders.PAID);//订单状态 待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//设置订单号
        orders.setPhone(addressBook.getPhone());//设置收货人手机号
        orders.setConsignee(addressBook.getConsignee());//设置收货人
        orders.setUserId(userId);//设置用户id

        //2.3 插入数据库订单表
        orderMapper.insert(orders);

        //3 向订单明细表插入多条数据
        //3.1 创建需要插入订单明细表的 订单集合对象
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //3.2 将 订单明细 加入 订单明细集合
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            //将cart的数据拷贝到orderDetail
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());  //设置订单id  2.3插入数据库语句中设置了返回id
            //加入 订单明细集合
            orderDetailList.add(orderDetail);
        }
        //3.3 进行数据库批量插入操作
        orderDetailMapper.insertBatch(orderDetailList);
        //4 清空当前用户的购物车数据
        shoppingCartMapper.deleteAll(shoppingCart);
        //5 封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())  //订单id
                .orderTime(orders.getOrderTime())  //下单时间
                .orderNumber(orders.getNumber())   //订单号
                .orderAmount(orders.getAmount())   //订单金额
                .build();

        return orderSubmitVO;
    }

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    public PageResult pageHistory(int page, int pageSize, Integer status) {
        //进行分页查询
        PageHelper.startPage(page, pageSize);
        //进行数据库分页查询
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> ordersPage = orderMapper.pageHistory(ordersPageQueryDTO);


        List<OrderVO> list = new ArrayList<>();
        //查询订单明细，并封装入OrderVO进行响应
        if (ordersPage != null && ordersPage.getTotal() > 0) {
            for (Orders orders : ordersPage) {
                //订单id
                Long orderId = orders.getId();
                //获取订单明细
                List<OrderDetail> orderDetails =
                        orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                //添加入list集合
                list.add(orderVO);
            }

        }
        //获取总记录数
        long total = ordersPage.getTotal();

        return new PageResult(total, list);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端浏览器推送消息  type  orderId  content
        Map map=new HashMap();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:"+outTradeNo);

        String json= JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 查询订单详情
     * @param id 订单id
     * @return
     */
    public OrderVO orderDetail(Long id) {
        //根据订单id获取订单基本信息
        Orders orders = orderMapper.getById(id);
        //根据订单id获取订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //创建orderVO对象并封装
        OrderVO orderVO = new OrderVO();
        //将orders的数据拷贝给orderVO
        BeanUtils.copyProperties(orders, orderVO);
        //设置订单详细信息
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 取消订单
     * @param id 订单id
     * @return
     */
    public void cancelOrder(Long id) throws Exception {
        //根据订单id获取订单基本信息
        Orders orderDB = orderMapper.getById(id);

        //判断订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单支付状态,支付状态为 待付款、待接单 时可取消
        Integer status = orderDB.getStatus();
        if (status > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //创建Orders对象
        Orders orders = new Orders();
        orders.setId(orderDB.getId());

        //订单处于待接单状态下取消，需要进行退款
        if (orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    orderDB.getNumber(), //商户订单号
                    orderDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        //更新订单状态，取消原因，取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        //进行数据库数据更新
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id 订单id
     * @return
     */
    public void repetition(Long id) {


        //1 清空购物车所有商品
        //1.1 获取用户id
        Long userId = BaseContext.getCurrentId();
        //1.2 创建ShoppingCart对象 并 封装
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        //1.3 清空购物车所有商品
        shoppingCartMapper.deleteAll(shoppingCart);

        //2 根据id获取订单详细信息
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        //批量加入购物车
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart cart = new ShoppingCart();

            //将orderDetail数据拷贝到cart
            BeanUtils.copyProperties(orderDetail, cart);
            //设置用户id
            cart.setUserId(userId);
            //设置创建时间
            cart.setCreateTime(LocalDateTime.now());

            shoppingCartList.add(cart);
        }

        //将数据批量加入购物车
        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 商家订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //开启分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //进行数据库查询订单
        Page<Orders> ordersPage = orderMapper.pageHistory(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if (ordersPage != null && ordersPage.size() > 0) {
            for (Orders orders : ordersPage) {
                //订单id
                Long ordersId = orders.getId();
                //获取订单明细
                List<OrderDetail> orderDetailList =
                        orderDetailMapper.getByOrderId(ordersId);

                // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
                List<String> orderDishList = orderDetailList.stream().map(x -> {
                    String orderDish = x.getName() + "*" + x.getNumber() + ";";
                    return orderDish;
                }).collect(Collectors.toList());
                String orderDishes = String.join("", orderDishList);

                //创建OrderVO对象并封装
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDishes(orderDishes);
                //orderVO.setOrderDetailList(orderDetailList);

                //加入list集合
                list.add(orderVO);
            }
        }


        //获取总记录数
        long total = ordersPage.getTotal();

        return new PageResult(total, list);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    public OrderStatisticsVO statistics() {
        //获取所有订单待派送的数量
        Integer confirmed = orderMapper.confirmed();
        //获取所有订单派送中的数量
        Integer deliveryInProgress = orderMapper.deliveryInProgress();
        //获取所有订单待接单的数量
        Integer toBeConfirmed = orderMapper.toBeConfirmed();

        //创建OrdersPageQueryDTO对象 并 封装
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);

        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        //修改订单状态
        orderMapper.update(orders);

        //通过websocket向客户端浏览器推送消息  type  orderId  content
        Map map=new HashMap();
        map.put("type",1);
        map.put("orderId",ordersConfirmDTO.getId());
        map.put("content","订单号:"+orders.getNumber());

        String json= JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .build();

        //修改订单状态
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     * @return
     */
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();

        //修改订单状态
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    public void deliveryOrder(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        //修改订单状态
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    public void completeOrder(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();

        //修改订单状态
        orderMapper.update(orders);
    }

    /**
     * 催单
     * @param id
     */
    public void reminder(Long id) {
        //获取该订单信息
        Orders orders = orderMapper.getById(id);

        //判断是否为空
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //通过websocket向浏览器发送消息提示
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+orders.getNumber());

        String json=JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }


}
