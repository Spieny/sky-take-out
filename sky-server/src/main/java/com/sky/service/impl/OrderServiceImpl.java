package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
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
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) {
        //处理各种业务异常
        // 1.地址是空的
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if(addressBook == null){throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);}
        // 2.购物车是空的
        //   获取用户的购物车
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
            //检查
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            //抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //插入订单数据
        Orders orders = new Orders();
        //  属性拷贝
        BeanUtils.copyProperties(orderSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        //  返回订单id
        orderMapper.insert(orders);
        Long orderId = orders.getId();
        log.info("返回下单Id = {}",orders.getId());
        List<OrderDetail> orderDetails = new ArrayList<>();
        //向订单明细添加所有菜品明细数据
        for(ShoppingCart cart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orderId);
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        //清空购物车
        shoppingCartMapper.deleteAll(userId);
        //封装VO返回结果
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();

        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("用户付款: {}",ordersPaymentDTO);
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, Long.valueOf(ordersPaymentDTO.getOrderNumber()),null);
        paySuccess(ordersPaymentDTO.getOrderNumber());
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

        Map<String, Object> map = new HashMap();
        map.put("type", 1);//消息类型，1表示来单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + outTradeNo);

        //通过WebSocket实现来单提醒，向客户端浏览器推送消息
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    @Override
    public PageResult pageHistroyOrders(Integer pageSize,Integer page,Integer status) {
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        if(status != null){
            ordersPageQueryDTO.setStatus(status);
        }

        Page<Orders> pages = orderMapper.listOrders(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (pages != null && pages.getTotal() > 0) {
            for (Orders orders : pages.getResult()) {
                // 获取订单Id
                Integer orderId = Math.toIntExact(orders.getId());
                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.queryDetails(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
                log.info("用户(id:{}) 查询订单(id:{}) 明细:{}", orders.getUserId(), orderId, list);
            }
        }

        return new PageResult(pages.getTotal(),list);
    }

    @Override
    public OrderVO getOrderDetail(Integer orderId) {
        OrderVO orderVO = orderMapper.getOrderById(orderId);
        orderVO.setOrderDetailList(orderDetailMapper.queryDetails(orderId));
        //获取此订单的送货地址
        AddressBook userAddressBook = addressBookMapper.getById(orderVO.getAddressBookId());
        String address = userAddressBook.getProvinceName() + userAddressBook.getCityName() + userAddressBook.getDistrictName() + userAddressBook.getDetail();
        orderVO.setAddress(address);
        return orderVO;
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.adminQueryOrders(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.queryDetails(Math.toIntExact(orders.getId()));

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    @Override
    public void repeatOrder(Integer orderId) {
        //清空所有购物车
        shoppingCartMapper.deleteAll(BaseContext.getCurrentId());
        OrderVO orderVO = orderMapper.getOrderById(orderId);
        //删除用户的id
        List<OrderDetail> orderDetails = orderDetailMapper.queryDetails(orderId);
        List<ShoppingCart> repetitiveOrder = new ArrayList<>();
        //装配为shopping cart对象
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            repetitiveOrder.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(repetitiveOrder);
    }

    @Override
    public void cancelOrder(Integer orderId) {
        orderMapper.updateStatus(Orders.CANCELLED, Orders.UN_PAID,LocalDateTime.now(), null, Long.valueOf(orderId));
    }

    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        orderMapper.adminCancelOrder(ordersCancelDTO,LocalDateTime.now());
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        Integer toBeConfirmed = orderMapper.getOrdersWithStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getOrdersWithStatus(Orders.CONFIRMED);
        Integer inDelivery = orderMapper.getOrdersWithStatus(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(inDelivery);
        return orderStatisticsVO;
    }

    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.updateStatus2M(orders);
    }

    @Override
    public void deliveryOrder(Integer id) {
        Orders orders = Orders.builder()
                .id(Long.valueOf(id))
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.updateStatus2M(orders);
    }

    @Override
    public void completeOrder(Integer id) {
        Orders orders = Orders.builder()
                .id(Long.valueOf(id))
                .status(Orders.COMPLETED)
                .build();
        orderMapper.updateStatus2M(orders);
    }

    @Override
    public void remind(Integer orderId) {
        Orders orders = orderMapper.getOrderById(orderId);
        if(orders == null){
            throw new OrderBusinessException("订单不存在");
        }
        Map<String, Object> map = new HashMap();
        map.put("type", 0);//消息类型，1表示来单提醒
        map.put("orderId", orderId);
        map.put("content", "订单号：" + orders.getNumber());

        webSocketServer.sendToAllClient(JSONObject.toJSONString(map));
    }
}
