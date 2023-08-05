package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     *
     * @param ids
     * @return
     */
    // select setmeal_id from setmeal_dish where dish_id in (1,2,3....)
    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    @AutoFill(OperationType.INSERT)
    void insertBatch(List<SetmealDish> setmealDishes);
}
