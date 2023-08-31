package com.sky.service.impl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断加入购物车的商品是否存在
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //如果存在数量加1即可
        if(list !=null && list.size()>0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            //不存在执行插入操作
            //判断本次添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCart.getSetmealId();
            if(dishId != null){
                //本次添加的信息是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                //本次添加的是套餐
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前微信用户的id
        Long userId=BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void subOne(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        shoppingCart=list.get(0);
        if(shoppingCart.getNumber()==1){
            //如果商品的数量为1，则直接从购物车中删除该商品
            shoppingCartMapper.deleteById(shoppingCart.getId());
        }else {
            //否则重新设置number
            shoppingCart.setNumber(shoppingCart.getNumber()-1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
