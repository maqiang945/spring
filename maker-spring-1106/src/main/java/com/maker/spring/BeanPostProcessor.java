package com.maker.spring;

/**
 * @Description:Bean后置处理器,Spring容器在初始化bean的时候，会回调BeanPostProcessor中的两个方法
 * @Author: Maker
 * @Date: 2020/11/5 14:39
 */
public interface BeanPostProcessor {

    /**
     * 在每一个bean对象的初始化方法调用之前回调
     */
    void postProcessBeforeInitialization(String beanName, Object bean);

    /**
     * 会在每个bean对象的初始化方法调用之后被回调
     */
    void postProcessAfterInitialization(String beanName, Object bean);
}
