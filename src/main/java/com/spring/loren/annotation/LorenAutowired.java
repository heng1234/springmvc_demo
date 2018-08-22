package com.spring.loren.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author heng
 *<p>Title: LorenAutowired</p>  
 * @date 2018年8月22日
 */
@Target(ElementType.FIELD)//字段、枚举的常量
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Documented//说明该注解将被包含在javadoc中
public @interface LorenAutowired {
 
	 String value() default "";
}
