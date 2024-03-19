package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;

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
}
