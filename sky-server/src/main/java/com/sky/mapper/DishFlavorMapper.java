package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 通过id获取指定菜品的口味
     * @param id
     * @return
     */
    List<DishFlavor> getFlavorsById(Long id);

    /**
     * 通过id删除指定菜品的所有口味
     * @param id
     */
    void deleteByDishId(Long id);

    /**
     * 通过id集合批量删除对应菜品的口味
     * @param ids
     */
    void deleteByDishIds(List<Long> ids);
}
