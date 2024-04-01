package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void startOrStop(Integer status, Long id);

    DishVO getByIdWithFlavor(Long id);

    void deleteBatch(List<Long> ids);

    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 获取指定分类id的菜品列表
     * @return
     */
    List<DishVO> ListByCategoryIdWithFlavor(Long categoryId);
}
