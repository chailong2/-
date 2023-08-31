package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        String key="dish_"+dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询");
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品的批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品的批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品的批量删除:{}",ids);
        dishService.deleteBatch(ids);
        //清理所有的菜品缓存数据
        ////获得redis中所有以dish开头的菜品数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据ID查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据ID查询菜品:{}",id);
        DishVO byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return Result.success(byIdWithFlavor);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息");
        dishService.updateWithFlavor(dishDTO);
        //清理所有的菜品缓存数据
        ////获得redis中所有以dish开头的菜品数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类ID查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> getByDishId(@RequestParam Long categoryId){
        List<Dish> dishes=dishService.getByDishId(categoryId);
        return Result.success(dishes);
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status,id);
        //清理所有的菜品缓存数据
        ////获得redis中所有以dish开头的菜品数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     *清理缓存数据
     * @param partten
     */
    private void cleanCache(String partten){
        Set keys = redisTemplate.keys(partten);
        redisTemplate.delete(keys);
    }
}

