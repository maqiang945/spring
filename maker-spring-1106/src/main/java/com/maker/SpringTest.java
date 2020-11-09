package com.maker;

import com.maker.spring.MakerApplicationContext;

/**
 * @Description: Spring 模拟启动
 * @Author: Maker
 * @Date: 2020/11/9 16:04
 */
public class SpringTest {

    public static void main(String[] args) {
        MakerApplicationContext applicationContext = new MakerApplicationContext(AppConfig.class);

        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));

    }
}
