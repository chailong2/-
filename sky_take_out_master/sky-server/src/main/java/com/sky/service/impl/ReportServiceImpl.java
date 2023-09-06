package com.sky.service.impl;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
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
import javax.servlet.http.HttpServlet;
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
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    WorkspaceService workspaceService;

    /**
     * 获得营业额信息
     * @return
     */
    @Override
    public TurnoverReportVO getTurnOverStatistics(LocalDate begin, LocalDate end) {
        //该集合用于记录从Begin到end这段时间内每天的日期
        List<LocalDate> dateList=new ArrayList<>();
        //记录营业额
        List<Double> turnOverList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            //计算指定日期后一天的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        for (LocalDate localDate : dateList) {
            //日期转换（因为数据库中的数据有时分秒，而我们这里的数据没有时分秒）
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate, LocalTime.MAX);
            //查询date对应的营业额
            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            if(turnover==null){
                turnover=0.0;
            }
            turnOverList.add(turnover);
        }
        //转换为字符串
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnOverList,","))
                .build();
    }

    /**
     * 获得用户信息
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            //计算指定日期后一天的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天新增用户数量
        List<Integer> newUserList=new ArrayList<>();
        //存放每总用户数量
        List<Integer> totalUserList=new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //日期转换（因为数据库中的数据有时分秒，而我们这里的数据没有时分秒）
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate, LocalTime.MAX);
            Map map=new HashMap();
            map.put("end",endTime);
            Integer totalUser = userMapper.countBymap(map);
            map.put("begin",beginTime);
            Integer newUser=userMapper.countBymap(map);
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }
    /**
     * 获得订单信息
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            //计算指定日期后一天的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> OrderContList=new ArrayList<>();
        List<Integer> ValidOrderCountList=new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //日期转换（因为数据库中的数据有时分秒，而我们这里的数据没有时分秒）
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate, LocalTime.MAX);
            Integer orderCount=getOrderCount(beginTime,endTime,null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            OrderContList.add(orderCount);
            ValidOrderCountList.add(validOrderCount);

        }
        //计算时间区间内的订单总数量
        Integer totalordercount = OrderContList.stream().reduce(Integer::sum).get();
        //有效订单数
        Integer validtotalordercount=ValidOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate=0.0;
        if (totalordercount!=0) {
            orderCompletionRate=validtotalordercount.doubleValue()/totalordercount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(OrderContList,","))
                .validOrderCountList(StringUtils.join(ValidOrderCountList,","))
                .totalOrderCount(totalordercount)
                .validOrderCount(validtotalordercount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 获取运营数据报表
     * @param response
     */
    @Override
    public void exportBuisinessData(HttpServletResponse response) {
        //查询数据库，获得营业数据（最近30天营业数据）
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //通过Apache POI将数据写入Excel表格
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/cqwm.xlsx");
        try {
            //创建一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(resourceAsStream);
            //向excel填充数据
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+" 至 "+dateEnd );
            XSSFRow row4 = sheet.getRow(3);
            row4.getCell(2).setCellValue(businessData.getTurnover());
            row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row4.getCell(6).setCellValue(businessData.getNewUsers());
            XSSFRow row5 = sheet.getRow(4);
            row5.getCell(2).setCellValue(businessData.getValidOrderCount());
            row5.getCell(4).setCellValue(businessData.getUnitPrice());
             //通过输出流将Excel文件下载
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            for (int i = 0; i < 30; i++) {
                LocalDate localDate = dateBegin.plusDays(i);
                //查询某一天的营业数据
                XSSFRow row = sheet.getRow(7 + i);
                BusinessDataVO businessDatatoday = workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN), LocalDateTime.of(localDate, LocalTime.MAX));
                row.getCell(1).setCellValue(localDate.toString());
                row.getCell(2).setCellValue(businessDatatoday.getTurnover());
                row.getCell(3).setCellValue(businessDatatoday.getValidOrderCount());
                row.getCell(4).setCellValue(businessDatatoday.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDatatoday.getUnitPrice());
                row.getCell(6).setCellValue(businessDatatoday.getNewUsers());
            }
            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 统计销量排名前十
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10=orderMapper.getSalesTop(beginTime,endTime);
        List<String> name = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(name, ",");
        List<Integer> number = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(number, ',');
        return  SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countBymap(map);
    }

}
