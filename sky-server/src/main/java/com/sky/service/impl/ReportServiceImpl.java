package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定日期内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("商家获取营业额统计数据：{} -> {}",begin,end);
        //判断日期合法性
        if(!begin.isBefore(end) && !begin.isEqual(end)){
            throw new OrderBusinessException("开始日期不能在结束日期之后");
        }
        List<LocalDate> dateList;
        List<Double> turnoverList = new ArrayList<>();

        dateList = generateDateList(begin, end);
        String dateStr = StringUtils.join(dateList, ",");

        for (LocalDate date : dateList){
            LocalDateTime begin_time = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end_time = LocalDateTime.of(date, LocalTime.MAX);
            Double money = orderMapper.getTurnoverStatistics(begin_time,end_time);
            //查询结果为null，说明当天没有销售额，设置为0
            if(money == null){
                money = 0.0;
            }
            turnoverList.add(money);
        }

        String turnovers = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder().dateList(dateStr).turnoverList(turnovers).build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        log.info("商家获取用户统计数据：{} -> {}",begin,end);
        //判断日期合法性
        if(!begin.isBefore(end) && !begin.isEqual(end)){
            throw new OrderBusinessException("开始日期不能在结束日期之后");
        }

        List<LocalDate> dateList = generateDateList(begin, end);
        String dateStr = StringUtils.join(dateList, ",");

        //计算总用户数量 生产对应字符串
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date: dateList){
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MAX);
            totalUserList.add(userMapper.getTotalUser(dateTime));
        }
        String totalUserStr = StringUtils.join(totalUserList, ",");

        //计算新增用户列表 生成用户增长数据字符串
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date: dateList){
            LocalDateTime begin_time = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end_time = LocalDateTime.of(date, LocalTime.MAX);
            totalUserList.add(userMapper.getNewUser(begin_time,end_time));
        }
        String newUserStr = StringUtils.join(totalUserList, ",");

        //获取用户总量
        return UserReportVO.builder().dateList(dateStr).totalUserList(totalUserStr).newUserList(newUserStr).build();
    }

    @Override
    public SalesTop10ReportVO getTopTen(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()),",");
        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()),",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    @Override
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //每天订单总数集合
        List<Integer> orderCountList = new ArrayList<>();
        //每天有效订单数集合
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的总订单数 select count(id) from orders where order_time > ? and order_time < ?
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单数 select count(id) from orders where order_time > ? and order_time < ? and status = ?
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //时间区间内的总订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //时间区间内的总有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    public List<LocalDate> generateDateList(LocalDate begin,LocalDate end){
        //计算日期列表 生成日期字符串
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate tmp = begin;
        while(!tmp.isEqual(end)){
            dateList.add(tmp);
            tmp = tmp.plusDays(1L);
        }
        dateList.add(end);
        return dateList;
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库 获取营业数据
        LocalDateTime dateBegin =  LocalDateTime.of(LocalDate.now().minusDays(30),LocalTime.MIN);
        LocalDateTime dateEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(dateBegin, dateEnd);
        // 通过POI写入Excel
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/template.xlsx");
        try{
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //填充日期
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            //第四行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            //第五行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            //填充明细数据
            LocalDate start = LocalDate.now().minusDays(30);
            for (int i = 0; i < 30; i++) {
                LocalDate date = start.plusDays(i);
                BusinessDataVO detailBusinessData = workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN), LocalDateTime.of(date,LocalTime.MAX));

                log.info("{}的数据为{}",date,detailBusinessData);
                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(detailBusinessData.getTurnover());
                row.getCell(3).setCellValue(detailBusinessData.getValidOrderCount());
                row.getCell(4).setCellValue(detailBusinessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(detailBusinessData.getUnitPrice());
                row.getCell(6).setCellValue(detailBusinessData.getNewUsers());
            }
            // 通过输出流下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            //关闭资源
            excel.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    /**
     * 根据时间区间统计指定状态的订单数量
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap();
        map.put("status", status);
        map.put("begin",beginTime);
        map.put("end", endTime);
        return orderMapper.countByMap(map);
    }
}
