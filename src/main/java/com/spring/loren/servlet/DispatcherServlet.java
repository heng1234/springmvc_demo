package com.spring.loren.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spring.loren.annotation.LorenAutowired;
import com.spring.loren.annotation.LorenController;
import com.spring.loren.annotation.LorenRequestMapping;
import com.spring.loren.annotation.LorenRequestParam;
import com.spring.loren.annotation.LorenService;
import com.spring.loren.controller.HlvyController;

import jdk.internal.org.objectweb.asm.Handle;

public class DispatcherServlet extends HttpServlet {
	
List<String> classNames = new ArrayList<String>();//存储扫描到的所有class 

Map<String, Object> handerMap =new HashMap<String, Object>();//

Map<String, Object> benas =new HashMap<String, Object>();//存储拿到的对象

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);//调用doPost这样无论是get提交还是post都会进入doPost
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

      String uri = req.getRequestURI();// /spring_hw_demo/loren/query
		
      String context = req.getContextPath();// /spring_hw_demo
      
      String path = uri.replace(context, ""); // /loren/query ---> key
      
      Method method = (Method) handerMap.get(path);
      
      String keypath = "/"+path.split("/")[1];
      HlvyController instance = (HlvyController) benas.get(keypath);

      try {
    	  Object[] args =hand(req, resp, method);
     	  
		method.invoke(instance, args);//从底层调用方法 args[] 方法里的参数 数组
		
	} catch (IllegalAccessException e) {
		
		e.printStackTrace();
		
	} catch (IllegalArgumentException e) {
		
		e.printStackTrace();
		
	} catch (InvocationTargetException e) {
		
		e.printStackTrace();
	}
	}
	
	/**
	 * 这里没使用策略模式
	 * @author heng
	 * <p>Title: Handle</p>  
	 * <p>Description: </p>  
	 * @return
	 */
	private static Object[] hand(HttpServletRequest request,HttpServletResponse response,Method method) {
		
		//拿到当前执行的有哪些参数
		Class<?>[] paramClazzs = method.getParameterTypes();
		
		//根据参数的个数, new 一个参数的数组 将方法里的参数赋值到args来
		Object[] args =new Object[paramClazzs.length];	
	
		int args_i = 0;
		
		int index = 0;
		
		for (Class<?> paramClzz : paramClazzs) {
			if (ServletRequest.class.isAssignableFrom(paramClzz)) {
				args[args_i++] = request;
			}
			if (ServletResponse.class.isAssignableFrom(paramClzz)) {
				args[args_i++] = response;
			}
			// 从0-3判断有没有RequestParam注解，很明显paramClazz为0和1时,不是
			// 当为2和3时为@RequestParam,需要解析
			// [@com.spring.loren.annotation.LorenRequsttParam(value=name)]
			Annotation[] paramAns = method.getParameterAnnotations()[index];
			
			if (paramAns.length > 0) {
				 for (Annotation paramAn : paramAns) {
					 
					 if (LorenRequestParam.class.isAssignableFrom(paramAn.getClass())) {
						 
						 LorenRequestParam rp = (LorenRequestParam) paramAn;
					 
					     //找到注解里的name和age
						 args[args_i++] = request.getParameter(rp.value());
					 }
					
				}
			}
			
			index++;
		}
		
		return args;
		
	}
/**
 * 初始化
 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		//扫描
		doScanClass("com.spring");//找到所有的class
		
		//根据list结果 生成对象  反射
		doInstance();
		
		//注入到Controlle
		doAutowired();
		
		//urlMapping
		urlMapping();
	}
	public void urlMapping() {
		 for (Map.Entry<String, Object> entry : benas.entrySet()) {//遍历map集合
			 Object object = entry.getValue();//拿到map中的vlaue
				
			 Class<?> clazz = object.getClass();//拿到class
			 
			 if(clazz.isAnnotationPresent(LorenController.class)) {//遍历controlle
				 
				 LorenRequestMapping mapping = clazz.getAnnotation(LorenRequestMapping.class);
				 
				 String classPath = mapping.value();//"/loren"
				 
				 Method[] methods = clazz.getMethods();//拿到类里的所有方法
				 
				 for (Method method : methods) {
					if(method.isAnnotationPresent(LorenRequestMapping.class)) {
						
						LorenRequestMapping mappingOne = method.getAnnotation(LorenRequestMapping.class);
						
						String methodPath = mappingOne.value();// "/query"
						
						handerMap.put(classPath+methodPath, method); // /loren/query --> method(query)
					}else {
						continue;
					}
				}
			 }
		 }
	}
	
	/**
	 * 
	 * @author heng
	 * <p>Title: doAutowired</p>  
	 * <p>Description: </p>
	 */
	public void doAutowired() {
		
		 for (Map.Entry<String, Object> entry : benas.entrySet()) {//遍历map集合
			 
			Object object = entry.getValue();//拿到map中的vlaue
			
			Class<?> clazz = object.getClass();//拿到class
			
			if(clazz.isAnnotationPresent(LorenController.class)) {//遍历controlle
				
			Field[] fields = clazz.getDeclaredFields();//拿到类里面定义的所有属性包括私有的
			
				for (Field field : fields) {
					
					if(field.isAnnotationPresent(LorenAutowired.class)) {//如果属性上是@LorenAutowired
						
						LorenAutowired autowired = field.getAnnotation(LorenAutowired.class);
						
				     	String key = autowired.value();//拿到service的key
				     	
				     	field.setAccessible(true);//设置类里的私有属性可以改变值 不然设置不了
				     	
				     	try {
				     		
							field.set(object, benas.get(key));//给对应的赋值
							
						} catch (IllegalArgumentException e) {
							
							e.printStackTrace();
							
						} catch (IllegalAccessException e) {
							
							e.printStackTrace();
						}
					}else {
						continue;
					}
				}
			}else {
				continue;
			}
		}
	}
	
	/**
	 * 
	 * @author heng
	 * <p>Title: doInstance</p>  
	 * <p>Description: </p>
	 */
	public void doInstance() {
		for (String className : classNames) {
			
			//com.spring.loren.controller.HlvyController.class
			String cn = className.replace(".class", "");//com.spring.loren.controller.HlvyController
			try {
				
				Class<?> clz = Class.forName(cn);//根据实例拿到xxx类
				
				if(clz.isAnnotationPresent(LorenController.class)) {//如果拿到的这个与LorenController的匹配的话 @LorenController
					
					Object object = clz.newInstance();//反射创建xxx对象
					
					LorenRequestMapping lorenRequestMapping = clz.getAnnotation(LorenRequestMapping.class);
					
					String key = lorenRequestMapping.value();//拿到注解上的value
					
					benas.put(key, object);//把注解上的key作为map key value就是反射拿到的对象 IOC容器
					
				}else if(clz.isAnnotationPresent(LorenService.class)) {//@LorenService
					
					Object object = clz.newInstance();//反射创建xxx对象
					
					LorenService service = clz.getAnnotation(LorenService.class);
					
					String key = service.value();//拿到注解上的value
					
					benas.put(key, object);//把注解上的key作为map key value就是反射拿到的对象 IOC容器
				}else {
					continue;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @author heng
	 * <p>Title: doScanClass</p>  
	 * <p>Description: </p>  
	 * @param basePackage
	 */
	public void doScanClass(String basePackage) {
		//读取所有的类路径把/替换为.  E:\eclipsepro\spring_hw_demo\src\main\java\com\spring\loren\service\HlvyService.java
		URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.", "/"));
		System.out.println(url+"---one");
		String fileStr = url.getFile();//com spring
		System.out.println(fileStr+"---two");
		File file = new File(fileStr);
		String[] filesStr = file.list();
		for (String path : filesStr) {
			System.out.println(fileStr+path+"---for one");
			File filePath = new File(fileStr+path);//com.spring.loren
			if(filePath.isDirectory()) {//如果是文件夹的话
				doScanClass(basePackage+"."+path);//递归查询 com.spring.loren
			}else {
				//找到了class
				classNames.add(basePackage+"."+filePath.getName());//存储的定义的class  com.spring.loren.controller.HlvyController.class  待会需要把.class去掉
			}
		}
	};

}
