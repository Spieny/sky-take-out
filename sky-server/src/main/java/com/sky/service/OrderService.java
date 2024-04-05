package com.sky.service;

import com.github.pagehelper.Page;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface OrderService {
    /**
     * 下单
     * @param orderSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO);

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
     * 用户获取历史订单
     * @return
     */
    PageResult pageHistroyOrders(Integer page,Integer pageSize,Integer status);

    /**
     * 根据订单号获取订单详情
     * @param orderId
     * @return
     */
    OrderVO getOrderDetail(Integer orderId);

    /**
     * 用户再来一单
     * @param orderId
     */
    void repeatOrder(Integer orderId);

    /**
     * 用户取消订单
     * @param orderId
     */
    void cancelOrder(Integer orderId);

    /**
     * 商家搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO);

    /**
     * 获取三种待处理的订单数量
     * @return
     */
    OrderStatisticsVO getOrderStatistics();

    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    void deliveryOrder(Integer id);

    void completeOrder(Integer id);

    void remind(Integer orderId);
}
