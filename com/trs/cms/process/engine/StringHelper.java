/*
 * Title: TRS 身份服务器 Copyright: Copyright (c) 2004-2005, TRS信息技术有限公司. All rights reserved. License: see the license file.
 * Company: TRS信息技术有限公司(www.trs.com.cn)
 * 
 * Created on 2004-12-8
 */
package com.trs.cms.process.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 字符串处理的工具类. <BR>
 * 
 * @author TRS信息技术股份有限公司
 * @version 1.0
 */
public class StringHelper {

	/**
	 * 将指定字符串的指定位置的字符以byte方式返回. 该方法中对返回的字符有一次ascii转换, 即减去一个'0'的ascii值(十进制的48).
	 * 
	 * @param strHead
	 *            指定字符串
	 * @param index
	 *            指定位置
	 * @return 指定位置的字符的byte值
	 */
	public static byte byteAt(String strHead, int index) {
		byte result = (byte) strHead.charAt(index);
		result -= '0';
		return result;
	}

	/**
	 * 将给定字符串(origin)以字符串token作为分隔符进行分隔, 得到一个字符串数组. 该函数不依赖于JDK 1.4, 和JDK 1.4中String.split(String regex)的区别是不支持正则表达式.<br>
	 * 在不包含有token字符串时, 本函数返回以原字符串构成的数组.
	 * 
	 * @param origin
	 *            给定字符串
	 * @param token
	 *            分隔符
	 * @return 字符串数组
	 */
	public static String[] split(String origin, String token) {
		if (isEmpty(origin))
			return null;

		final StringTokenizer st = new StringTokenizer(origin, token);
		final int countTokens = st.countTokens();
		if (countTokens <= 0) {
			return new String[] { origin };
		}
		String[] results = new String[countTokens];
		for (int i = 0; i < countTokens; i++) {
			results[i] = st.nextToken();
		}
		return results;
	}

	/**
	 * 按顺序合并两个字符数组
	 * 
	 * @param firstArray
	 * @param secondArray
	 * @return
	 * @since v3.5
	 * @creator yaonengjun @ Jun 8, 2010
	 */
	public static String[] mergeArrary(String[] firstArray, String[] secondArray) {
		if (isStringArrayEmpty(firstArray) && isStringArrayEmpty(secondArray))
			return null;
		if (isStringArrayEmpty(firstArray) && secondArray != null)
			return secondArray;
		if (firstArray != null && isStringArrayEmpty(secondArray))
			return firstArray;

		int firstLength = firstArray.length;
		int secondLength = secondArray.length;
		int resultLength = firstLength + secondLength;
		String[] resultArray = new String[resultLength];
		for (int i = 0; i < firstLength; i++) {
			resultArray[i] = firstArray[i];
		}
		for (int j = 0; j < secondLength; j++) {
			resultArray[firstLength + j] = secondArray[j];
		}
		return resultArray;
	}

	public static boolean isStringArrayEmpty(String[] strArray) {
		if (strArray == null || strArray.length == 0)
			return true;
		return false;
	}

	/**
	 * 将给定字符串(origin)以字符串token作为分隔符进行分隔,并将结果以List返回
	 * 
	 * @param origin
	 *            给定字符串
	 * @param token
	 *            分隔符
	 * @return
	 * @creator huangshengbo @ 2009-7-9
	 */
	public static List splitToList(String origin, String token) {
		List result = new ArrayList();
		String[] arr = split(origin, token);
		if (arr != null) {
			for (int i = 0; i < arr.length; i++) {
				result.add(arr[i]);
			}
		}
		return result;
	}

	/**
	 * 将给定字符串(origin)以字符串token作为分隔符进行分隔, 得到一个字符串数组. 该函数不依赖于JDK 1.4, 和JDK <br>
	 * 该函数仅仅从给定字符串(origin)中，第一个token的地方进行分割.<br>
	 * 例如 paramA=aa===,分割的结果为 String[0]:paramA, String[1]:aa===
	 * 
	 * @param origin
	 * @param token
	 * @return
	 * @creator yao.nengjun@trs.com.cn @ May 5, 2009
	 */
	public static String[] splitAtFirstToken(String origin, String token) {
		if (isEmpty(origin))
			return null;

		if (isEmpty(token))
			return new String[] { origin };

		int firstTokenIndex = origin.indexOf(token, 0);
		if (firstTokenIndex == -1)
			return new String[] { origin };

		String firstString = origin.substring(0, firstTokenIndex);
		String secondString = origin.substring(firstTokenIndex + token.length(), origin.length());
		String[] arrayToReturn = new String[] { firstString, secondString };
		return arrayToReturn;
	}

	/**
	 * 得到给定字符串的逆序的字符串. <BR>
	 * 用例: <code>
	 * <pre>
	 *         assertEquals("/cba", StringHelper.reverse("abc/"));
	 *         assertEquals("aabbbccccx", StringHelper.reverse("xccccbbbaa"));
	 * 		   assertEquals("试测^%6cbA数参", StringHelper.reverse("参数Abc6%^测试"));
	 * </pre>
	 * </code>
	 */
	public static String reverse(String origin) {
		if (origin == null) {
			throw new NullPointerException("参数为null!");
		}
		return new StringBuffer(origin).reverse().toString();
	}

	/**
	 * 用ISO-8859-1对给定字符串编码.
	 * 
	 * @param string
	 *            给定字符串
	 * @return 编码后所得的字符串. 如果给定字符串为null或"", 则原样返回.如果在转换过程中发生异常，那么原样返回。
	 */
	public static String encodingByISO8859_1(String string) {
		if ((string != null) && !("".equals(string))) {
			try {
				return new String(string.getBytes(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				System.out.println("进行ISO-8859-1转码时出错");
				e.printStackTrace();
				return string;
			}
		}
		return string;
	}

	/**
	 * The method is identical with {@link #split(String, String)}, and will be deleted.
	 */
	public static String[] splitAlways(String origin, String token) {
		return split(origin, token);
	}

	/**
	 * 使用多个字符进行分隔。例如qqq123uuu123ooo,使用本方法，按照123分隔的话，可以得到 qqq,uuu,ooo的字符串数组
	 * 
	 * @param origin
	 * @param token
	 * @return
	 * @since v3.5
	 * @creator Administrator @ 2010-9-1
	 */
	public static String[] splitWithMutiChar(String origin, String token) {
		if (isEmpty(origin))
			return null;

		if (isEmpty(token))
			return new String[] { origin };

		if (token.length() == 1)
			return split(origin, token);

		int index = origin.indexOf(token);
		// 如果找不到token,返回原始字符串
		if (index < 0) {
			return new String[] { origin };
		}
		//
		return origin.split(token);
	}

	// from commons-codec: char[] Hex.encodeHex(byte[])
	/**
	 * 将给定的字节数组用十六进制字符串表示.
	 */
	public static String toString(byte[] data) {
		if (data == null) {
			return "null!";
		}
		int l = data.length;

		char[] out = new char[l << 1];

		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS[0x0F & data[i]];
		}

		return new String(out);
	}

	// from commons-codec: byte[] Hex.decodeHex(char[])
	/**
	 * 将给定的十六进制字符串转化为字节数组. <BR>
	 * 与<code>toString(byte[] data)</code>作用相反.
	 * 
	 * @throws RuntimeException
	 *             当给定十六进制字符串的长度为奇数时或给定字符串包含非十六进制字符.
	 * @see #toString(byte[])
	 */
	public static byte[] toBytes(String str) {
		if (str == null) {
			return null;
		}
		char[] data = str.toCharArray();
		int len = data.length;

		if ((len & 0x01) != 0) {
			throw new RuntimeException("Odd number of characters!");
		}

		byte[] out = new byte[len >> 1];

		// two characters form the hex value.
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}

		return out;
	}

	// [liushen@2005-04-21] from caohui
	/**
	 * HTML元素value值过滤处理函数：将 <code> & &lt; &gt; &quot </code> 等特殊字符作转化处理. <BR>
	 * 用例: <code>
	 *    &lt;input type="text" name="Name" value="<%=StringHelper.filterForHTMLValue(sContent)%>"&gt;
	 * </code>
	 *
	 * @param _sContent
	 *            指定的文本内容. 如果为null则返回"".
	 * @return 处理后的文本内容.
	 */
	public static String filterForHTMLValue(String _sContent) {
		/*return com.trs.dev4.jdk16.utils.StringHelper.filterForHTMLValue(_sContent);*/
		return null;
	}

	/**
	 * HTML元素value值反过滤处理函数，方法{@link #filterForHTMLValue(String)}的反向处理：将 <code> & &lt; &gt; &quot; </code>
	 * 等特殊字符转化为html元素. <BR>
	 * 用例: <code>
	 *    &lt;input type="text" name="Name" value="<%=StringHelper.filterForHTMLValue(sContent)%>"&gt;
	 * </code>
	 * 
	 * @param _sContent
	 *            指定的文本内容. 如果为null则返回"".
	 * @return 处理后的文本内容.
	 */
	public static String antiFilterForHTMLValue(String _sContent) {
		if (_sContent == null)
			return "";
		_sContent = _sContent.replaceAll("&amp;", "&");
		_sContent = _sContent.replaceAll("&lt;", "<");
		_sContent = _sContent.replaceAll("&gt;", ">");
		_sContent = _sContent.replaceAll("&quot;", "\"");

		return _sContent;
	}

	/**
	 * 将字符串转化为UTF-8的"(\\uXXXX)"的形式. <BR>
	 * 使用了不公开的sun.io.ByteToCharConverter类. 因此只能在Sun JDK下运行.
	 * 
	 * @param strSource
	 *            给定的字符串, 不能为null.
	 * @return 字符串的UTF-8的"(\\uXXXX)"的形式.
	 * @throws sun.io.MalformedInputException
	 * @throws UnsupportedEncodingException
	 *             public static String convert(String strSource) throws sun.io.MalformedInputException,
	 *             UnsupportedEncodingException { sun.io.ByteToCharConverter b2c = sun.io.ByteToCharConverter
	 *             .getConverter("UTF-16BE"); char[] chars = b2c.convertAll(strSource.getBytes("UTF-16BE"));
	 *             StringBuffer sb = new StringBuffer(); for (int i = 0; i < chars.length; i++) { sb.append("\\u");
	 *             sb.append(Integer.toHexString(chars[i])); } return sb.toString(); }
	 */

	/**
	 * 等价于<code>toString(objs, false, ",");</code>.
	 */
	public static String toString(Object[] objs) {
		return toString(objs, false, ", ");
	}

	/**
	 * 等价于<code>toString(objs, showOrder, ",");</code>.
	 * 
	 * @see #toString(Object[], boolean, String)
	 */
	public static String toString(Object[] objs, boolean showOrder) {
		return toString(objs, showOrder, ",");
	}

	/**
	 * 输出数组内容. 如果数组为null, 返回null. 如果数组某元素为null则该元素输出为null.
	 * 
	 * @param objs
	 *            待输出的数组
	 * @param showOrder
	 *            是否输出元素的序号
	 * @param token
	 *            元素间的分割串
	 */
	public static String toString(Object[] objs, boolean showOrder, String token) {
		if (objs == null) {
			return "null";
		}
		int len = objs.length;
		StringBuffer sb = new StringBuffer(10 * len);
		for (int i = 0; i < len; i++) {
			if (showOrder) {
				sb.append(i).append(':');
			}
			sb.append(objs[i]);
			if (i < len - 1) {
				sb.append(token);
			}
		}
		return sb.toString();
	}

	public static String avoidNull(String str) {
		if ("null".equals(str)) {
			return "";
		}
		return (str == null) ? "" : str;
	}

	public static String trim(String str) {
		return (str == null) ? "" : str.trim();
	}

	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	/**
	 * Converts a hexadecimal character to an integer.
	 * 
	 * @param ch
	 *            A character to convert to an integer digit
	 * @param index
	 *            The index of the character in the source
	 * @return An integer
	 * @throws RuntimeException
	 *             Thrown if ch is an illegal hex character
	 */
	static int toDigit(char ch, int index) {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal charcter " + ch + " at index " + index);
		}
		return digit;
	}

	/**
	 * 采用正则表达式进行替换。<br>
	 * <br>
	 * 
	 * 常用正则表达式：<br>
	 * 1）\\%([^%]+)%<br>
	 * 如%aa% cc %bb% 其中 %aa%, %bb% 为占位符. 可用传入的Map中Key相同的变量进行替换<br>
	 * <br>
	 * 
	 * 2) \\$\\{([^}]+)}<br>
	 * 如${aa} cc ${bb} 其中 ${aa}, ${bb} 为占位符. 可用传入的Map中Key相同的变量进行替换<br>
	 * <br>
	 * 
	 * 
	 * @param templateStr
	 *            模板字符串
	 * @param data
	 *            替换的变量值
	 * @return 返回替换后的字符串。如果模板字符串中的变量在Map中未找到相应的值，则将变量原样返回。
	 */

	public static String replaceWithRegex(String templateStr, Map data, Pattern pattern) {
		if (isEmpty(templateStr))
			return null;

		if (data == null)
			data = Collections.EMPTY_MAP;

		StringBuffer newValue = new StringBuffer(templateStr.length());
		Matcher matcher = pattern.matcher(templateStr);
		while (matcher.find()) {
			String key = matcher.group(1);
			if (data.get(key) != null) {
				String r = data.get(key).toString();
				// 这个是为了替换windows下的文件目录在java里用\\表示
				matcher.appendReplacement(newValue, r.replaceAll("\\\\", "\\\\\\\\"));
			}
		}

		matcher.appendTail(newValue);

		return newValue.toString();
	}

	// ==========================================================================
	// ===
	// 字符串替换, (2005-07-21) from CMyString
	/**
	 * 字符串替换函数：用于将指定字符串中指定的字符串替换为新的字符串。
	 * 
	 * @param _strSrc
	 *            源字符串。
	 * @param _strOld
	 *            被替换的旧字符串
	 * @param _strNew
	 *            用来替换旧字符串的新字符串
	 * @return 替换处理后的字符串
	 */
	public static String replaceStr(String _strSrc, String _strOld, String _strNew) {
		if (_strSrc == null)
			return null;

		// 提取源字符串对应的字符数组
		char[] srcBuff = _strSrc.toCharArray();
		int nSrcLen = srcBuff.length;
		if (nSrcLen == 0)
			return "";

		// 提取旧字符串对应的字符数组
		char[] oldStrBuff = _strOld.toCharArray();
		int nOldStrLen = oldStrBuff.length;
		if (nOldStrLen == 0 || nOldStrLen > nSrcLen)
			return _strSrc;

		StringBuffer retBuff = new StringBuffer((nSrcLen * (1 + _strNew.length() / nOldStrLen)));

		int i, j, nSkipTo;
		boolean bIsFound = false;

		i = 0;
		while (i < nSrcLen) {
			bIsFound = false;

			// 判断是否遇到要找的字符串
			if (srcBuff[i] == oldStrBuff[0]) {
				for (j = 1; j < nOldStrLen; j++) {
					if (i + j >= nSrcLen)
						break;
					if (srcBuff[i + j] != oldStrBuff[j])
						break;
				}
				bIsFound = (j == nOldStrLen);
			}

			// 若找到则替换，否则跳过
			if (bIsFound) { // 找到
				retBuff.append(_strNew);
				i += nOldStrLen;
			} else { // 没有找到
				if (i + nOldStrLen >= nSrcLen) {
					nSkipTo = nSrcLen - 1;
				} else {
					nSkipTo = i;
				}
				for (; i <= nSkipTo; i++) {
					retBuff.append(srcBuff[i]);
				}
			}
		}// end while
		srcBuff = null;
		oldStrBuff = null;
		return retBuff.toString();
	}

	/**
	 * 按给定分割符对给定字符串作分割, 然后作trim处理.<BR>
	 * <li>origin为null时返回null. <li>不包含有token字符串时, 本函数返回以原字符串trim后构成的数组.
	 * 
	 * @param origin
	 *            给定字符串
	 * @param token
	 *            分隔符. 不允许为null.
	 * @return 字符串数组
	 */
	public static String[] splitAndTrim(String origin, String token) {
		if (origin == null) {
			return null;
		}
		origin = origin.trim();
		final StringTokenizer st = new StringTokenizer(origin, token);
		final int countTokens = st.countTokens();
		if (countTokens <= 0) {
			return new String[] { origin };
		}
		List strs = new ArrayList(countTokens);
		String str;
		for (int i = 0; i < countTokens; i++) {
			str = st.nextToken().trim();
			if (str.length() > 0) {
				strs.add(str);
			}
		}
		return (String[]) strs.toArray(new String[0]);
	}

	public static String hexToStr(String hex) {
		return new String(toBytes(hex));
	}

	public static String truncateAndTrim(String str, String delim) {
		if (str == null || delim == null) {
			return str;
		}
		int nStart = str.indexOf(delim);
		if (nStart < 0) {
			return str;
		}
		return str.substring(nStart + delim.length()).trim();
	}

	/**
	 * 获得字符串指定编码的字符串
	 * 
	 * @param originalStr
	 * @param encoding
	 * @return 编码后的字符串
	 */
	public static String getStringByEncoding(String originalStr, String encoding) {

		String encodingStr = originalStr;
		try {
			encodingStr = new String(originalStr.getBytes(), encoding);
		} catch (UnsupportedEncodingException e) {
			return originalStr;
		}
		return encodingStr;

	}

	/**
	 * 判断字符串是否为null或空.
	 * 
	 * @return true if <code>(str == null || str.trim().length() == 0)</code>, otherwise false.
	 * @since ls@07.0624
	 */
	public static boolean isEmpty(String str) {
		return (str == null || str.trim().length() == 0);
	}

	/**
	 * 将字符串调整为不超过maxLength长度的字符串, 调整方法为去掉中间的适当长度, 以...区分开头和结束.
	 * 
	 * @param maxLength
	 *            指定的长度.
	 * @since ls@07.1227
	 */
	public static String adjustLength(String str, int maxLength) {
		if (str == null) {
			return str;
		}
		if (maxLength <= 0) {
			throw new IllegalArgumentException("Illegal value of maxLength: " + maxLength
					+ "! It must be a positive integer.");
		}
		int strLength = str.length();
		if (maxLength > strLength) {
			return str;
		}
		final String DELIT = "...";
		StringBuffer sb = new StringBuffer(maxLength);
		int splitPos = (maxLength - DELIT.length()) / 2;
		sb.append(str.substring(0, splitPos));
		sb.append(DELIT);
		sb.append(str.substring(strLength - splitPos));
		return sb.toString();
	}

	/**
	 * 获得安全的URL，避免跨站式攻击 处理方法同
	 * ynj@2008-11-03
	 * 
	 * @return
	 */
	public static String getURLSafe(String url) {
		if (url == null || "".equals(url))
			return "";

		StringBuffer strBuff = new StringBuffer();
		char[] charArray = url.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] == '<' || charArray[i] == '>')
				continue;

			strBuff.append(charArray[i]);
		}
		return strBuff.toString();
	}

	/**
	 * 名/值的分割
	 */
	public static final String KEYVALUE_SPLITTER = "=";
	/**
	 * 属性间的分隔符
	 */
	public static final String PROPERTY_SPLITTER = "&";

	/**
	 * 
	 * @param properties
	 * @return
	 */
	public static Map String2Map(String properties) {
		return String2Map(properties, PROPERTY_SPLITTER, KEYVALUE_SPLITTER);
	}

	/**
	 * 把String成Map的方法
	 * 
	 * 
	 * @param properties
	 * @return
	 */

	/**
	 * 把String转换成Map的方法
	 * 
	 * @param properties
	 *            需要做转换String，其格式类似于 paramA=aa&paramB=bb&paramC=cc
	 * @param outerSplitter
	 *            将字符串做第一次切割的分隔符，如例子中的 "&"
	 * @param innerSplitter
	 *            将字符串做第二次切割的分隔符, 如例子中的 "="
	 * @return Map的实例对象，如例子中，最终得到的Map有3条Entry，paramA、paramB、paramC为其key，aa、bb、 cc分别为各自的value
	 * @creator fangxiang
	 */
	public static Map String2Map(String properties, String outerSplitter, String innerSplitter) {
		if (isEmpty(properties)) {
			return null;
		}

		Map outProperties = new Hashtable();
		StringTokenizer tokenizerOuter = new StringTokenizer(properties, outerSplitter);
		while (tokenizerOuter.hasMoreTokens()) {
			String currProperty = tokenizerOuter.nextToken();
			int index = currProperty.indexOf(innerSplitter);
			if (index != -1) {
				String value = currProperty.substring(index + 1, currProperty.length());
				outProperties.put(currProperty.substring(0, index), value);
			}
		}
		return outProperties;
	}

	/**
	 * 
	 * @param inProperties
	 * @return
	 */
	public static String Map2String(Map inProperties) {
		return Map2String(inProperties, PROPERTY_SPLITTER, KEYVALUE_SPLITTER);
	}

	/**
	 * 
	 * @param inProperties
	 * @return
	 */
	public static String Map2String(Map inProperties, String outerSplitter, String innerSplitter) {
		StringBuffer propertiesBuffer = new StringBuffer();
		for (Iterator i = inProperties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Object valueObj = inProperties.get(key);
			propertiesBuffer.append(key).append(innerSplitter).append(valueObj).append(outerSplitter);
		}

		return propertiesBuffer.toString();
	}

	/**
	 * 将String数组中的值转为List
	 * 
	 * @param strArray
	 * @return
	 * @creator yao.nengjun@trs.com.cn @ Apr 10, 2009
	 */
	public static List StringArrayToList(String[] strArray) {
		if (strArray == null || strArray.length == 0)
			return new ArrayList();

		List list2Return = new ArrayList();
		for (int i = 0; i < strArray.length; i++) {
			String str = strArray[i];
			list2Return.add(str);
		}
		return list2Return;
	}

	/**
	 * 将String数组中的值转化为set，避免重复值
	 * 
	 * @param strArray
	 * @return
	 * @creator yao.nengjun@trs.com.cn @ Apr 10, 2009
	 */
	public static Set StringArrayToSet(String[] strArray) {
		if (strArray == null || strArray.length == 0)
			return null;

		Set set2Return = new HashSet();
		for (int i = 0; i < strArray.length; i++) {
			String str = strArray[i];
			set2Return.add(str);
		}
		return set2Return;
	}

	/**
	 * 返回字符串长度
	 * 
	 * @param str
	 * @return
	 * @creator lzp @ 2009-7-25
	 */
	public static int length(String str) {
		if (isEmpty(str))
			return 0;
		else
			return str.length();
	}

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine() method. We iterate until the
		 * BufferedReader return null which means there's no more data to read. Each line will appended to a
		 * StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static String convertStreamToString(InputStream is, String charSet) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine() method. We iterate until the
		 * BufferedReader return null which means there's no more data to read. Each line will appended to a
		 * StringBuilder and returned as String.
		 */

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is, charSet));
		} catch (UnsupportedEncodingException e1) {
		}
		StringBuffer sb = new StringBuffer();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * 
	 * @return
	 * @creator fx @ Aug 10, 2009
	 */
	public static String filterXSS(String value) {
		if (value == null || value.length() == 0) {
			return "";
		}
		value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
		value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
		value = value.replaceAll("'", "& #39;");
		value = value.replaceAll("eval\\((.*)\\)", "");
		value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
		// value = value.replaceAll("script", "");
		return value;
	}

	/**
	 * 过滤单引号以外的XSS字符
	 * 
	 * @param value
	 *            待过滤字符串
	 * @return 过滤后字符串
	 * @since v5.0
	 * @creator SHIXIN-dev4 @ 2014-8-13
	 */
	public static String filterXSSWithoutQuotation(String value) {
		if (value == null || value.length() == 0) {
			return "";
		}
		value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
		value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
		value = value.replaceAll("eval\\((.*)\\)", "");
		value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
		return value;
	}

	/**
	 * 将整形数据转换为以token间隔的字符串
	 * 
	 * @param token
	 * @creator lzp @ 2009-8-14
	 */
	public static final String toString(Object[] number, String token) {
		StringBuffer sb = new StringBuffer();
		if (number != null) {
			int length = number.length;
			for (int i = 0; i < number.length; i++) {
				sb.append(number[i]);
				if (i != length - 1) {
					sb.append(token);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Parses the string argument as a signed decimal integer.
	 * 
	 * @param value
	 *            待解析的字符串值
	 * @return 解析后的int值，如果不合法返回-1
	 * @creator fangxiang @ Aug 15, 2009
	 */
	public static int parseInt(String value) {
		return parseInt(value, -1);
	}

	/**
	 * Parses the string argument as a signed decimal integer.
	 * 
	 * @param value
	 *            待解析的字符串值
	 * @param defValue
	 *            TODO
	 * @return 解析后的int值，如果不合法返回-1
	 * @creator fangxiang @ Aug 15, 2009
	 */
	public static int parseInt(String value, int defValue) {
		if (StringHelper.isEmpty(value)) {
			return defValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	/**
	 * Parses the string argument as a signed decimal <code>long</code>;
	 * 
	 * @param value
	 *            待解析的字符串值
	 * @return 解析后的long值，如果不合法返回-1
	 * @since v3.5
	 * @creator shixin @ 2012-5-7
	 */
	public static long parseLong(String value) {
		if (StringHelper.isEmpty(value)) {
			return -1;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	/**
	 * Parses the string argument as a signed decimal <code>double</code>;
	 * 
	 * @param value
	 *            待解析的字符串值
	 * @return 解析后的double值，如果不合法返回-1
	 * @since v5.0
	 * @creator chenzigan @ 2013-5-16
	 */
	public static double parseDouble(String value) {
		if (StringHelper.isEmpty(value)) {
			return -1;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	/**
	 * Parses the string argument as a boolean
	 * 
	 * @param value
	 *            待解析的字符串值
	 * @return 解析后的boolean值，如果不合法返回false
	 * @since v3.5
	 * @creator shixin @ 2012-6-8
	 */
	public static boolean parseBoolean(String value) {
		if (StringHelper.isEmpty(value)) {
			return false;
		}
		try {
			return Boolean.parseBoolean(value);
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * 返回两个分割符之间的子串，不包括这两个分割符. <br>
	 * 
	 * @param origin
	 *            待分隔的字符串
	 * @param begin
	 *            开始分隔子符。可以是一个字母，也可以是多个字母。如果希望从头开始截取，可以设为null。
	 * @param end
	 *            结束的分隔子符。可以是一个字母，也可以是多个字母。如果不设定结束字符，可以设为null，此时会一直取到待分隔字符的最后一位
	 * @return <ul>
	 *         <li>如果传入的待分隔字符为null或者空字符串，返回null</li>
	 *         <li>如果在待分隔字符串中找不到开始分隔符或者结束分隔符，返回null</li>
	 *         <li>
	 *         如果开始分隔字符和结束分隔字符相同，返回null</li>
	 *         <li>如果开始分隔字符和结束分隔字符都为null，返回原字符串</li>
	 *         </ul>
	 * @since v3.5
	 * @creator liushen @ Dec 12, 2009
	 * @modify yaonengjun@2010-04-02
	 */
	public static String substringByFirstOccurance(String origin, String begin, String end) {
		if (isEmpty(origin)) {
			return null;
		}

		// 开始和结束相等，直接返回空
		if (!isEmpty(begin) && !isEmpty(end) && begin.equals(end))
			return null;

		// 如果其中有一个找不到，返回null
		if ((!isEmpty(begin) && origin.indexOf(begin) < 0) || (!isEmpty(end) && origin.indexOf(end) < 0))
			return null;

		int beginIndex = 0;

		// 如果begin为空，则从开始算起
		if (isEmpty(begin)) {
			beginIndex = 0;
		}

		if (!isEmpty(begin) && origin.indexOf(begin) >= 0) {
			beginIndex = origin.indexOf(begin) + begin.length();
		}

		int endIndex = 0;

		// 如果end为空，则end为origin的长度
		if (isEmpty(end)) {
			endIndex = origin.length();
		}

		if (!isEmpty(end) && origin.indexOf(end, beginIndex) >= 0) {
			endIndex = origin.indexOf(end, beginIndex);
		}
		if (endIndex <= beginIndex)
			return null;

		String result = origin.substring(beginIndex, endIndex);
		if (isEmpty(result))
			return null;
		return result;
	}

	/**
	 * @since v3.5
	 * @creator liushen @ Jan 15, 2010
	 */
	public static int parseIntUsingFormat(String source, String formatPattern) {
		DecimalFormat df = new DecimalFormat(formatPattern);
		try {
			return df.parse(source).intValue();
		} catch (ParseException e) {
			return -1;
		}
	}

	/**
	 * 将int数字转成一定格式的字符串，例如，将int i = 1 转成 001的String字符串
	 * 
	 * @param intToFormat
	 * @param formatPattern
	 *            {@linkplain DecimalFormat} 的Java doc的定义,常用的格式例子如下：
	 *            <ul>
	 *            <li>000:</li>将数字补全为3位字符串。<br>
	 *            如果数字不足三位，则前面以0填充。例如数字3，将其转为"003"的字符串。<br>
	 *            如果数字为负数，例如-3，则将其转为-003。
	 *            </ul>
	 * @return
	 * @since v3.5
	 * @creator yaonengjun @ Sep 30, 2010
	 */
	public static String parseIntAsStringUsingFormat(int intToFormat, String formatPattern) {
		DecimalFormat df = new DecimalFormat(formatPattern);
		long intOfLong = 0;
		try {
			intOfLong = new Integer(intToFormat).longValue();
		} catch (Exception e) {
			return null;
		}

		try {
			return df.format(intOfLong);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 排除HTTP协议中QueryString中的某个参数项及其值。 <br>
	 * 假设queryString 为 param=999&aa=11&bb=22&cc=333，排除其param项以后，得到的值为 aa=11&bb=22&cc=333
	 * 
	 * @param queryString
	 *            按照HttpServletRequest.getQueryString得到的参数值，如例子中的 param=999&aa=11&bb=22&cc=333
	 * @param excludeParam
	 *            要排除的参数项名称，如例子中的 param
	 * @return 得到的符合HttpServletRequest.getQueryString格式的字符串，如例子中的 aa=11&bb=22&cc=333
	 * @since v3.5
	 * @creator yaonengjun @ May 6, 2010
	 */
	public static String excludeParamInQueryString(String queryString, String excludeParam) {
		if (isEmpty(queryString))
			return null;

		if (isEmpty(excludeParam))
			return queryString;

		String[] strArray = split(queryString, "&");
		StringBuffer str2Return = new StringBuffer();
		if (strArray == null)
			return null;
		for (int i = 0; i < strArray.length; i++) {
			String str = strArray[i];

			if (!isEmpty(str) && str.indexOf(excludeParam) < 0) {
				if (i > 0)
					str2Return.append("&");

				str2Return.append(str);
			}

		}
		// 如果最后生成的QueryString以“&”开头，将其截掉
		String toReturnQueryString = str2Return.toString();
		if (!isEmpty(toReturnQueryString) && toReturnQueryString.indexOf("&") == 0)
			toReturnQueryString = substringByFirstOccurance(toReturnQueryString, "&", null);
		return toReturnQueryString;
	}

	/**
	 * 将 String 对象编码为 JSON 格式时，只需处理好特殊字符即可。另外，必须用 (") 而非 (') 表示字符串；
	 * 
	 * @param str
	 * @return
	 * @since v3.5
	 * @creator shixin @ 2010-6-10
	 */
	public static String string2Json(String str) {
		StringBuffer sb = new StringBuffer(str.length() + 20);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 把输入的字节树组，逐字节转化为其16进制的文本描述形式
	 * 
	 * @param buf
	 *            输入的字节树组
	 * @param off
	 *            需要转化的开始位置
	 * @param len
	 *            需要转化的结束位置
	 * @return 转化结果
	 */
	public final static String bytesToHex(byte[] buf, int off, int len) {
		char[] out = new char[len * 2];

		for (int i = 0, j = 0; i < len; i++) {
			int a = buf[off++];

			out[j++] = DIGITS[(a >>> 4) & 0X0F];
			out[j++] = DIGITS[a & 0X0F];
		}
		return (new String(out));
	}

	public final static String removeFromStringWithSeperator(String stringWithSeperator, String stringToRemove,
			String seperator) {
		if (StringHelper.isEmpty(stringWithSeperator))
			return null;

		if (StringHelper.isEmpty(stringToRemove))
			return stringWithSeperator;

		String[] strArray = StringHelper.split(stringWithSeperator, seperator);
		StringBuffer str2Return = new StringBuffer();
		if (strArray == null)
			return null;
		for (int i = 0; i < strArray.length; i++) {
			String str = strArray[i];

			if (!StringHelper.isEmpty(str) && str.indexOf(stringToRemove) < 0) {
				if (i > 0)
					str2Return.append(seperator);

				str2Return.append(str);
			}

		}
		// 如果最后生成的String以分隔符开头，将其截掉
		String toReturnString = str2Return.toString();
		if (!StringHelper.isEmpty(toReturnString) && toReturnString.indexOf(seperator) == 0)
			toReturnString = StringHelper.substringByFirstOccurance(toReturnString, seperator, null);
		return toReturnString;
	}

	/**
	 * @deprecated yaonengjun@20120404：实现过于复杂，请使用本类中的 {@link #replaceWithRegex(String, Map, Pattern)}，采用正则表达式进行直接替换
	 * 
	 *             从带变量的字符串中解析某变量%Key=Value%，只保留Value
	 * 
	 * @param contentWithKeyValue
	 *            带KEY=VALUE值的模板通知内容，替换前形式如："xxx%REGPLACE=VariableValue%xxxxx"
	 * @param variableName
	 *            形式必须如：%REGPLACE%，以百分号开头和结尾
	 * @return 解析某变量KeyValue以后的Content，替换后形式如："xxxVariableValuexxxxx"
	 * @since v3.5
	 * @creator shixin @ 2010-4-14
	 */
	public static String replaceVariableContentWithValue(String contentWithKeyValue, String variableName) {
		if (StringHelper.isEmpty(variableName) || variableName.indexOf("%") == -1) {
			return contentWithKeyValue;
		}
		if (StringHelper.isEmpty(contentWithKeyValue)) {
			return contentWithKeyValue;
		}
		String variableNameFront = variableName.substring(0, variableName.length() - 1);
		// System.out.println("variableNameFront=" + variableNameFront);
		if (contentWithKeyValue.indexOf(variableNameFront) == -1) {
			return contentWithKeyValue;
		}

		// 匹配%AAA=aaa%的变量，并替换为aaa
		String variableMatchReg = variableNameFront + "=" + "([^%]*)" + "%";
		// System.out.println("variableMatchReg=" + variableMatchReg);
		Pattern p = Pattern.compile(variableMatchReg);
		Matcher m = p.matcher(contentWithKeyValue);
		while (m.find()) {
			// System.out.println("contentWithKeyValue=" + contentWithKeyValue);
			// int startIndex = m.start();
			// System.out.println("startIndex=" + startIndex);
			// int endIndex = m.end();
			// System.out.println("endIndex=" + endIndex);
			String matchGroup = m.group();
			matchGroup = matchGroup.replaceAll("\\?", "\\\\?"); // 问号特殊字符转义
			matchGroup = matchGroup.replaceAll("\\$", "\\\\\\$"); // “$”特殊字符转义
			matchGroup = matchGroup.replaceAll("\\{", "\\\\{"); // “{”特殊字符转义
			matchGroup = matchGroup.replaceAll("\\.", "\\\\."); // .
			matchGroup = matchGroup.replaceAll("\\^", "\\\\\\^"); // ^
			matchGroup = matchGroup.replaceAll("\\[", "\\\\["); // [
			matchGroup = matchGroup.replaceAll("\\(", "\\\\("); // (
			matchGroup = matchGroup.replaceAll("\\)", "\\\\)"); // )
			matchGroup = matchGroup.replaceAll("\\|", "\\\\|"); // |
			matchGroup = matchGroup.replaceAll("\\*", "\\\\*"); // *
			matchGroup = matchGroup.replaceAll("\\+", "\\\\+"); // +
			// matchGroup = matchGroup.replaceAll("\\", "\\\\\\"); // \

			// System.out.println("matchGroup=" + matchGroup);
			String replacedValue = matchGroup.substring(variableNameFront.length() + 1, matchGroup.length() - 1);
			// System.out.println("replacedValue=" + replacedValue);

			contentWithKeyValue = contentWithKeyValue.replaceFirst(matchGroup, replacedValue);
			// System.out.println("contentWithKeyValue=" + contentWithKeyValue);
		}

		// 匹配%AAA%的变量，并替换为""
		return contentWithKeyValue.replaceAll(variableName, "");
	}

	/**
	 * 生成指定位数的随机code
	 * 
	 * @param length
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2012-8-30
	 */
	public static String generateRandomCode(int length) {
		StringBuffer code = new StringBuffer();
		code.delete(0, code.capacity() - 1);
		Random random = new Random((new Date()).getTime());
		for (int i = 0; i < length; i++) {
			code.append(random.nextInt(length + 1));
		}
		return code.toString();
	}

	/**
	 * 如果字符串参数<code>str</code>为空({@link #isEmpty(String)})，则返回字符串参数<code>defaultValue</code>；否则返回<code>str</code>.
	 * 
	 * @since v3.5
	 * @creator wangcheng @ 2012-10-18
	 */
	public static String avoidEmpty(String str, String defaultValue) {
		return isEmpty(str) ? defaultValue : str;
	}

	/**
	 * 所给字符串是否为<code>null</code>或空。
	 * 
	 * @since v3.5
	 * @creator liushen @ Oct 17, 2012
	 */
	public static boolean isNotEmpty(String str) {
		return false == isEmpty(str);
	}

	/**
	 * 将字符串中&转化为&amp;
	 * 
	 * @param str
	 *            字符串
	 * @return 转化后字符串
	 * @since v3.5
	 * @creator SHIXIN-dev4 @ 2013-3-20
	 */
	public static String filterAndCharacter(String str) {
		if (isEmpty(str)) {
			return "";
		}
		return str.replaceAll("&", "&amp;");
	}

	/**
	 * 获取指定长度的随机小写字符串
	 * 
	 * @param length
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2013-3-28
	 */
	public static String getRandomLower(int length) {
		if (length <= 0) {
			return "";
		}
		String str = "";
		for (int i = 0; i < length; i++) {
			str += String.valueOf((char) Math.round(Math.random() * 25 + 97));
		}
		return str;
	}

	/**
	 * 获取指定长度的随机大写字符串
	 * 
	 * @param length
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2013-3-28
	 */
	public static String getRandomUpper(int length) {
		if (length <= 0) {
			return "";
		}
		String str = "";
		for (int i = 0; i < length; i++) {
			str += String.valueOf((char) Math.round(Math.random() * 25 + 65));
		}
		return str;
	}

	/**
	 * 获取指定长度的随机数字
	 * 
	 * @param length
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2013-3-28
	 */
	public static String getRandomNumber(int length) {
		if (length <= 0) {
			return "";
		}
		String str = "";
		Random r = new Random();
		for (int i = 0; i < length; i++) {
			str += r.nextInt(10);
		}
		return str;
	}

	/**
	 * 获取包含有数字、大小写、特殊字符的指定位数的随机字符串
	 * 
	 * @param length
	 *            长度，要求至少大于等于8，否则按8位长度处理
	 * @return 随机串
	 * @since v3.5
	 * @creator zhangshi @ 2013-4-10
	 */
	public static String generateRandomCodeMixed(int length) {
		return generateRandomCodeMixed(length, true);
	}

	/**
	 * 获取包含有数字、大小写、特殊字符的指定位数的随机字符串
	 * 
	 * @param length
	 *            长度
	 * @param limitMinLength
	 *            是否限制生成字符串的最小长度，如果不限制，则按指定的生成；如果限制，如果指定长度小于8，则按8位处理
	 * @return
	 * @since v4.0
	 * @creator wangcheng @ 2014-3-14
	 */
	public static String generateRandomCodeMixed(int length, boolean limitMinLength) {
		if (limitMinLength && length < 8) {
			length = 8;
		}
		String str = "";
		for (int i = 0; i < length; i++) {
			str += String.valueOf((char) Math.round(Math.random() * 74 + 48));
		}
		return str;
	}


	/**
	 * 判断指定字符串在数组中是否存在，存在则返回true
	 * 
	 * @param orignalStrs
	 * @param key
	 * @return
	 * @since v3.5
	 * @creator zhangshi @ 2013-5-28
	 */
	public static boolean contains(String[] orignalStrs, String key) {
		if (null == orignalStrs || orignalStrs.length == 0) {
			return false;
		}

		if (isEmpty(key)) {
			return false;
		}
		/*for (String value : orignalStrs) {
			if (value.trim().equals(key.trim())) {
				return true;
			}
		}*/
		for(int i = 0;i < orignalStrs.length;i++){
			if(orignalStrs[i].trim().equals(key)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 替换所有的回车换行为空格
	 * 
	 * @param str
	 *            当前字符串
	 * @return 替换后字符串
	 * @since v4.0
	 * @creator SHIXIN-dev4 @ 2013-9-26
	 */
	public static String replaceCRLF(String str) {
		if (str == null) {
			return str;
		}
		str = str.replaceAll("\r\n", " ");
		str = str.replaceAll("\n", " ");
		str = str.replaceAll("\r", " ");
		return str;
	}

	/**
	 * 随机生成一个指定范围内的整形数字
	 * 
	 * @param maxValue
	 *            最大值，需要大于零，如果为0或者小于零，那么则返回0；
	 * @return 返回一个在最大值范围内的随机整形数字；如果最大值大于0，但是随即生成了0，则默认返回1
	 * @since v4.0
	 * @creator ZhangShi @ 2013-11-11
	 */
	public static int getRandomNum(int maxValue) {
		if (maxValue == 0 || maxValue < 0) {
			return 0;
		}
		double randomNum = Math.random();
		int random = (int) (randomNum * maxValue);
		if (random == 0) {
			return 1;
		}
		return random;
	}

	/**
	 * 字符串截取
	 * 
	 * @param originalStr
	 *            原值，为空，则直接返回空
	 * @param firstPlace
	 *            如果为0，那么则返回一开始到lastPlace的位置的串
	 * @param lastPlace
	 *            如果为0，则返回空串
	 * @return 原值中位于firstPlace和lastPlace之间的值，从第0位（即第一个字符）开始，包括firstPlace的值，不包括lastPlace的值<br>
	 *         如果firstPlace=lastPlace，返回空；<br>
	 *         如果firstPlace>lastPlace，返回空<br>
	 *         如果firstPlace大于字符串长度，则返回空；如果lastPlace大于字符串长度，则取firstPlace之后所有的字符串<br>
	 *         如果firstPlace或lastPlace小于0，则以0计
	 * @since v4.0
	 * @creator ZhangShi @ 2013-11-11
	 */
	public static String substring(String originalStr, int firstPlace, int lastPlace) {
		if (firstPlace < 0) {
			firstPlace = 0;
		}
		if (lastPlace < 0) {
			lastPlace = 0;
		}
		int length = length(originalStr);
		if (StringHelper.isEmpty(originalStr) || !(firstPlace < lastPlace) || firstPlace > length) {
			return "";
		}
		if (lastPlace > length) {
			lastPlace = length;
		}
		return originalStr.substring(firstPlace, lastPlace);
	}

	/**
	 * 按照指定字符编码压缩指定字符串
	 * 
	 * @param str
	 *            特定的字符串，如果为空，则直接返回该字符串
	 * @return 如果遇到异常，则直接返回当前字符串
	 * @since v4.0
	 * @creator ZhangShi @ 2014-1-13
	 */
	public static String compress(String str) {
		if (isEmpty(str)) {
			return str;
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes());
			gzip.close();
			return out.toString("ISO-8859-1");
		} catch (Throwable e) {
			e.printStackTrace();
			return str;
		}
	}

	/**
	 * 按照指定字符编码解压缩指定字符串
	 * 
	 * @param str
	 *            特定的字符串,如果为空，则直接返回该字符串
	 * @param encoding
	 *            应与服务器的编码一致，如果为空则按照UTF -8解压
	 * @return 如果遇到异常，则直接返回当前字符串
	 * @throws IOException
	 * @since v4.0
	 * @creator ZhangShi @ 2014-1-13
	 */
	public static String uncompress(String str, String encoding) {
		if (isEmpty(str)) {
			return str;
		}
		if (isEmpty(encoding)) {
			encoding = "utf-8";
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[4000];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			return out.toString(encoding);
		} catch (Throwable e) {
			e.printStackTrace();
			return str;
		}
	}

	/**
	 * 在原字符串末尾添加给定的后缀, 如果不是以该后缀结尾的话.
	 * 
	 * @param str
	 *            原字符串
	 * @param endingStr
	 *            后缀(用来结束的字符或字符串)
	 * @return 原字符串(如果原字符串为<code>null</code>或已经以该后缀结束)；或者原字符串加上该后缀
	 * @since liushen @ Jun 21, 2010
	 */
	public static String smartAppendSuffix(String str, String endingStr) {
		if (str == null) {
			return null;
		}
		return str.endsWith(endingStr) ? str : str + endingStr;
	}

	/**
	 * 根据csrf规则替换字符串
	 * 
	 * @param source
	 *            原始字符串
	 * @param csrfToken
	 * @return
	 * @since v4.0
	 * @creator fangxiang @ Jan 25, 2014
	 */
	public static String csrfReplace(String source, String csrfToken) {
		StringBuffer csrfBuffer = new StringBuffer((int) (source.length() * 1.2));
		int index = 0, indexPrev = 0;
		do {
			index = source.indexOf(".jsp", indexPrev);
			if (index == -1) {
				break;
			}
			char questionMark = '-';
			try {
				questionMark = source.charAt(index + 4);
			} catch (Throwable e) {
			}
			if ('?' == questionMark) {
				csrfBuffer.append(source.substring(indexPrev, index + 5));
				csrfBuffer.append("csrftoken=").append(csrfToken).append("&");
				indexPrev = index + 5;
			} else {
				csrfBuffer.append(source.substring(indexPrev, index + 4));
				csrfBuffer.append("?csrftoken=").append(csrfToken);
				indexPrev = index + 4;
			}
		} while (index < source.length());
		csrfBuffer.append(source.substring(indexPrev, source.length()));
		return csrfBuffer.toString();
	}

	/**
	 * 基于原始url和参数构造最终url：主要处理连接串？or&
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @since v5.0
	 * @creator zhangshi @ 2014-4-19
	 */
	public static String buildUrl(String url, String params) {
		if (StringHelper.isEmpty(params)) {
			return url;
		}
		String concat = "?";
		if (url.contains("?")) {
			concat = "&";
		}
		return url + concat + params;
	}
}