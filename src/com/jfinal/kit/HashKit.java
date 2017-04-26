/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.kit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.jfinal.core.Const;

public class HashKit {
	
	private static final java.security.SecureRandom random = new java.security.SecureRandom();
	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
	private static final char[] CHAR_ARRAY = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	
	public static String md5(String srcStr){
		return hash("MD5", srcStr);
	}
	
	public static String sha1(String srcStr){
		return hash("SHA-1", srcStr);
	}
	
	public static String sha256(String srcStr){
		return hash("SHA-256", srcStr);
	}
	
	public static String sha384(String srcStr){
		return hash("SHA-384", srcStr);
	}
	
	public static String sha512(String srcStr){
		return hash("SHA-512", srcStr);
	}
	
	public static String hash(String algorithm, String srcStr) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] bytes = md.digest(srcStr.getBytes(Const.DEFAULT_ENCODING));
			return toHex(bytes);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 将二进制转换成16进制
	 * 
	 * @param bytes
	 * @return
	 */
	private static String toHex(byte[] bytes) {
		StringBuilder ret = new StringBuilder(bytes.length * 2);
		for (int i=0,len = bytes.length; i<len; i++) {
			ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
			ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
		}
		return ret.toString();
	}
	
	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] hexToByte(String hexStr) {
		int hexStrLength = hexStr.length();
		if (hexStrLength < 1)
			return null;
		byte[] result = new byte[hexStrLength / 2];
		for (int i = 0; i < hexStrLength / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}
	
	/**
	 * md5 128bit 16bytes
	 * sha1 160bit 20bytes
	 * sha256 256bit 32bytes
	 * sha384 384bit 48bytes
	 * sha512 512bit 64bytes
	 */
	public static String generateSalt(int saltLength) {
		StringBuilder salt = new StringBuilder();
		for (int i=0; i<saltLength; i++) {
			salt.append(CHAR_ARRAY[random.nextInt(CHAR_ARRAY.length)]);
		}
		return salt.toString();
	}
	
	public static String generateSaltForSha256() {
		return generateSalt(32);
	}
	
	public static String generateSaltForSha512() {
		return generateSalt(64);
	}
	
	public static boolean slowEquals(byte[] a, byte[] b) {
		if (a == null || b == null) {
			return false;
		}
		
		int diff = a.length ^ b.length;
		for(int i=0; i<a.length && i<b.length; i++) {
			diff |= a[i] ^ b[i];
		}
		return diff == 0;
    }
	
	private final static String AES = "AES";
	/**
	 * AES加密
	 * @param strKey
	 * @return
	 */
	private static SecretKey getKey(String strKey) {
		try {
			KeyGenerator _generator = KeyGenerator.getInstance(AES);
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(strKey.getBytes());
			_generator.init(128, secureRandom);
			return _generator.generateKey();
		} catch (Exception e) {
			throw new RuntimeException(" 初始化密钥出现异常 ");
		}
	}
	
	public static String encryptToAes(String content, String pwd) {
		if(StrKit.isBlank(content) || StrKit.isBlank(pwd)){
			return null;
		}
		try {
			SecretKeySpec key = new SecretKeySpec(getKey(pwd).getEncoded(), AES);
			Cipher cipher = Cipher.getInstance(AES);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] result = cipher.doFinal(content.getBytes(Const.DEFAULT_ENCODING));
			return toHex(result);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AES解密
	 * @param content
	 * @param pwd
	 * @return
	 */
	public static String decryptToAes(String content, String pwd) {
		if(StrKit.isBlank(content) || StrKit.isBlank(pwd)){
			return null;
		}
		try {
			SecretKeySpec key = new SecretKeySpec(getKey(pwd).getEncoded(), AES);
			Cipher cipher = Cipher.getInstance(AES);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] result = cipher.doFinal(hexToByte(content));
			return new String(result);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Charset CHARSET = Charset.forName(Const.DEFAULT_ENCODING);
	
	/**
	 * Base64加密
	 * @param content
	 * @return
	 */
	public static String encryptToBase64(String content) {
		byte[] val = content.getBytes(CHARSET);
		return DatatypeConverter.printBase64Binary(val);
	}
	
	/**
	 * Base64解密
	 * @param content
	 * @return
	 */
	public static String decryptToBase64(String content) {
		byte[] decodedValue = DatatypeConverter.parseBase64Binary(content);
		return new String(decodedValue, CHARSET);
	}
	
	/**
	 * Base64加密 url变种，[+替换成*] [/替换成_]
	 * @param url
	 * @return
	 */
	public static String encryptToBase64Url(String url){
		url = encryptToBase64(url);
		if(url.indexOf("+") != -1){
			url = url.replaceAll("\\+", "*");
		}
		if(url.indexOf("/") != -1){
			url = url.replaceAll("/", "_");
		}
		return url;
	}
	
	/**
	 * Base64解密 url变种
	 * @param encodeUrl
	 * @return
	 */
	public static String decryptToBase64Url(String encodeUrl){
		encodeUrl = encryptToBase64Url(encodeUrl);
		if(encodeUrl.indexOf("*") != -1){
			encodeUrl = encodeUrl.replaceAll("\\*", "\\+");
		}
		if(encodeUrl.indexOf("_") != -1){
			encodeUrl = encodeUrl.replaceAll("_", "/");
		}
		return encodeUrl;
	}
	
	/**
	 * url encode
	 * @param url
	 * @return
	 */
	public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, Const.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }
	
	/**
	 * url decode
	 * @param url
	 * @return
	 */
	public static String urlDecode(String url) {
        try {
        	return URLDecoder.decode(url, Const.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }
}




