package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

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

    @Override
    public void updateWithDish(SetmealDTO setmealDTO) {
        // 更新套餐的基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 删除原来的菜品
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());

        // 把新菜品全部插入
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (!setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                // 给套餐里面的每一条菜品数据设置套餐id
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 起售中的套餐不能删除

        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                // 起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });


        ids.forEach(setmealId -> {
            // 删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            // 删除套餐菜品关系表中的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    @Override
    public void startOrStop(Integer state, Long id) {
        // 如果是起售，要看菜品这个套餐里面有没有停售的菜品，有的话就不让起售
        if (state == StatusConstant.ENABLE) {
            // 根据套餐id查询出套餐里面所有的菜品（需要链接菜品表查询，菜品表里面有status）
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(state);
        setmealMapper.update(setmeal);
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }
}
