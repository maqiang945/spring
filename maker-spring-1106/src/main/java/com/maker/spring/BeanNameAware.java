package com.maker.spring;

/**
 * 用于实例化后的回调
 */
public interface BeanNameAware {
    public void setBeanName(String name);
}
