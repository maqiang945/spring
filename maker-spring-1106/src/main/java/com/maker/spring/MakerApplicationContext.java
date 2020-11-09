package com.maker.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 手写模拟Spring框架核心逻辑
 * @Author: Maker
 * @Date: 2020/11/9 16:05
 */
public class MakerApplicationContext {

    //每次创建好一个bean的定义信息，就存起来，一个文件路径下可能会有多个.class文件，key为bean的名称，value为bean的定义信息
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //就是一个list，存放bean的后置处理器
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    //单例池，存放所有的单例bean
    private ConcurrentHashMap<String, Object> singletonObjectMap = new ConcurrentHashMap<>();


    /**
     * Spring启动两大核心步骤
     * 1.扫描指定路径下的所有类（扫描的是target下的.class文件）
     * 2.2.创建实例bean（自动注入）--Spring启动的时候只会实例化非懒加载的单例bean
     *
     * @param configClass
     */
    public MakerApplicationContext(Class configClass) {
        /**
         *  扫描类，得到BeanDefinition（里面封装的bean的属性）
         *  依据@ComponentScan("com.maker.service") 注解配置的路径扫描，路径可以为多个
         */
        scan(configClass);

        /**
         *  实例化非懒加载单例bean，分五步执行
         *  1.实例化
         *  2.属性填充
         *  3.Aware回调
         *  4.初始化
         *  5.添加到单例池
         */
        instanceSingletonBean();
    }

    /**
     * 实例化非懒加载单例bean
     */
    private void instanceSingletonBean() {
        //从bean的定义map中获取
        for (String beanName : beanDefinitionMap.keySet()) {
            //判断是否为单例
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScopeValue().equals(ScopeEnum.singleton.name())) {
                //是单例，创建单例的bean，并放入单例池中
                Object bean = doCreateBean(beanName, beanDefinition);
                singletonObjectMap.put(beanName, bean);
            }
        }
    }

    /**
     * 创建bean
     */
    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        //基于bean的定义，也就是BeanDefinition创建bean
        Class beanClass = beanDefinition.getBeanClass();

        try {
            /**
             * 1.实例化bean
             */
            Object instance = beanClass.getDeclaredConstructor().newInstance();

            /**
             *  2.属性填充
             *  使用Bean的后置处理器
             */
            //获取实例bean中的所有属性
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                //判断属性中是否有@Autowired 注解注入的
                if (field.isAnnotationPresent(Autowired.class)) {
                    //Todo:此处后面在补充先通过byType寻找，在通过byName寻找
                    String fieldName = field.getName();
                    //直接通过bean的名字获得bean
                    Object bean = getBean(fieldName);
                    //如果取得的field属性使用private的，则必须设置true才能访问，否则会报错
                    field.setAccessible(true);
                    field.set(instance, bean);
                }
            }

            /**
             *  Bean后置处理器
             *  例如：UserService中有用@Autowired和@Resource注解注入的属性对象
             *       那么UserService bean实例化好之后，分别处理@Autowired和@Resource
             *       的内容
             *  Spring源码中处理@Autowired是AutowiredAnnotationBeanPostProcessor
             *                @Resource是CommonAnnotationBeanPostProcessor
             *
             */
            /*for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }*/

            /**
             *  3.Aware回调--->判断当前创建的实例bean是否实现了BeanNameAware回调接口
             *  Spring在扫描带@Component注解的类时，会给类赋值一个名称（或者@Component中配置），
             *  此时需要知道bean对应的名称是什么，所以回调获取bean的名称
             */
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            /**
             *  4.初始化，校验spring创建的bean是否创建成功
             *  执行顺序放在实例bean、属性填充、Aware回调之后
             */
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void scan(Class configClass) {
        /**
         * 1.扫描指定路径下的所有类（扫描的事target下的.class文件）
         *   转化为BeanDefinition对象，最后添加到beanDefinitionMap中
         */
        //先获取扫描的路径范围
        if (configClass.isAnnotationPresent(ComponentScan.class)) { //判断是否存在@ComponentScan注解
            //获取ComponentScan注解对象
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            //获取ComponentScan注解对象中配置的扫描路径
            String packagePath = componentScanAnnotation.value();
            System.out.println("从注解对象ComponentScan中获取的扫描路径：" + packagePath);

            //扫描路径下的所有的.class文件
            List<Class> beanClassList = getBeanClass(packagePath);
            //遍历beanClassList中的每一个bean，并且需要解析bean中属性信息，为了后面流程不重复getBeanClass，所以封装一个BeanDefinition
            //将每个bean的属性信息封装在BeanDefinition，BeanDefinition是一个bean的定义
            for (Class clazz : beanClassList) {
                //判断当前bean有没有被@Component注解标识
                if (clazz.isAnnotationPresent(Component.class)) {
                    BeanDefinition beanDefinition = new BeanDefinition();
                    beanDefinition.setBeanClass(clazz);
                    //获取注解@Component中标识的bean的名称，例如@Component("userService")形式
                    Component componentAnnotation = (Component) clazz.getAnnotation(Component.class);
                    String beanName = componentAnnotation.value();

                    //添加Bean的后置处理逻辑，Spring在扫描时，将实现BeanPostProcessor接口全部添加到后置处理器集合中
                    /*if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                        try {
                            BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            beanPostProcessorList.add(instance);
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }*/

                    //判断是否有Scope注解,如果没有@Scope注解，Spring扫描时默认单例
                    if (clazz.isAnnotationPresent(Scope.class)) {
                        Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                        String scopeValue = scopeAnnotation.value();
                        beanDefinition.setScopeValue(scopeValue);
                    } else {
                        beanDefinition.setScopeValue(ScopeEnum.singleton.name());
                    }

                    //判断是否有@Lazy懒加载注解，如果没有@Lazy注解，Spring扫描时默认是非懒加载
                    if (clazz.isAnnotationPresent(Lazy.class)) {
                        beanDefinition.setLazy(true);
                    }

                    //将所有的bean封装
                    beanDefinitionMap.put(beanName, beanDefinition);
                }
            }
        }
    }

    /**
     * 从指定的路径中获取bean
     * 此类为自己单独模拟获取的，写的比较简单，方便理解
     *
     * @param packagePath
     * @return
     */
    private List<Class> getBeanClass(String packagePath) {
        //封装的list集合
        List<Class> beanClassList = new ArrayList<>();
        //获取一个类加载器
        ClassLoader classLoader = MakerApplicationContext.class.getClassLoader();
        //通过类加载器获取一个资源（此时是一个文件夹），例如：file:/E:/code/maker/study/maker-spring/target/classes/com/maker/service
        URL resource = classLoader.getResource(packagePath.replace(".", "/"));
        System.out.println("类加载器加载的资源路径：" + resource);
        File file = new File(resource.getFile());
        //判断当前文件是否为一个文件夹
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //获取.class文件名称
                String fileName = f.getAbsolutePath();
                //由于此文件夹下可能存在其他非.class类型的文件，所以需要判断
                if (fileName.endsWith(".class")) {
                    //获取.class文件的对应的类名
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    //将路径中的"\\"替换为"."
                    className = className.replace("\\", ".");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        beanClassList.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return beanClassList;
    }

    /**
     * 获取Bean
     */
    public Object getBean(String beanName) {
        //创建bean之前，判断一下是否为单例，
        //如果为单例，直接看单例池中是否有此实例bean，如果有直接取出，如果没有新创建一个单例bean，并且放入单例池中
        if (singletonObjectMap.containsKey(beanName)) {
            return singletonObjectMap.get(beanName);
        } else {
            //属性bean在单例池中不存在，再去beanDefinitionMap中查询是否存在
            return doCreateBean(beanName, beanDefinitionMap.get(beanName));
        }
    }
}
