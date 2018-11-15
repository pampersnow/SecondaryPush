/*
 * Title: TRS 身份服务器
 * Copyright: Copyright (c) 2004-2005, TRS信息技术有限公司. All rights reserved.
 * License: see the license file.
 * Company: TRS信息技术有限公司(www.trs.com.cn)
 * 
 * Created on 2005-2-22
 */
package com.trs.cms.process.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * 处理配置属性的工具类. <BR>
 * 
 * @author TRS信息技术有限公司
 */
public class PropertyUtil {

	private static final Logger LOG = Logger.getLogger(PropertyUtil.class);

	/**
	 * 从指定文件名的文件中获取属性. <BR>
	 * 文件名参数说明: by converting the given pathname string into an abstract
	 * pathname.
	 * 
	 * @param fileName
	 *            给定的文件名
	 * @return 参见 {@link #loadProperties(File)}方法.
	 */
	public static Properties loadProperties(String fileName) {
		if (fileName == null) {
			return new Properties();
		}
		return loadProperties(new File(fileName));
	}

	/**
	 * 从给定文件中获取属性.
	 * 
	 * @param f
	 *            给定的文件
	 * @return 给定的文件中获取到的属性. 如果有异常发生, 则返回一个empty的属性.
	 * @deprecated liushen@Jan 17, 2010: 异常处理不恰当，不应被吞没！使用 {@link #assertAndLoadProperties(File)}代替.
	 */
	public static Properties loadProperties(File f) {
		Properties props = new Properties();
		if (f == null || false == f.exists()) {
			return props;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			props.load(fis);
		} catch (Exception e) {
			LOG.error("erron on load file: " + f, e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
					LOG.error("erron on close file: " + f, e1);
				}
			}
		}
		return props;
	}

	/**
	 * 从给定文件中获取属性.
	 * 
	 * @param f
	 *            给定的文件
	 * @return 给定的文件中获取到的属性.
	 * @since v3.5
	 * @creator liushen @ Jan 17, 2010
	 */
	public static Properties assertAndLoadProperties(File f) throws IOException {
		if (f == null || false == f.exists()) {
			throw new IllegalArgumentException("file is null!");
		}
		if (false == f.isFile()) {
			throw new IOException("file [ " + f + " ] not found.");
		}
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			props.load(fis);
			return props;
		} catch (IOException e) {
			LOG.error("erron on load file: " + f, e);
			throw new IOException("fail to read [" + f + "]", e);
		} finally {
			CloseUtil.closeInputStream(fis);
		}
	}

	/**
	 * 从给定的资源文件获取属性. <BR>
	 * 资源文件是指位于classpath中的文件.
	 * 
	 * @param resName
	 *            给定的资源文件
	 * @return 给定的资源文件中获取到的属性. 如果有异常发生, 则返回一个empty的属性.
	 * @deprecated ls@08.1225 请使用{@link #assertAndLoadFromResource(String)}代替.
	 */
	public static Properties loadFromResource(String resName) {
		return loadFromResource(PropertyUtil.class, resName);
	}

	/**
	 * 从给定的资源文件获取属性. <BR>
	 * 资源文件是指位于classpath中的文件.
	 * 
	 * @param resName
	 *            给定的资源文件
	 * @return 给定的资源文件中获取到的属性. 如果有异常发生, 则返回一个empty的属性.
	 * @deprecated ls@08.1225 请使用 {@link #assertAndLoadFromResource(Class, String)}代替.
	 */
	public static Properties loadFromResource(Class clazz, String resName) {
		Properties props = new Properties();
		URL resUrl = clazz.getResource(resName);
		if (resUrl == null) {
			LOG.warn("resUrl=null! resName=" + resName);
			return props;
		}
		InputStream fis = null;
		try {
			fis = resUrl.openStream();
		} catch (IOException e) {
			LOG.error("erron on url.openStream(), url=" + resUrl, e);
		}
		try {
			props.load(fis);
		} catch (Exception e) {
			LOG.error("erron on load resource: " + resName + ", url=" + resUrl, e);
		} finally {
			// [ls@2005-02-05] The JDK doesn't close the inputstream, so we must close it.
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
					LOG.error("erron on close resource: " + resName + ", url=" + resUrl, e1);
				}
			}
		}
		return props;
	}

	/**
	 * 从给定的资源文件获取属性. <BR>
	 * 资源文件是指位于classpath中的文件.
	 * 
	 * @param resName
	 *            给定的资源文件
	 * @return 给定的资源文件中获取到的属性.
	 * @throws IllegalArgumentException
	 */
	public static Properties assertAndLoadFromResource(Class clazz, String resName) throws 	IOException {
		if (resName == null) {
			throw new IllegalArgumentException("the resource name is null!");
		}
		if (clazz == null) {
			throw new IllegalArgumentException("the class object is null!");
		}
		URL resUrl = clazz.getResource(resName);
		if (resUrl == null) {
			if (resName.startsWith("/")) {
				throw new IOException("resource [ " + resName + " ] not found in [ " + clazz.getResource("/")
						+ " ]!");
			} else {
				throw new IOException("resource [ " + resName + " ] not found in [ " + clazz.getResource("/")
						+ getFSPathFormOfPackage(clazz) + " ]!");
			}
		}
		InputStream fis = null;
		try {
			fis = resUrl.openStream();
		} catch (IOException e) {
			throw new IOException("fail to open [" + resUrl + "]", e);
		}

		Properties props = new Properties();
		try {
			props.load(fis);
		} catch (IOException e) {
			throw new IOException("fail to read [" + resUrl + "]", e);
		} finally {
			CloseUtil.closeInputStream(fis);
		}
		return props;
	}

	/**
	 * 从给定的资源文件获取以指定前缀开始的配置项的取值属性集合. <BR>
	 * 资源文件是指位于classpath中的文件.
	 * 
	 * @param resName
	 *            给定的资源文件
	 * @return 给定的资源文件中获取到的所有以指定前缀开始的配置项的属性. 如果有异常发生, 则返回一个empty的属性.
	 */
	public static Properties loadFromResource(Class clazz, String resName, String keyPrefix) {
		Properties props = loadFromResource(clazz, resName);
		Properties result = new Properties();
		synchronized (props) {
			int max = props.size() - 1;
			Iterator it = props.entrySet().iterator();
			for (int i = 0; i <= max; i++) {
				Map.Entry e = (Map.Entry) (it.next());
				String key = (String) e.getKey();
				if (key.startsWith(keyPrefix)) {
					result.put(key, e.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * 以int值返回给定的Properties对象中的指定项的取值. <BR>
	 * 如果Properties对象为null, 或Properties对象中找不到该key, 或该value解析成整数时发生异常, 则返回给定的默认值.
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项
	 * @param defaultValue
	 *            给定的默认值
	 * @return Properties对象中的指定项的取值
	 */
	public static int getPropertyAsInt(Properties props, String key, int defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String strValue = props.getProperty(key);
		if (strValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(strValue.trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 以指定编码的字符串值返回给定的Properties对象中的指定项的取值. <BR>
	 * 如果Properties对象为null或是Properties对象中找不到该key, 则返回空串("").
	 * 该方法用于处理从Properties文件中获取中文值的问题.
	 * 
	 * @param props
	 * @param key
	 * @param encoding
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2012-2-22
	 */
	public static String getPropertyByEncoding(Properties props, String key, String encoding) {
		return getPropertyByEncoding(props, key, "", encoding);
	}

	/**
	 * 以指定编码的字符串值返回给定的Properties对象中的指定项的取值. <BR>
	 * 如果Properties对象为null或是Properties对象中找不到该key, 则返回空串("").
	 * 该方法用于处理从Properties文件中获取中文值的问题.
	 * 
	 * @param props
	 * @param key
	 * @param defaultValue
	 * @param encoding
	 *            此处填写"UTF-8"或"GBK"
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2012-2-22
	 */
	public static String getPropertyByEncoding(Properties props, String key, String defaultValue, String encoding) {
		if (props == null) {
			return defaultValue;
		}
		String strValue = props.getProperty(key);
		if (strValue == null) {
			return defaultValue;
		}
		try {
			return new String(strValue.getBytes("ISO-8859-1"), encoding);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 以GBK编码的字符串值返回给定的Properties对象中的指定项的取值. <BR>
	 * 如果Properties对象为null或是Properties对象中找不到该key, 则返回空串("").
	 * 该方法用于处理从Properties文件中获取中文值的问题.
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项
	 * @return Properties对象中的指定项的取值
	 */
	public static String getPropertyAsGBK(Properties props, String key) {
		return getPropertyByEncoding(props, key, "", "GBK");
	}

	/**
	 * 以GBK编码的字符串值返回给定的Properties对象中的指定项的取值. <BR>
	 * 如果Properties对象为null或是Properties对象中找不到该key, 则返回给定的默认值.
	 * 该方法用于处理从Properties文件中获取中文值的问题.
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项
	 * @param defaultValue
	 *            给定的默认值
	 * @return Properties对象中的指定项的取值
	 */
	public static String getPropertyAsGBK(Properties props, String key, String defaultValue) {
		return getPropertyByEncoding(props, key, defaultValue, "GBK");
	}

	/**
	 * 返回给定的Properties对象中的指定项的字符串取值, 并作trim()处理. <BR>
	 * The method return defaultValue on one of these conditions: <li>props == null <li>props.getProperty(key) == null
	 * <li>props.getProperty(key).trim().length() == 0
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项
	 * @param defaultValue
	 *            给定的默认值
	 * @return Properties对象中的指定项trim后的取值
	 */
	public static String getTrimString(Properties props, String key, String defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String strValue = props.getProperty(key);
		if (strValue == null || "".equals(strValue)) {
			return defaultValue;
		}
		strValue = strValue.trim();
		return strValue.length() == 0 ? defaultValue : strValue;
	}

	/**
	 * 以long值返回给定的Properties对象中的指定项的取值. 如果Properties对象中找不到该key, 则返回给定的默认值.
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项
	 * @param defaultValue
	 *            给定的默认值
	 * @return Properties对象中的指定项的取值
	 */
	public static long getPropertyAsLong(Properties props, String key, long defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String strValue = props.getProperty(key);
		if (strValue == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(strValue.trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 以基本类型boolean值返回给定的Properties对象中的指定项的取值. 如果Properties对象中找不到该key,
	 * 则返回给定的默认值. 表示布尔值的字符串大小写无关. 当且仅当表示布尔值的字符串为"true"时(忽略大小写), 返回true. 例如: <tt>Boolean.valueOf("True")</tt> returns
	 * <tt>true</tt>.<br>
	 * 再如: <tt>Boolean.valueOf("yes")</tt> returns <tt>false</tt>.
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项
	 * @param defaultValue
	 *            给定的默认值
	 * @return Properties对象中的指定项的取值. 当且仅当表示布尔值的字符串为"true"时(忽略大小写), 返回true.
	 */
	public static boolean getPropertyAsBool(Properties props, String key, boolean defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String strValue = props.getProperty(key);
		if (strValue == null || "".equals(strValue)) {
			return defaultValue;
		}
		try {
			// [liushen@2005-02-23]内联方法Boolean.valueOf(strValue).booleanValue()的实现,去除多余操作
			return strValue.trim().equalsIgnoreCase("true");
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 以字符串数组返回给定的Properties对象中的指定项的取值. <BR>
	 * <BR>
	 * 下列情况下, 本方法返回null: <li>给定的Properties对象为null. <li>给定的Properties对象中找不到该key. <li>给定的Properties对象中该key对应的字符串值为空(即
	 * <code>trim().length() == 0</code>).
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项. 不允许为null.
	 * @param token
	 *            将字符串分割为字符串数组的分隔标记. 不允许为null.
	 * @return Properties对象中的指定项的字符串数组取值.
	 * @see StringHelper#splitAlways(String, String)
	 */
	public static String[] getPropertyAsStrAry(Properties props, String key, String token) {
		if (props == null) {
			return null;
		}
		String value = props.getProperty(key);
		if (value == null) {
			return null;
		}
		value = value.trim();
		if (value.length() == 0) {
			return null;
		}
		return StringHelper.splitAlways(value, token);
	}

	/**
	 * 以float值返回给定的Properties对象中的指定项的取值. 如果Properties对象中找不到该key, 则返回给定的默认值.
	 * 
	 * @param props
	 *            给定的Properties对象
	 * @param key
	 *            指定项. 不允许为null.
	 * @param defaultValue
	 *            给定的默认值
	 * @return Properties对象中的指定项的取值
	 */
	public static float getPropertyAsFloat(Properties props, String key, float defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String strValue = props.getProperty(key);
		if (strValue == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(strValue.trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 输出给定的JDK Properties对象中以给定字符串为前缀的所有Key及其取值. <BR>
	 * 由于JDK Properties类自己的toString()方法得到的是所有key及其取值, 有时使得调试信息中无用的部分太多, 所以提供本方法.
	 * 
	 * @param props
	 *            给定的JDK Properties对象
	 * @param keyPrefix
	 *            给定的key前缀
	 * @return Properties对象中以给定字符串为前缀的所有Key及其取值组成的字符串.
	 */
	public static String toString(Properties props, String keyPrefix) {
		if (props == null || keyPrefix == null) {
			return "null! keyPrefix=" + keyPrefix;
		}
		final String delit = ", ";
		final int delitLen = delit.length();
		StringBuffer sb = new StringBuffer(64);
		sb.append('{');
		// 添加符合条件(以给定字符串开头)的Key的信息. 借鉴JDK
		// Hashtable类(Properties类的父类)自己的toString过程的算法.
		synchronized (props) {
			int max = props.size() - 1;
			Iterator it = props.entrySet().iterator();
			for (int i = 0; i <= max; i++) {
				Map.Entry e = (Map.Entry) (it.next());
				String key = (String) e.getKey();
				if (key.startsWith(keyPrefix)) {
					sb.append(key).append('=').append(e.getValue());
					sb.append(delit);
				}
			}
		}
		sb.delete((sb.length() > delitLen) ? (sb.length() - delitLen) : 1, sb.length());
		sb.append('}');
		return sb.toString();
	}

	/**
	 * 获取给定的JDK Properties对象中以给定字符串为前缀的所有Key及其取值组成的Properties对象. <BR>
	 * 
	 * @param props
	 *            给定的JDK Properties对象
	 * @param keyPrefix
	 *            给定的key前缀
	 * @return Properties对象中以给定字符串为前缀的所有Key及其取值组成的Properties对象.
	 */
	public static Properties getSubProperties(Properties props, String keyPrefix) {
		if (props == null || keyPrefix == null) {
			return props;
		}
		Properties result = new Properties();
		synchronized (props) {
			int max = props.size() - 1;
			Iterator it = props.entrySet().iterator();
			for (int i = 0; i <= max; i++) {
				Map.Entry e = (Map.Entry) (it.next());
				String key = (String) e.getKey();
				if (key.startsWith(keyPrefix)) {
					result.put(key, e.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * @see #assertAndLoadFromResource(Class, String)
	 */
	public static Properties assertAndLoadFromResource(String resName) throws Exception {
		return assertAndLoadFromResource(PropertyUtil.class, resName);
	}

	/**
	 * 将package名转换为路径形式.
	 */
	private static String getFSPathFormOfPackage(Class clazz) {
		Class c = clazz;
		while (c.isArray()) {
			c = c.getComponentType();
		}
		String baseName = c.getName();
		int index = baseName.lastIndexOf('.');
		if (index != -1) {
			return baseName.substring(0, index).replace('.', '/');
		} else {
			return baseName;
		}
	}
}