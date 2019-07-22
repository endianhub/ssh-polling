package com.xh.ssh.polling.common.tool;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Title: 日志</b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2018年8月1日
 */
public class LogUtils {

	private static Logger LOG4J_LOGGER = null;
	private static org.slf4j.Logger SLF4J_LOGGER = null;

	private static final <T> void apache_init(Class<T> clazz) {
		LOG4J_LOGGER = LogManager.getLogger(clazz);
	}

	private static final <T> void slf4j_init(Class<T> clazz) {
		SLF4J_LOGGER = LoggerFactory.getLogger(clazz);
	}

	/**
	 * <b>Title: 普通打印</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 */
	public static <T> void info(Class<T> clazz, Object message) {
		LogUtils.apache_init(clazz);
		LOG4J_LOGGER.info(message);
	}

	/**
	 * <b>Title: 普通打印</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 */
	public static <T> void debug(Class<T> clazz, Object message) {
		LogUtils.apache_init(clazz);
		LOG4J_LOGGER.debug(message);
	}

	/**
	 * <b>Title: 普通打印</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 */
	public static <T> void warn(Class<T> clazz, Object message) {
		LogUtils.apache_init(clazz);
		LOG4J_LOGGER.warn(message);
	}

	/**
	 * <b>Title: 普通打印</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 */
	public static <T> void error(Class<T> clazz, Object message) {
		LogUtils.apache_init(clazz);
		LOG4J_LOGGER.error(message);
	}

	/**
	 * <b>Title: 打印平等线</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 * @param isPrintEqualLine
	 */
	public static <T> void info(Class<T> clazz, Object message, boolean isPrintEqualLine) {
		LogUtils.apache_init(clazz);
		if (isPrintEqualLine) {
			if (LOG4J_LOGGER.isInfoEnabled()) {
				LOG4J_LOGGER.info("===========================================================================\n" + message);
			}
		} else {
			if (LOG4J_LOGGER.isDebugEnabled()) {
				LOG4J_LOGGER.info(message);
			}
		}
	}

	/**
	 * <b>Title: 打印平等线</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 * @param isPrintEqualLine
	 */
	public static <T> void debug(Class<T> clazz, Object message, boolean isPrintEqualLine) {
		LogUtils.apache_init(clazz);
		if (isPrintEqualLine) {
			if (LOG4J_LOGGER.isInfoEnabled()) {
				LOG4J_LOGGER.debug("===========================================================================\n" + message);
			}
		} else {
			if (LOG4J_LOGGER.isDebugEnabled()) {
				LOG4J_LOGGER.debug(message);
			}
		}
	}

	/**
	 * <b>Title: 打印平等线</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 * @param isPrintEqualLine
	 */
	public static <T> void warn(Class<T> clazz, Object message, boolean isPrintEqualLine) {
		LogUtils.apache_init(clazz);
		if (isPrintEqualLine) {
			if (LOG4J_LOGGER.isInfoEnabled()) {
				LOG4J_LOGGER.warn("===========================================================================\n" + message);
			}
		} else {
			if (LOG4J_LOGGER.isDebugEnabled()) {
				LOG4J_LOGGER.warn(message);
			}
		}
	}

	/**
	 * <b>Title: 打印平等线</b>
	 * <p>Description: org.apache.log4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param message
	 * @param isPrintEqualLine
	 */
	public static <T> void error(Class<T> clazz, Object message, boolean isPrintEqualLine) {
		LogUtils.apache_init(clazz);
		if (isPrintEqualLine) {
			if (LOG4J_LOGGER.isInfoEnabled()) {
				LOG4J_LOGGER.error("===========================================================================\n" + message);
			}
		} else {
			if (LOG4J_LOGGER.isDebugEnabled()) {
				LOG4J_LOGGER.error(message);
			}
		}
	}

	/**
	 * <b>Title: 打印异常</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param e
	 */
	public static <T> void error(Class<T> clazz, Object message, Exception e) {
		LogUtils.apache_init(clazz);
		LOG4J_LOGGER.error(message + "  " + LogUtils.getExceptionStr(e));
	}

	/**
	 * <b>Title: 打印异常</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param e
	 */
	public static <T> void error(Class<T> clazz, Exception e) {
		LogUtils.apache_init(clazz);
		LOG4J_LOGGER.error(LogUtils.getExceptionStr(e));
	}

	/**
	 * <b>Title: 把异常信息转换成字符串</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param e
	 * @return
	 */
	private static String getExceptionStr(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}

	/**
	 * <b>Title: 占位符-打印日志 </b>
	 * <p>Description: org.slf4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param format 占位符
	 * @param arg
	 */
	public static <T> void info(Class<T> clazz, String format, Object arg) {
		LogUtils.slf4j_init(clazz);
		SLF4J_LOGGER.info(format, arg);
	}

	/**
	 * <b>Title: 占位符-打印日志 </b>
	 * <p>Description: org.slf4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public static <T> void info(Class<T> clazz, String format, Object arg1, Object arg2) {
		LogUtils.slf4j_init(clazz);
		SLF4J_LOGGER.info(format, arg1, arg2);
	}

	/**
	 * <b>Title: 占位符-打印日志 </b>
	 * <p>Description: org.slf4j.Logger</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param clazz
	 * @param format
	 * @param argArray
	 */
	public static <T> void info(Class<T> clazz, String format, Object[] argArray) {
		LogUtils.slf4j_init(clazz);
		SLF4J_LOGGER.info(format, argArray);
	}
}
