package com.maker.spring;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) //注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
/**
 * @Target 注解表示使用的作用域范围，也就说这个注解可以放在哪些地方
 * ElementType.TYPE            ： 接口、类、枚举
 * ElementType.FIELD           ： 字段、枚举的常量
 * ElementType.METHOD          ： 方法
 * ElementType.PARAMETER       ： 方法参数
 * ElementType.CONSTRUCTOR     ： 构造函数
 * ElementType.LOCAL_VARIABLE  ： 局部变量
 * ElementType.ANNOTATION_TYPE ： 注解
 * ElementType.PACKAGE         ： 包
 */

@Target(ElementType.TYPE)
public @interface ComponentScan {

    String value() default "";
}
