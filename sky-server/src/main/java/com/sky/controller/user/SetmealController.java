package com.sky.controller.user;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("userSetmealController")
@Api(tags = "套餐接口")
@RequestMapping("/user/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐接口")
    @Cacheable(cacheNames = "setmealCache",key = "#categoryId")
    public Result<List<Setmeal>> list(Integer categoryId){
        List<Setmeal> setmeals = setmealService.getSetmealsById(categoryId);
        /*//将套餐存入redis缓存
        redisTemplate.opsForValue().set(key,setmeals);*/
        return Result.success(setmeals);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("获取套餐id对应套餐的所有菜品")
    public Result<List<DishItemVO>> getDishes(@PathVariable Integer id){
        List<DishItemVO> list = setmealService.getDishesBySetmealId(id);
        return Result.success(list);
    }

}
