package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     *
     * @param orders
     * @return
     */
    Long insert(Orders orders);

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
     * 假装支付成功，更新订单
     * @param orderStatus
     * @param orderPaidStatus
     * @param check_out_time
     * @param id
     */
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long number,Long id);

    Page<Orders> listOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    Page<Orders> adminQueryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{orderId}")
    OrderVO getOrderById(Integer orderId);

    @Update("update orders set status = 6,cancel_reason = #{dto.cancelReason}, cancel_time = #{time} where id = #{dto.id}")
    void adminCancelOrder(OrdersCancelDTO dto,LocalDateTime time);

    @Select("select count(0) from orders where status = #{status}")
    Integer getOrdersWithStatus(Integer status);

    void updateStatus2M(Orders orders);

    /**
     * 根据状态和下单时间查询订单
     * @param status
     * @param orderTime
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrdertimeLT(Integer status, LocalDateTime orderTime);

    Double getTurnoverStatistics(LocalDateTime begin,LocalDateTime end);

    /**
     * 查询商品销量排名
     * @param begin
     * @param end
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    /**
     *根据动态条件统计订单数量
     * @param map
     */
    Integer countByMap(Map map);

}
