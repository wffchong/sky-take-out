package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 保存套餐的基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        log.info("插入后的套餐id为：{}", setmeal.getId());
        // 获取插入后的id
        Long setmealId = setmeal.getId();

        // 向菜品-套餐关联表插入数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            // 给套餐里面的每一条菜品数据设置套餐id
            setmealDish.setSetmealId(setmealId);
        });

        // 设置好了id后批量插入 菜品--套餐表
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);

        // 获取和套餐关联的菜品数据
        List<SetmealDish> setmealDishList = setmealDishMapper.getDishBySetmealId(id);
        // 创建返回值 vo 对象
        SetmealVO setmealVO = new SetmealVO();
        // 复制套餐基本属性
        BeanUtils.copyProperties(setmeal, setmealVO);
        // 设置套餐的菜品数据
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }
}
