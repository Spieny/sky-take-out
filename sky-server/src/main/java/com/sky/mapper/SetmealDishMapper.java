package com.sky.mapper;

import com.sky.entity.SetmealDish;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealByDishIds(List<Long> dishIds);

    void deleteByIds(List<Long> ids);

    void insertSetmealDishes(List<SetmealDish> dishes, Long setmealId);

    List<SetmealDish> getSetmealBySetmealId(Long id);
}
