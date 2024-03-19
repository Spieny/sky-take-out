package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "套餐接口")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result saveWithDish(@RequestBody SetmealDTO setmealDTO){
        log.info("添加新套餐");
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("启用套餐，id：{}",id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteSetmeal(@RequestParam List<Long> ids){
        log.info("删除或批量删除套餐，id:{}",ids);
        setmealService.deleteInBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> get(@PathVariable Long id){
        log.info("获取id为{}的套餐",id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }



}
