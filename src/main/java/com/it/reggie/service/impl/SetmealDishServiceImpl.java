package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.entity.SetmealDish;
import com.it.reggie.mapper.SetmealDishMapper;
import com.it.reggie.mapper.SetmealMapper;
import com.it.reggie.service.SetmealDishService;
import com.it.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl  extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
