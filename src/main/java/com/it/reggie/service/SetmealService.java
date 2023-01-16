package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.dto.DishDto;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.entity.SetmealDish;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public void DeleteWithDish(Long id);

    //根据id停售菜品
    public void Status0(Long id);

    //根据id启售菜品
    public void Status1(Long id);

    public SetmealDto getByIdWithDish(Long id);

    public void UpdateWithFlavor (SetmealDto setmealDto);
}
