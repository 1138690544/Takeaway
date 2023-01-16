package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.Employee;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.mapper.CategoryMapper;
import com.it.reggie.mapper.EmployeeMapper;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishService;
import com.it.reggie.service.EmployeeService;
import com.it.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl  extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    //根据id删除分类,删除前需要进行判断
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(queryWrapper);
        //查询当前分类是否关联了菜品,如果已经关联则抛出异常
        if(count>0){
            //抛出异常
            throw new CustomException("当前分类项关联了菜品,不能删除");
        }

        LambdaQueryWrapper<Setmeal> queryWrapper1 =new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper1.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(queryWrapper1);
        //查询当前分类是否关联了套餐,如果已经关联则抛出异常
        if(count1>0){
            //抛出异常
            throw new CustomException("当前分类项关联了套餐 ,不能删除");
        }

        //正常删除分类
        super.removeById(id);
    }
}
