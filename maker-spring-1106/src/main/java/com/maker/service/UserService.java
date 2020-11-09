package com.maker.service;

import com.maker.spring.Autowired;
import com.maker.spring.Component;
import com.maker.spring.Lazy;

/**
 * @Description:
 * @Author: Maker
 * @Date: 2020/11/9 16:16
 */
@Component("userService")  //@Component 添加了这个注解，Spring在启动时，实例bean的时候默认为单例
//@Scope("prototype")  //原型bean，此注解不加时，Spring 默认为singleton单例
@Lazy
public class UserService {

    private String name;

    @Autowired
    private OrderService orderService;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
