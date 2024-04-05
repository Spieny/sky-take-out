package com.sky.controller.user;

import com.github.pagehelper.Page;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user/order")
@RestController("userOrderController")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSubmitDTO){
        log.info("用户下单，参数：{}",orderSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(orderSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        orderService.payment(ordersPaymentDTO);
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = new OrderPaymentVO();
        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderPaymentVO.setNonceStr("妈妈生的");
        orderPaymentVO.setPaySign("妈妈生的");
        orderPaymentVO.setSignType("妈妈生的");
        orderPaymentVO.setSignType("1");
        orderPaymentVO.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("分页获取历史订单")
    public Result<PageResult> historyOrders(@RequestParam("pageSize") Integer pageSize, @RequestParam("page") Integer page,@RequestParam(value = "status",required = false) Integer status){
        log.info("用户查询历史订单: {},{},{}",pageSize,page,status);
        PageResult pageResult = orderService.pageHistroyOrders(pageSize,page,status);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{orderId}")
    @ApiOperation("获取订单详情")
    public Result<OrderVO> detail(@PathVariable Integer orderId){
        log.info("用户获取订单详情, id = {}",orderId);
        OrderVO orderVO = orderService.getOrderDetail(orderId);
        return Result.success(orderVO);
    }

    @PostMapping("/repetition/{orderId}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Integer orderId){
        log.info("用户再来一单:{}",orderId);
        orderService.repeatOrder(orderId);
        return Result.success();
    }

    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Integer orderId){
        log.info("用户取消订单:{}",orderId);
        orderService.cancelOrder(orderId);
        return Result.success();
    }

    @GetMapping("/reminder/{orderId}")
    public Result remind(@PathVariable Integer orderId){
        log.info("用户催单: {}",orderId);
        orderService.remind(orderId);
        return Result.success();
    }


}
