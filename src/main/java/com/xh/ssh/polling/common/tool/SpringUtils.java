package com.xh.ssh.polling.common.tool;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <b>Title: </b>
 * <p>Description: 
 * WEB容器在启动时，它会为每个WEB应用程序都创建一个对应的ServletContext对象，它代表当前web应用。
 * https://blog.csdn.net/qiqiongran_luck/article/details/6889037
 * </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月17日
 */
@SuppressWarnings("all")
public class SpringUtils {

	// 在系统启动时赋
	public static ServletContext servletContext;

	public static Object getBean(HttpServletRequest request, String beanName) {
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
		Object bean = ctx.getBean(beanName);
		return bean;
	}

	public static <T> T getSpringBean(HttpServletRequest request, String beanName) {
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
		T bean = (T) ctx.getBean(beanName);
		return bean;
	}

	public static <T> T getSpringBean(String beanName) {
		T bean = null;
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		if (ctx.getBean(StringUtils.uncapitalize(beanName)) != null) {
			bean = (T) ctx.getBean(StringUtils.uncapitalize(beanName));
		}
		return bean;
	}

	public static <T> T getSpringBean(String beanName, boolean isThrow) {
		if (getSpringBean(StringUtils.uncapitalize(beanName)) == null) {
			if (isThrow) {
				throw new RuntimeException("can not get bean:" + beanName);
			}
			return null;
		}
		T bean = (T) getSpringBean(StringUtils.uncapitalize(beanName));
		return bean;
	}

	public static <T> T getSpringBean(Class<T> t) {
		String beanName = StringUtils.uncapitalize(t.getSimpleName());
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		if (ctx.getBean(beanName) == null) {
			return null;
		}
		T bean = (T) ctx.getBean(beanName);
		return bean;
	}

	public static Object getSpringBeanByXml(String springXmlPath, String beanName) {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { springXmlPath });
		BeanFactory factory = (BeanFactory) context;
		Object bean = factory.getBean(beanName);
		return bean;
	}

	public static Object getSpringBeanByXml(String[] xmlPath, String beanName) {
		ApplicationContext context = new ClassPathXmlApplicationContext(xmlPath);
		BeanFactory factory = (BeanFactory) context;
		Object bean = factory.getBean(beanName);
		return bean;
	}

	public static <T> T getBeanByXml(String springXmlPath, String beanName) {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { springXmlPath });
		BeanFactory factory = (BeanFactory) context;
		T bean = (T) factory.getBean(beanName);
		return bean;
	}

	public static <T> T getBeanByXml(String springXmlPath, Class<T> t) {
		String beanName = StringUtils.uncapitalize(t.getSimpleName());
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { springXmlPath });
		BeanFactory factory = (BeanFactory) context;
		T bean = (T) factory.getBean(beanName);
		return bean;
	}

	public static ServletContext getServletContext() {
		return servletContext;
	}

	public static void setServletContext(ServletContext servletContext) {
		SpringUtils.servletContext = servletContext;
	}

}
