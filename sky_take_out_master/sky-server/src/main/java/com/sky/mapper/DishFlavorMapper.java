package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据dishid删除菜品口味
     * @param dish_id
     */
    @Delete("delete from dish_flavor where dish_id = #{dish_id}")
    void deleteByDishId(Long dish_id);

    /**
     * 根据dishid查询口味数据
     * @param id
     * @return
     */
    @Select("Select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> getByDishId(Long id);
}
