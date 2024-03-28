package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 添加新套餐
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 启用或禁用套餐售卖
     */
    void startOrStop(Integer status, Long id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteInBatch(List<Long> ids);

    /**
     * 根据id获取对应的套餐
     * @param id
     * @return
     */
    SetmealVO getById(Long id);

    /**
     * 更新套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 根据分类id查询套餐接口
     *
     * @param categoryId
     * @return
     */
    Setmeal getSetmealById(Integer categoryId);

    /**
     * 根据套餐的id获取
     *
     * @param id
     * @return
     */
    List<DishItemVO> getDishesBySetmealId(Integer id);

    /**
     * 根据分类id获取所有的套餐
     * @param categoryId
     * @return
     */
    List<Setmeal> getSetmealsById(Integer categoryId);
}
