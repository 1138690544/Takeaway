package com.it.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.dto.DishDto;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.entity.SetmealDish;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishService;
import com.it.reggie.service.SetmealDishService;
import com.it.reggie.service.SetmealService;
import com.it.reggie.service.impl.SetmealImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    //新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    //分页 看不懂看p77
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageInfo =new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage =new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝 因为SetmealDto中比Setmeal多有菜品分类,将数据给Dto在页面上显示
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal>records=pageInfo.getRecords();

        List<SetmealDto> list= records.stream().map((item)->{
            SetmealDto setmealDto =new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //查询分类对象
            Category category = categoryService.getById(categoryId);
            //分类名称
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    //删除与批量删除
    @DeleteMapping
    public R<String> delete(Long[] ids){
        List<Long> idList = Arrays.asList(ids);
        for (int i = 0; i < ids.length; i++) {
            // 得到每个dish对象
            Long id = ids[i];
            setmealService.DeleteWithDish(id);
        }
        return R.success("删除套餐成功");
    }

    //停售与批量停售菜品
    @PostMapping("/status/0")
    public R<String> status0(Long[] ids){
        List<Long> idList = Arrays.asList(ids);
        for (int i = 0; i < ids.length; i++) {
            // 得到每个dish对象
            Long id = ids[i];
            setmealService.Status0(id);
        }
        return R.success("修改状态成功");
    }

    //启售与批量启首菜品
    @PostMapping("/status/1")
    public R<String> status1(Long[] ids){
        List<Long> idList = Arrays.asList(ids);
        for (int i = 0; i < ids.length; i++) {
            // 得到每个dish对象
            Long id = ids[i];
            setmealService.Status1(id);
        }
        return R.success("修改状态成功");
    }

    //根据id查询套餐信息和菜品信息,以便回显
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    //修改套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.UpdateWithFlavor(setmealDto);
        return R.success("更新套餐成功");
    }

    //根据条件查询套餐数据 (用户移动端界面)
    @GetMapping("/list")
    public R<List<Setmeal>> list( Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByAsc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    //点击图片查看套餐详情
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> showSetmealDish(@PathVariable Long id) {
        //条件构造器
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //手里的数据只有setmealId
        dishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        //查询数据
        List<SetmealDish> records = setmealDishService.list(dishLambdaQueryWrapper);
        List<DishDto> dtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //copy数据
            BeanUtils.copyProperties(item,dishDto);
            //查询对应菜品id
            Long dishId = item.getDishId();
            //根据菜品id获取具体菜品数据
            Dish dish = dishService.getById(dishId);
            //其实主要数据是要那个图片，不过我们这里多copy一点也没事
            BeanUtils.copyProperties(dish,dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dtoList);
    }

}
