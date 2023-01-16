package com.it.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.DishFlavor;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/dish")
@RestController
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;
    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        //清理某个分类菜品下的缓存数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    //分页 看不懂看p62
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //根据id查询菜品信息和对应口味信息,以便回显
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto disDto = dishService.getByIdWithFlavor(id);
        return R.success(disDto);
    }

    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.UpdateWithFlavor(dishDto);

        //清理某个分类菜品下的缓存数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("更新菜品成功");
    }

    //删除与批量删除菜品
    @DeleteMapping
    public R<String> delete(Long[] ids){

        List<Long> idList = Arrays.asList(ids);
        for (int i = 0; i < ids.length; i++) {
            // 得到每个dish对象
            Long id = ids[i];
            dishService.DeleteWithFlavor(id);
        }

        //清理所有的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);


        return R.success("删除菜品成功");
    }

    //停售与批量停售菜品
    @PostMapping("/status/0")
    public R<String> status0(Long[] ids){

        List<Long> idList = Arrays.asList(ids);
        for (int i = 0; i < ids.length; i++) {
            // 得到每个dish对象
            Long id = ids[i];
            dishService.Status0(id);
        }

        //清理所有的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("修改状态成功");
    }

    //启售与批量启首菜品
    @PostMapping("/status/1")
    public R<String> status1(Long[] ids){
        List<Long> idList = Arrays.asList(ids);
        for (int i = 0; i < ids.length; i++) {
            // 得到每个dish对象
            Long id = ids[i];
            dishService.Status1(id);
        }

        //清理所有的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("修改状态成功");
    }

/*    //根据条件查询对应的菜品数据
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //构造查询条件对象
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        //查询启售状态的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/

    //根据条件查询对应的菜品数据
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList =null;

        //动态构造分类id
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();

        //先从Redis中获取缓存数据
        dishDtoList =(List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList!=null){
            //如果存在,直接返回,无需查询数据库
            return R.success(dishDtoList);
        }

        //构造查询条件对象
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        //查询启售状态的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        //口味追加到菜品里
         dishDtoList = list.stream().map((item) -> {

            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);

            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在,需要查询数据库,将查询的菜品放到Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
