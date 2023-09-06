package com.sky.mapper;
import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入新的订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 更新订单的支付状态
     * @param orderNumber
     */
    @Update("update orders set pay_status=1, status=2  where number=#{orderNumber}" )
    void updatePsByNumber(String orderNumber);
    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据ID查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 更新订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 按订单状态统计订单数量
     * @param status
     * @return
     */
    @Select("SELECT count(*) from orders where status=#{status}")
    Integer statistics(Integer status);

    /**
     * 按照订单状态和日期查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /**
     * 根据订单号和员工ID查询订单
     * @param orderNumber
     * @param userId
     * @return
     */
    @Select("select * from orders where number=#{orderNumber} and user_id=#{userId}")
    Orders getByNumberAndUserId(String orderNumber,Long userId);

    /**
     * 根据动态条件查询营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单
     * @param map
     * @return
     */
    Integer countBymap(Map map);

    /**
     * 根据指定时间内的TOP 10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop(LocalDateTime begin,LocalDateTime end);
}
