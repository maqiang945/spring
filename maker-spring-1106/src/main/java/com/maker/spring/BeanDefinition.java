package com.maker.spring;

/**
 * @Description: bean的相关定义，就是将当前Bean的信息封装在一起
 * @Author: Maker
 * @Date: 2020/11/9 17:10
 */
public class BeanDefinition {

    /**
     * bean的类型
     */
    private Class beanClass;

    /**
     * 是否懒加载
     */
    private boolean isLazy;

    /**
     * 单例，原型
     */
    private String scopeValue;

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public boolean isLazy() {
        return isLazy;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }
}
