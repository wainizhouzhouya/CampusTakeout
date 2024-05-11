package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/20 15:26
 *Description:
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 指定日期的营业额统计
     * @param begin
     * @param end
     */
    public TurnoverReportVO getTurnOverStatistics(LocalDate begin, LocalDate end) {
        //获取指定日期的每天的日期 并 存入dataList集合中
        List<LocalDate> dateList = new ArrayList<>();
        //加入开始日期
        dateList.add(begin);
        //如果begin不等于最后一天，begin就往后推一天加入dataList
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //使用工具类将dateList集合转为字符串
        String dateStringList = StringUtils.join(dateList, ",");


        //存放每天营业额的集合turnOverList
        List<Double> turnOverList = new ArrayList<>();
        //获取每天的营业额
        for (LocalDate date : dateList) {
            //设置每天的统计营业额的起始时间和恶结束时间
            //起始时间 --  每天00：00
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //结束时间 --  每天23：59：999999
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //创建一个map集合用来封装数据
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnOver = orderMapper.sumByMap(map);
            //判断turnOver是否为空，为空转为0
            turnOver = turnOver == null ? 0.0 : turnOver;

            //加入turnOverList集合
            turnOverList.add(turnOver);
        }

        //使用工具类将turnOverList集合转为字符串
        String turnOverStringList = StringUtils.join(turnOverList, ",");


        return TurnoverReportVO.builder()
                .dateList(dateStringList)
                .turnoverList(turnOverStringList)
                .build();
    }

    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //获取指定日期的每天的日期 并 存入dateList集合中
        List<LocalDate> dateList = new ArrayList<>();
        //将begin加入dateList
        dateList.add(begin);
        //如果begin不等于最后一天，begin就往后推一天加入dataList
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //创建totalUserList集合用户存储当日用户总量
        List<Integer> totalUserList = new ArrayList<>();
        //创建newUserList集合用户存储当日 新增 新用户总量
        List<Integer> newUserList = new ArrayList<>();
        //查询当日用户总量 即 当日新增新用户数量
        for (LocalDate date : dateList) {
            //设置每天的统计营业额的起始时间和恶结束时间
            //起始时间 --  每天00：00
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //结束时间 --  每天23：59：999999
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);


            //创建一个map集合用来封装数据
            Map map = new HashMap();
            map.put("begin", beginTime);

            //进行数据库查询 查询当日用户总量
            Integer totalUser = userMapper.sumUserByMap(map);
            //对totalUser进行判断是否为空
            totalUser = totalUser == null ? 0 : totalUser;
            //加入totalUserList集合
            totalUserList.add(totalUser);

            //进行数据库查询 当日新增 新用户数量
            map.put("end", endTime);
            Integer newUser = userMapper.sumUserByMap(map);
            //对newUser进行判断是否为空
            newUser = newUser == null ? 0 : newUser;
            //加入newUserList集合
            newUserList.add(newUser);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //获取指定日期的每天的日期 并 存入dateList集合中
        List<LocalDate> dateList = new ArrayList<>();
        //将begin加入dateList
        dateList.add(begin);
        //如果begin不等于最后一天，begin就往后推一天加入dataList
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //创建订单数集合
        List<Integer> orderCountList = new ArrayList<>();
        //创建有效订单数集合
        List<Integer> validOrderCountList = new ArrayList<>();

        //获取订单数列表
        for (LocalDate date : dateList) {
            //设置每天的统计营业额的起始时间和恶结束时间
            //起始时间 --  每天00：00
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //结束时间 --  每天23：59：999999
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //创建一个map集合用来封装数据
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //进行数据库查询
            Integer orderCount = orderMapper.getorderCountByMap(map);
            //判断orderCount是否为空
            orderCount = orderCount == null ? 0 : orderCount;
            //添加入orderCountList集合
            orderCountList.add(orderCount);

            //查询有效订单列表
            //设置有效订单的状态条件
            map.put("status", Orders.COMPLETED);
            //进行数据库查询
            Integer validOrderCount = orderMapper.getorderCountByMap(map);
            //判断validOrderCount是否为空
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            //添加validOrderCount到validOrderCountList集合
            validOrderCountList.add(validOrderCount);
        }

        //计算订单总数变量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算有效订单总数变量
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();


        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalValidOrderCount != 0) {
            orderCompletionRate = (totalOrderCount.doubleValue() / totalValidOrderCount);
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名前10的菜品/套餐
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        //设置每天的统计营业额的起始时间和恶结束时间
        //起始时间 --  每天00：00
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        //结束时间 --  每天23：59：999999
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(beginTime, endTime);


        //创建商品名称列表
        List<String> nameList =
                salesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameListString = StringUtils.join(nameList, ",");
        //创建销量列表
        List<Integer> numberList =
                salesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        ;
        String numberListString = StringUtils.join(numberList, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameListString)
                .numberList(numberListString)
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询数据库，获取营业额，最近30天的运营数据
        //1.1 30天中开始的日期
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        //1.2 30天中结束的日期
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //1.3 进行数据库查询  查询概览数据
        BusinessDataVO businessData =
                workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入Excel文件中
        InputStream in =
                this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //2.1 基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //2.2 获取Excel模板文件的分页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //2.3 填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间: " + dateBegin + "至" + dateEnd);

            //2.4.1 获取第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            //2.4.2 获取第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //2.5 填充30日明细数据
            for (int i = 0; i < 30; i++) {
                //2.5.1  获取每一天的日期
                LocalDate date = dateBegin.plusDays(i);
                //2.5.2 获取每一天的详细数据
                BusinessDataVO data =
                        workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //2.5.3 获取某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            //3 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //4. 关闭资源
            out.close();
            excel.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
