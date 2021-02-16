package com.lagou.edu.context.factory;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.lagou.edu.annotation.component.Bean;
import com.lagou.edu.annotation.component.Component;
import com.lagou.edu.annotation.component.Configuration;
import com.lagou.edu.annotation.context.ComponentScan;
import com.lagou.edu.annotation.context.PropValue;
import com.lagou.edu.annotation.context.PropertySource;
import com.lagou.edu.annotation.dependency.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 应癫
 * 此处定义工厂中存储 bean 对象的单例池，以及初始化解析的方法
 * <p>
 * 工厂类，生产对象（使用反射技术）
 */
public abstract class AbstractBeanFactory implements BeanFactory {

    private static final String PROPERTIES_KEY = "_properties_";
    /**
     * 模拟容器启动完成之后的使用的单例池
     * 此处作业工程示例考虑循环依赖的问题
     */
    protected final ConcurrentHashMap<String, Object> singletonBeanPool = new ConcurrentHashMap<>(16);
    private final Set<Class<?>> classesAutowiring;


    public AbstractBeanFactory(Class<?> configClass) throws Exception {
        this.classesAutowiring = new HashSet<>(16);
        //此处为初始化 IOC 容器（Bean 工厂的操作）
        initWebApplicationContext(configClass);
    }

    @Override
    public boolean containsBean(String beanName) {
        return this.getBean0(beanName) != null;
    }

    @Override
    public <T> T getBean0(String beanName) {
        //noinspection unchecked
        return (T) singletonBeanPool.get(beanName);
    }

    @Override
    public <T> T getBean0(Class<T> clazz) {
        for (Object value : singletonBeanPool.values()) {
            if (clazz.isAssignableFrom(value.getClass())) {
                //noinspection unchecked
                return (T) value;
            }
        }
        return null;
    }


    //================== 初始化 ==================

    /**
     * 初始化
     */
    private void initWebApplicationContext(Class<?> configClass) throws Exception {
        //注入配置文件中配置的一些值
        autowiredProp(configClass);

        //注入配置了 @Configuration 和 @Component 注解的对象类
        autowiredConfigurationAndComponentBeans(configClass);
    }

    /**
     * 注入配置文件配置的一些值
     */
    private void autowiredProp(Class<?> configClass) throws Exception {
        //获取配置需要引入的配置文件
        String[] dependProperties = getConfigProperties(configClass);
        Properties properties = getProperties(dependProperties);
        //并且把属性注入到工厂中待用，因为是配置属性，此处就简略的直接放入正式缓存池中
        this.addSingletonBean(PROPERTIES_KEY, properties);
    }

    /**
     * 往单例 bean 工厂添加一个单例对象
     *
     * @param name
     * @param bean
     */
    private void addSingletonBean(String name, Object bean) throws Exception {
        synchronized (singletonBeanPool) {
            //如果存在，所以此处我就不用 putIfAbsent需要抛出异常
//            this.singletonBeanPool.putIfAbsent(name, bean);

            if (singletonBeanPool.get(name) != null) {
                throw new Exception("单例池中已存在 bean：" + name);
            } else {
                singletonBeanPool.put(name, bean);
            }
        }
    }

    /**
     * 注入配置了 @Component 注解的 Bean
     */
    private void autowiredConfigurationAndComponentBeans(Class<?> configClass) throws Exception {
        //获取程序配置的所需要扫描的包
        String[] scanPackageNames = getScanPackageNames(configClass);
        //扫描包下所有class，过滤掉不需要注入的 bean并返回 class 数组对象
        Set<Class<?>> classes = scanReturnClass(scanPackageNames);

        // @Configuration 注解的配置类
        Set<Class<?>> configurationClasses = classes.stream().filter(loopClass -> loopClass.isAnnotationPresent(Configuration.class)).collect(Collectors.toSet());
        this.classesAutowiring.addAll(configurationClasses);
        // @Component 注解的类
        Set<Class<?>> componentClasses = classes.stream().filter(loopClass -> loopClass.isAnnotationPresent(Component.class)).collect(Collectors.toSet());
        this.classesAutowiring.addAll(componentClasses);

        //注入 bean 到单例池中（填充属性后的 & 代理后的 bean）
        autowiredConfigurations(configurationClasses);

        //读取解析并注入到单例池中
        autowiredBeans(componentClasses);
    }

    //=======   用于autowiredConfigurationAndComponentBeans 方法内部 ===========

    private void autowiredConfigurations(Set<Class<?>> configClasses) throws Exception {
        for (Class<?> configClass : configClasses) {
            //获取 配置类的实例，无参构造反射，此时暂未注入属性的值
            Object instance = configClass.getDeclaredConstructor().newInstance();

            //获取配置类定义的所有属性，循环注入属性
            Field[] declaredFields = configClass.getDeclaredFields();
            autowiredInnerFields(instance, declaredFields);

            //配置类中可能还存在 @Bean 的配置，出处也要把这些注入到单例池中
            //获取类中所有方法，并开始注入/代理注入
            Method[] declaredMethods = configClass.getDeclaredMethods();
            autowiredInnerBeans(instance, declaredMethods);

            //所有的属性、方法都反射注入/代理完成，那么就把 bean 放入单例池中
            Component component = configClass.getAnnotation(Component.class);
            String beanName = component == null || StrUtil.isEmpty(component.value()) ?
                    StrUtil.lowerFirst(configClass.getSimpleName()) :
                    StrUtil.lowerFirst(component.value());
            this.addSingletonBean(beanName, instance);
        }
    }

    private void autowiredBeans(Set<Class<?>> componentClasses) throws Exception {
        for (Class<?> componentClass : componentClasses) {
            //获取 配置类的实例，无参构造反射，此时暂未注入属性的值
            Object instance = componentClass.getDeclaredConstructor().newInstance();

            //所有的属性、方法都反射注入/代理完成，那么就把 bean 放入单例池中
            Component component = componentClass.getAnnotation(Component.class);
            String beanName = component == null || StrUtil.isEmpty(component.value()) ?
                    StrUtil.lowerFirst(componentClass.getSimpleName()) :
                    StrUtil.lowerFirst(component.value());
            //如果单例池中已有，那么取出来把这个对象注入一些属性和代理增强
            if (!this.containsBean(beanName)) {
                this.addSingletonBean(beanName, instance);
            }
            instance = this.getBean0(beanName);
            //获取配置类定义的所有属性，循环注入属性
            Field[] declaredFields = componentClass.getDeclaredFields();
            autowiredInnerFields(instance, declaredFields);
        }
    }

    //=======   用于autowiredConfigurationAndComponentBeans 方法内部 ===========

    //=======   autowiredConfigurations  autowiredBeans 方法内部 ===========

    /**
     * 注入属性的值或对象值
     *
     * @param instance
     * @param declaredFields
     * @throws IllegalAccessException
     */
    private void autowiredInnerFields(Object instance, Field[] declaredFields) throws Exception {
        for (Field field : declaredFields) {
            //如果是配置的属性注入，那么从属性池中拿值注入（因为在这一步之前，配置属性已经提前注入到单例池中了，直接拿就行）
            if (field.isAnnotationPresent(PropValue.class)) {
                field.setAccessible(true);
                Properties propertiesBean = this.getBean0(PROPERTIES_KEY);
                PropValue propValue = field.getAnnotation(PropValue.class);
                String propKey = StrUtil.isEmpty(propValue.value()) ? field.getName() : StrUtil.lowerFirst(propValue.value());
                String fieldValue = propertiesBean.getProperty(propKey);
                //注入值，此处注入的都是配置文件的中的值，非依赖注入
                field.set(instance, fieldValue);
            }
            //如果是依赖注入，那么从单例池中拿，同时注入到此对象中，之后把当前对象注入到单例池中
            else if (field.isAnnotationPresent(Autowired.class)) {
                //拿到此属性的类型，
                Class<?> fieldType = field.getType();
                //如果不是基本类型，那么就要去创建这个 bean 到单例池中或者去单例池中找已存在的
                if (!fieldType.isPrimitive()) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String beanName = StrUtil.isEmpty(autowired.value()) ? StrUtil.lowerFirst(field.getName()) : autowired.value();
                    //如果单例池中没有，那么就先反射实例化此对象注入，然后再拿来注入到当前的对象中
                    if (!this.containsBean(beanName)) {
                        Class<?> classAutowired = getClassByBeanName(beanName);
                        autowiredBeans(Collections.singleton(classAutowired));
                    }
                    //继续
                    //1、如果上一步if 中单例池中没有，那么现在单例池中应该注册好了可以拿来注入到当前类中了
                    // 2、如果单例池中有，上一步 if 直接略过了，这里直接注入到此属性就好了
                    Object objectBean = this.getBean0(beanName);
                    field.setAccessible(true);
                    field.set(instance, objectBean);
                }
            }
        }
    }

    /**
     * 注入 带有 @Bean 注解的方法
     *
     * @param instance
     * @param declaredMethods
     * @throws Exception
     */
    private void autowiredInnerBeans(Object instance, Method[] declaredMethods) throws Exception {
        for (Method method : declaredMethods) {
            //如果方法上有@Bean 注解则注入
            if (method.isAnnotationPresent(Bean.class)) {
                Bean beanAnnotation = method.getAnnotation(Bean.class);

                //判断是否有其他参数，如果有其他参数对象，那么需要去单例池中拿
                Parameter[] parameters = method.getParameters();

                //此处作业的示例工程我就默认此方法为无参的@Bean 注入，就不考虑依赖其他的 bean 了（如果有参数，那么需要单例池中拿，没有就要去注入此参数 bean即可）
//                for (Parameter param : parameters) {
                //此处需要 java8以上编译时候开启 -parameter 否则读取到的属性可能是 arg0 arg1 这种
//                    String name = param.getName();
                //还有一种办法就是在每个参数前面加一个注解表名，类似于 Mybatis 的 @Param
//                }

                //获取注解上的 value，如果没有定义则获取方法名（首字母小写）
                String name = StrUtil.isEmpty(beanAnnotation.value()) ? StrUtil.lowerFirst(method.getName()) : beanAnnotation.value();
                Object bean = this.getBean0(name);
                if (bean == null) {
                    //反射得到方法返回对象，作业这里我就默认为无参@Bean
                    Object methodBean = method.invoke(instance);
                    this.addSingletonBean(name, methodBean);
                }
            }
        }
    }

    //=======   autowiredConfigurations  autowiredBeans 方法内部 ===========


    // ============ 初始化方法中的公共方法集合 =================

    /**
     * 从待注入的 @Component 的所有 class 中根据 beanName 查找返回对应的 class
     *
     * @param beanName
     * @return
     */
    private Class<?> getClassByBeanName(String beanName) throws ClassNotFoundException {
        for (Class<?> clazzAutowiring : classesAutowiring) {
            String manualBeanName;
            Component component = clazzAutowiring.getAnnotation(Component.class);
            if (component != null) {
                manualBeanName = component.value();
            } else {
                Configuration configuration = clazzAutowiring.getAnnotation(Configuration.class);
                manualBeanName = configuration.value();
            }
            String name = StrUtil.isEmpty(manualBeanName) ? StrUtil.lowerFirst(clazzAutowiring.getSimpleName()) : manualBeanName;
            if (name.equals(beanName)) {
                //取类名的全限定名
                return Class.forName(clazzAutowiring.getName());
            }
        }
        return null;
    }

    /**
     * 获取扫描包的路径
     *
     * @return
     */
    private String[] getScanPackageNames(Class<?> configClass) {
        boolean isConfigComponent = configClass.isAnnotationPresent(ComponentScan.class);
        if (isConfigComponent) {
            ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
            //返回配置的包路径
            return componentScan.value();
        }
        //否则没有配置扫描的路径，则默认启动类/监听器（此类）的目录下的所有包
        return new String[]{this.getClass().getPackageName()};
    }

    /**
     * 扫描包名下所有 class 并返回 Class 对象
     *
     * @param packageNames
     * @return
     */
    private Set<Class<?>> scanReturnClass(String[] packageNames) {
        Set<Class<?>> classes = new HashSet<>();
        for (String packageName : packageNames) {
            Set<Class<?>> scanClasses = ClassUtil.scanPackage(packageName);
            classes.addAll(scanClasses);
        }
        return classes;
    }


    /**
     * 获取需要导入的配置文件路径
     *
     * @param configClass
     * @return
     */
    private String[] getConfigProperties(Class<?> configClass) {
        boolean isConfigPropertySource = configClass.isAnnotationPresent(PropertySource.class);
        if (isConfigPropertySource) {
            PropertySource propertySource = configClass.getAnnotation(PropertySource.class);
            //返回配置的或者缺省配置的文件路径
            return propertySource.value();
        }
        //否则返回默认的 config.properties
        return new String[]{"config.properties"};
    }


    /**
     * 获取配置类属性
     *
     * @param properties
     * @return
     */
    private Properties getProperties(String[] properties) {
        Properties allProp = new Properties();
        for (String property : properties) {
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(property);
            try {
                Properties prop = new Properties();
                prop.load(resource);
                allProp.putAll(prop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allProp;
    }
}
