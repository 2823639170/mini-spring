package com.spring;

import com.spring.Interface.BeanNameAware;
import com.spring.Interface.BeanPostProcessor;
import com.spring.Interface.InitializingBean;
import com.spring.annotation.*;
import com.spring.aop.anno.Around;
import com.spring.aop.anno.Aspect;
import com.spring.aop.model.ProceedingJoinPoint;
import com.spring.aop.proxy.CglibProxy;
import com.spring.aop.proxy.JdkProxy;
import com.spring.contant.ScopeType;
import com.spring.model.BeanDefinition;
import jdk.nashorn.internal.ir.JoinPredecessor;


import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
public class GjySpringApplicationContext {

    private final Class<?> appConfig;
    private final String springConfig = "spring.xml";

    /**
     * 保存beanDefinition信息,key为beanName
     */
    private Map<String, BeanDefinition> beanNameDefinitionMap = new ConcurrentHashMap<>();
    /**
     * 保存beanDefinition信息,,key为class对象
     */
    private Map<Class, BeanDefinition> classDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 单例池
     */
    private Map<String, Object> singletonObjectMap = new ConcurrentHashMap<>();
    /**
     * 接口和实现类的映射单例池
     */
    private Map<Class, ArrayList<Class>> interfaceImplMap = new ConcurrentHashMap<>();
    /**
     * Class对象和实例对象的映射
     */
    private Map<Class, Object> classObjectMap = new ConcurrentHashMap<>();
    /**
     * Class对象和实例对象的映射
     */
    private Set<Class<?>> aopClassSet = new CopyOnWriteArraySet<>();
    /**
     * 代理对象和未被代理实例对象的映射，用于依赖注入
     */
    private Map<Object, Object> proxyObjectMap = new ConcurrentHashMap<>();
    /**
     * 用来保存所有实现了BeanPostProcessor的对象，在beanMap中也会再保存一份
     */
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    /**
     * 用来保存控制层对象
     */
    private Map<Class, Object> controllerMap = new ConcurrentHashMap<>();

    public GjySpringApplicationContext(Class<?> appConfig) {
        this.appConfig = appConfig;
        // 注册BeanDefinition
        registerBeanDefinition();
        // 注册BeanPostProcess
        registerBeanPostProcess();
        // 注册单例bean
        registerSingletonBean();
        // aop代理
        doAop();
        // 依赖注入
        doDI();
        // 获取controller集合
        registerController();
    }

    /**
     * 存放所有controller对象
     */
    private void registerController() {
        for (Map.Entry<Class, Object> entry : controllerMap.entrySet()) {
            Class clazz = entry.getKey();
            controllerMap.put(clazz, getBean(clazz));
        }
    }

    private void doAop() {

        for (Class aopClass : aopClassSet) {
            for (Method aopMethod : aopClass.getDeclaredMethods()) {
                if (aopMethod.isAnnotationPresent(Around.class)) {
                    Around aroundAnnotation = aopMethod.getDeclaredAnnotation(Around.class);
                    // 获取注解上的值
                    String execution = aroundAnnotation.execution();
                    // 要代理的目标类全名称： com.bruceliu.service.impl.OrderServiceImpl
                    String fullClass = execution.substring(0, execution.lastIndexOf("."));
                    // 要被代理的方法名字
                    String methodName = execution.substring(execution.lastIndexOf(".") + 1);
                    try {
                        // 获取目标类的class对象
                        Class targetClass = Class.forName(fullClass);
                        // 获取目标方法
                        Method targetMethod = targetClass.getDeclaredMethod(methodName);
                        // 获取目标类的实例对象，用于反射调用
                        Object targetBean = getBean(targetClass);
                        // 获取aop类的实例对象，用于反射调用
                        Object aopBean = getBean(aopClass);
                        JdkProxy jdkProxy = new JdkProxy(targetClass, targetBean, targetMethod, aopBean, aopMethod);
                        Object proxy = jdkProxy.getProxy();
                        BeanDefinition beanDefinition = classDefinitionMap.get(targetClass);
                        proxyObjectMap.put(proxy, classObjectMap.get(targetClass));
                        singletonObjectMap.put(beanDefinition.getBeanName(), proxy);
                        classObjectMap.put(targetClass, proxy);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                }
            }

        }


    }

    /**
     * 依赖注入
     */
    private void doDI() {
        for (Map.Entry<Object, Object> entry : proxyObjectMap.entrySet()) {
            Object instance = entry.getValue();
            doDIAllField(instance);
        }
        for (Map.Entry<String, Object> entry : singletonObjectMap.entrySet()) {
            Object instance = entry.getValue();
            doDIAllField(instance);
        }
    }

    private void doDIAllField(Object instance) {
        Class clz = instance.getClass();
        Field[] fields = clz.getDeclaredFields();
        // 依赖注入
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class clazz = field.getType();
                // 根据类型获取bean对象
                Object bean = getBean(clazz);
                // 根据接口类型获取bean对象
                if (bean == null) {
                    bean = getBeanByInterface(clazz);
                }
                // 根据beanName获取对象
                if (bean == null) {
                    bean = getBean(fieldName);
                }
                try {
                    field.set(instance, bean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册单例bean
     */
    private void registerSingletonBean() {
        for (Map.Entry<String, BeanDefinition> entry : beanNameDefinitionMap.entrySet()) {
//            getBean(entry.getKey());
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            Class clazz = beanDefinition.getClazz();
            // 寻找Aspect类
            if (clazz.isAnnotationPresent(Aspect.class)) {
                aopClassSet.add(clazz);
            }
            // 单例
            if (beanDefinition.isSingleton()) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjectMap.put(beanName, bean);
                classObjectMap.put(clazz, bean);
                for (Class implInterface : clazz.getInterfaces()) {
                    if (!interfaceImplMap.containsKey(implInterface)) {
                        interfaceImplMap.put(implInterface, new ArrayList<>());
                    }
                    interfaceImplMap.get(implInterface).add(clazz);
                }
            }
        }

    }

    /**
     * 注册BeanPostProcess
     */
    private void registerBeanPostProcess() {

        for (Map.Entry<String, BeanDefinition> entry : beanNameDefinitionMap.entrySet()) {
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
    private void registerBeanDefinition() {
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
                // 解析bean对象
                parseBean(className);

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
            if (loadClass.isAnnotationPresent(Component.class)
                    || loadClass.isAnnotationPresent(Controller.class)
                    || loadClass.isAnnotationPresent(Service.class)
                    || loadClass.isAnnotationPresent(Repository.class)) {
                if (loadClass.isAnnotationPresent(Controller.class)) {
                    // 先暂时保存一个数据，后面要换的
                    controllerMap.put(loadClass, this);
                }
                Component componentAnnotation = (Component) loadClass.getDeclaredAnnotation(Component.class);
                String beanName = "";
                if (loadClass.isAnnotationPresent(Component.class)) {
                    beanName = ((Component) loadClass.getDeclaredAnnotation(Component.class)).value();
                } else if (loadClass.isAnnotationPresent(Controller.class)) {
                    beanName = ((Controller) loadClass.getDeclaredAnnotation(Controller.class)).value();
                } else if (loadClass.isAnnotationPresent(Service.class)) {
                    beanName = ((Service) loadClass.getDeclaredAnnotation(Service.class)).value();
                } else {
                    beanName = ((Repository) loadClass.getDeclaredAnnotation(Repository.class)).value();
                }
                if ("".equals(beanName)) {
                    beanName = Introspector.decapitalize(loadClass.getSimpleName());
                }

                ScopeType scopeType = ScopeType.SINGLETON;
                if (loadClass.isAnnotationPresent(Scope.class)) {
                    scopeType = ((Scope) loadClass.getDeclaredAnnotation(Scope.class)).value();
                }
                BeanDefinition beanDefinition = new BeanDefinition(beanName, loadClass, scopeType);
                beanNameDefinitionMap.put(beanName, beanDefinition);
                classDefinitionMap.put(loadClass, beanDefinition);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

//    /**
//     * 获取bean对象
//     * 1. 调用这个方法是，可能还没有被加入map中，一开始调用时和依赖注入时
//     *
//     * @param beanName beanName
//     * @return
//     */
//    public Object getBean(String beanName) {
//        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
//        if (beanDefinition == null) {
//            throw new NullPointerException();
//        } else {
//            // 单例
//            if (beanDefinition.isSingleton()) {
//                if (!singletonObjectMap.containsKey(beanName)) {
//                    singletonObjectMap.put(beanName, createBean(beanName, beanDefinition));
//                }
//                return singletonObjectMap.get(beanName);
//            } else { // 多例
//                return createBean(beanName, beanDefinition);
//            }
//
//        }
//    }

    /**
     * 获取bean对象
     * 1. 调用这个方法是，可能还没有被加入map中，一开始调用时和依赖注入时
     *
     * @param beanName beanName
     * @return
     */
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanNameDefinitionMap.get(beanName);
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
     * 获取bean对象
     *
     * @param clazz
     * @return
     */
    public Object getBean(Class clazz) {
        BeanDefinition beanDefinition = classDefinitionMap.get(clazz);
        if (beanDefinition == null) {
            System.out.println(clazz + "不存在！");
            return null;
        } else {
            // 单例
            if (beanDefinition.isSingleton()) {
                if (!classObjectMap.containsKey(clazz)) {
                    classObjectMap.put(clazz, createBean(beanDefinition.getBeanName(), beanDefinition));
                }
                return classObjectMap.get(clazz);
            } else { // 多例
                return createBean(beanDefinition.getBeanName(), beanDefinition);
            }

        }
    }

    /**
     * 获取bean对象
     *
     * @param clazz
     * @return
     */
    public Object getBeanByInterface(Class clazz) {
        ArrayList<Class> classList = interfaceImplMap.get(clazz);
        if (classList == null) {
            throw new NullPointerException();
        } else {
            if (classList.size() > 1) {
                throw new RuntimeException("接口实现类数量不合法，大于1");
            } else {
                return getBean(classList.get(0));
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

            // 设置beamName
            if (instance instanceof BeanNameAware) {
                BeanNameAware beanNameAware = (BeanNameAware) instance;
                beanNameAware.setBeamName(beanDefinition.getBeanName());
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

            // 判断一下是否需要事务代理
            Method[] methods = clz.getDeclaredMethods();
            HashSet<Method> set = new HashSet<>();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Transactional.class)) {
                    set.add(method);
                }
            }
            if (!set.isEmpty()) {
                CglibProxy cglibProxy = new CglibProxy(clz, instance, set);
                Object proxyInstance = cglibProxy.getProxyInstance();
                proxyObjectMap.put(proxyInstance, instance);
                instance = proxyInstance;
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Map<Class, Object> getControllerMap() {
        return this.controllerMap;
    }
}
