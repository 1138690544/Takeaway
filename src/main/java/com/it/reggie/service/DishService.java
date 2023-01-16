package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品,同时插入菜品对应的口味数据,需要操作两张表,dish和dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息和口味信息
    public void UpdateWithFlavor(DishDto dishDto);

    //根据id删除菜品和口味
    public void DeleteWithFlavor(Long id);

    //根据id停售菜品
    public void Status0(Long id);

    //根据id启售菜品
    public void Status1(Long id);

}
