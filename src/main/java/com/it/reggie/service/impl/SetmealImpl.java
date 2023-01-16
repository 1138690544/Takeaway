package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.DishFlavor;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.entity.SetmealDish;
import com.it.reggie.mapper.SetmealMapper;
import com.it.reggie.service.SetmealDishService;
import com.it.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;


    //新增套餐,同时需要保存菜品和套餐的关联
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息,操作setmeal,执行insert操作
        this.save(setmealDto);

        //取出套餐中菜品的id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

         //保存套餐和菜品的关联信息,操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void DeleteWithDish(Long id) {
       Setmeal setmeal = this.getById(id);

        //查询套餐id,从setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());

        setmealDishService.remove(queryWrapper);
        this.removeById(id);
    }

    //根据id停售菜品
    @Override
    public void Status0(Long id) {
        Setmeal setmeal =this.getById(id);
        setmeal.setStatus(0);
        this.updateById(setmeal);
    }
    //根据id启售菜品
    @Override
    public void Status1(Long id) {
        Setmeal setmeal =this.getById(id);
        setmeal.setStatus(1);
        this.updateById(setmeal);
    }
    //回显
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal=this.getById(id);

        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }
    //修改
    @Override
    public void UpdateWithFlavor(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes=setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }
}
