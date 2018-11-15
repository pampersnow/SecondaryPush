/*
 * Title: TRS 身份服务器 Copyright: Copyright (c) 2004-2011, TRS信息技术股份有限公司. All rights reserved. License: see the license
 * file. Company: TRS信息技术股份有限公司(www.trs.com.cn)
 * 
 * Created: shixin@2011-3-2 上午08:18:45
 */
package com.trs.cms.process.engine;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

/**
 * DES加密处理工具类.主要用来对数据进行DES加密/解密操作 <BR>
 * 
 * @author TRS信息技术有限公司
 * @since shixin@2011-3-2
 */
public class DesEncryptUtil {

	/**
	 * DES算法名称
	 */
	private final static String DES_ALGORITHM_NAME = "DES";

	/**
	 * 默认的DES算法转换方式
	 */
	private final static String DES_ALGORITHM_TRANSFORMATION_DEFAULT = "DES/ECB/PKCS5Padding";

	/**
	 * 默认的DES加密密钥存储属性名称
	 */
	private static final String DES_KEY_DATA_PROPERTY_NAME = "desKeyData";

	/**
	 * 密钥存储的配置文件. 由于这部分配置信息较为独特, 在系统配置完毕后不可轻易修改, 因此单独使用一个文件存放.
	 */
	private static final String SEC_CFG_FILE = "/security.ini";

	/**
	 * 使用默认密钥对数据进行DES加密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toEncryptData
	 *            需要加密的数据
	 * @return 加密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] encrypt(byte[] toEncryptData) {
		return doEncrypt(getKey(), toEncryptData);
	}

	/**
	 * 使用默认密钥对数据进行DES加密，返回16进制字符串数据<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toEncryptData
	 *            需要加密的数据
	 * @return 返回加密后的数据，以16进制字符串形式表示
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String encryptToHex(byte[] toEncryptData) {
		byte[] encryptedData = doEncrypt(getKey(), toEncryptData);
		if (null == encryptedData) {
			return "";
		}
		return StringHelper.bytesToHex(encryptedData, 0, encryptedData.length);
	}

	/**
	 * 根据指定密钥，对数据DES加密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toEncryptData
	 *            需要加密的数据
	 * @param keyResourceName
	 *            存放密钥的资源名称
	 * @param propertyName
	 *            密钥数据属性名称
	 * @return 加密后的数据 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] encrypt(byte[] toEncryptData, String keyResourceName, String propertyName) {
		return doEncrypt(getKeyFromResource(keyResourceName, propertyName), toEncryptData);
	}

	/**
	 * 根据指定密钥数据内容，对数据DES加密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toEncryptData
	 *            需要加密的数据
	 * @param keyData
	 *            密钥数据内容, 使用该数据的byte[]缓冲区前 8 个字节作为密钥
	 * @return 加密后的数据 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] encrypt(byte[] toEncryptData, String keyData) {
		return doEncrypt(getKeyByData(keyData), toEncryptData);
	}

	/**
	 * 根据指定密钥，对数据DES加密，返回16进制字符串数据<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toEncryptData
	 *            需要加密的数据
	 * @param keyResourceName
	 *            存放密钥的资源名称
	 * @param propertyName
	 *            密钥数据属性名称
	 * @return 返回加密后的数据，以16进制字符串形式表示 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String encryptToHex(byte[] toEncryptData, String keyResourceName, String propertyName) {
		byte[] encryptedData = doEncrypt(getKeyFromResource(keyResourceName, propertyName), toEncryptData);
		if (null == encryptedData) {
			return "";
		}
		return StringHelper.bytesToHex(encryptedData, 0, encryptedData.length);
	}

	/**
	 * 根据指定密钥，对数据DES加密，返回16进制字符串数据<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toEncryptData
	 *            需要加密的数据
	 * @param keyData
	 *            密钥数据内容, 使用该数据的byte[]缓冲区前 8 个字节作为密钥
	 * @return 返回加密后的数据，以16进制字符串形式表示 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String encryptToHex(byte[] toEncryptData, String keyData) {
		byte[] encryptedData = doEncrypt(getKeyByData(keyData), toEncryptData);
		if (null == encryptedData) {
			return "";
		}
		return StringHelper.bytesToHex(encryptedData, 0, encryptedData.length);
	}

	/**
	 * 指定密钥和转换模式，对数据DES加密
	 * 
	 * @param key
	 *            密钥
	 * @param toEncryptData
	 *            需要加密的数据
	 * @param transformation
	 *            转换的名称，例如 DES/CBC/PKCS5Padding。有关标准转换名称的信息，请参见 Java Cryptography Architecture Reference Guide 的附录 A。
	 * @return 加密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] doEncrypt(Key key, byte[] toEncryptData, String transformation) {
		if (key == null) {
			return null;
		}
		return JceEncryptUtil.doEncrypt(key, toEncryptData, transformation);
	}

	/**
	 * 指定密钥，对数据DES加密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param key
	 *            密钥
	 * @param toEncryptData
	 * @return 加密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] doEncrypt(Key key, byte[] toEncryptData) {
		if (key == null) {
			return null;
		}
		return JceEncryptUtil.doEncrypt(key, toEncryptData, DES_ALGORITHM_TRANSFORMATION_DEFAULT);
	}

	/**
	 * 使用默认密钥对数据进行DES解密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toDecryptData
	 *            要解密的数据
	 * @return 解密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] decrypt(byte[] toDecryptData) {
		return JceEncryptUtil.doDecrypt(getKey(), toDecryptData, DES_ALGORITHM_TRANSFORMATION_DEFAULT);
	}

	/**
	 * 使用默认密钥对数据进行DES解密，并返回16进制字符串数据<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toDecryptData
	 *            要解密的数据
	 * @return 返回解密后的数据，以16进制字符串形式表示；
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String decryptToHex(byte[] toDecryptData) {
		byte[] decryptedData = JceEncryptUtil.doDecrypt(getKey(), toDecryptData, DES_ALGORITHM_TRANSFORMATION_DEFAULT);
		if (null == decryptedData) {
			return "";
		}
		return StringHelper.bytesToHex(decryptedData, 0, decryptedData.length);
	}

	/**
	 * 指定密钥，对数据进行DES解密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toDecryptData
	 *            要解密的数据
	 * @param keyResourceName
	 *            存放密钥的资源名称
	 * @param propertyName
	 *            密钥数据属性名称
	 * @return 解密后的数据 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] decrypt(byte[] toDecryptData, String keyResourceName, String propertyName) {
		return JceEncryptUtil.doDecrypt(getKeyFromResource(keyResourceName, propertyName), toDecryptData,
				DES_ALGORITHM_TRANSFORMATION_DEFAULT);
	}

	/**
	 * 根据指定的密钥数据内容，对数据进行DES解密<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toDecryptData
	 *            要解密的数据
	 * @param keyData
	 *            DES密钥数据，使用该数据的byte[]缓冲区前 8 个字节作为密钥
	 * @return 解密后的数据 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] decrypt(byte[] toDecryptData, String keyData) {
		return JceEncryptUtil.doDecrypt(getKeyByData(keyData), toDecryptData, DES_ALGORITHM_TRANSFORMATION_DEFAULT);
	}

	/**
	 * 根据指定的密钥数据内容，对数据进行DES解密，并返回16进制字符串数据<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toDecryptData
	 *            要解密的数据
	 * @param keyData
	 *            DES密钥数据，使用该数据的byte[]缓冲区前 8 个字节作为密钥
	 * @return 返回解密后的数据，以16进制字符串形式表示 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String decryptToHex(byte[] toDecryptData, String keyData) {
		byte[] decryptedData = JceEncryptUtil.doDecrypt(getKeyByData(keyData), toDecryptData,
				DES_ALGORITHM_TRANSFORMATION_DEFAULT);
		if (null == decryptedData) {
			return "";
		}
		return StringHelper.bytesToHex(decryptedData, 0, decryptedData.length);
	}

	/**
	 * 指定密钥，对数据进行DES解密，并返回16进制字符串数据<br>
	 * 默认转换模式transformation为：DES/ECB/PKCS5Padding
	 * 
	 * @param toDecryptData
	 *            要解密的数据
	 * @param keyResourceName
	 *            存放密钥的资源名称
	 * @param propertyName
	 *            密钥数据属性名称
	 * @return 返回解密后的数据，以16进制字符串形式表示 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String decryptToHex(byte[] toDecryptData, String keyResourceName, String propertyName) {
		byte[] decryptedData = JceEncryptUtil.doDecrypt(getKeyFromResource(keyResourceName, propertyName),
				toDecryptData, DES_ALGORITHM_TRANSFORMATION_DEFAULT);
		if (null == decryptedData) {
			return "";
		}
		return StringHelper.bytesToHex(decryptedData, 0, decryptedData.length);
	}

	/**
	 * 指定转换模式和密钥，对数据进行DES解密
	 * 
	 * @param key
	 *            密钥
	 * @param toDecryptData
	 *            要解密的数据
	 * @param transformation
	 *            转换的名称，例如 DES/CBC/PKCS5Padding。有关标准转换名称的信息，请参见 Java Cryptography Architecture Reference Guide 的附录 A。
	 * @return 解密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] doDecrypt(Key key, byte[] toDecryptData, String transformation) {
		if (key == null) {
			return null;
		}
		return JceEncryptUtil.doDecrypt(key, toDecryptData, transformation);
	}

	/**
	 * 指定密钥，对数据进行DES解密
	 * 
	 * @param key
	 *            密钥
	 * @param toDecryptData
	 *            要解密的数据
	 * @return 解密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] doDecrypt(Key key, byte[] toDecryptData) {
		if (key == null) {
			return null;
		}
		return JceEncryptUtil.doDecrypt(key, toDecryptData, DES_ALGORITHM_TRANSFORMATION_DEFAULT);
	}

	/**
	 * 获取默认的DES加密密钥
	 * 
	 * @return 密钥 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static SecretKey getKey() {
		String keyData = getKeyDataFromResource(SEC_CFG_FILE, DES_KEY_DATA_PROPERTY_NAME);
		if (StringHelper.isEmpty(keyData)) {
			return null;
		}
		return getKeyByData(keyData);
	}

	/**
	 * 根据指定存放密钥的资源文件名称，获取DES加密密钥
	 * 
	 * @param resName
	 *            给定的资源文件名称
	 * @param propertyName
	 *            密钥数据属性名称
	 * @return DES密钥 @
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static SecretKey getKeyFromResource(String resName, String propertyName) {
		String keyData = getKeyDataFromResource(resName, propertyName);
		return getKeyByData(keyData);
	}

	/**
	 * 根据密钥数据生成DES密钥
	 * 
	 * @param keyData
	 *            DES密钥数据，使用该数据的byte[]缓冲区前 8 个字节作为密钥
	 * @return DES密钥
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static SecretKey getKeyByData(String keyData) {
		DESKeySpec desKeySpec = null;
		try {
			desKeySpec = new DESKeySpec(keyData.getBytes());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return JceEncryptUtil.getKey(DES_ALGORITHM_NAME, desKeySpec);
	}

	/**
	 * 根据指定属性名称，从给定的资源文件获取密钥数据内容
	 * 
	 * @param resName
	 *            给定的资源文件名称
	 * @param propertyName
	 *            密钥数据属性名称
	 * @return 密钥数据内容
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static String getKeyDataFromResource(String resName, String propertyName) {
		return JceEncryptUtil.getKeyDataFromResource(resName, propertyName);
	}

	/**
	 * 根据指定密钥，对DES加密数据进行解密
	 * 
	 * @pram encryptedDataHex 加密字符串(十六进制)
	 * @param keyData
	 *            密钥
	 * @return 解密字符串
	 * @since v3.5
	 * @creator SHIXIN-dev4 @ 2013-3-27
	 */
	public static String decrypt(String encryptedDataHex, String keyData) {
		if (StringHelper.isEmpty(encryptedDataHex)) {
			return "";
		}
		if (StringHelper.isEmpty(keyData)) {
			keyData = getKeyDataFromResource(SEC_CFG_FILE, DES_KEY_DATA_PROPERTY_NAME);
		}

		String decryptedHexStr = DesEncryptUtil.decryptToHex(StringHelper.toBytes(encryptedDataHex), keyData);
		return StringHelper.hexToStr(decryptedHexStr);
	}

	/**
	 * 对DES加密数据进行解密
	 * 
	 * @pram encryptedDataHex 加密字符串(十六进制)
	 * @return 解密字符串
	 * @since v3.5
	 * @creator SHIXIN-dev4 @ 2013-3-27
	 */
	public static String decrypt(String encryptedDataHex) {
		return decrypt(encryptedDataHex, getKeyDataFromResource(SEC_CFG_FILE, DES_KEY_DATA_PROPERTY_NAME));
	}
}