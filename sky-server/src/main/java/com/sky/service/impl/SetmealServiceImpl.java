package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sky.vo.SetmealVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        for (SetmealVO setmealVO: page){
            List<SetmealDish> setmealDishList = setmealMapper.getSetmealDishById(setmealVO.getId());
            setmealVO.setSetmealDishes(setmealDishList);
        }
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 添加新套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insertSetmeal(setmeal);

        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        Long id = setmeal.getId();
        setmealDishMapper.insertSetmealDishes(dishes,id);
    }

    /**
     * 启用或禁用套餐
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        setmealMapper.startOrStopDish(status,id);
    }

    @Override
    @Transactional
    public void deleteInBatch(List<Long> ids) {
        //判断当前套餐能否删除
        for (Long id: ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus().equals(StatusConstant.ENABLE)){
                //当前菜品在销售，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteByIds(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);

        List<SetmealDish> list = setmealDishMapper.getSetmealBySetmealId(id);
        setmealVO.setSetmealDishes(list);

        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        setmealMapper.updateSetmeal(setmealDTO);
        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        if(dishes != null && !dishes.isEmpty()){
            List<Long> ids = new ArrayList<>();
            ids.add(setmealDTO.getId());
            //删除原套餐内容
            setmealDishMapper.deleteByIds(ids);
            //插入新的套餐菜品
            setmealDishMapper.insertSetmealDishes(dishes,setmealDTO.getId());
        }
    }

    /**
     * 根据分类id查询套餐接口
     * @param categoryId
     * @return
     */
    @Override
    public Setmeal getSetmealById(Integer categoryId) {
        Setmeal setmeal = setmealMapper.getById(Long.valueOf(categoryId));
        return setmeal;
    }

    /**
     * 根据套餐id获取套餐包含的所有菜品的简单数据
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishesBySetmealId(Integer id) {
        return dishMapper.getDishBySetmealId(id);
    }

    @Override
    public List<Setmeal> getSetmealsById(Integer categoryId) {
        return setmealMapper.getAllByCategoryId(categoryId);
    }
}
