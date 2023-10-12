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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
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


    public GjySpringApplicationContext(Class<?> appConfig) {
        this.appConfig = appConfig;
        init();
        initBeanPostProcess();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            singletonObjectMap.put(entry.getKey(), createBean(entry.getKey(), entry.getValue()));
        }
    }

    private void initBeanPostProcess() {

//        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
//            Class aClass = entry.getClass().getClass();
//            if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
//                BeanDefinition definition = entry.getValue();
//                getBean(entry.getKey())
//            }
//
//
//        }

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
     * @param file
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
     *
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            // 单例
            if (beanDefinition.isSingleton()) {
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
            // 调用afterPropertySet方法
            if (instance instanceof InitializingBean) {
                InitializingBean initializingBean = (InitializingBean) instance;
                initializingBean.afterPropertySet();
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
