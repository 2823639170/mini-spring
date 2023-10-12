package com.spring;

import com.spring.Interface.BeanNameAware;
import com.spring.Interface.BeanPostProcessor;
import com.spring.Interface.InitializingBean;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.annotation.ComponentScan;
import com.spring.annotation.Scope;
import com.spring.contant.ScopeType;
import com.spring.model.BeanDefinition;


import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
public class GjySpringApplicationContext {

    private final Class<?> appConfig;

    /**
     * 保存beanDefinition信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 单例池
     */
    private Map<String, Object> singletonObjectMap = new ConcurrentHashMap<>();

    /**
     * 用来保存所有实现了BeanPostProcessor的对象，在beanMap中也会再保存一份
     */
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public GjySpringApplicationContext(Class<?> appConfig) {
        this.appConfig = appConfig;
        init();
        // 注册BeanPostProcess
        registerBeanPostProcess();
        // 注册单例bean
        registerSingletonBean();

    }

    /**
     * 注册单例bean
     */
    private void registerSingletonBean() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            getBean(entry.getKey());
        }
    }

    /**
     * 注册BeanPostProcess
     */
    private void registerBeanPostProcess() {

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class aClass = entry.getValue().getClazz();
            if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                Object bean = getBean(entry.getKey());
                beanPostProcessorList.add((BeanPostProcessor) bean);
                singletonObjectMap.put(entry.getKey(), bean);
            }
        }

    }

    /**
     * 解析目录
     */
    private void init() {
        ComponentScan componentScan = appConfig.getDeclaredAnnotation(ComponentScan.class);
        if (componentScan == null) {
            return;
        }
        String path = componentScan.value();
        path = path.replace('.', '/');
        System.out.println(path);
        // 获得当前线程类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 加载当前项目先对路径先的path路径
        // 当前项目路径就是target/class路径
        URL resource = classLoader.getResource(path);
        // file:/C:/Users/EVA/Desktop/java-workplace/mini-spring/target/classes/com/gjy/service
        System.out.println(resource);
        // 通过url对象，加载需要扫描的文件夹
        File file = new File(resource.getFile());
        // 变量file下的全部文件
        scanFile(file);
    }

    /**
     * 扫描目录，解析类上面的注解，并生成definition对象
     *
     * @param file 文件对象
     */
    private void scanFile(File file) {
        if (!file.isDirectory()) {
            System.out.println(file);
            return;
        }
        File[] listFile = file.listFiles();
        for (File chileFile : listFile) {
            if (chileFile.isDirectory()) {
                scanFile(chileFile);
            } else {
                System.out.println(chileFile);
                String absPathName = chileFile.toString();
                String className = absPathName.substring(absPathName.indexOf("com"), absPathName.indexOf(".class"))
                        .replace('\\', '.');
                System.out.println(className);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try {

                    Class loadClass = classLoader.loadClass(className);
                    if (loadClass.isAnnotationPresent(Component.class)) {
                        Component componentAnnotation = (Component) loadClass.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.value();
                        if ("".equals(beanName)) {
                            beanName = Introspector.decapitalize(loadClass.getSimpleName());
                        }
                        // 判断是不是单例
                        ScopeType scopeType = ScopeType.SINGLETON;
                        if (loadClass.isAnnotationPresent(Scope.class)) {
                            scopeType = ((Scope) loadClass.getDeclaredAnnotation(Scope.class)).value();
                        }
                        BeanDefinition beanDefinition = new BeanDefinition(loadClass, scopeType);
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 解析bean对象
     *
     * @param className
     */
    private void parseBean(String className) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {

            Class loadClass = classLoader.loadClass(className);
            if (loadClass.isAnnotationPresent(Component.class)) {
                Component componentAnnotation = (Component) loadClass.getDeclaredAnnotation(Component.class);
                String beanName = componentAnnotation.value();
                if ("".equals(beanName)) {
                    beanName = Introspector.decapitalize(loadClass.getSimpleName());
                }

                ScopeType scopeType = ScopeType.SINGLETON;
                if (loadClass.isAnnotationPresent(Scope.class)) {
                    scopeType = ((Scope) loadClass.getDeclaredAnnotation(Scope.class)).value();
                }
                BeanDefinition beanDefinition = new BeanDefinition(loadClass, scopeType);
                beanDefinitionMap.put(beanName, beanDefinition);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取bean对象
     * 1. 调用这个方法是，可能还没有被加入map中，一开始调用时和依赖注入时
     *
     * @param beanName beanName
     * @return
     */
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            // 单例
            if (beanDefinition.isSingleton()) {
                if (!singletonObjectMap.containsKey(beanName)) {
                    singletonObjectMap.put(beanName, createBean(beanName, beanDefinition));
                }
                return singletonObjectMap.get(beanName);
            } else { // 多例
                return createBean(beanName, beanDefinition);
            }

        }


    }

    /**
     * 创建bean对象
     *
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {

        try {
            Object instance = beanDefinition.getClazz().newInstance();
            Class<?> clz = instance.getClass();
            Field[] fields = clz.getDeclaredFields();
            // 依赖注入
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object bean = getBean(fieldName);
                    field.set(instance, bean);
                }
            }
            // 设置beamName
            if (instance instanceof BeanNameAware) {
                BeanNameAware beanNameAware = (BeanNameAware) instance;
                beanNameAware.setBeamName(beanName);
            }
            // 如果没有实现BeanPostProcessor，则需要在创建前调用BeanPostProcessor的before方法
            if (!(instance instanceof BeanPostProcessor)) {
                for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                    instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
                }
            }

            // 调用InitializingBean的afterPropertySet方法
            if (instance instanceof InitializingBean) {
                InitializingBean initializingBean = (InitializingBean) instance;
                initializingBean.afterPropertySet();
            }
            // 如果没有实现BeanPostProcessor，则需要在创建后调用BeanPostProcessor的after方法
            if (!(instance instanceof BeanPostProcessor)) {
                for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                    instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
                }
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


}
