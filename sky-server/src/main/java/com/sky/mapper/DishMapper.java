package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 菜品分类查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 启用或禁用菜品
     *
     * @param status
     * @param id
     */
    @AutoFill(OperationType.UPDATE)
    void startOrStopDish(Integer status, Long id);

    /**
     * 根据id获取菜品
     *
     * @param id
     * @return
     */
    Dish getById(Long id);

    /**
     * 根据id删除菜品
     * @param id
     * @return
     */
    void deleteById(Long id);

    /**
     * 通过id集合批量删除对应菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 获取指定分类id的菜品列表
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId}")
    List<DishVO> getDishListByCid(Long categoryId);
}
