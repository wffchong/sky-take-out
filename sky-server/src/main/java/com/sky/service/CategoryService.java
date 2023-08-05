package com.sky.service;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    List<Category> list(Integer type);

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
}
