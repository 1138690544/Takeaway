package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.DishFlavor;
import com.it.reggie.mapper.CategoryMapper;
import com.it.reggie.mapper.DishMapper;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
   //新增菜品,同时保存对应的口味数据
    @Transactional//事务
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //因为如果将dishDto直接保存到菜品口味表,匹配不到id
        //所以取出dishDto中的菜品id后,通过循环存到DishFlavor中的菜品id
        Long dishId=dishDto.getId();//菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    //根据id查询菜品信息和对应口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息,从dish表
        Dish dish = this.getById(id);

        //查询菜品对应口味信息,从dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors =dishFlavorService.list(queryWrapper);

        //将查出的dish中信息拷贝到dishDto中
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //将dishDto中再加入flavors信息
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Transactional
    @Override
    public void UpdateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据 dish_flavor的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper =new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据 dish_flavor的update操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        //获取菜品id放到flavors中
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    //删除菜品信息和口味
    @Override
    public void DeleteWithFlavor(Long id) {
        Dish dish = this.getById(id);

        //查询菜品id,从dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        dishFlavorService.remove(queryWrapper);
        this.removeById(id);

    }
    //根据id停售菜品
    @Override
    public void Status0(Long id) {
        Dish dish =this.getById(id);
        dish.setStatus(0);
        this.updateById(dish);
    }
    //根据id启售菜品
    @Override
    public void Status1(Long id) {
        Dish dish =this.getById(id);
        dish.setStatus(1);
        this.updateById(dish);
    }
}
