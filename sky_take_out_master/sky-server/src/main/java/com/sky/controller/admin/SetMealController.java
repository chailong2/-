package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理相关接口")
@Slf4j
public class SetMealController {

    @Autowired
    SetMealService setMealService;

    /**
     * 新增套餐信息
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}",setmealDTO);
        setMealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询，参数为:{}",setmealPageQueryDTO);
        PageResult pageResult=setMealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获得套餐信息
     * @param id
     * @return
     */
    @GetMapping({"/{id}"})
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐，参数为{}",id);
        SetmealVO setmealVO=setMealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐信息")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息,{}",setmealDTO);
        setMealService.updateSetmealWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐的批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("套餐的批量删除")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("套餐的批量删除:{}",ids);
        setMealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 套餐的启用和禁用
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐的禁用与启用")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrstop(@PathVariable Integer status,Long id){
        log.info("启用和禁用套餐,{},{}",status,id);
        setMealService.startOrstop(status,id);
        return Result.success();
    }
}
