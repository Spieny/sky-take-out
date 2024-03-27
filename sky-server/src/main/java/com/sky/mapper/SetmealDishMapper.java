package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealByDishIds(List<Long> dishIds);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.INSERT)
    void insertSetmealDishes(List<SetmealDish> dishes, Long setmealId);

    List<SetmealDish> getSetmealBySetmealId(Long id);

    @AutoFill(OperationType.UPDATE)
    void updateSetmealDish(SetmealDish dish);
}
