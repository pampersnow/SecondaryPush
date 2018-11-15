/*
 * Title: TRS 身份服务器 Copyright: Copyright (c) 2004-2011, TRS信息技术股份有限公司. All rights reserved. License: see the license
 * file. Company: TRS信息技术股份有限公司(www.trs.com.cn)
 * 
 * Created: shixin@2011-3-2 上午08:18:45
 */
package com.trs.cms.process.engine;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;

import org.apache.log4j.Logger;

/**
 * 支持Java Cryptographic Extension (JCE)框架的加密解密工具类 <BR>
 * 
 * @author TRS信息技术有限公司
 * @since shixin@2011-3-2
 */
public class JceEncryptUtil {

	private static final Logger logger = Logger.getLogger(JceEncryptUtil.class);

	/**
	 * 指定密钥和转换方式，进行数据加密
	 * 
	 * @param key
	 *            密钥
	 * @param toEncryptData
	 *            要加密的数据
	 * @param transformation
	 *            转换的名称，例如 DES/CBC/PKCS5Padding。有关标准转换名称的信息，请参见 Java Cryptography Architecture Reference Guide 的附录 A。
	 * @return 加密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] doEncrypt(Key key, byte[] toEncryptData, String transformation) {
		if (null == toEncryptData) {
			return null;
		}

		// Get a cipher object
		Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE, transformation);

		// Encrypt
		byte[] encryptedData = null;
		try {
			encryptedData = cipher.doFinal(toEncryptData);
		} catch (IllegalBlockSizeException e) {
			logger.error("get error while encrypt data by key[" + key + "] and transformation[" + transformation
					+ "], error info: " + e);
		} catch (BadPaddingException e) {
			logger.error("get error while encrypt data by key[" + key + "] and transformation[" + transformation
					+ "], error info: " + e);
		}

		return encryptedData;
	}

	/**
	 * 指定密钥和转换方式，进行数据解密
	 * 
	 * @param key
	 *            密钥
	 * @param toDecryptData
	 *            要解密的数据
	 * @param transformation
	 *            * 转换的名称，例如 DES/CBC/PKCS5Padding。有关标准转换名称的信息，请参见 Java Cryptography Architecture Reference Guide 的附录 A。
	 * @return 解密后的数据
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static byte[] doDecrypt(Key key, byte[] toDecryptData, String transformation) {
		if (null == toDecryptData) {
			return null;
		}

		// Get a cipher object
		Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE, transformation);

		// Decrypt
		byte[] decryptedData = null;
		try {
			decryptedData = cipher.doFinal(toDecryptData);
		} catch (IllegalBlockSizeException e) {
			logger.error("get error while decrypt data by key[" + key + "] and transformation[" + transformation
					+ "], error info: " + e);
		} catch (BadPaddingException e) {
			logger.error("get error while decrypt data by key[" + key + "] and transformation[" + transformation
					+ "], error info: " + e);
		}
		return decryptedData;
	}

	/**
	 * 获取Cipher加密和解密对象
	 * 
	 * @param key
	 *            密钥
	 * @param cipherMode
	 *            此 Cipher的操作模式（为以下之一：ENCRYPT_MODE、DECRYPT_MODE、WRAP_MODE 或 UNWRAP_MODE）
	 * @param transformation
	 *            转换的名称，例如 DES/CBC/PKCS5Padding。有关标准转换名称的信息，请参见 Java Cryptography Architecture Reference Guide 的附录 A。
	 * @return
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static Cipher getCipher(Key key, int cipherMode, String transformation) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(transformation);
		} catch (NoSuchAlgorithmException e) {
			logger.error("get error while getCipher by key[" + key + "], cipherMode[" + cipherMode
					+ "], and transformation[" + transformation + "], error info: " + e);
		} catch (NoSuchPaddingException e) {
			logger.error("get error while getCipher by key[" + key + "], cipherMode[" + cipherMode
					+ "], and transformation[" + transformation + "], error info: " + e);
		}

		try {
			cipher.init(cipherMode, key);
		} catch (InvalidKeyException e) {
			logger.error("get error while int Cipher by key[" + key + "], cipherMode[" + cipherMode + "], error info: "
					+ e);
		}

		return cipher;
	}

	/**
	 * 根据指定的算法名称和密钥规范获取密钥
	 * 
	 * @param algorithm
	 *            所请求的秘密密钥算法的标准名称, 如"DES"。有关标准算法名称的信息，请参阅 Java Cryptography Architecture Reference Guide 中的附录 A。
	 * @param keySpec
	 *            组成加密密钥的密钥内容的（透明）规范
	 * @return
	 * @since v3.5
	 * @creator shixin @ 2011-3-2
	 */
	public static SecretKey getKey(String algorithm, KeySpec keySpec) {
		SecretKeyFactory keyFactory = null;
		try {
			keyFactory = SecretKeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			logger.error("get error while get key by algorithm [" + algorithm + "] and keySpec [" + keySpec
					+ "], error info: " + e);
		}
		try {
			return keyFactory.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			logger.error("get error while generate key by algorithm [" + algorithm + "] and keySpec [" + keySpec
					+ "], error info: " + e);
		}
		logger.error("can not get key by by algorithm [" + algorithm + "] and keySpec [" + keySpec + "]");
		return null;
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
		String keyData = "";
		try {
			keyData = PropertyUtil.assertAndLoadFromResource(JceEncryptUtil.class, resName).getProperty(propertyName);
		} catch (Exception e) {
			logger.error("get error of not such file by resourceName[" + resName + "] and propertyName[" + propertyName
					+ "], error info: " + e);
		}
		if (StringHelper.isEmpty(keyData)) {
			logger.error("can not get keyData[" + keyData + "] from resourceName[" + resName + "] by propertyName["
					+ propertyName + "]");
			return null;
		}
		return keyData;
	}

}